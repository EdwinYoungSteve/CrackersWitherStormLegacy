package com.wdcftgg.witherstormmod.common.network;

import com.wdcftgg.witherstormmod.Tags;
import com.wdcftgg.witherstormmod.WitherStormMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import com.wdcftgg.witherstormmod.common.entity.WitherStormEntity;
import com.wdcftgg.witherstormmod.common.inventory.SuperBeaconContainer;
import com.wdcftgg.witherstormmod.common.tile.AbstractSuperBeaconTileEntity;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/** 将上游仅发往客户端的表现消息映射到 Forge 1.12 的 SimpleImpl 通道。 */
public final class ModNetwork {
    public static final int SUPER_BEACON_RESUMMON_BURST = 0;
    public static final int SUPER_BEACON_ITEM_BURST = 1;
    private static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);
    private static boolean registered;

    private ModNetwork() {
    }

    public static synchronized void register() {
        if (registered) return;
        int discriminator = 0;
        CHANNEL.registerMessage(ShakeScreenMessage.Handler.class, ShakeScreenMessage.class,
                discriminator++, Side.CLIENT);
        CHANNEL.registerMessage(BlindScreenMessage.Handler.class, BlindScreenMessage.class,
                discriminator++, Side.CLIENT);
        CHANNEL.registerMessage(GlobalSoundMessage.Handler.class, GlobalSoundMessage.class,
                discriminator++, Side.CLIENT);
        CHANNEL.registerMessage(FormidibombExplosionMessage.Handler.class, FormidibombExplosionMessage.class,
                discriminator++, Side.CLIENT);
        CHANNEL.registerMessage(SuperBeaconSetEffectMessage.Handler.class, SuperBeaconSetEffectMessage.class,
                discriminator++, Side.SERVER);
        CHANNEL.registerMessage(SuperBeaconToggleAreaMessage.Handler.class, SuperBeaconToggleAreaMessage.class,
                discriminator++, Side.SERVER);
        CHANNEL.registerMessage(SuperBeaconParticlesMessage.Handler.class, SuperBeaconParticlesMessage.class,
                discriminator++, Side.CLIENT);
        CHANNEL.registerMessage(DistantSuperBeaconMessage.Handler.class, DistantSuperBeaconMessage.class,
                discriminator++, Side.CLIENT);
        CHANNEL.registerMessage(InjureWitherStormHeadMessage.Handler.class, InjureWitherStormHeadMessage.class,
                discriminator, Side.SERVER);
        registered = true;
    }

    public static void shakeTracking(Entity entity, float duration, float power) {
        if (entity == null || entity.world.isRemote) return;
        CHANNEL.sendToAllTracking(new ShakeScreenMessage(duration, power), entity);
    }

    public static void setPlayerMotion(EntityPlayerMP player, Vec3d motion) {
        if (player == null || motion == null) return;
        player.connection.sendPacket(new SPacketEntityVelocity(
                player.getEntityId(), motion.x, motion.y, motion.z));
    }

    public static void shakeNear(World world, double x, double y, double z, double radius,
                                 float duration, float power) {
        if (world == null || world.isRemote) return;
        CHANNEL.sendToAllAround(new ShakeScreenMessage(duration, power),
                new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, radius));
    }

    public static void shakeDimension(World world, float duration, float power) {
        if (world == null || world.isRemote) return;
        CHANNEL.sendToDimension(new ShakeScreenMessage(duration, power), world.provider.getDimension());
    }

    public static void shakeAll(World world, float duration, float power) {
        if (world == null || world.isRemote) return;
        CHANNEL.sendToAll(new ShakeScreenMessage(duration, power));
    }

    public static void blindTracking(Entity entity, int duration, int fadeInDuration, int fadeOutDuration) {
        if (entity == null || entity.world.isRemote) return;
        CHANNEL.sendToAllTracking(new BlindScreenMessage(duration, fadeInDuration, fadeOutDuration), entity);
    }

    public static void blindNear(World world, double x, double y, double z, double radius,
                                 int duration, int fadeInDuration, int fadeOutDuration) {
        if (world == null || world.isRemote) return;
        CHANNEL.sendToAllAround(new BlindScreenMessage(duration, fadeInDuration, fadeOutDuration),
                new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, radius));
    }

    public static void playGlobalSound(World world, SoundEvent sound, float volume, float pitch) {
        if (world == null || world.isRemote || sound == null || sound.getRegistryName() == null) return;
        CHANNEL.sendToDimension(new GlobalSoundMessage(sound.getRegistryName(), volume, pitch),
                world.provider.getDimension());
    }

    public static void playGlobalSoundAll(World world, SoundEvent sound, float volume, float pitch) {
        if (world == null || world.isRemote || sound == null || sound.getRegistryName() == null) return;
        CHANNEL.sendToAll(new GlobalSoundMessage(sound.getRegistryName(), volume, pitch));
    }

    public static void sendFormidibombExplosion(World world, Entity source, double x, double y, double z,
                                                int radius, int squish) {
        if (world == null || world.isRemote) return;
        int sourceId = source == null ? 0 : source.getEntityId();
        CHANNEL.sendToDimension(new FormidibombExplosionMessage(sourceId, x, y, z, radius, squish),
                world.provider.getDimension());
    }

    public static void setSuperBeaconEffect(int effectId) {
        CHANNEL.sendToServer(new SuperBeaconSetEffectMessage(effectId));
    }

    public static void toggleSuperBeaconArea(boolean show) {
        CHANNEL.sendToServer(new SuperBeaconToggleAreaMessage(show));
    }

    public static void sendSuperBeaconParticles(World world, net.minecraft.util.math.BlockPos position,
                                                int type) {
        if (world == null || world.isRemote || position == null) return;
        CHANNEL.sendToAllAround(new SuperBeaconParticlesMessage(position, type),
                new NetworkRegistry.TargetPoint(world.provider.getDimension(),
                        position.getX() + 0.5D, position.getY() + 1.5D, position.getZ() + 0.5D, 96.0D));
    }

    public static void updateDistantSuperBeacon(AbstractSuperBeaconTileEntity beacon) {
        if (beacon == null || beacon.getWorld() == null || beacon.getWorld().isRemote) return;
        int[] color = beacon.getBeamColor();
        CHANNEL.sendToDimension(new DistantSuperBeaconMessage(beacon.getPos(), color,
                        beacon.isActive(), beacon.getBeamHeight(), beacon.getBeamThickness(),
                        beacon.getOuterBeamThickness(), false),
                beacon.getWorld().provider.getDimension());
    }

    public static void removeDistantSuperBeacon(AbstractSuperBeaconTileEntity beacon) {
        if (beacon == null || beacon.getWorld() == null || beacon.getWorld().isRemote) return;
        CHANNEL.sendToDimension(new DistantSuperBeaconMessage(beacon.getPos(), new int[] {255, 255, 255},
                        false, 0, 0.0F, 0.0F, true),
                beacon.getWorld().provider.getDimension());
    }

    public static void injureWitherStormHead(WitherStormEntity storm, int head) {
        if (storm == null || !storm.world.isRemote || head < 0 || head >= storm.getTotalHeads()) return;
        CHANNEL.sendToServer(new InjureWitherStormHeadMessage(storm.getEntityId(), head));
    }

    public static final class ShakeScreenMessage implements IMessage {
        private float duration;
        private float power;

        public ShakeScreenMessage() {
        }

        public ShakeScreenMessage(float duration, float power) {
            this.duration = duration;
            this.power = power;
        }

        public float getDuration() {
            return duration;
        }

        public float getPower() {
            return power;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            duration = buffer.readFloat();
            power = buffer.readFloat();
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeFloat(duration);
            buffer.writeFloat(power);
        }

        public static final class Handler implements IMessageHandler<ShakeScreenMessage, IMessage> {
            @Override
            public IMessage onMessage(ShakeScreenMessage message, MessageContext context) {
                WitherStormMod.proxy.handleShakeScreen(message.duration, message.power);
                return null;
            }
        }
    }

    public static final class BlindScreenMessage implements IMessage {
        private int duration;
        private int fadeInDuration;
        private int fadeOutDuration;

        public BlindScreenMessage() {
        }

        public BlindScreenMessage(int duration, int fadeInDuration, int fadeOutDuration) {
            this.duration = duration;
            this.fadeInDuration = fadeInDuration;
            this.fadeOutDuration = fadeOutDuration;
        }

        public int getDuration() {
            return duration;
        }

        public int getFadeInDuration() {
            return fadeInDuration;
        }

        public int getFadeOutDuration() {
            return fadeOutDuration;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            duration = buffer.readInt();
            fadeInDuration = buffer.readInt();
            fadeOutDuration = buffer.readInt();
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeInt(duration);
            buffer.writeInt(fadeInDuration);
            buffer.writeInt(fadeOutDuration);
        }

        public static final class Handler implements IMessageHandler<BlindScreenMessage, IMessage> {
            @Override
            public IMessage onMessage(BlindScreenMessage message, MessageContext context) {
                WitherStormMod.proxy.handleBlindScreen(
                        message.duration, message.fadeInDuration, message.fadeOutDuration);
                return null;
            }
        }
    }

    public static final class GlobalSoundMessage implements IMessage {
        private ResourceLocation sound;
        private float volume;
        private float pitch;

        public GlobalSoundMessage() {
        }

        public GlobalSoundMessage(ResourceLocation sound, float volume, float pitch) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }

        public ResourceLocation getSound() {
            return sound;
        }

        public float getVolume() {
            return volume;
        }

        public float getPitch() {
            return pitch;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            sound = new ResourceLocation(ByteBufUtils.readUTF8String(buffer));
            volume = buffer.readFloat();
            pitch = buffer.readFloat();
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            ByteBufUtils.writeUTF8String(buffer, sound.toString());
            buffer.writeFloat(volume);
            buffer.writeFloat(pitch);
        }

        public static final class Handler implements IMessageHandler<GlobalSoundMessage, IMessage> {
            @Override
            public IMessage onMessage(GlobalSoundMessage message, MessageContext context) {
                WitherStormMod.proxy.handleGlobalSound(message.sound, message.volume, message.pitch);
                return null;
            }
        }
    }

    public static final class FormidibombExplosionMessage implements IMessage {
        private int sourceEntityId;
        private double x;
        private double y;
        private double z;
        private byte radius;
        private byte squish;

        public FormidibombExplosionMessage() {
        }

        public FormidibombExplosionMessage(int sourceEntityId, double x, double y, double z,
                                           int radius, int squish) {
            this.sourceEntityId = sourceEntityId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = (byte) radius;
            this.squish = (byte) squish;
        }

        public int getSourceEntityId() {
            return sourceEntityId;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        public int getRadius() {
            return radius;
        }

        public int getSquish() {
            return squish;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            sourceEntityId = buffer.readInt();
            x = buffer.readDouble();
            y = buffer.readDouble();
            z = buffer.readDouble();
            radius = buffer.readByte();
            squish = buffer.readByte();
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeInt(sourceEntityId);
            buffer.writeDouble(x);
            buffer.writeDouble(y);
            buffer.writeDouble(z);
            buffer.writeByte(radius);
            buffer.writeByte(squish);
        }

        public static final class Handler implements IMessageHandler<FormidibombExplosionMessage, IMessage> {
            @Override
            public IMessage onMessage(FormidibombExplosionMessage message, MessageContext context) {
                WitherStormMod.proxy.handleFormidibombExplosion(message.sourceEntityId,
                        message.x, message.y, message.z, message.radius, message.squish);
                return null;
            }
        }
    }

    public static final class SuperBeaconSetEffectMessage implements IMessage {
        private int effectId;

        public SuperBeaconSetEffectMessage() {
        }

        public SuperBeaconSetEffectMessage(int effectId) {
            this.effectId = effectId;
        }

        public int getEffectId() {
            return effectId;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            effectId = buffer.readInt();
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeInt(effectId);
        }

        public static final class Handler implements IMessageHandler<SuperBeaconSetEffectMessage, IMessage> {
            @Override
            public IMessage onMessage(final SuperBeaconSetEffectMessage message, final MessageContext context) {
                final EntityPlayerMP player = context.getServerHandler().player;
                player.getServerWorld().addScheduledTask(() -> {
                    if (player.openContainer instanceof SuperBeaconContainer
                            && player.openContainer.canInteractWith(player)) {
                        ((SuperBeaconContainer) player.openContainer).requestEffect(player, message.effectId);
                    }
                });
                return null;
            }
        }
    }

    public static final class SuperBeaconToggleAreaMessage implements IMessage {
        private boolean show;

        public SuperBeaconToggleAreaMessage() {
        }

        public SuperBeaconToggleAreaMessage(boolean show) {
            this.show = show;
        }

        public boolean shouldShowArea() {
            return show;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            show = buffer.readBoolean();
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeBoolean(show);
        }

        public static final class Handler implements IMessageHandler<SuperBeaconToggleAreaMessage, IMessage> {
            @Override
            public IMessage onMessage(final SuperBeaconToggleAreaMessage message,
                                      final MessageContext context) {
                final EntityPlayerMP player = context.getServerHandler().player;
                player.getServerWorld().addScheduledTask(() -> {
                    if (player.openContainer instanceof SuperBeaconContainer
                            && player.openContainer.canInteractWith(player)) {
                        ((SuperBeaconContainer) player.openContainer).setShowArea(message.show);
                    }
                });
                return null;
            }
        }
    }

    public static final class SuperBeaconParticlesMessage implements IMessage {
        private net.minecraft.util.math.BlockPos position;
        private int type;

        public SuperBeaconParticlesMessage() {
        }

        public SuperBeaconParticlesMessage(net.minecraft.util.math.BlockPos position, int type) {
            this.position = position;
            this.type = type;
        }

        public net.minecraft.util.math.BlockPos getPosition() {
            return position;
        }

        public int getType() {
            return type;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            position = net.minecraft.util.math.BlockPos.fromLong(buffer.readLong());
            type = buffer.readUnsignedByte();
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeLong(position.toLong());
            buffer.writeByte(type);
        }

        public static final class Handler implements IMessageHandler<SuperBeaconParticlesMessage, IMessage> {
            @Override
            public IMessage onMessage(SuperBeaconParticlesMessage message, MessageContext context) {
                WitherStormMod.proxy.handleSuperBeaconParticles(message.position, message.type);
                return null;
            }
        }
    }

    public static final class DistantSuperBeaconMessage implements IMessage {
        private net.minecraft.util.math.BlockPos position;
        private int red;
        private int green;
        private int blue;
        private boolean active;
        private int beamHeight;
        private float thickness;
        private float outerThickness;
        private boolean removed;

        public DistantSuperBeaconMessage() {
        }

        public DistantSuperBeaconMessage(net.minecraft.util.math.BlockPos position, int[] color,
                                         boolean active, int beamHeight, float thickness,
                                         float outerThickness, boolean removed) {
            this.position = position;
            red = color[0];
            green = color[1];
            blue = color[2];
            this.active = active;
            this.beamHeight = beamHeight;
            this.thickness = thickness;
            this.outerThickness = outerThickness;
            this.removed = removed;
        }

        public net.minecraft.util.math.BlockPos getPosition() { return position; }
        public int[] getColor() { return new int[] {red, green, blue}; }
        public boolean isActive() { return active; }
        public int getBeamHeight() { return beamHeight; }
        public float getThickness() { return thickness; }
        public float getOuterThickness() { return outerThickness; }
        public boolean isRemoved() { return removed; }

        @Override
        public void fromBytes(ByteBuf buffer) {
            position = net.minecraft.util.math.BlockPos.fromLong(buffer.readLong());
            red = buffer.readUnsignedByte();
            green = buffer.readUnsignedByte();
            blue = buffer.readUnsignedByte();
            active = buffer.readBoolean();
            beamHeight = buffer.readInt();
            thickness = buffer.readFloat();
            outerThickness = buffer.readFloat();
            removed = buffer.readBoolean();
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeLong(position.toLong());
            buffer.writeByte(red);
            buffer.writeByte(green);
            buffer.writeByte(blue);
            buffer.writeBoolean(active);
            buffer.writeInt(beamHeight);
            buffer.writeFloat(thickness);
            buffer.writeFloat(outerThickness);
            buffer.writeBoolean(removed);
        }

        public static final class Handler implements IMessageHandler<DistantSuperBeaconMessage, IMessage> {
            @Override
            public IMessage onMessage(DistantSuperBeaconMessage message, MessageContext context) {
                WitherStormMod.proxy.handleDistantSuperBeacon(message);
                return null;
            }
        }
    }

    public static final class InjureWitherStormHeadMessage implements IMessage {
        private int entityId;
        private int head;

        public InjureWitherStormHeadMessage() {
        }

        public InjureWitherStormHeadMessage(int entityId, int head) {
            this.entityId = entityId;
            this.head = head;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            entityId = buffer.readInt();
            head = buffer.readUnsignedByte();
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeInt(entityId);
            buffer.writeByte(head);
        }

        public static final class Handler implements IMessageHandler<InjureWitherStormHeadMessage, IMessage> {
            @Override
            public IMessage onMessage(final InjureWitherStormHeadMessage message,
                                      final MessageContext context) {
                final EntityPlayerMP player = context.getServerHandler().player;
                player.getServerWorld().addScheduledTask(() -> {
                    Entity entity = player.world.getEntityByID(message.entityId);
                    boolean accepted = false;
                    if (entity instanceof WitherStormEntity && message.head >= 0
                            && message.head < ((WitherStormEntity) entity).getTotalHeads()) {
                        WitherStormEntity storm = (WitherStormEntity) entity;
                        double reach = player.interactionManager.getBlockReachDistance();
                        if (storm.tractorBeamActive(message.head)
                                && storm.canPlayerReachHead(player, message.head, reach)) {
                            accepted = storm.attackHead(message.head, player);
                        }
                    }
                    player.world.playSound(null, player.posX, player.posY, player.posZ,
                            accepted ? SoundEvents.ENTITY_PLAYER_ATTACK_STRONG
                                    : SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE,
                            net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
                });
                return null;
            }
        }
    }
}
