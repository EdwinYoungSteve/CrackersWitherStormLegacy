package com.wdcftgg.witherstormmod.common.world;

import com.wdcftgg.witherstormmod.common.entity.PowerfulExplosiveEntity;
import com.wdcftgg.witherstormmod.common.entity.SickenedMobEntity;
import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import com.wdcftgg.witherstormmod.common.init.ModItems;
import com.wdcftgg.witherstormmod.common.init.ModSounds;
import com.wdcftgg.witherstormmod.common.network.ModNetwork;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Bowels command block 的完整 19 阶段服务端状态机。 */
public final class BowelsBossfightController {
    private static final int[] FIXED_PHASE_TICKS = {0, 60, 100, 20, 100, 0, 60, 100, 20, 100, 0, 0, 60, 100, 120, 0, 0, 0, 0};

    private static final MobWeight[] WAVE_1 = {
            mob("zombie", 15), mob("skeleton", 10), mob("spider", 8), mob("creeper", 2),
            mob("villager", 1), mob("phantom", 1), mob("chicken", 6), mob("cow", 6),
            mob("mushroom_cow", 1), mob("pig", 6), mob("bee", 3), mob("parrot", 2),
            mob("wolf", 2), mob("cat", 2), mob("pillager", 4), mob("vindicator", 2)
    };
    private static final MobWeight[] WAVE_2 = {
            mob("zombie", 10), mob("skeleton", 10), mob("spider", 8), mob("creeper", 6),
            mob("iron_golem", 4), mob("villager", 4), mob("phantom", 2), mob("chicken", 4),
            mob("cow", 4), mob("mushroom_cow", 1), mob("pig", 4), mob("bee", 6),
            mob("parrot", 4), mob("wolf", 3), mob("cat", 3), mob("pillager", 8), mob("vindicator", 4)
    };
    private static final MobWeight[] WAVE_3 = {
            mob("zombie", 10), mob("skeleton", 10), mob("spider", 4), mob("creeper", 6),
            mob("iron_golem", 6), mob("villager", 6), mob("phantom", 4), mob("chicken", 1),
            mob("cow", 1), mob("mushroom_cow", 1), mob("pig", 1), mob("bee", 8),
            mob("parrot", 6), mob("wolf", 5), mob("cat", 5), mob("pillager", 10), mob("vindicator", 5)
    };

    private BowelsBossfightController() {
    }

    public static void tick(SupplementalEntities.CommandBlockEntity core) {
        if (core.world.isRemote || !(core.world instanceof WorldServer)) return;
        WorldServer world = (WorldServer) core.world;
        BowelsInstanceData data = BowelsInstanceData.get(world);
        BowelsInstanceData.Instance instance = data.findContaining(core.getPosition());
        if (instance == null || instance.completed) return;

        int phase = instance.bossPhase;
        int ticks = ++instance.bossPhaseTicks;
        tickPhase(world, core, instance, phase, ticks);

        if (phase == 17) {
            if (ticks == 160) resolveDeath(world, core, instance);
        } else if (phase == 18) {
            if (ticks >= 80) cleanup(world, core, instance);
        } else if (isFixedPhase(phase) && ticks >= FIXED_PHASE_TICKS[phase]) {
            advance(world, core, data, instance);
        } else if ((phase == 10 || phase == 15) && guardsDefeated(world, core)) {
            advance(world, core, data, instance);
        }
        if (ticks % 20 == 0) data.markDirty();
    }

    public static boolean attack(SupplementalEntities.CommandBlockEntity core, DamageSource source) {
        if (core.world.isRemote || !(core.world instanceof WorldServer) || !isCommandBlockTool(source)) return false;
        WorldServer world = (WorldServer) core.world;
        BowelsInstanceData data = BowelsInstanceData.get(world);
        BowelsInstanceData.Instance instance = data.findContaining(core.getPosition());
        if (instance == null || instance.completed || !isVulnerablePhase(instance.bossPhase)) return false;

        float nextHealth = core.getHealth() - core.getMaxHealth() / 4.0F;
        if (nextHealth <= 0.0F) {
            // 原版实体不能在死亡 tick 前保留特殊序列，因此用 1 点生命值承载 160/240 tick Death 阶段。
            core.setHealth(1.0F);
            instance.bossPhase = 17;
            instance.bossPhaseTicks = 0;
            initPhase(world, core, instance, 17);
        } else {
            core.setHealth(nextHealth);
            advance(world, core, data, instance);
        }
        world.playSound(null, core.getPosition(), ModSounds.get("command_block_hit"),
                SoundCategory.HOSTILE, 8.0F, 1.0F);
        world.playSound(null, core.getPosition(), ModSounds.get("command_block_damage"),
                SoundCategory.HOSTILE, 8.0F, 1.0F);
        injureStormHeads(world, instance);
        return true;
    }

    private static void advance(WorldServer world, SupplementalEntities.CommandBlockEntity core,
                                BowelsInstanceData data, BowelsInstanceData.Instance instance) {
        finishPhase(world, core, instance.bossPhase);
        instance.bossPhase = Math.min(18, instance.bossPhase + 1);
        instance.bossPhaseTicks = 0;
        initPhase(world, core, instance, instance.bossPhase);
        data.markDirty();
    }

    private static void initPhase(WorldServer world, SupplementalEntities.CommandBlockEntity core,
                                  BowelsInstanceData.Instance instance, int phase) {
        switch (phase) {
            case 1:
            case 6:
            case 12:
                ModNetwork.shakeTracking(core, 240.0F, 12.0F);
                awakenTentacles(world, core, false);
                play(world, core, "loud_tremble", SoundCategory.AMBIENT, 1.0F);
                play(world, core, "bowels_loud_hurt", SoundCategory.HOSTILE, 1.0F);
                if (core.getHealth() / core.getMaxHealth() >= 0.75F) {
                    play(world, core, "wither_storm_reactivates", SoundCategory.HOSTILE, 64.0F);
                }
                break;
            case 2:
            case 7:
            case 13:
                ModNetwork.shakeTracking(core, 120.0F, 12.0F);
                core.createPodiumCluster();
                play(world, core, "loud_tremble", SoundCategory.AMBIENT, 1.0F);
                break;
            case 4:
                activateWave(world, core, 1, 60);
                break;
            case 9:
                ModNetwork.shakeTracking(core, 120.0F, 8.0F);
                activateWave(world, core, 2, 80);
                break;
            case 10:
                curlTentacles(world, core);
                break;
            case 14:
                ModNetwork.shakeTracking(core, 120.0F, 16.0F);
                activateWave(world, core, 3, 120);
                activateHeads(world, core);
                break;
            case 15:
                curlTentacles(world, core);
                break;
            case 17:
                ModNetwork.shakeTracking(core, 240.0F, 14.0F);
                ModNetwork.blindTracking(core, 240, 120, 80);
                play(world, core, "loud_tremble", SoundCategory.AMBIENT, 5.0F);
                play(world, core, "bowels_loud_hurt", SoundCategory.HOSTILE, 5.0F);
                play(world, core, "command_block_destruct", SoundCategory.HOSTILE, 64.0F);
                for (SickenedEntities.TentacleEntity tentacle : world.getEntitiesWithinAABB(SickenedEntities.TentacleEntity.class,
                        core.getEntityBoundingBox().grow(50.0D))) {
                    tentacle.setDormant(false);
                    tentacle.doIndefiniteAwakeAnimation();
                    tentacle.setCanSwing(false);
                    tentacle.setCanStrangle(false);
                }
                for (SupplementalEntities.WitherStormHeadEntity head : world.getEntitiesWithinAABB(SupplementalEntities.WitherStormHeadEntity.class,
                        core.getEntityBoundingBox().grow(50.0D))) head.setDead();
                for (SickenedMobEntity mob : world.getEntitiesWithinAABB(SickenedMobEntity.class,
                        core.getEntityBoundingBox().grow(50.0D))) {
                    if (mob != core && !(mob instanceof SickenedEntities.TentacleEntity)) mob.setDead();
                }
                break;
            default:
                break;
        }
    }

    private static void tickPhase(WorldServer world, SupplementalEntities.CommandBlockEntity core,
                                  BowelsInstanceData.Instance instance, int phase, int ticks) {
        if (phase == 2 || phase == 7 || phase == 13) {
            core.findPodiumCluster();
            core.movePodiumCluster(0.0D, 0.05D, 0.0D);
            core.setPosition(core.posX, core.posY + 0.05D, core.posZ);
            if (ticks >= FIXED_PHASE_TICKS[phase]) core.finishPodiumMove();
        } else if (phase == 4 && ticks % 8 == 0) {
            spawnWaveMob(world, core, WAVE_1, 2.0D);
        } else if (phase == 9 && ticks % 10 == 0) {
            spawnWaveMob(world, core, WAVE_2, 4.0D);
        } else if (phase == 14 && ticks % 5 == 0) {
            spawnWaveMob(world, core, WAVE_3, 8.0D);
        } else if ((phase == 10 || phase == 15) && ticks % 40 == 0) {
            curlTentacles(world, core);
        }

        if (phase == 9 && ticks == FIXED_PHASE_TICKS[phase]) {
            spawnRushSymbiont(world, core);
        }
    }

    private static void activateWave(WorldServer world, SupplementalEntities.CommandBlockEntity core, int wave, int particleCount) {
        play(world, core, "command_block_activates", SoundCategory.HOSTILE, wave == 3 ? 6.0F : 5.0F);
        for (int i = 0; i < particleCount; i++) {
            world.spawnParticle(EnumParticleTypes.PORTAL, core.posX, core.posY + 0.5D, core.posZ,
                    1, world.rand.nextGaussian(), world.rand.nextGaussian(), world.rand.nextGaussian(), 0.2D);
        }
        if (wave > 1) awakenTentacles(world, core, false);
    }

    private static void finishPhase(WorldServer world, SupplementalEntities.CommandBlockEntity core, int phase) {
        if (phase == 4 || phase == 9) {
            play(world, core, "command_block_power_down", SoundCategory.HOSTILE, 5.0F);
        } else if (phase == 14) {
            play(world, core, "command_block_power_down", SoundCategory.HOSTILE, 6.0F);
        }
    }

    private static void awakenTentacles(WorldServer world, SupplementalEntities.CommandBlockEntity core, boolean indefinite) {
        for (SickenedEntities.TentacleEntity tentacle : world.getEntitiesWithinAABB(SickenedEntities.TentacleEntity.class,
                core.getEntityBoundingBox().grow(50.0D))) {
            tentacle.setDormant(false);
            if (indefinite) tentacle.doIndefiniteAwakeAnimation();
            else tentacle.doAwakeAnimation();
            tentacle.setCanSwing(true);
            tentacle.setCanStrangle(true);
        }
    }

    private static void curlTentacles(WorldServer world, SupplementalEntities.CommandBlockEntity core) {
        for (SickenedEntities.TentacleEntity tentacle : world.getEntitiesWithinAABB(SickenedEntities.TentacleEntity.class,
                core.getEntityBoundingBox().grow(50.0D))) tentacle.curlAround(core.getPositionVector());
    }

    private static void activateHeads(WorldServer world, SupplementalEntities.CommandBlockEntity core) {
        for (SupplementalEntities.WitherStormHeadEntity head : world.getEntitiesWithinAABB(SupplementalEntities.WitherStormHeadEntity.class,
                core.getEntityBoundingBox().grow(50.0D))) {
            head.setActive(true);
            head.setRoar(false);
            head.setRoarTime(40);
        }
    }

    private static void injureStormHeads(WorldServer world, BowelsInstanceData.Instance instance) {
        WitherStormEntity storm = findStorm(world, instance.stormUuid);
        if (storm == null) return;
        for (int i = 0; i < 3; i++) {
            if (world.rand.nextFloat() > 0.6F) storm.attackHead(i, null);
        }
    }

    private static void spawnWaveMob(WorldServer world, SupplementalEntities.CommandBlockEntity core,
                                     MobWeight[] weights, double healthBonus) {
        SickenedMobEntity mob = createMob(world, choose(weights, world.rand));
        if (mob == null) return;
        BlockPos pos = randomNearbyPosition(world, core, 50, 6);
        if (pos == null) {
            mob.setDead();
            return;
        }
        mob.setPosition(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
        double maxHealth = mob.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() + healthBonus;
        mob.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth);
        mob.setHealth((float) maxHealth);
        mob.enablePersistence();
        if (world.rand.nextFloat() < 0.65F || healthBonus >= 8.0D) equipWaveMob(mob, world, healthBonus >= 8.0D);
        world.spawnEntity(mob);
        world.spawnParticle(EnumParticleTypes.PORTAL, mob.posX, mob.posY + 1.0D, mob.posZ,
                20, 0.25D, 0.5D, 0.25D, 0.01D);
        world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, mob.posX, mob.posY + 1.0D, mob.posZ,
                20, 0.25D, 0.5D, 0.25D, 0.01D);
    }

    private static void equipWaveMob(SickenedMobEntity mob, WorldServer world, boolean hard) {
        if (!(mob instanceof EntityMob)) return;
        ItemStack weapon;
        if (mob instanceof SickenedEntities.SickenedSkeletonEntity || mob instanceof SickenedEntities.SickenedPillagerEntity) {
            weapon = new ItemStack(hard ? Items.IRON_SWORD : Items.BOW);
        } else {
            weapon = new ItemStack(hard ? Items.DIAMOND_SWORD : Items.IRON_SWORD);
        }
        mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, weapon);
        if (hard) {
            mob.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            mob.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        }
    }

    private static void spawnRushSymbiont(WorldServer world, SupplementalEntities.CommandBlockEntity core) {
        BlockPos pos = randomNearbyPosition(world, core, 50, 6);
        if (pos == null) return;
        SickenedEntities.WitheredSymbiontEntity symbiont = new SickenedEntities.WitheredSymbiontEntity(world);
        symbiont.setPosition(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
        symbiont.setNonBossMode(true);
        symbiont.setRushMode(true);
        double maxHealth = symbiont.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() * 0.5D;
        symbiont.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth);
        symbiont.setHealth((float) maxHealth);
        symbiont.enablePersistence();
        world.spawnEntity(symbiont);
        world.spawnParticle(EnumParticleTypes.PORTAL, symbiont.posX, symbiont.posY + 1.0D, symbiont.posZ,
                40, 0.25D, 0.5D, 0.25D, 0.01D);
        symbiont.playSound(ModSounds.get("withered_symbiont_spawn"), 4.0F, 1.0F);
    }

    private static boolean guardsDefeated(WorldServer world, SupplementalEntities.CommandBlockEntity core) {
        AxisAlignedBB area = core.getEntityBoundingBox().grow(50.0D);
        for (SickenedEntities.WitheredSymbiontEntity symbiont : world.getEntitiesWithinAABB(SickenedEntities.WitheredSymbiontEntity.class, area)) {
            if (!symbiont.isDead && symbiont.isEntityAlive()) return false;
        }
        for (SupplementalEntities.WitherStormHeadEntity head : world.getEntitiesWithinAABB(SupplementalEntities.WitherStormHeadEntity.class, area)) {
            if (!head.isDead && head.isActive() && !head.isPlayingDead() && !head.isHurt()) return false;
        }
        return true;
    }

    private static void resolveDeath(WorldServer bowels, SupplementalEntities.CommandBlockEntity core,
                                     BowelsInstanceData.Instance instance) {
        WitherStormEntity storm = findStorm(bowels, instance.stormUuid);
        if (storm != null && !storm.isDead) storm.finishBowelsDeath();
        for (EntityPlayerMP player : playersNear(bowels, core, 192.0D)) {
            BowelsManager.leave(player);
        }
        play(bowels, core, "wither_storm_death", SoundCategory.HOSTILE, 20.0F);
        instance.bossPhase = 18;
        instance.bossPhaseTicks = 0;
    }

    private static void cleanup(WorldServer world, SupplementalEntities.CommandBlockEntity core,
                                BowelsInstanceData.Instance instance) {
        for (Entity entity : world.getEntitiesWithinAABB(Entity.class, core.getEntityBoundingBox().grow(64.0D))) {
            if (entity != core && !(entity instanceof EntityPlayerMP)) entity.setDead();
        }
        core.setDead();
        instance.completed = true;
        ChunkLoadingManager.INSTANCE.releaseBowelsInstance(world, instance.stormUuid);
        BowelsInstanceData.get(world).markDirty();
    }

    @Nullable
    private static WitherStormEntity findStorm(WorldServer world, UUID uuid) {
        if (world.getMinecraftServer() == null) return null;
        for (WorldServer level : world.getMinecraftServer().worlds) {
            if (level == null) continue;
            Entity entity = level.getEntityFromUuid(uuid);
            if (entity instanceof WitherStormEntity) return (WitherStormEntity) entity;
        }
        return null;
    }

    private static List<EntityPlayerMP> playersNear(WorldServer world, SupplementalEntities.CommandBlockEntity core, double radius) {
        List<EntityPlayerMP> result = new ArrayList<EntityPlayerMP>();
        for (EntityPlayerMP player : world.getEntitiesWithinAABB(EntityPlayerMP.class,
                core.getEntityBoundingBox().grow(radius))) result.add(player);
        return result;
    }

    @Nullable
    private static BlockPos randomNearbyPosition(WorldServer world, SupplementalEntities.CommandBlockEntity core,
                                                 int diameter, int attempts) {
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = Math.floorDiv(core.getPosition().getX() + world.rand.nextInt(diameter) - diameter / 2, 1);
            int z = Math.floorDiv(core.getPosition().getZ() + world.rand.nextInt(diameter) - diameter / 2, 1);
            BlockPos cursor = new BlockPos(x, core.getPosition().getY(), z);
            for (int down = 0; down < 30 && world.isAirBlock(cursor.down()); down++) cursor = cursor.down();
            BlockPos floor = cursor.down();
            if (!world.isSideSolid(floor, net.minecraft.util.EnumFacing.UP)) continue;
            if (Math.sqrt(floor.distanceSq(core.getPosition())) <= 6.0D) continue;
            if (!world.checkNoEntityCollision(new AxisAlignedBB(floor.getX() + 0.1D, floor.getY() + 1.0D,
                    floor.getZ() + 0.1D, floor.getX() + 0.9D, floor.getY() + 5.0D, floor.getZ() + 0.9D))) continue;
            return floor;
        }
        return null;
    }

    private static boolean isCommandBlockTool(DamageSource source) {
        if (!(source.getTrueSource() instanceof EntityPlayerMP)) return false;
        ResourceLocation name = ((EntityPlayerMP) source.getTrueSource()).getHeldItemMainhand().getItem().getRegistryName();
        if (name == null || !"witherstormmod".equals(name.getNamespace())) return false;
        String path = name.getPath();
        return path.contains("command_block_") && (path.endsWith("_sword") || path.endsWith("_pickaxe")
                || path.endsWith("_axe") || path.endsWith("_shovel") || path.endsWith("_hoe"));
    }

    private static boolean isVulnerablePhase(int phase) {
        return phase == 0 || phase == 5 || phase == 11 || phase == 16;
    }

    private static boolean isFixedPhase(int phase) {
        return phase > 0 && phase < FIXED_PHASE_TICKS.length && FIXED_PHASE_TICKS[phase] > 0;
    }

    private static void play(WorldServer world, Entity core, String sound, SoundCategory category, float volume) {
        world.playSound(null, core.getPosition(), ModSounds.get(sound), category, volume, 1.0F);
    }

    private static void play(WorldServer world, SupplementalEntities.CommandBlockEntity core,
                             String sound, SoundCategory category, float volume) {
        play(world, (Entity) core, sound, category, volume);
    }

    private static MobWeight mob(String name, int weight) {
        return new MobWeight(name, weight);
    }

    @Nullable
    private static SickenedMobEntity createMob(WorldServer world, @Nullable MobWeight selected) {
        if (selected == null) return null;
        if ("zombie".equals(selected.name)) return new SickenedEntities.SickenedZombieEntity(world);
        if ("skeleton".equals(selected.name)) return new SickenedEntities.SickenedSkeletonEntity(world);
        if ("spider".equals(selected.name)) return new SickenedEntities.SickenedSpiderEntity(world);
        if ("creeper".equals(selected.name)) return new SickenedEntities.SickenedCreeperEntity(world);
        if ("iron_golem".equals(selected.name)) return new SickenedEntities.SickenedIronGolemEntity(world);
        if ("villager".equals(selected.name)) return new SickenedEntities.SickenedVillagerEntity(world);
        if ("phantom".equals(selected.name)) return new SickenedEntities.SickenedPhantomEntity(world);
        if ("chicken".equals(selected.name)) return new SickenedEntities.SickenedChickenEntity(world);
        if ("cow".equals(selected.name)) return new SickenedEntities.SickenedCowEntity(world);
        if ("mushroom_cow".equals(selected.name)) return new SickenedEntities.SickenedMushroomCowEntity(world);
        if ("pig".equals(selected.name)) return new SickenedEntities.SickenedPigEntity(world);
        if ("bee".equals(selected.name)) return new SickenedEntities.SickenedBeeEntity(world);
        if ("parrot".equals(selected.name)) return new SickenedEntities.SickenedParrotEntity(world);
        if ("wolf".equals(selected.name)) return new SickenedEntities.SickenedWolfEntity(world);
        if ("cat".equals(selected.name)) return new SickenedEntities.SickenedCatEntity(world);
        if ("pillager".equals(selected.name)) return new SickenedEntities.SickenedPillagerEntity(world);
        if ("vindicator".equals(selected.name)) return new SickenedEntities.SickenedVindicatorEntity(world);
        return null;
    }

    @Nullable
    private static MobWeight choose(MobWeight[] values, java.util.Random random) {
        int total = 0;
        for (MobWeight value : values) total += value.weight;
        int selected = random.nextInt(total);
        for (MobWeight value : values) {
            selected -= value.weight;
            if (selected < 0) return value;
        }
        return values[values.length - 1];
    }

    private static final class MobWeight {
        private final String name;
        private final int weight;

        private MobWeight(String name, int weight) {
            this.name = name;
            this.weight = weight;
        }
    }
}
