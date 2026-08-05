package com.wdcftgg.witherstormmod.common.block;

import com.google.common.base.Predicate;
import com.wdcftgg.witherstormmod.common.entity.SickenedEntities;
import com.wdcftgg.witherstormmod.common.init.ModBlocks;
import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMaterialMatcher;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.block.state.pattern.FactoryBlockPattern;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class LegacyTaintedCarvedPumpkinBlock extends BlockHorizontal {

    private static final Predicate<IBlockState> IS_TAINTED_PUMPKIN = state -> state != null
            && (state.getBlock() == ModBlocks.get("tainted_carved_pumpkin")
            || state.getBlock() == ModBlocks.get("tainted_jack_o_lantern"));

    private BlockPattern snowGolemBasePattern;
    private BlockPattern snowGolemPattern;
    private BlockPattern ironGolemBasePattern;
    private BlockPattern ironGolemPattern;

    public LegacyTaintedCarvedPumpkinBlock(String name, boolean lit) {
        super(Material.GOURD, MapColor.ADOBE);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(1.0F);
        setSoundType(SoundType.WOOD);
        if (lit) {
            setLightLevel(10.0F / 15.0F);
        }
    }

    @Override
    public void onBlockAdded(World world, BlockPos position, IBlockState state) {
        super.onBlockAdded(world, position, state);
        trySpawnGolem(world, position);
    }

    public boolean canDispenserPlace(World world, BlockPos position) {
        return getSnowGolemBasePattern().match(world, position) != null
                || getIronGolemBasePattern().match(world, position) != null;
    }

    public void registerDispenserBehavior(Item item) {
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(item, new Bootstrap.BehaviorDispenseOptional() {
            @Override
            protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
                World world = source.getWorld();
                EnumFacing dispenserFacing = source.getBlockState().getValue(BlockDispenser.FACING);
                BlockPos target = source.getBlockPos().offset(dispenserFacing);
                successful = true;
                if (world.isAirBlock(target) && canDispenserPlace(world, target)) {
                    if (!world.isRemote) {
                        world.setBlockState(target, getDefaultState(), 3);
                    }
                    stack.shrink(1);
                } else if (ItemArmor.dispenseArmor(source, stack).isEmpty()) {
                    successful = false;
                }
                return stack;
            }
        });
    }

    private void trySpawnGolem(World world, BlockPos position) {
        BlockPattern.PatternHelper match = getSnowGolemPattern().match(world, position);
        if (match != null) {
            clearSnowGolemPattern(world, match);
            BlockPos spawnPosition = match.translateOffset(0, 2, 0).getPos();
            SickenedEntities.SickenedSnowGolem golem = new SickenedEntities.SickenedSnowGolem(world);
            golem.setLocationAndAngles(spawnPosition.getX() + 0.5D, spawnPosition.getY() + 0.05D,
                    spawnPosition.getZ() + 0.5D, 0.0F, 0.0F);
            world.spawnEntity(golem);
            spawnCreationParticles(world, spawnPosition, 2.5D, EnumParticleTypes.SNOW_SHOVEL);
            notifySnowGolemPattern(world, match);
            return;
        }

        match = getIronGolemPattern().match(world, position);
        if (match == null) {
            return;
        }
        clearIronGolemPattern(world, match);
        BlockPos spawnPosition = match.translateOffset(1, 2, 0).getPos();
        SickenedEntities.SickenedIronGolem golem = new SickenedEntities.SickenedIronGolem(world);
        golem.setLocationAndAngles(spawnPosition.getX() + 0.5D, spawnPosition.getY() + 0.05D,
                spawnPosition.getZ() + 0.5D, 0.0F, 0.0F);
        world.spawnEntity(golem);
        for (EntityPlayerMP player : world.getEntitiesWithinAABB(EntityPlayerMP.class,
                golem.getEntityBoundingBox().grow(5.0D))) {
            CriteriaTriggers.SUMMONED_ENTITY.trigger(player, golem);
        }
        spawnCreationParticles(world, spawnPosition, 3.9D, EnumParticleTypes.SNOWBALL);
        notifyIronGolemPattern(world, match);
    }

    private void clearSnowGolemPattern(World world, BlockPattern.PatternHelper match) {
        for (int row = 0; row < getSnowGolemPattern().getThumbLength(); row++) {
            world.setBlockState(match.translateOffset(0, row, 0).getPos(), Blocks.AIR.getDefaultState(), 2);
        }
    }

    private void clearIronGolemPattern(World world, BlockPattern.PatternHelper match) {
        for (int column = 0; column < getIronGolemPattern().getPalmLength(); column++) {
            for (int row = 0; row < getIronGolemPattern().getThumbLength(); row++) {
                world.setBlockState(match.translateOffset(column, row, 0).getPos(),
                        Blocks.AIR.getDefaultState(), 2);
            }
        }
    }

    private static void spawnCreationParticles(World world, BlockPos position, double height,
                                               EnumParticleTypes particle) {
        for (int count = 0; count < 120; count++) {
            world.spawnParticle(particle, position.getX() + world.rand.nextDouble(),
                    position.getY() + world.rand.nextDouble() * height,
                    position.getZ() + world.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
        }
    }

    private void notifySnowGolemPattern(World world, BlockPattern.PatternHelper match) {
        for (int row = 0; row < getSnowGolemPattern().getThumbLength(); row++) {
            world.notifyNeighborsRespectDebug(match.translateOffset(0, row, 0).getPos(), Blocks.AIR, false);
        }
    }

    private void notifyIronGolemPattern(World world, BlockPattern.PatternHelper match) {
        for (int column = 0; column < getIronGolemPattern().getPalmLength(); column++) {
            for (int row = 0; row < getIronGolemPattern().getThumbLength(); row++) {
                world.notifyNeighborsRespectDebug(match.translateOffset(column, row, 0).getPos(), Blocks.AIR, false);
            }
        }
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos position) {
        return world.getBlockState(position).getBlock().isReplaceable(world, position)
                && world.isSideSolid(position.down(), EnumFacing.UP);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos position, EnumFacing facing, float hitX, float hitY,
                                            float hitZ, int metadata, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(metadata));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
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
        return new BlockStateContainer(this, new IProperty<?>[] {FACING});
    }

    protected BlockPattern getSnowGolemBasePattern() {
        if (snowGolemBasePattern == null) {
            snowGolemBasePattern = FactoryBlockPattern.start().aisle(" ", "#", "#")
                    .where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.SNOW))).build();
        }
        return snowGolemBasePattern;
    }

    protected BlockPattern getSnowGolemPattern() {
        if (snowGolemPattern == null) {
            snowGolemPattern = FactoryBlockPattern.start().aisle("^", "#", "#")
                    .where('^', BlockWorldState.hasState(IS_TAINTED_PUMPKIN))
                    .where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.SNOW))).build();
        }
        return snowGolemPattern;
    }

    protected BlockPattern getIronGolemBasePattern() {
        if (ironGolemBasePattern == null) {
            ironGolemBasePattern = FactoryBlockPattern.start().aisle("~ ~", "###", "~#~")
                    .where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.IRON_BLOCK)))
                    .where('~', BlockWorldState.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
        }
        return ironGolemBasePattern;
    }

    protected BlockPattern getIronGolemPattern() {
        if (ironGolemPattern == null) {
            ironGolemPattern = FactoryBlockPattern.start().aisle("~^~", "###", "~#~")
                    .where('^', BlockWorldState.hasState(IS_TAINTED_PUMPKIN))
                    .where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.IRON_BLOCK)))
                    .where('~', BlockWorldState.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
        }
        return ironGolemPattern;
    }
}
