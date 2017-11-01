package de.pkmnplatin.ztag.util;

import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.pkmnplatin.ztag.TagBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by Jona on 11.07.2017.
 */
public class GameProfileFetcher {

    private static Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).registerTypeAdapter(GameProfile.class, new GameProfileSerializer()).registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create();
    private static HashMap<UUID, GameProfile> gameProfileCache = new HashMap<>();

    public static GameProfile getGameProfile(UUID uuid) {
        if (gameProfileCache.containsKey(uuid)) {
            return gameProfileCache.get(uuid);
        } else {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL("https://use.gameapis.net/mc/player/profile/" + uuid).openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                JsonObject main = gson.fromJson(reader, JsonElement.class).getAsJsonObject();
                reader.close();
                con.disconnect();
                String id = main.get("id").getAsString();
                String name = main.get("name").getAsString();
                main = main.get("properties").getAsJsonArray().get(0).getAsJsonObject();
                String signature = main.get("signature").getAsString();
                String value = main.get("value").getAsString();
                GameProfile gp = gson.fromJson(getJSONString(id, name, signature, value), GameProfile.class);
                gameProfileCache.put(uuid, gp);
                return gp;
            } catch (Exception ex) {
                TagBase.log(ex);
            }
        }
        return null;
    }

    private static String getJSONString(String id, String name, String signature, String value) {
        return "{  \n" +
                "   \"id\":\"" + id + "\",\n" +
                "   \"name\":\"" + name + "\",\n" +
                "   \"properties\":[  \n" +
                "      {  \n" +
                "         \"signature\":\"" + signature + "\",\n" +
                "         \"name\":\"textures\",\n" +
                "         \"value\":\"" + value + "\"\n" +
                "      }\n" +
                "   ]\n" +
                "}";
    }

    private static class GameProfileSerializer implements JsonSerializer<GameProfile>, JsonDeserializer<GameProfile> {

        public GameProfile deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context) throws JsonParseException {
            final JsonObject object = (JsonObject) json;
            final UUID id = object.has("id") ? ((UUID) context.deserialize(object.get("id"), (Type) UUID.class)) : null;
            final String name = object.has("name") ? object.getAsJsonPrimitive("name").getAsString() : null;
            final GameProfile profile = new GameProfile(id, name);
            if (object.has("properties")) {
                for (final Map.Entry<String, Property> prop : ((PropertyMap) context.deserialize(object.get("properties"), PropertyMap.class)).toRealMap().entries()) {
                    profile.getProperties().put(prop.getKey(), prop.getValue());
                }
            }
            return profile;
        }

        public JsonElement serialize(final GameProfile profile, final Type type, final JsonSerializationContext context) {
            final JsonObject result = new JsonObject();
            if (profile.getId() != null) {
                result.add("id", context.serialize(profile.getId()));
            }
            if (profile.getName() != null) {
                result.addProperty("name", profile.getName());
            }
            if (!profile.getProperties().isEmpty()) {
                result.add("properties", context.serialize(profile.getProperties()));
            }
            return result;
        }
    }

    private static class UUIDTypeAdapter extends TypeAdapter<UUID> {

        public UUIDTypeAdapter() {
        }

        public void write(JsonWriter var1, UUID var2) throws IOException {
            var1.value(fromUUID(var2));
        }

        public UUID read(JsonReader var1) throws IOException {
            return fromString(var1.nextString());
        }

        public static String fromUUID(UUID var0) {
            return var0.toString().replace("-", "");
        }

        public static UUID fromString(String var0) {
            return UUID.fromString(var0.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
        }
    }

    private static class PropertyMap extends ForwardingMultimap<String, Property> {

        private final Multimap<String, Property> properties = LinkedHashMultimap.create();

        public PropertyMap() {
        }

        protected Multimap<String, Property> delegate() {
            return this.properties;
        }

        public static class Serializer implements JsonSerializer<PropertyMap>, JsonDeserializer<PropertyMap> {
            public Serializer() {
            }

            public PropertyMap deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
                PropertyMap var4 = new PropertyMap();
                if (var1 instanceof JsonObject) {
                    JsonObject var5 = (JsonObject) var1;
                    Iterator var6 = var5.entrySet().iterator();

                    while (true) {
                        Map.Entry var7;
                        do {
                            if (!var6.hasNext()) {
                                return var4;
                            }

                            var7 = (Map.Entry) var6.next();
                        } while (!(var7.getValue() instanceof JsonArray));

                        Iterator var8 = ((JsonArray) var7.getValue()).iterator();

                        while (var8.hasNext()) {
                            JsonElement var9 = (JsonElement) var8.next();
                            var4.put((String) var7.getKey(), new Property((String) var7.getKey(), var9.getAsString()));
                        }
                    }
                } else if (var1 instanceof JsonArray) {
                    Iterator var10 = ((JsonArray) var1).iterator();

                    while (var10.hasNext()) {
                        JsonElement var11 = (JsonElement) var10.next();
                        if (var11 instanceof JsonObject) {
                            JsonObject var12 = (JsonObject) var11;
                            String var13 = var12.getAsJsonPrimitive("name").getAsString();
                            String var14 = var12.getAsJsonPrimitive("value").getAsString();
                            if (var12.has("signature")) {
                                var4.put(var13, new Property(var13, var14, var12.getAsJsonPrimitive("signature").getAsString()));
                            } else {
                                var4.put(var13, new Property(var13, var14));
                            }
                        }
                    }
                }

                return var4;
            }

            public JsonElement serialize(PropertyMap var1, Type var2, JsonSerializationContext var3) {
                JsonArray var4 = new JsonArray();

                JsonObject var7;
                for (Iterator var5 = var1.values().iterator(); var5.hasNext(); var4.add(var7)) {
                    Property var6 = (Property) var5.next();
                    var7 = new JsonObject();
                    var7.addProperty("name", var6.getName());
                    var7.addProperty("value", var6.getValue());
                    if (var6.hasSignature()) {
                        var7.addProperty("signature", var6.getSignature());
                    }
                }

                return var4;
            }
        }

        public PropertyMap toRealMap() {
            PropertyMap realMap = new PropertyMap();
            for(String key : this.properties.keySet()) {
                Collection<Property> prop = this.properties.get(key);
                realMap.putAll(key, prop);
            }
            return realMap;
        }
    }

}
