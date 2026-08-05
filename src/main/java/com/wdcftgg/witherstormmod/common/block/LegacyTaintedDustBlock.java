package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public final class LegacyTaintedDustBlock extends Block {

    public static final PropertyEnum<WireConnection> NORTH = PropertyEnum.create("north", WireConnection.class);
    public static final PropertyEnum<WireConnection> EAST = PropertyEnum.create("east", WireConnection.class);
    public static final PropertyEnum<WireConnection> SOUTH = PropertyEnum.create("south", WireConnection.class);
    public static final PropertyEnum<WireConnection> WEST = PropertyEnum.create("west", WireConnection.class);

    private static final Map<EnumFacing, PropertyEnum<WireConnection>> PROPERTY_BY_DIRECTION =
            new EnumMap<EnumFacing, PropertyEnum<WireConnection>>(EnumFacing.class);
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D / 16.0D, 1.0D);
    private static final int COLOR = 0xFF40D6;

    static {
        PROPERTY_BY_DIRECTION.put(EnumFacing.NORTH, NORTH);
        PROPERTY_BY_DIRECTION.put(EnumFacing.EAST, EAST);
        PROPERTY_BY_DIRECTION.put(EnumFacing.SOUTH, SOUTH);
        PROPERTY_BY_DIRECTION.put(EnumFacing.WEST, WEST);
    }

    public LegacyTaintedDustBlock(String name) {
        super(Material.CIRCUITS);
        setDefaultState(blockState.getBaseState()
                .withProperty(NORTH, WireConnection.NONE)
                .withProperty(EAST, WireConnection.NONE)
                .withProperty(SOUTH, WireConnection.NONE)
                .withProperty(WEST, WireConnection.NONE));
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(0.0F);
        setSoundType(SoundType.STONE);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos position) {
        return getConnectionState(world, state, position);
    }

    IBlockState getConnectionState(IBlockAccess world, IBlockState state, BlockPos position) {
        boolean wasDot = isDot(state);
        state = getMissingConnections(world, getDefaultState(), position);
        if (wasDot && isDot(state)) {
            return state;
        }

        boolean north = isConnected(state.getValue(NORTH));
        boolean south = isConnected(state.getValue(SOUTH));
        boolean east = isConnected(state.getValue(EAST));
        boolean west = isConnected(state.getValue(WEST));
        boolean missingNorthSouth = !north && !south;
        boolean missingEastWest = !east && !west;
        if (!west && missingNorthSouth) state = state.withProperty(WEST, WireConnection.SIDE);
        if (!east && missingNorthSouth) state = state.withProperty(EAST, WireConnection.SIDE);
        if (!north && missingEastWest) state = state.withProperty(NORTH, WireConnection.SIDE);
        if (!south && missingEastWest) state = state.withProperty(SOUTH, WireConnection.SIDE);
        return state;
    }

    private IBlockState getMissingConnections(IBlockAccess world, IBlockState state, BlockPos position) {
        boolean allowUpwardConnection = !world.getBlockState(position.up()).isNormalCube();
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            PropertyEnum<WireConnection> property = PROPERTY_BY_DIRECTION.get(direction);
            if (!isConnected(state.getValue(property))) {
                state = state.withProperty(property,
                        getConnectingSide(world, position, direction, allowUpwardConnection));
            }
        }
        return state;
    }

    private WireConnection getConnectingSide(IBlockAccess world, BlockPos position, EnumFacing direction,
                                              boolean allowUpwardConnection) {
        BlockPos adjacentPosition = position.offset(direction);
        IBlockState adjacentState = world.getBlockState(adjacentPosition);
        if (allowUpwardConnection && canSurviveOn(world, adjacentPosition, adjacentState)
                && canConnectTo(world.getBlockState(adjacentPosition.up()))
                && adjacentState.isSideSolid(world, adjacentPosition, direction.getOpposite())) {
            return adjacentState.isNormalCube()
                    ? WireConnection.UP
                    : WireConnection.SIDE;
        }
        if (canConnectTo(adjacentState)
                || !adjacentState.isNormalCube() && canConnectTo(world.getBlockState(adjacentPosition.down()))) {
            return WireConnection.SIDE;
        }
        return WireConnection.NONE;
    }

    private static boolean isConnected(WireConnection attachment) {
        return attachment != WireConnection.NONE;
    }

    private static boolean isDot(IBlockState state) {
        return !isConnected(state.getValue(NORTH)) && !isConnected(state.getValue(EAST))
                && !isConnected(state.getValue(SOUTH)) && !isConnected(state.getValue(WEST));
    }

    static boolean canConnectTo(IBlockState state) {
        return state.getBlock() instanceof LegacyTaintedDustBlock;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos position) {
        IBlockState below = world.getBlockState(position.down());
        return canSurviveOn(world, position.down(), below);
    }

    private static boolean canSurviveOn(IBlockAccess world, BlockPos position, IBlockState state) {
        return state.isSideSolid(world, position, EnumFacing.UP) || state.getBlock() == Blocks.GLOWSTONE;
    }

    @Override
    public void onBlockAdded(World world, BlockPos position, IBlockState state) {
        super.onBlockAdded(world, position, state);
        refreshNeighboringWires(world, position);
    }

    @Override
    public void breakBlock(World world, BlockPos position, IBlockState state) {
        super.breakBlock(world, position, state);
        refreshNeighboringWires(world, position);
    }

    private void refreshNeighboringWires(World world, BlockPos position) {
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            BlockPos adjacent = position.offset(direction);
            refreshWire(world, adjacent);
            refreshWire(world, adjacent.up());
            refreshWire(world, adjacent.down());
        }
    }

    private void refreshWire(World world, BlockPos position) {
        IBlockState state = world.getBlockState(position);
        if (state.getBlock() == this) {
            world.notifyBlockUpdate(position, state, state, 3);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos position, Block block, BlockPos fromPosition) {
        if (!world.isRemote && !canPlaceBlockAt(world, position)) {
            dropBlockAsItem(world, position, state, 0);
            world.setBlockToAir(position);
        }
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos position) {
        return NULL_AABB;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos position) {
        return SHAPE;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return false;
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos position, EnumFacing side) {
        return 0;
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos position, EnumFacing side) {
        return 0;
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos position, @Nullable EnumFacing side) {
        return false;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(this);
    }

    @Override
    public ItemStack getItem(World world, BlockPos position, IBlockState state) {
        return new ItemStack(this);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos position, Random random) {
        IBlockState actual = getActualState(state, world, position);
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            WireConnection attachment = actual.getValue(PROPERTY_BY_DIRECTION.get(direction));
            if (attachment == WireConnection.NONE || random.nextFloat() >= 0.2F) continue;
            double distance = 0.2D + random.nextDouble() * 0.3D;
            double x = position.getX() + 0.5D + direction.getXOffset() * distance;
            double y = position.getY() + (attachment == WireConnection.UP
                    ? 0.2D + random.nextDouble() * 0.6D : 0.0625D);
            double z = position.getZ() + 0.5D + direction.getZOffset() * distance;
            world.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z,
                    1.0D, 64.0D / 255.0D, 214.0D / 255.0D);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 -> state.withProperty(NORTH, state.getValue(SOUTH))
                    .withProperty(EAST, state.getValue(WEST))
                    .withProperty(SOUTH, state.getValue(NORTH))
                    .withProperty(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90 -> state.withProperty(NORTH, state.getValue(EAST))
                    .withProperty(EAST, state.getValue(SOUTH))
                    .withProperty(SOUTH, state.getValue(WEST))
                    .withProperty(WEST, state.getValue(NORTH));
            case CLOCKWISE_90 -> state.withProperty(NORTH, state.getValue(WEST))
                    .withProperty(EAST, state.getValue(NORTH))
                    .withProperty(SOUTH, state.getValue(EAST))
                    .withProperty(WEST, state.getValue(SOUTH));
            default -> state;
        };
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT -> state.withProperty(NORTH, state.getValue(SOUTH))
                    .withProperty(SOUTH, state.getValue(NORTH));
            case FRONT_BACK -> state.withProperty(EAST, state.getValue(WEST))
                    .withProperty(WEST, state.getValue(EAST));
            default -> state;
        };
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty<?>[] {NORTH, EAST, SOUTH, WEST});
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos position, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    public static int getColor() {
        return COLOR;
    }

    public enum WireConnection implements IStringSerializable {
        UP("up"),
        SIDE("side"),
        NONE("none");

        private final String name;

        WireConnection(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
