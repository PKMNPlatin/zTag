package de.pkmnplatin.ztag.reflect;

import lombok.Getter;
import org.bukkit.Bukkit;

/**
 * Created by Jona on 16.06.2017.
 */
public enum Version {

    UNKNOW("Before 1.8", 0000),
    v1_12_R1("1.12.0", 1121),
    v1_11_R1("1.11.0 - 1.11.2", 1111),
    v1_10_R1("1.10.0 - 1.10.2", 1101),
    v1_9_R2("1.9.2 - 1.9.4", 192),
    v1_9_R1("1.9.0 - 1.9.1", 191),
    v1_8_R3("1.8.3 - 1.8.8", 183),
    v1_8_R2("1.8.2", 182),
    v1_8_R1("1.8.0 - 1.8.1", 181);

    @Getter private String mcVersion;
    @Getter private int versionId;

    Version(String mcVersion, int versionId) {
        this.mcVersion = mcVersion;
        this.versionId = versionId;
    }

    public boolean isVersion(Version version) {
        return this.getVersionId() == version.getVersionId();
    }

    public boolean isNewerThan(Version version) {
        return this.getVersionId() > version.getVersionId();
    }

    public boolean isOlderThan(Version version) {
        return this.getVersionId() < version.getVersionId();
    }

    public static Version detectServerVersion() {
        String versionString = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        for(Version v : Version.values()) {
            if(versionString.equals(v.toString())) {
                return v;
            }
        }
        return Version.UNKNOW;
    }


}
