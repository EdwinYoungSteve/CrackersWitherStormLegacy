package com.wdcftgg.witherstormmod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 兜底保护：运行环境中 Entity.getPosition() 曾被观察到返回 null 并导致
 * BossVisibility/AvoidWitherStormAI 等 AI 路径 NPE 崩溃。原版 1.12.2 这两个
 * 方法始终基于坐标字段重建对象，此处仅在其被环境改写且返回 null 时按相同
 * 语义重建，保证所有 getPosition()/getEntityBoundingBox() 调用点安全。
 */
@Mixin(Entity.class)
public abstract class EntityPositionMixin {
    @Inject(method = "getPosition", at = @At("RETURN"), cancellable = true)
    private void witherstormmod$ensureNonNullPosition(CallbackInfoReturnable<BlockPos> callback) {
        if (callback.getReturnValue() == null) {
            Entity self = (Entity) (Object) this;
            callback.setReturnValue(new BlockPos(
                    Math.floor(self.posX), Math.floor(self.posY + 0.5D), Math.floor(self.posZ)));
        }
    }

    @Inject(method = "getEntityBoundingBox", at = @At("RETURN"), cancellable = true)
    private void witherstormmod$ensureNonNullBoundingBox(CallbackInfoReturnable<AxisAlignedBB> callback) {
        if (callback.getReturnValue() == null) {
            Entity self = (Entity) (Object) this;
            float halfWidth = self.width / 2.0F;
            callback.setReturnValue(new AxisAlignedBB(
                    self.posX - halfWidth, self.posY, self.posZ - halfWidth,
                    self.posX + halfWidth, self.posY + self.height, self.posZ + halfWidth));
        }
    }
}
