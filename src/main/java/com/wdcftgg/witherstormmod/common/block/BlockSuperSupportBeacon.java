package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import com.wdcftgg.witherstormmod.common.tile.TileEntitySuperSupportBeacon;
import com.wdcftgg.witherstormmod.common.tile.TileEntitySuperBeacon;
import com.wdcftgg.witherstormmod.WitherStormMod;
import com.wdcftgg.witherstormmod.common.gui.LegacyGuiHandler;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSuperSupportBeacon extends BlockContainer {

    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D);

    public BlockSuperSupportBeacon(String name) {
        super(Material.ROCK);
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(2.5F);
        setResistance(LegacyBlock.toLegacyResistance(2.5F));
        setLightLevel(12.0F / 15.0F);
        setSoundType(SoundType.METAL);
    }

    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new TileEntitySuperSupportBeacon(); }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                              EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntitySuperSupportBeacon) {
                TileEntitySuperBeacon main = ((TileEntitySuperSupportBeacon) tile).getConnectedBeaconEntity();
                if (main == null || !main.isDoingResummonAnimation()) {
                    player.openGui(WitherStormMod.INSTANCE, LegacyGuiHandler.SUPER_BEACON, world,
                            pos.getX(), pos.getY(), pos.getZ());
                }
            }
        }
        return true;
    }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) { return SHAPE; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override @SideOnly(Side.CLIENT) public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT; }
}
