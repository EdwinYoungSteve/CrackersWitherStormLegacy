package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import com.wdcftgg.witherstormmod.common.tile.SuperSupportBeaconTileEntity;
import com.wdcftgg.witherstormmod.common.tile.SuperBeaconTileEntity;
import com.wdcftgg.witherstormmod.WitherStormMod;
import com.wdcftgg.witherstormmod.common.gui.ModGuiHandler;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.Explosion;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class SuperSupportBeaconBlock extends BlockContainer {

    private static final AxisAlignedBB BASE = new AxisAlignedBB(0.0D, 0.0D, 0.0D,
            1.0D, 3.0D / 16.0D, 1.0D);
    private static final AxisAlignedBB MIDDLE = new AxisAlignedBB(2.0D / 16.0D, 3.0D / 16.0D,
            2.0D / 16.0D, 14.0D / 16.0D, 4.0D / 16.0D, 14.0D / 16.0D);
    private static final AxisAlignedBB CORE = new AxisAlignedBB(3.0D / 16.0D, 4.0D / 16.0D,
            3.0D / 16.0D, 13.0D / 16.0D, 14.0D / 16.0D, 13.0D / 16.0D);
    private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(0.0D, 0.0D, 0.0D,
            1.0D, 14.0D / 16.0D, 1.0D);

    public SuperSupportBeaconBlock(String name) {
        super(Material.GLASS);
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(2.5F);
        setResistance(SimpleBlock.toLegacyResistance(2.5F));
        setLightLevel(12.0F / 15.0F);
        setSoundType(SoundType.GLASS);
        setLightOpacity(0);
    }

    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new SuperSupportBeaconTileEntity(); }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                              EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof SuperSupportBeaconTileEntity) {
                SuperSupportBeaconTileEntity beacon = (SuperSupportBeaconTileEntity) tile;
                SuperBeaconTileEntity main = beacon.getConnectedBeaconEntity();
                if (main == null || !main.isDoingResummonAnimation()) {
                    if (!beacon.canPlayerUseItems(player)) return true;
                    player.openGui(WitherStormMod.INSTANCE, ModGuiHandler.SUPER_BEACON, world,
                            pos.getX(), pos.getY(), pos.getZ());
                }
            }
        }
        return true;
    }
    @Override public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
                                           EntityLivingBase placer, ItemStack stack) {
        if (!stack.hasDisplayName()) return;
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof SuperSupportBeaconTileEntity) {
            ((SuperSupportBeaconTileEntity) tile).setCustomName(stack.getDisplayName());
        }
    }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) { return BOUNDS; }
    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
                                      AxisAlignedBB entityBox, List<AxisAlignedBB> boxes,
                                      Entity entity, boolean actualState) {
        addCollisionBoxToList(pos, BASE, boxes, entityBox);
        addCollisionBoxToList(pos, MIDDLE, boxes, entityBox);
        addCollisionBoxToList(pos, CORE, boxes, entityBox);
    }
    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos,
                                            Vec3d start, Vec3d end) {
        RayTraceResult closest = null;
        for (AxisAlignedBB box : new AxisAlignedBB[] {BASE, MIDDLE, CORE}) {
            RayTraceResult hit = rayTrace(pos, start, end, box);
            if (hit != null && (closest == null
                    || hit.hitVec.squareDistanceTo(start) < closest.hitVec.squareDistanceTo(start))) {
                closest = hit;
            }
        }
        return closest;
    }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return !SimpleBlock.isDestructiveBoss(entity)
                && super.canEntityDestroy(state, world, pos, entity);
    }
    @Override public boolean canDropFromExplosion(Explosion explosion) { return false; }
    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        if (!world.isRemote) dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
        super.onBlockExploded(world, pos, explosion);
    }
    @Override @SideOnly(Side.CLIENT) public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT; }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
}
