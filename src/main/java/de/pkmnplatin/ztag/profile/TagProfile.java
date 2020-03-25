package de.pkmnplatin.ztag.profile;

import de.pkmnplatin.ztag.TagBase;
import org.bukkit.entity.Player;

/**
 * Created by Jona on 06.07.2017.
 */
public class TagProfile {

    private final Player player;

    private String tag;
    private String skin;
    private final String realName;

    public Player getPlayer() {
        return player;
    }

    public String getTag() {
        return tag;
    }

    public String getSkin() {
        return skin;
    }

    public String getRealName() {
        return realName;
    }

    public TagProfile(Player player, String realName, String tag, String skin) {
        this.player = player;
        this.realName = realName;
        this.tag = tag;
        this.skin = skin;
    }

    public void update() {
        TagBase.getInstance().getProfileManager().updateProfile(getPlayer(), this);
    }

    public void setTag(String tag) {
        this.tag = tag;
        update();
    }

    public void setSkin(String skin) {
        this.skin = skin;
        update();
    }

}
