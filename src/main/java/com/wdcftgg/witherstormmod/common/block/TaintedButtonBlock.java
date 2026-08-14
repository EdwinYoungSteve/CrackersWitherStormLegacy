package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.BlockButton;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TaintedButtonBlock extends BlockButton {
    private final boolean wooden;

    public TaintedButtonBlock(String name, boolean wooden) {
        super(wooden);
        this.wooden = wooden;
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(0.5F);
        setResistance(SimpleBlock.toLegacyResistance(0.5F));
        setSoundType(wooden ? SoundType.WOOD : SoundType.STONE);
    }

    @Override
    protected void playClickSound(EntityPlayer player, World world, BlockPos pos) {
        world.playSound(player, pos, wooden ? SoundEvents.BLOCK_WOOD_BUTTON_CLICK_ON
                        : SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON,
                SoundCategory.BLOCKS, 0.3F, 0.6F);
    }

    @Override
    protected void playReleaseSound(World world, BlockPos pos) {
        world.playSound(null, pos, wooden ? SoundEvents.BLOCK_WOOD_BUTTON_CLICK_OFF
                        : SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF,
                SoundCategory.BLOCKS, 0.3F, 0.5F);
    }

}
