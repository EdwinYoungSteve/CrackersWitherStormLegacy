package com.wdcftgg.witherstormmod.common.init;

import com.wdcftgg.witherstormmod.Tags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.LinkedHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModSounds {
    private static final Map<String, SoundEvent> SOUNDS = new LinkedHashMap<String, SoundEvent>();
    private static final String[] NAMES = {
            "amulet_bind", "amulet_swaps", "amulet_unbind", "block_cluster_shake", "bowels_loop",
            "bowels_loud_hurt", "bowels_mood", "bowels_transport", "bowels_tremble", "cave_wind",
            "command_block_activates", "command_block_build", "command_block_cracks", "command_block_damage",
            "command_block_death", "command_block_destruct", "command_block_hit", "command_block_power_down",
            "command_block_pulse_loop", "command_block_summon", "earth_rumble", "flaming_skull_impact",
            "formidi_blade_charging", "formidi_blade_decharge", "formidibomb_explosion",
            "formidibomb_explosion_quiet", "formidibomb_pulse_loop", "loud_tremble", "mob_cured", "mob_infected",
            "rib_bone_crack", "super_tnt_fuse", "tentacle_spike_stab", "tremble", "whoosh",
            "wither_storm_ambient", "wither_storm_bite", "wither_storm_boss_theme",
            "wither_storm_bowels_exposed_theme", "wither_storm_close_loop", "wither_storm_death",
            "wither_storm_distant_loop", "wither_storm_evolves", "wither_storm_far_loop",
            "wither_storm_final_boss_theme", "wither_storm_formidibomb_theme", "wither_storm_hurt",
            "wither_storm_loop", "wither_storm_reactivates", "wither_storm_reviving_theme", "wither_storm_roar",
            "wither_storm_shoot", "wither_storm_splits", "wither_storm_thump", "wither_storm_tractor_beam",
            "wither_storm_tractor_beam_activate", "wither_storm_tremble", "withered_beacon_activate",
            "withered_beacon_ambient", "withered_beacon_deactivate", "withered_beacon_power_up",
            "withered_phlegm_block_close", "withered_phlegm_block_open", "withered_symbiont_ambient",
            "withered_symbiont_cast_spell", "withered_symbiont_death", "withered_symbiont_heart_beat",
            "withered_symbiont_hurt", "withered_symbiont_intense_theme", "withered_symbiont_launch_mob",
            "withered_symbiont_normal_death", "withered_symbiont_power_down", "withered_symbiont_prepare_spell",
            "withered_symbiont_pull", "withered_symbiont_spawn", "withered_symbiont_step",
            "withered_symbiont_summon", "withered_symbiont_theme"
    };

    static {
        for (String name : NAMES) {
            ResourceLocation id = new ResourceLocation(Tags.MOD_ID, name);
            SOUNDS.put(name, new SoundEvent(id).setRegistryName(id));
        }
    }

    private ModSounds() {
    }

    public static void bootstrap() {
    }

    public static SoundEvent get(String name) {
        return SOUNDS.get(name);
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(SOUNDS.values().toArray(new SoundEvent[0]));
    }
}
