package com.wdcftgg.witherstormmod.common.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class SoundResourceConverter {

    private static final String SOUNDS = "assets/witherstormmod/sounds.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, String> EVENT_REWRITES;

    static {
        Map<String, String> rewrites = new HashMap<String, String>();
        rewrites.put("minecraft:entity.evoker.prepare_attack",
                "minecraft:entity.evocation_illager.prepare_attack");
        rewrites.put("minecraft:entity.evoker.prepare_summon",
                "minecraft:entity.evocation_illager.prepare_summon");
        rewrites.put("minecraft:entity.illusioner.cast_spell",
                "minecraft:entity.illusion_illager.cast_spell");
        // The vanilla beacon deactivate event was introduced after 1.12.2.
        rewrites.put("minecraft:block.beacon.deactivate",
                "minecraft:block.end_portal_frame.fill");
        EVENT_REWRITES = Collections.unmodifiableMap(rewrites);
    }

    private SoundResourceConverter() {
    }

    static boolean handles(String name) {
        return SOUNDS.equals(name);
    }

    static byte[] convert(byte[] source) {
        JsonObject definitions = JsonParser.parseString(
                new String(source, StandardCharsets.UTF_8)).getAsJsonObject();
        for (Map.Entry<String, JsonElement> definition : definitions.entrySet()) {
            if (!definition.getValue().isJsonObject()) continue;
            JsonArray sounds = definition.getValue().getAsJsonObject().getAsJsonArray("sounds");
            if (sounds == null) continue;
            for (JsonElement sound : sounds) {
                if (!sound.isJsonObject()) continue;
                JsonObject object = sound.getAsJsonObject();
                JsonElement type = object.get("type");
                JsonElement name = object.get("name");
                if (type == null || !"event".equals(type.getAsString()) || name == null) continue;
                String replacement = EVENT_REWRITES.get(name.getAsString());
                if (replacement != null) {
                    object.add("name", new JsonPrimitive(replacement));
                }
            }
        }
        return GSON.toJson(definitions).getBytes(StandardCharsets.UTF_8);
    }
}
