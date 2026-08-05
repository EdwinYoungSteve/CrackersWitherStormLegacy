package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.entity.EntityPowerfulExplosive;
import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;
import com.wdcftgg.witherstormmod.common.tile.TileEntityFormidibomb;

public class LegacyExplosiveBlock extends BlockTNT {

    private final boolean formidibomb;

    public LegacyExplosiveBlock(String name, boolean formidibomb) {
        this.formidibomb = formidibomb;
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(formidibomb ? 0.8F : 0.0F);
        setResistance(LegacyBlock.toLegacyResistance(formidibomb ? 0.8F : 0.0F));
        setSoundType(SoundType.WOOD);
        if (formidibomb) {
            setLightLevel(7.0F / 15.0F);
        }
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return formidibomb;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return formidibomb ? new TileEntityFormidibomb() : null;
    }

    @Override
    public void explode(World world, BlockPos position, IBlockState state, EntityLivingBase igniter) {
        if (world.isRemote || !state.getValue(EXPLODE)) {
            return;
        }
        EntityTNTPrimed explosive = createExplosive(world, position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D, igniter);
        world.spawnEntity(explosive);
        world.playSound((EntityPlayer) null, explosive.posX, explosive.posY, explosive.posZ,
                SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, formidibomb ? 0.65F : 0.9F);
    }

    @Override
    public void onExplosionDestroy(World world, BlockPos position, Explosion explosion) {
        if (world.isRemote) {
            return;
        }
        EntityTNTPrimed explosive = createExplosive(world, position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D,
                explosion.getExplosivePlacedBy());
        explosive.setFuse(world.rand.nextInt(Math.max(1, explosive.getFuse() / 4)) + explosive.getFuse() / 8);
        world.spawnEntity(explosive);
    }

    private EntityTNTPrimed createExplosive(World world, double positionX, double positionY, double positionZ, EntityLivingBase igniter) {
        return formidibomb
                ? new EntityPowerfulExplosive.Formidibomb(world, positionX, positionY, positionZ, igniter)
                : new EntityPowerfulExplosive.SuperTnt(world, positionX, positionY, positionZ, igniter);
    }
}
