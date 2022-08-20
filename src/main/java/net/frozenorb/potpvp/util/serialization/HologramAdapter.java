package net.frozenorb.potpvp.util.serialization;

import com.google.gson.*;
import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.hologram.HologramMeta;
import net.frozenorb.potpvp.hologram.HologramType;
import net.frozenorb.potpvp.hologram.PracticeHologram;
import net.frozenorb.potpvp.hologram.impl.GlobalHologram;
import net.frozenorb.potpvp.hologram.impl.KitHologram;
import net.frozenorb.potpvp.kit.kittype.KitType;


import java.lang.reflect.Type;
import java.util.UUID;

public class HologramAdapter implements JsonDeserializer<PracticeHologram>, JsonSerializer<PracticeHologram> {

    @Override
    public PracticeHologram deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return fromJson(jsonElement);
    }

    @Override
    public JsonElement serialize(PracticeHologram practiceHologram, Type type, JsonSerializationContext jsonSerializationContext) {
        return toJson(practiceHologram);
    }

    public static JsonObject toJson(PracticeHologram hologram) {
        if (hologram == null) {
            return (null);
        }

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", hologram.getMeta().getName());
        jsonObject.addProperty("type", hologram.getMeta().getType().name());
        jsonObject.add("location", LocationAdapter.toJson(hologram.getMeta().getLocation()));

        if (hologram instanceof KitHologram) {
            jsonObject.addProperty("kit", true);
            KitHologram kitHologram = (KitHologram) hologram;
            jsonObject.addProperty("kitName", kitHologram.getKit().getId());
        } else {
            jsonObject.addProperty("kit", false);
        }

        return (jsonObject);
    }

    public static PracticeHologram fromJson(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return (null);
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        if (jsonObject.get("kit").getAsBoolean()) {
            String kitName = jsonObject.get("kitName").getAsString();
            KitHologram kitHologram = new KitHologram(PotPvPRP.getInstance(), KitType.byId(kitName));

            HologramMeta meta = new HologramMeta(UUID.randomUUID());
            meta.setLocation(LocationAdapter.fromJson(jsonObject.get("location")));
            meta.setName(jsonObject.get("name").getAsString());
            meta.setType(HologramType.valueOf(jsonObject.get("type").getAsString()));

            kitHologram.setMeta(meta);
            return (kitHologram);
        } else {
            HologramMeta meta = new HologramMeta(UUID.randomUUID());
            meta.setLocation(LocationAdapter.fromJson(jsonObject.get("location")));
            meta.setName(jsonObject.get("name").getAsString());
            meta.setType(HologramType.valueOf(jsonObject.get("type").getAsString()));

            GlobalHologram hologram = new GlobalHologram(PotPvPRP.getInstance());
            hologram.setMeta(meta);
            return (hologram);
        }


    }

}
