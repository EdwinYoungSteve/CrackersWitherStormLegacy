package com.wdcftgg.witherstormmod.common.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Compiles exact IDs and namespace wildcards used by server configuration lists. */
public final class ConfiguredListMatcher {

    private ConfiguredListMatcher() {
    }

    public static boolean allows(String value, String[] entries, boolean whitelistMode) {
        boolean listed = compile(entries).matches(value);
        return whitelistMode ? listed : !listed;
    }

    public static Matcher compile(String[] entries) {
        if (entries == null || entries.length == 0) return Matcher.EMPTY;
        Set<String> exact = new HashSet<String>();
        Set<String> namespaces = new HashSet<String>();
        boolean all = false;
        for (String entry : entries) {
            if (entry == null) continue;
            String normalized = entry.trim().toLowerCase(Locale.ROOT);
            if (normalized.isEmpty()) continue;
            if ("*".equals(normalized)) {
                all = true;
            } else if (normalized.endsWith(":*") && normalized.length() > 2) {
                namespaces.add(normalized.substring(0, normalized.length() - 2));
            } else {
                exact.add(normalized);
            }
        }
        return new Matcher(all, exact, namespaces);
    }

    public static final class Matcher {
        private static final Matcher EMPTY = new Matcher(false,
                Collections.<String>emptySet(), Collections.<String>emptySet());

        private final boolean all;
        private final Set<String> exact;
        private final Set<String> namespaces;

        private Matcher(boolean all, Set<String> exact, Set<String> namespaces) {
            this.all = all;
            this.exact = exact;
            this.namespaces = namespaces;
        }

        public boolean matches(String value) {
            if (value == null) return false;
            String normalized = value.trim().toLowerCase(Locale.ROOT);
            if (all || exact.contains(normalized)) return true;
            int separator = normalized.indexOf(':');
            return separator > 0 && namespaces.contains(normalized.substring(0, separator));
        }
    }
}
