package com.wdcftgg.witherstormmod.common.entity;

import com.wdcftgg.witherstormmod.common.advancement.LegacyCriteriaTriggers;
import com.wdcftgg.witherstormmod.common.config.LegacyWitherStormConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * 三个头部的目标、旋转、攻击和动画状态机。
 *
 * 上游把每个头实现成独立的 Head 实例。这里仍然使用一个管理器，但每个
 * HeadState 保存完整的独立状态，并通过 EntityWither 的 watched target id
 * 将瞄准方向同步给 1.12 客户端。
 */
public final class LegacyWitherStormHeadManager {
    private static final double[][][] OFFSETS = {
            {{0, 3, 0}, {-1.3, 2.2, 0}, {1.3, 2.2, 0}},
            {{0, 3, 0}, {-1.3, 2.2, 0}, {1.3, 2.2, 0}},
            {{0, 2.75, .5}, {-1.3, 2.2, 0}, {1.3, 2.2, 0}},
            {{0, 2.75, .5}, {-1.3, 2.2, 0}, {1.3, 2.2, 0}},
            {{0, 12, 10}, {-12, 22.5, 10}, {8.5, 24.5, 16}},
            {{0, 12, 10}, {-12, 22.5, 10}, {8.5, 24.5, 16}},
            {{0, 12, 10}, {-12, 22.5, 10}, {8.5, 24.5, 16}},
            {{0, 12, 10}, {-12, 22.5, 10}, {8.5, 24.5, 16}}
    };

    private final EntityWitherStormLegacy storm;
    private final HeadState[] heads = {new HeadState(), new HeadState(), new HeadState()};
    private final boolean[] jawMirror = new boolean[3];

    LegacyWitherStormHeadManager(EntityWitherStormLegacy storm) {
        this.storm = storm;
        Random random = new Random(storm.getEntityId() * 31L + 17L);
        for (int i = 0; i < jawMirror.length; i++) jawMirror[i] = random.nextBoolean();
    }

    public void tick() {
        int flags = storm.getHeadAnimationFlags();
        for (int index = 0; index < heads.length; index++) {
            HeadState head = heads[index];
            head.positionO = head.position;
            head.position = calculatePosition(index);
            head.yawO = head.yaw;
            head.pitchO = head.pitch;
            updateLook(index, head);
            updateBeamCutoff(head);
            head.mouthO = head.mouth;
            head.brokenO = head.broken;
            head.shakeO = head.shake;

            boolean roaring = (flags & roarBit(index)) != 0;
            boolean biting = (flags & biteBit(index)) != 0;
            if (!biting && roaring) {
                head.mouth += (1.0F - head.mouth) * 0.15F + 0.04F;
                head.mouth = Math.min(head.mouth, 2.0F);
            } else if (biting) {
                head.mouth += (1.0F - head.mouth) * 0.16F + 0.1F;
                head.mouth = Math.min(head.mouth, 1.4F);
            } else {
                head.mouth += -head.mouth * 0.16F - 0.02F;
                head.mouth = Math.max(head.mouth, 0.0F);
            }
            if (storm.onGround && storm.isDeadOrPlayingDead()) {
                head.broken += (1.0F - head.broken) * 0.2F + 0.05F;
                head.broken = Math.min(head.broken, 1.5F);
            } else {
                head.broken += -head.broken * 0.2F - 0.05F;
                head.broken = Math.max(head.broken, 0.0F);
            }
            if ((flags & shakeBit(index)) != 0) {
                head.shake += 0.02F + storm.getRNG().nextFloat() * 0.05F;
                if (head.shakeO >= 2.0F) {
                    head.shakeO = head.shake = 0.0F;
                    if (!storm.world.isRemote) storm.setHeadFlag(shakeBit(index), false);
                }
            } else if (head.shake != 0.0F) {
                head.shakeO = head.shake = 0.0F;
            }
        }
        if (!storm.world.isRemote) serverTick();
    }

    private void serverTick() {
        for (int index = 0; index < heads.length; index++) {
            HeadState head = heads[index];
            boolean enabled = isEnabled(index);
            EntityLivingBase target = selectTarget(index);
            head.target = target;
            storm.updateWatchedTargetId(index, target == null || !enabled ? 0 : target.getEntityId());

            if (head.injuryTicks > 0) head.injuryTicks--;
            if (head.injuryCooldown > 0) head.injuryCooldown--;
            if (storm.getInvulnerableTicks() > 0) {
                head.target = null;
                storm.updateWatchedTargetId(index, 0);
                continue;
            }
            if (storm.isHeadFlagSet(roarBit(index)) && ++head.roarTicks > 40) {
                head.roarTicks = 0;
                storm.setHeadFlag(roarBit(index), false);
            }
            if (storm.isHeadFlagSet(biteBit(index)) && ++head.biteTicks > 10) {
                head.biteTicks = 0;
                storm.setHeadFlag(biteBit(index), false);
                storm.playHeadBiteSound(index);
            }
            if (head.nextRoar <= 0) head.nextRoar = nextRoarDelay();
            if (--head.nextRoar <= 0 && !storm.isDeadOrPlayingDead() && enabled) {
                head.nextRoar = nextRoarDelay();
                if (storm.tractorBeamActive(index) && !storm.isAttractingFormidibomb()) {
                    Vec3d look = getLookVector(head);
                    storm.spawnFlamingWitherSkull(index, head.position.x + look.x,
                            head.position.y + look.y, head.position.z + look.z);
                }
                storm.setHeadFlag(roarBit(index), true);
                storm.playHeadRoarSound(index);
            }
            if (storm.tractorBeamActive(index) && storm.getPhase() >= 2
                    && storm.ticksExisted >= head.nextClusterPickup) {
                head.nextClusterPickup = storm.ticksExisted + nextClusterPickupDelay();
                storm.createClusterFromLook(head.pitch, head.yaw, storm.getClusterRadius(), index);
                storm.removeFluidFromLook(head.pitch, head.yaw, index);
            }
            if (!enabled || head.injuryTicks > 0 || storm.isDeadOrPlayingDead()) continue;

            if (head.nextAttack <= 0) {
                head.nextAttack = storm.getPhase() < 4
                        ? 10 + storm.getRNG().nextInt(10)
                        : 1200 + storm.getRNG().nextInt(120);
            }
            if (--head.nextAttack > 0) continue;
            if (target != null && !storm.isDistracted()) {
                if (!storm.tractorBeamActive(index)) {
                    storm.performRangedAttack(index, target);
                }
                head.nextAttack = storm.getPhase() < 4
                        ? 40 + storm.getRNG().nextInt(20)
                        : 1800 + storm.getRNG().nextInt(160);
            } else if (head.idleAttacks++ > 15 && !storm.tractorBeamActive(index)) {
                Vec3d origin = head.position;
                storm.performRangedAttack(index, origin.x + storm.getRNG().nextInt(21) - 10.0D,
                        origin.y + storm.getRNG().nextInt(11) - 5.0D,
                        origin.z + storm.getRNG().nextInt(21) - 10.0D, true);
                head.idleAttacks = 0;
                head.nextAttack = 40 + storm.getRNG().nextInt(20);
            } else {
                head.nextAttack = 40 + storm.getRNG().nextInt(20);
            }
            if (target != null && head.position.squareDistanceTo(target.getPositionVector()) < 36.0D
                    && !storm.isHeadFlagSet(biteBit(index))) startBiting(index);
        }
    }

    private int nextClusterPickupDelay() {
        int phase = storm.getPhase();
        if (phase <= 2) return 24;
        if (phase == 3) return 15;
        if (phase == 4) return 5 + storm.getRNG().nextInt(20);
        if (phase == 5) return 5 + storm.getRNG().nextInt(15);
        return storm.getRNG().nextInt(15);
    }

    private int nextRoarDelay() {
        int minimum = Math.max(1, LegacyWitherStormConfig.minimumRoarInterval) * 20;
        int maximum = Math.max(minimum, LegacyWitherStormConfig.maximumRoarInterval * 20);
        return minimum + (maximum > minimum ? storm.getRNG().nextInt(maximum - minimum) : 0);
    }

    private void updateBeamCutoff(HeadState head) {
        if (storm.world.isRemote || head.position == null) return;
        Vec3d end = head.position.add(getLookVector(head).scale(250.0D));
        net.minecraft.util.math.RayTraceResult result = storm.world.rayTraceBlocks(head.position, end, false, true, false);
        head.beamCutoff = result != null && result.typeOfHit == net.minecraft.util.math.RayTraceResult.Type.BLOCK
                ? head.position.distanceTo(result.hitVec) : -1.0D;
    }

    @Nullable
    private EntityLivingBase selectTarget(int index) {
        if (!isEnabled(index)) return null;
        if (index == 0) return storm.getUltimateTarget() != null ? storm.getUltimateTarget() : storm.getAttackTarget();
        EntityLivingBase main = storm.getUltimateTarget();
        List<EntityLivingBase> candidates = storm.world.getEntitiesWithinAABB(EntityLivingBase.class,
                storm.getEntityBoundingBox().grow(storm.getPhase() > 3 ? 160.0D : 32.0D, 80.0D,
                        storm.getPhase() > 3 ? 160.0D : 32.0D), entity -> entity != storm && entity != main
                        && entity.isEntityAlive() && !(entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.disableDamage)
                        && !(entity instanceof EntityWitherStormLegacy)
                        && !(entity instanceof SupplementalEntities.StormPartBase));
        EntityLivingBase nearest = null;
        double distance = Double.MAX_VALUE;
        for (EntityLivingBase candidate : candidates) {
            double current = storm.getDistanceSq(candidate);
            if (current < distance && storm.canSee(index, candidate)) {
                distance = current;
                nearest = candidate;
            }
        }
        return nearest != null ? nearest : main;
    }

    private void updateLook(int index, HeadState head) {
        EntityLivingBase target = head.target;
        if (target == null && !storm.world.isRemote) target = selectTarget(index);
        if (target == null && storm.world.isRemote) {
            int id = storm.getWatchedTargetId(index);
            Entity entity = id > 0 ? storm.world.getEntityByID(id) : null;
            if (entity instanceof EntityLivingBase) target = (EntityLivingBase) entity;
        }
        if (target != null) {
            double dx = target.posX - head.position.x;
            double dy = target.posY + target.getEyeHeight() - head.position.y;
            double dz = target.posZ - head.position.z;
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            float wantedYaw = (float) (MathHelper.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
            float wantedPitch = (float) (-(MathHelper.atan2(dy, horizontal) * 180.0D / Math.PI));
            head.yaw = rotlerp(head.yaw, wantedYaw, 10.0F);
            head.pitch = rotlerp(head.pitch, wantedPitch, 20.0F);
        } else {
            head.yaw = rotlerp(head.yaw, storm.renderYawOffset, 10.0F);
            head.pitch = rotlerp(head.pitch, 0.0F, 10.0F);
        }
    }

    private Vec3d getLookVector(HeadState head) {
        float pitch = head.pitch * 0.017453292F;
        float yaw = head.yaw * 0.017453292F;
        float horizontal = MathHelper.cos(pitch);
        return new Vec3d(-MathHelper.sin(yaw) * horizontal, -MathHelper.sin(pitch), MathHelper.cos(yaw) * horizontal).normalize();
    }

    public Vec3d getLookVector(int index) {
        return getLookVector(heads[head(index)]);
    }

    public double getTractorBeamCutoff(int index) {
        return heads[head(index)].beamCutoff;
    }

    public void onPhaseChanged(int phase) {
        for (HeadState head : heads) {
            head.requiredHits = phase > 3 ? 3 + storm.getRNG().nextInt(3) : 1 + storm.getRNG().nextInt(2);
            head.hits = 0;
            head.nextAttack = 0;
            head.nextClusterPickup = 0;
        }
    }

    private boolean isEnabled(int index) { return index == 0 || !storm.areOtherHeadsDisabled(); }

    public void startBiting(int index) {
        index = head(index);
        heads[index].biteTicks = 0;
        storm.setHeadFlag(biteBit(index), true);
    }

    public void onHurt() { storm.setHeadFlag(shakeBit(storm.getRNG().nextInt(3)), true); }

    public void onStartFalling() {
        for (int index = 0; index < heads.length; index++) {
            storm.setHeadFlag(roarBit(index), true);
            heads[index].roarTicks = 0;
            storm.playHeadRoarSound(index);
        }
    }

    public void onDeath() {
        for (int index = 0; index < heads.length; index++) {
            storm.setHeadFlag(roarBit(index), true);
            storm.setHeadFlag(biteBit(index), false);
        }
    }

    public boolean checkAndCountAttack(int index, @Nullable Entity attacker) {
        if (!LegacyWitherStormConfig.canAttackHeads) return false;
        index = head(index);
        HeadState state = heads[index];
        if (state.injuryCooldown > 0 || state.injuryTicks > 0) return false;
        state.hits++;
        if (state.hits < requiredHits(index)) {
            storm.setHeadFlag(roarBit(index), true);
            state.roarTicks = 20;
            return false;
        }
        hurt(index, attacker);
        return true;
    }

    private int requiredHits(int index) {
        HeadState state = heads[index];
        if (state.requiredHits <= 0) state.requiredHits = storm.getPhase() > 3 ? 3 + storm.getRNG().nextInt(3) : 1 + storm.getRNG().nextInt(2);
        return state.requiredHits;
    }

    private void hurt(int index, @Nullable Entity attacker) {
        HeadState state = heads[index];
        boolean attackerWasTarget = attacker instanceof EntityPlayerMP
                && isTargetedByAnyHead((EntityPlayerMP) attacker);
        state.injuryTicks = 320;
        state.injuryCooldown = 40;
        state.hits = 0;
        state.requiredHits = storm.getPhase() > 3 ? 3 + storm.getRNG().nextInt(3) : 1 + storm.getRNG().nextInt(2);
        storm.setHeadFlag(roarBit(index), true);
        storm.setHeadFlag(shakeBit(index), true);
        Vec3d look = getLookVector(state);
        storm.spawnBlueFlamingWitherSkull(index, state.position.x + look.x, state.position.y + look.y, state.position.z + look.z);
        storm.playHeadRoarSound(index);
        if (attackerWasTarget) {
            LegacyCriteriaTriggers.ESCAPE_WITHER_STORM.trigger(
                    (EntityPlayerMP) attacker, storm);
        }
    }

    private boolean isTargetedByAnyHead(EntityLivingBase target) {
        for (HeadState head : heads) {
            if (head.target == target) return true;
        }
        return false;
    }

    public boolean isHeadInjured(int index) { return heads[head(index)].injuryTicks > 0; }
    public int getHeadInjuryTicks(int index) { return heads[head(index)].injuryTicks; }
    public EntityLivingBase getTarget(int index) { return heads[head(index)].target; }
    public void setTarget(int index, @Nullable EntityLivingBase target) {
        heads[head(index)].target = target;
        storm.updateWatchedTargetId(head(index), target == null ? 0 : target.getEntityId());
    }

    public float getYaw(int index, float partial) {
        HeadState state = heads[head(index)];
        return lerp(state.yawO, state.yaw, partial);
    }

    public float getPitch(int index, float partial) {
        HeadState state = heads[head(index)];
        return lerp(state.pitchO, state.pitch, partial);
    }

    public Vec3d getPosition(int index, float partial) {
        HeadState state = heads[head(index)];
        return new Vec3d(lerp(state.positionO.x, state.position.x, partial),
                lerp(state.positionO.y, state.position.y, partial),
                lerp(state.positionO.z, state.position.z, partial));
    }

    public AxisAlignedBB getBounds(int index) {
        Vec3d position = heads[head(index)].position;
        double size = storm.getPhase() > 3 ? 3.0D : 0.5D;
        return new AxisAlignedBB(position.x - size, position.y - size, position.z - size,
                position.x + size, position.y + size, position.z + size);
    }

    public float getMouth(int index, float partial) {
        HeadState state = heads[head(index)];
        return lerp(state.mouthO, state.mouth, partial);
    }

    public float getBrokenRoll(int index, float partial) {
        HeadState state = heads[head(index)];
        float value = MathHelper.sin(lerp(state.brokenO, state.broken, partial) * 0.3F) * 10.0F - 10.0F;
        return value * (jawMirror[head(index)] ? -1.0F : 1.0F);
    }

    public float getShakeRoll(int index, float partial) {
        HeadState state = heads[head(index)];
        float value = MathHelper.clamp(lerp(state.shakeO, state.shake, partial), 0.0F, 1.0F);
        return MathHelper.sin(value * (float) Math.PI) * MathHelper.sin(value * (float) Math.PI * 12.0F) * 0.05F * (float) Math.PI;
    }

    private Vec3d calculatePosition(int index) {
        int phase = MathHelper.clamp(storm.getPhase(), 0, OFFSETS.length - 1);
        double[] offset = OFFSETS[phase][head(index)];
        float yaw = (storm.renderYawOffset + 180.0F) * 0.017453292F;
        double x = MathHelper.cos(yaw) * offset[0] + MathHelper.cos(yaw + (float) Math.PI / 2.0F) * offset[2];
        double z = MathHelper.sin(yaw) * offset[0] + MathHelper.sin(yaw + (float) Math.PI / 2.0F) * offset[2];
        return new Vec3d(storm.posX + x, storm.posY + offset[1], storm.posZ + z);
    }

    public void writeToNBT(NBTTagCompound tag) {
        for (int index = 0; index < heads.length; index++) {
            HeadState state = heads[index];
            NBTTagCompound head = new NBTTagCompound();
            head.setInteger("RoarTicks", state.roarTicks);
            head.setInteger("BiteTicks", state.biteTicks);
            head.setInteger("NextRoarTick", state.nextRoar);
            head.setInteger("NextAttackTick", state.nextAttack);
            head.setInteger("NextClusterPickup", state.nextClusterPickup);
            head.setInteger("IdleAttacks", state.idleAttacks);
            head.setInteger("InjuryTicks", state.injuryTicks);
            head.setInteger("InjuryCooldown", state.injuryCooldown);
            head.setInteger("HeadHits", state.hits);
            head.setInteger("RequiredHits", state.requiredHits);
            tag.setTag("WitherStormInternalHead" + index, head);
        }
    }

    public void readFromNBT(NBTTagCompound tag) {
        for (int index = 0; index < heads.length; index++) {
            String key = "WitherStormInternalHead" + index;
            if (!tag.hasKey(key, 10)) continue;
            NBTTagCompound head = tag.getCompoundTag(key);
            HeadState state = heads[index];
            state.roarTicks = Math.max(0, head.getInteger("RoarTicks"));
            state.biteTicks = Math.max(0, head.getInteger("BiteTicks"));
            state.nextRoar = Math.max(0, head.getInteger("NextRoarTick"));
            state.nextAttack = Math.max(0, head.getInteger("NextAttackTick"));
            state.nextClusterPickup = Math.max(0, head.getInteger("NextClusterPickup"));
            state.idleAttacks = Math.max(0, head.getInteger("IdleAttacks"));
            state.injuryTicks = Math.max(0, head.getInteger("InjuryTicks"));
            state.injuryCooldown = Math.max(0, head.getInteger("InjuryCooldown"));
            state.hits = Math.max(0, head.getInteger("HeadHits"));
            state.requiredHits = Math.max(0, head.getInteger("RequiredHits"));
        }
    }

    private static int roarBit(int index) { return 1 << head(index); }
    private static int biteBit(int index) { return 1 << (3 + head(index)); }
    private static int shakeBit(int index) { return 1 << (6 + head(index)); }
    private static int head(int index) { return MathHelper.clamp(index, 0, 2); }
    private static float lerp(float a, float b, float partial) { return a + (b - a) * partial; }
    private static double lerp(double a, double b, float partial) { return a + (b - a) * partial; }
    private static float rotlerp(float current, float wanted, float max) {
        float delta = MathHelper.wrapDegrees(wanted - current);
        delta = MathHelper.clamp(delta, -max, max);
        return current + delta;
    }

    private static final class HeadState {
        Vec3d position = Vec3d.ZERO;
        Vec3d positionO = Vec3d.ZERO;
        EntityLivingBase target;
        float yaw;
        float yawO;
        float pitch;
        float pitchO;
        float mouth;
        float mouthO;
        float broken;
        float brokenO;
        float shake;
        float shakeO;
        int roarTicks;
        int biteTicks;
        int nextRoar;
        int nextAttack;
        int nextClusterPickup;
        int idleAttacks;
        int injuryTicks;
        int injuryCooldown;
        int hits;
        int requiredHits;
        double beamCutoff = -1.0D;
    }
}
