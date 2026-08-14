package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.client.particle.CommandBlockParticle;
import com.wdcftgg.witherstormmod.common.entity.FormidibombSource;
import com.wdcftgg.witherstormmod.common.entity.PowerfulExplosiveEntity;
import com.wdcftgg.witherstormmod.common.item.FormidibombItem;
import com.wdcftgg.witherstormmod.common.tile.FormidibombTileEntity;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class FormidibombBlock extends PowerfulExplosiveBlock {
    public static final PropertyDirection FACING = PropertyDirection.create(
            "facing", EnumFacing.Plane.HORIZONTAL);

    public FormidibombBlock(String name) {
        super(name, true);
        setDefaultState(blockState.getBaseState()
                .withProperty(EXPLODE, false)
                .withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos position, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int metadata,
                                            EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        return getDefaultState()
                .withProperty(FACING, EnumFacing.byHorizontalIndex(metadata & 3))
                .withProperty(EXPLODE, (metadata & 4) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex()
                | (state.getValue(EXPLODE) ? 4 : 0);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(FACING)));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty<?>[]{EXPLODE, FACING});
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos position, Random random) {
        CommandBlockParticle.spawnForBlock(world, position, random);
    }

    @Override
    public void onBlockExploded(World world, BlockPos position, Explosion explosion) {
        IBlockState state = world.getBlockState(position);
        TileEntity tile = world.getTileEntity(position);
        FormidibombSource previous = tile instanceof FormidibombSource
                ? (FormidibombSource) tile : null;
        if (!world.isRemote) {
            PowerfulExplosiveEntity.FormidibombEntity entity =
                    new PowerfulExplosiveEntity.FormidibombEntity(world,
                            position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D,
                            explosion.getExplosivePlacedBy(), previous, state);
            entity.initiateFuse(20);
            world.spawnEntity(entity);
        }
        world.setBlockToAir(position);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos position,
                         IBlockState state, int fortune) {
        ItemStack stack = new ItemStack(this);
        TileEntity tile = world.getTileEntity(position);
        if (tile instanceof FormidibombTileEntity) {
            FormidibombTileEntity formidibomb = (FormidibombTileEntity) tile;
            FormidibombItem.setFuseState(stack, formidibomb.getFuseLife(), formidibomb.getStartFuse());
        }
        drops.add(stack);
    }
}
