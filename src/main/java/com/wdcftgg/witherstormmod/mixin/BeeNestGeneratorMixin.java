package com.wdcftgg.witherstormmod.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thedarkcolour.futuremc.world.gen.feature.BeeNestGenerator;

import java.util.Random;

/**
 * FutureMC 0.2.6 的 BeeNestGenerator.cannotGenerate 会直接调用
 * sun.reflect.Reflection.getCallerClass(4)，该内部类自 JDK 17 起被移除，
 * 导致 Java 17+ 上任何生成树木的世界在装饰阶段崩溃。
 * 本垫片在方法入口直接走 fastCannotGenerate，等价于上游从世界生成
 * （BiomeDecorator 调用链）进入时的行为，完全避开已移除的内部类。
 */
@Mixin(value = BeeNestGenerator.class, remap = false)
public abstract class BeeNestGeneratorMixin {

    @Inject(method = "cannotGenerate", at = @At("HEAD"), cancellable = true, remap = false)
    private void witherstormmod$bypassSunReflectionCallerCheck(
            World world, Random random, BlockPos position,
            CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(
                ((BeeNestGenerator) (Object) this).fastCannotGenerate(world, random, position));
    }
}
