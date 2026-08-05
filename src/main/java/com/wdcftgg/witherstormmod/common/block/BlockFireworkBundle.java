package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import com.wdcftgg.witherstormmod.common.tile.TileEntityFireworkBundle;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockFireworkBundle extends BlockContainer {

    public BlockFireworkBundle(String name) {
        super(Material.TNT);
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(2.5F);
        setResistance(LegacyBlock.toLegacyResistance(2.5F));
        setSoundType(SoundType.PLANT);
    }

    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new TileEntityFireworkBundle(); }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() != Items.FLINT_AND_STEEL && stack.getItem() != Items.FIRE_CHARGE) return false;
        beginFuse(world, pos);
        if (!player.capabilities.isCreativeMode) {
            if (stack.getItem() == Items.FLINT_AND_STEEL) stack.damageItem(1, player); else stack.shrink(1);
        }
        return true;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, net.minecraft.block.Block block, BlockPos fromPos) {
        if (world.isBlockPowered(pos)) beginFuse(world, pos);
    }

    private static void beginFuse(World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityFireworkBundle) ((TileEntityFireworkBundle) tile).beginFuse();
    }
}
