package com.wdcftgg.witherstormmod.common.resource;

import com.wdcftgg.witherstormmod.common.config.ConfiguredListMatcher;
import com.wdcftgg.witherstormmod.common.config.WitherStormConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

/** Applies user block-list overrides before the upstream protected-block tag. */
public final class WitherStormBlockRules {
    private static final int BLOCK_STATE_CACHE_SIZE = 1 << 16;
    private static final byte UNKNOWN = 0;
    private static final byte DENIED = 1;
    private static final byte ALLOWED = 2;

    private static volatile String[] whitelistReference;
    private static volatile String[] blacklistReference;
    private static volatile Rules currentRules;

    private WitherStormBlockRules() {
    }

    public static boolean canConsume(IBlockState state) {
        if (state == null || state.getBlock() == Blocks.AIR) return false;
        Rules rules = rules();
        int stateId = Block.getStateId(state);
        if (stateId < 0 || stateId >= BLOCK_STATE_CACHE_SIZE) return evaluate(rules, state);
        byte cached = rules.stateCache[stateId];
        if (cached != UNKNOWN) return cached == ALLOWED;
        boolean allowed = evaluate(rules, state);
        rules.stateCache[stateId] = allowed ? ALLOWED : DENIED;
        return allowed;
    }

    private static Rules rules() {
        String[] whitelist = WitherStormConfig.consumableBlockWhitelist;
        String[] blacklist = WitherStormConfig.consumableBlockBlacklist;
        Rules rules = currentRules;
        if (rules != null && whitelist == whitelistReference && blacklist == blacklistReference) {
            return rules;
        }
        synchronized (WitherStormBlockRules.class) {
            if (currentRules == null || whitelist != whitelistReference
                    || blacklist != blacklistReference) {
                whitelistReference = whitelist;
                blacklistReference = blacklist;
                currentRules = new Rules(ConfiguredListMatcher.compile(whitelist),
                        ConfiguredListMatcher.compile(blacklist));
            }
            return currentRules;
        }
    }

    private static boolean evaluate(Rules rules, IBlockState state) {
        ResourceLocation registryName = state.getBlock().getRegistryName();
        if (registryName == null) registryName = Block.REGISTRY.getNameForObject(state.getBlock());
        String blockId = registryName == null ? null : registryName.toString();
        if (rules.blacklist.matches(blockId)) return false;
        if (rules.whitelist.matches(blockId)) return true;
        return !UpstreamBlockTags.contains(UpstreamBlockTags.WITHER_STORM_BLOCK_BLACKLIST, state);
    }

    private static final class Rules {
        private final ConfiguredListMatcher.Matcher whitelist;
        private final ConfiguredListMatcher.Matcher blacklist;
        private final byte[] stateCache = new byte[BLOCK_STATE_CACHE_SIZE];

        private Rules(ConfiguredListMatcher.Matcher whitelist,
                      ConfiguredListMatcher.Matcher blacklist) {
            this.whitelist = whitelist;
            this.blacklist = blacklist;
        }
    }
}
