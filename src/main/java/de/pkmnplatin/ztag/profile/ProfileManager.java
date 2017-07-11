package de.pkmnplatin.ztag.profile;

import de.pkmnplatin.ztag.TagBase;
import de.pkmnplatin.ztag.util.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Created by Jona on 06.07.2017.
 */
public class ProfileManager implements Listener {

    public ProfileManager() {
        Bukkit.getPluginManager().registerEvents(this, TagBase.getInstance());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        getProfile(p);
    }

    public TagProfile getProfile(Player player) {
        TagProfile tp = null;
        if(player.hasMetadata("zTag") && player.getMetadata("zTag").size() == 3) {
            String tag = player.getMetadata("zTag").get(0).asString();
            String skin = player.getMetadata("zTag").get(1).asString();
            String realName = player.getMetadata("zTag").get(2).asString();
            tp = new TagProfile(player, realName, tag, skin);
        } else {
            if (player.hasMetadata("zTag")) {
                player.removeMetadata("zTag", TagBase.getInstance());
            }
            tp = new TagProfile(player, UUIDFetcher.getName(player.getUniqueId()), player.getName(), player.getName());
            player.setMetadata("zTag", new FixedMetadataValue(TagBase.getInstance(), tp.getTag()));
            player.setMetadata("zTag", new FixedMetadataValue(TagBase.getInstance(), tp.getSkin()));
            player.setMetadata("zTag", new FixedMetadataValue(TagBase.getInstance(), tp.getRealName()));
        }
        return tp;
    }

    public TagProfile updateProfile(Player player, TagProfile profile) {
        if(player.hasMetadata("zTag")) {
            player.removeMetadata("zTag", TagBase.getInstance());
        }
        player.setMetadata("zTag", new FixedMetadataValue(TagBase.getInstance(), profile.getTag()));
        player.setMetadata("zTag", new FixedMetadataValue(TagBase.getInstance(), profile.getSkin()));
        player.setMetadata("zTag", new FixedMetadataValue(TagBase.getInstance(), profile.getRealName()));
        return profile;
    }

}
