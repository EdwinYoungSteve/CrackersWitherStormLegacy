package com.wdcftgg.witherstormmod.common.event;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.common.advancement.ModCriteriaTriggers;
import com.wdcftgg.witherstormmod.common.entity.SupplementalEntities;
import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import thedarkcolour.futuremc.tile.BellTileEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class AdvancementEvents {

    private static final ResourceLocation FUTURE_MC_BELL = new ResourceLocation("futuremc", "bell");
    private static final double BELL_STORM_RANGE = 300.0D;
    private static final List<PendingBellUse> PENDING_BELL_USES = new ArrayList<PendingBellUse>();

    private AdvancementEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBellUsed(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled() || event.getUseBlock() == Event.Result.DENY
                || event.getWorld().isRemote
                || !(event.getEntityPlayer() instanceof EntityPlayerMP)) return;
        boolean bypassSneak = event.getEntityPlayer().getHeldItemMainhand()
                .doesSneakBypassUse(event.getWorld(), event.getPos(), event.getEntityPlayer())
                && event.getEntityPlayer().getHeldItemOffhand()
                .doesSneakBypassUse(event.getWorld(), event.getPos(), event.getEntityPlayer());
        if (event.getEntityPlayer().isSneaking() && !bypassSneak
                && event.getUseBlock() != Event.Result.ALLOW) return;
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        Block block = state.getBlock();
        if (!FUTURE_MC_BELL.equals(block.getRegistryName())
                || !canRingFrom(state, event.getFace(), event.getHitVec().y - event.getPos().getY())) return;
        TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        if (!(tile instanceof BellTileEntity)) return;
        BellTileEntity bell = (BellTileEntity) tile;
        PENDING_BELL_USES.add(new PendingBellUse(event.getWorld(), event.getPos(),
                event.getEntityPlayer().getUniqueID(), bell.isRinging(), bell.getRingingTicks()));
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote) return;
        Iterator<PendingBellUse> iterator = PENDING_BELL_USES.iterator();
        while (iterator.hasNext()) {
            PendingBellUse pending = iterator.next();
            if (pending.world != event.world) continue;
            iterator.remove();
            EntityPlayerMP player = event.world.getMinecraftServer().getPlayerList()
                    .getPlayerByUUID(pending.playerUuid);
            TileEntity tile = event.world.getTileEntity(pending.position);
            if (player == null || !(tile instanceof BellTileEntity)
                    || !hasRung((BellTileEntity) tile, pending)) continue;
            Entity storm = nearestStorm(event.world, player);
            if (storm != null) ModCriteriaTriggers.RING_BELL_NEAR_STORM.trigger(player, storm);
        }
    }

    private static boolean hasRung(BellTileEntity bell, PendingBellUse pending) {
        if (!bell.isRinging()) return false;
        if (!pending.wasRinging) return true;
        return bell.getRingingTicks() < pending.ringingTicks || bell.getRingingTicks() <= 1;
    }

    private static Entity nearestStorm(World world, EntityPlayerMP player) {
        AxisAlignedBB area = player.getEntityBoundingBox().grow(BELL_STORM_RANGE);
        List<Entity> storms = world.getEntitiesWithinAABB(Entity.class, area,
                storm -> !storm.isDead && (storm instanceof WitherStormEntity
                        || storm instanceof SupplementalEntities.WitherStormSegmentEntity));
        Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity storm : storms) {
            double distance = player.getDistanceSq(storm);
            if (distance < nearestDistance) {
                nearest = storm;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static boolean canRingFrom(IBlockState state, EnumFacing clickedFace,
                                       double relativeHitY) {
        if (clickedFace == null || clickedFace.getAxis() == EnumFacing.Axis.Y
                || relativeHitY > 0.8124D) return false;
        String attachment = propertyValue(state, "attachment");
        String facingName = propertyValue(state, "facing");
        if (attachment == null || facingName == null) return false;
        EnumFacing facing = EnumFacing.byName(facingName);
        if (facing == null) return false;
        if ("floor".equals(attachment)) return facing.getAxis() == clickedFace.getAxis();
        if ("ceiling".equals(attachment) || "single_wall".equals(attachment)) {
            return facing.getAxis() != clickedFace.getAxis();
        }
        return "double_wall".equals(attachment);
    }

    private static String propertyValue(IBlockState state, String propertyName) {
        Collection<IProperty<?>> properties = state.getPropertyKeys();
        for (IProperty<?> property : properties) {
            if (propertyName.equals(property.getName())) return propertyValue(state, property);
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String propertyValue(IBlockState state, IProperty property) {
        return property.getName((Comparable) state.getValue(property));
    }

    private static final class PendingBellUse {
        private final World world;
        private final BlockPos position;
        private final UUID playerUuid;
        private final boolean wasRinging;
        private final int ringingTicks;

        private PendingBellUse(World world, BlockPos position, UUID playerUuid,
                               boolean wasRinging, int ringingTicks) {
            this.world = world;
            this.position = position.toImmutable();
            this.playerUuid = playerUuid;
            this.wasRinging = wasRinging;
            this.ringingTicks = ringingTicks;
        }
    }
}
