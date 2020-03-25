package de.pkmnplatin.ztag;

import de.pkmnplatin.ztag.profile.ProfileManager;
import de.pkmnplatin.ztag.reflect.Version;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

/**
 * Created by Jona on 16.06.2017.
 */
public class TagBase extends JavaPlugin {

    private static TagBase instance;

    private Version version;
    private ProfileManager profileManager;

    public static TagBase getInstance() {
        return instance;
    }

    public Version getVersion() {
        return version;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public static void log(String msg) {
        instance.getLogger().info(msg);
    }

    public static void log(Exception ex) {
        log(ex.getMessage());
    }

    @Override
    public void onEnable() {
        TagBase.instance = this;
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (Exception ex) {
            log(ex);
        }
        this.profileManager = new ProfileManager();
        this.version = Version.detectServerVersion();
        if (this.version.equals(Version.UNKNOWN)) {
            log("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= zTag =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
            log("Your Spigot-Version isn't compatible with this Version of zTag!");
            log("Please use a Spigot from " + Version.values()[Version.values().length - 1] + " to " + Version.values()[1] + "!");
            log("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= zTag =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            log("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= zTag =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
            log("The Spigot-Version " + version.getMcVersion() + " was detected!");
            log("zTag should work without any issues!");
            log("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= zTag =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        }
    }

}
