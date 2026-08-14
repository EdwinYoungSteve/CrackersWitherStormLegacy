package com.wdcftgg.witherstormmod.mixin;

import net.minecraft.block.BlockFalling;
import net.minecraft.entity.item.EntityFallingBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** 允许洞穴震动生成路径调用原版下落方块的受保护初始化钩子。 */
@Mixin(BlockFalling.class)
public interface BlockFallingMixin {

    @Invoker("onStartFalling")
    void witherstormmod$invokeOnStartFalling(EntityFallingBlock entity);
}
