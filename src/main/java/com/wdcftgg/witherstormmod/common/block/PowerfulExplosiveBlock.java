package com.wdcftgg.witherstormmod.common.block;

import com.wdcftgg.witherstormmod.common.config.WitherStormConfig;
import com.wdcftgg.witherstormmod.common.entity.FormidibombSource;
import com.wdcftgg.witherstormmod.common.entity.PowerfulExplosiveEntity;
import com.wdcftgg.witherstormmod.common.init.ModCreativeTabs;
import com.wdcftgg.witherstormmod.common.init.ModSounds;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.EnumPushReaction;
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
import com.wdcftgg.witherstormmod.common.tile.FormidibombTileEntity;

public class PowerfulExplosiveBlock extends BlockTNT {

    private final boolean formidibomb;

    public PowerfulExplosiveBlock(String name, boolean formidibomb) {
        this.formidibomb = formidibomb;
        setRegistryName(name);
        setTranslationKey(name);
        setCreativeTab(ModCreativeTabs.MAIN);
        setHardness(formidibomb ? 0.8F : 0.0F);
        setResistance(SimpleBlock.toLegacyResistance(formidibomb ? 0.8F : 0.0F));
        setSoundType(SoundType.PLANT);
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
        return formidibomb ? new FormidibombTileEntity() : null;
    }

    @Override
    public void explode(World world, BlockPos position, IBlockState state, EntityLivingBase igniter) {
        if (world.isRemote || !state.getValue(EXPLODE)) {
            return;
        }
        TileEntity tile = world.getTileEntity(position);
        FormidibombSource previous = tile instanceof FormidibombSource ? (FormidibombSource) tile : null;
        EntityTNTPrimed explosive = createExplosive(world, position.getX() + 0.5D, position.getY(),
                position.getZ() + 0.5D, igniter, previous, state);
        if (explosive instanceof PowerfulExplosiveEntity.FormidibombEntity
                && explosive.getFuse() > WitherStormConfig.catchFireFuseTicks) {
            ((PowerfulExplosiveEntity.FormidibombEntity) explosive)
                    .initiateFuse(WitherStormConfig.catchFireFuseTicks);
        }
        world.spawnEntity(explosive);
        world.playSound((EntityPlayer) null, explosive.posX, explosive.posY, explosive.posZ,
                formidibomb ? SoundEvents.ENTITY_TNT_PRIMED : ModSounds.get("super_tnt_fuse"),
                SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    @Override
    public void onExplosionDestroy(World world, BlockPos position, Explosion explosion) {
        if (world.isRemote) {
            return;
        }
        EntityTNTPrimed explosive = createExplosive(world, position.getX() + 0.5D, position.getY(),
                position.getZ() + 0.5D, explosion.getExplosivePlacedBy(), null, null);
        if (explosive instanceof PowerfulExplosiveEntity.FormidibombEntity) {
            ((PowerfulExplosiveEntity.FormidibombEntity) explosive).initiateFuse(20);
        } else {
            explosive.setFuse(world.rand.nextInt(Math.max(1, explosive.getFuse() / 4)) + explosive.getFuse() / 8);
        }
        world.spawnEntity(explosive);
    }

    protected EntityTNTPrimed createExplosive(World world, double positionX, double positionY, double positionZ,
                                               EntityLivingBase igniter, FormidibombSource previous,
                                               IBlockState state) {
        return formidibomb
                ? new PowerfulExplosiveEntity.FormidibombEntity(
                        world, positionX, positionY, positionZ, igniter, previous, state)
                : new PowerfulExplosiveEntity.SuperTntEntity(world, positionX, positionY, positionZ, igniter);
    }

    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.BLOCK;
    }
}
