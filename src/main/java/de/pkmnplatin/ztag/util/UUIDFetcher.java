package de.pkmnplatin.ztag.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.util.UUIDTypeAdapter;
import de.pkmnplatin.ztag.TagBase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Jona on 07.07.2017.
 */
public class UUIDFetcher {

    private static HashMap<UUID, String> nameCache = new HashMap<>();
    private static HashMap<String, UUID> uuidCache = new HashMap<>();
    private static Gson gson = new GsonBuilder().create();

    public static String getName(UUID uuid) {
        if(nameCache.containsKey(uuid)) {
            return nameCache.get(uuid);
        }
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("https://api.minetools.eu/uuid/" + uuid.toString().replaceAll("-", "")).openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            StringBuilder builder = new StringBuilder();
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }
            con.disconnect();
            reader.close();

            JsonObject main = gson.fromJson(builder.toString(), JsonElement.class).getAsJsonObject();
            String name = main.get("name").getAsString();
            String sId = main.get("id").getAsString();

            if(sId.equals("null") || sId == null || sId.toString().isEmpty() || sId.toString().equals("")) {
                return null;
            }

            UUID id = UUIDTypeAdapter.fromString(sId);

            if(name.equals("null") || name == null || name.isEmpty() || name.equals("")) {
                return null;
            }

            if(id.equals("null") || id == null || id.toString().isEmpty() || id.toString().equals("")) {
                return null;
            }
            nameCache.put(id, name);
            uuidCache.put(name.toLowerCase(), id);
            return name;
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return null;
    }

    public static UUID getUUID(String name) {
        if(uuidCache.containsKey(name.toLowerCase())) {
            return uuidCache.get(name.toLowerCase());
        }
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("https://api.minetools.eu/uuid/" + name).openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            StringBuilder builder = new StringBuilder();
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            con.disconnect();

            JsonObject main = gson.fromJson(builder.toString(), JsonElement.class).getAsJsonObject();
            String nme = main.get("name").getAsString();
            String sId = main.get("id").getAsString();

            if(sId.equals("null") || sId == null || sId.toString().isEmpty() || sId.toString().equals("")) {
                return null;
            }

            UUID id = UUIDTypeAdapter.fromString(sId);

            if(name.equals("null") || name == null || name.isEmpty() || name.equals("")) {
                return null;
            }

            if(id.equals("null") || id == null || id.toString().isEmpty() || id.toString().equals("")) {
                return null;
            }
            nameCache.put(id, nme);
            uuidCache.put(nme.toLowerCase(), id);
            return id;
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return null;
    }

}
