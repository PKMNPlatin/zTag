package de.pkmnplatin.ztag.profile;

import de.pkmnplatin.ztag.TagBase;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * Created by Jona on 06.07.2017.
 */
@Getter
public class TagProfile {

    private final Player player;

    private String tag;
    private String skin;
    private final String realName;

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
