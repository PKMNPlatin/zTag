package de.pkmnplatin.ztag.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.util.UUIDTypeAdapter;
import com.mysql.jdbc.StringUtils;
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

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0";

    private static HashMap<UUID, String> nameCache = new HashMap<>();
    private static HashMap<String, UUID> uuidCache = new HashMap<>();
    private static Gson gson = new GsonBuilder().create();

    public static String getName(UUID uuid) {
        if(nameCache.containsKey(uuid)) {
            return nameCache.get(uuid);
        }
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("https://use.gameapis.net/mc/player/profile/" + uuid.toString()).openConnection();
            con.setRequestProperty("User-Agent", USER_AGENT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            JsonObject main = gson.fromJson(reader, JsonElement.class).getAsJsonObject();
            reader.close();
            con.disconnect();
            String name = main.get("name").getAsString();
            String sId = main.get("id").getAsString();
            if((! isValid(name) && isValid(sId))) {
                return null;
            }
            UUID id = UUIDTypeAdapter.fromString(sId);
            if(! (isValid(id.toString()))) {
                return null;
            }
            nameCache.put(id, name);
            uuidCache.put(name, id);
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
            HttpURLConnection con = (HttpURLConnection) new URL("https://use.gameapis.net/mc/player/profile/" + name).openConnection();
            con.setRequestProperty("User-Agent", USER_AGENT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            JsonObject main = gson.fromJson(reader, JsonElement.class).getAsJsonObject();
            reader.close();
            con.disconnect();
            String nme = main.get("name").getAsString();
            String sId = main.get("id").getAsString();
            if((! isValid(name) && isValid(sId))) {
                return null;
            }
            UUID id = UUIDTypeAdapter.fromString(sId);
            if(! (isValid(id.toString()))) {
                return null;
            }
            nameCache.put(id, nme);
            uuidCache.put(nme, id);
            return id;
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return null;
    }

    private static boolean isValid(String string) {
        if(StringUtils.isNullOrEmpty(string) || string.equals("null")) {
            return false;
        }
        return true;
    }

}
