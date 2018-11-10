package de.pkmnplatin.ztag;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by Jona on 11.07.2017.
 */
public class zTag {

    public static void refreshPlayer(Player player, String name) {
        refreshPlayer(player, name, name);
    }

    public static void refreshPlayer(Player player, String tag, String skin) {
        if (player == null) {
            TagBase.log("Player cannot be null!");
            return;
        }

        if (!player.isOnline()) {
            TagBase.log("Player must be online!");
            return;
        }

        Bukkit.getPluginManager().callEvent(new zTagEvent(player, tag, skin));
    }

    public static void refreshPlayer(Player player, String tag, String skin, Player... forWho) {
        if (player == null) {
            TagBase.log("Player cannot be null!");
            return;
        }

        if (!player.isOnline()) {
            TagBase.log("Player must be online!");
            return;
        }
        Bukkit.getPluginManager().callEvent(new zTagEvent(player, tag, skin, forWho));
    }

    public static String getRealName(Player player) {
        return TagBase.getInstance().getProfileManager().getProfile(player).getRealName();
    }

    public static String getTag(Player player) {
        return TagBase.getInstance().getProfileManager().getProfile(player).getTag();
    }

    public static String getSkin(Player player) {
        return TagBase.getInstance().getProfileManager().getProfile(player).getSkin();
    }

    public static void resetPlayer(Player player) {
        refreshPlayer(player, getRealName(player));
    }

}
