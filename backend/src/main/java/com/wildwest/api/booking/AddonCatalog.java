package com.wildwest.api.booking;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AddonCatalog {

    public record Addon(String code, String label, int priceCents) {}

    private static final Map<String, Addon> ADDONS = new LinkedHashMap<>();

    static {
        register("airport-transfer", "Private airport transfer", 18000);
        register("travel-cover",     "Comprehensive travel cover", 32000);
        register("balloon-safari",   "Hot-air balloon safari", 62000);
    }

    private static void register(String code, String label, int priceCents) {
        ADDONS.put(code, new Addon(code, label, priceCents));
    }

    public static Addon get(String code) {
        Addon addon = ADDONS.get(code);
        if (addon == null) {
            throw new IllegalArgumentException("Unknown addon: " + code);
        }
        return addon;
    }

    public static Map<String, Addon> all() {
        return ADDONS;
    }

    private AddonCatalog() {}
}