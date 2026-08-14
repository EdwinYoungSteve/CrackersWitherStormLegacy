package com.wdcftgg.witherstormmod.mixin;

import com.wdcftgg.witherstormmod.common.resource.UpstreamItemTags;
import net.minecraft.entity.item.EntityItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** 对应上游 MixinItemEntity：cannot_fall_in_void 标签物品在虚空下方漂浮。 */
@Mixin(EntityItem.class)
public abstract class EntityItemMixin {

    @ModifyConstant(method = "onUpdate", constant = @Constant(doubleValue = 0.03999999910593033D))
    private double witherstormmod$reverseGravityBelowBuildHeight(double gravity) {
        EntityItem item = (EntityItem) (Object) this;
        return item.posY < 0.0D
                && UpstreamItemTags.contains(UpstreamItemTags.CANNOT_FALL_IN_VOID, item.getItem())
                ? -gravity : gravity;
    }
}
