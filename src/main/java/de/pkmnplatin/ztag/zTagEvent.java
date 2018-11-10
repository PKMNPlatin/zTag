package de.pkmnplatin.ztag;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.pkmnplatin.ztag.profile.TagProfile;
import de.pkmnplatin.ztag.reflect.Reflection;
import de.pkmnplatin.ztag.util.GameProfileFetcher;
import de.pkmnplatin.ztag.util.UUIDFetcher;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.pkmnplatin.ztag.reflect.Reflection.*;

/**
 * Created by Jona on 06.07.2017.
 */
@Getter
@Setter
public class zTagEvent extends Event {

    private static final ExecutorService pool = Executors.newCachedThreadPool();

    @Setter(AccessLevel.NONE) private Player player;
    @Setter(AccessLevel.NONE) private TagProfile profile;
    @Setter(AccessLevel.NONE) private List<Player> players = new ArrayList<Player>();

    private boolean updateChunk = true;
    private boolean forceSkinUpdate = true;
    private String tag;
    private String skin;

    public zTagEvent(Player player, String tag, String skin) {
        this(player, tag, skin, Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]));
    }

    public zTagEvent(Player player, String tag, String skin, Player... forWho) {
        this.player = player;
        this.profile = TagBase.getInstance().getProfileManager().getProfile(player);
        this.profile.update();
        this.players = Arrays.asList(forWho);
        this.tag = tag;
        this.skin = skin;
        this.profile.setTag(tag);
        this.profile.setSkin(skin);

        try {
            GameProfile gp = getGameProfile(player);
            gp = fixSkin(gp, skin);
            setValue(gp, gp.getClass(), "name", tag);
            player.setDisplayName(gp.getName());
            int entityId = getEntityId(player);
            Object despawnPacket = getDestroyPacket(entityId);
            Object removePlayer = getInfoPacket("REMOVE_PLAYER", gp, -1, null, null);
            Object spawnPacket = getSpawnPacket(player);
            Object addPlayer = getInfoPacket("ADD_PLAYER", gp, Reflection.getPing(player), player.getGameMode(), tag);
            Object respawnPacket = getRespawnPacket(player, player.getWorld());

            final boolean flying = player.isFlying();
            final Location location = player.getLocation();
            final int level = player.getLevel();
            final float xp = player.getExp();
            final double maxHealth = player.getMaxHealth();
            final double health = player.getHealth();

            if(forceSkinUpdate) {
                sendPacket(player, removePlayer);
                sendPacket(player, respawnPacket);
                sendPacket(player, addPlayer);
                player.teleport(location);
                if(updateChunk) {
                    Chunk chunk = player.getWorld().getChunkAt(player.getLocation());
                    player.getWorld().refreshChunk(chunk.getX() + 8, chunk.getZ() + 8);
                }
            } else {
                sendPacket(player, removePlayer);
                sendPacket(player, addPlayer);
            }

            player.setFlying(flying);
            player.updateInventory();
            player.setLevel(level);
            player.setExp(xp);
            player.setMaxHealth(maxHealth);
            player.setHealth(health);

            if(!players.isEmpty()) {
                for (Player p : players) {
                    if (p != player) {
                        sendPacket(p, removePlayer);
                        sendPacket(p, despawnPacket);
                        sendPacket(p, addPlayer);
                        sendPacket(p, spawnPacket);
                    }
                }
            }
        } catch (Exception ex) {
            TagBase.log(ex);
        }
    }

    private GameProfile fixSkin(GameProfile gp, String skin) {
        UUID uuid = UUIDFetcher.getUUID(skin);
        if (uuid == null) {
            return gp;
        }
        GameProfile fetched = GameProfileFetcher.getGameProfile(uuid);
        if(fetched != null) {
            Collection<Property> props = fetched.getProperties().get("textures");
            gp.getProperties().removeAll("textures");
            gp.getProperties().putAll("textures", props);
        }
        return gp;
    }

    private static HandlerList handlerList = new HandlerList();

    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}
