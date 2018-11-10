package de.pkmnplatin.ztag.reflect;

import com.mojang.authlib.GameProfile;
import de.pkmnplatin.ztag.TagBase;
import net.minecraft.server.v1_13_R2.DimensionManager;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.entity.Player;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Created by Jona on 16.06.2017.
 */
public class Reflection {

    private static String version;

    private static String getVersion() {
        if(version == null) {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        }
        return version;
    }

    public static Field getField(Class clazz, String name) {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            if(Modifier.isFinal(f.getModifiers())) {
                getField(f.getClass(), "modifiers").set(f, Modifier.FINAL);
            }
            return f;
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return null;
    }

    public static void setValue(Object instance, Class clazz, String field, Object value) {
        try {
            getField(clazz, field).set(instance, value);
        } catch (Exception ex) {
            TagBase.log(ex);
        }
    }

    public static Class getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + TagBase.getInstance().getVersion().toString() + "." + name);
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return null;
    }

    public static Class getCraftbukkitClass(String name) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + getVersion() + "." + name);
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return null;
    }

    public static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = getField(handle.getClass(), "playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception ex) {
            TagBase.log(ex);
        }
    }

    public static Object getEntityPlayer(Player player) {
        Object entity = null;
        try {
            entity = player.getClass().getMethod("getHandle").invoke(player);
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return entity;
    }

    public static int getEntityId(Player player) {
        int id = -1;
        try {
            id = (int) player.getClass().getMethod("getEntityId").invoke(player);
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return id;
    }

    public static Object getSpawnPacket(Player player) {
        Object packet = null;
        try {
            Constructor<?> packetConstructor = getNMSClass("PacketPlayOutNamedEntitySpawn").getConstructors()[1];
            packet = packetConstructor.newInstance(getEntityPlayer(player));
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return packet;
    }

    public static Object getDestroyPacket(int entityId) {
        Object packet = null;
        try {
            packet = getNMSClass("PacketPlayOutEntityDestroy").newInstance();
            setValue(packet, packet.getClass(), "a", new int[] {entityId});
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return packet;
    }

    public static Object getInfoPacket(String action, GameProfile gp, int ping, GameMode gameMode, String name) {
        Object packet = null;
        try {
            packet = getNMSClass("PacketPlayOutPlayerInfo").newInstance();
            setValue(packet, packet.getClass(), "a", getEnumInfoAction(action));
            setValue(packet, packet.getClass(), "b", Arrays.asList(getPlayerInfoData(packet, gp, ping, gameMode, name)));
        } catch (Exception ex) {
            TagBase.log(ex.getMessage());
        }
        return packet;
    }

    public static Object getEnumInfoAction(String action) {
        Object obj = null;
        try {
            Version v = TagBase.getInstance().getVersion();
            if(v.equals(Version.v1_8_R1)) {
                Class enumClass = getNMSClass("EnumPlayerInfoAction");
                for(Field field : enumClass.getFields()) {
                    Object o = field.get(enumClass);
                    if(o.toString().equals(action)) {
                        obj = o;
                    }
                }
            } else {
                Class packetClass = getNMSClass("PacketPlayOutPlayerInfo");
                Class enumClass = packetClass.getClasses()[1];
                for(Field field : enumClass.getFields()) {
                    Object o = field.get(enumClass);
                    if(o.toString().equals(action)) {
                        obj = o;
                    }
                }
            }
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return obj;
    }

    public static GameProfile getGameProfile(Player player) {
        GameProfile gp = null;
        try {
            gp = (GameProfile) player.getClass().getMethod("getProfile").invoke(player);
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return gp;
    }

    public static int getPing(Player player) {
        int ping = -1;
        try {
            ping = (int) getField(getEntityPlayer(player).getClass(), "ping").get(getEntityPlayer(player));
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return ping;
    }

    public static Object getRespawnPacket(Player player, World world) {
        Object packet = null;
        try {
            Constructor<?> packetConstructor = getNMSClass("PacketPlayOutRespawn").getConstructors()[1];
            Object enumDifficulty = getEnumDifficulty(world.getDifficulty());
            Object worldType = getWorldType(world);
            Object enumGamemode = getEnumGamemode(player.getGameMode());
            if(TagBase.getInstance().getVersion().isOlderThan(Version.v1_13_R1)) {
                int enviroment = world.getEnvironment().getId();
                packet = packetConstructor.newInstance(enviroment, enumDifficulty, worldType , enumGamemode);
            } else {
                Object dimensionManager = getNMSClass("DimensionManager").getMethod("a", int.class).invoke(null, world.getEnvironment().getId());
                packet = packetConstructor.newInstance(dimensionManager, enumDifficulty, worldType , enumGamemode);
            }
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return packet;
    }

    public static Object getPlayerInfoData(Object infoPacket, GameProfile gp, int ping, GameMode gameMode, String name) {
        Object playerInfoData = null;
        try {
            if(TagBase.getInstance().getVersion().equals(Version.v1_8_R1)) {
                playerInfoData = getNMSClass("PlayerInfoData").getConstructors()[0].newInstance(infoPacket, gp, ping, getEnumGamemode(gameMode), getNameFromString(name));
            } else {
                Constructor infoConstruct = getNMSClass("PacketPlayOutPlayerInfo").getClasses()[0].getConstructors()[0];
                playerInfoData = infoConstruct.newInstance(infoPacket, gp, ping, getEnumGamemode(gameMode), getNameFromString(name));
            }
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return playerInfoData;
    }

    public static Object getEnumGamemode(GameMode gameMode) {
        Object enumGamemode = null;
        int value = -1;
        if(gameMode != null) {
            value = gameMode.getValue();
        }
        try {
            Version version = TagBase.getInstance().getVersion();
            if(version.equals(Version.v1_8_R2) || version.equals(Version.v1_8_R3) || version.equals(Version.v1_9_R1) || version.equals(Version.v1_9_R2)) {
                Class worldSettings = getNMSClass("WorldSettings");
                Class enumGamemodeClass = worldSettings.getClasses()[0];
                enumGamemode = enumGamemodeClass.getMethod("getById", int.class).invoke(null, value);
            } else {
                Class enumGamemodeClass = getNMSClass("EnumGamemode");
                enumGamemode = enumGamemodeClass.getMethod("getById", int.class).invoke(null, value);
            }
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return enumGamemode;
    }

    public static Object getEnumDifficulty(Difficulty difficulty) {
        Object enumDifficulty = null;
        try {
            Class enumDifficultyClass = getNMSClass("EnumDifficulty");
            enumDifficulty = enumDifficultyClass.getMethod("getById", int.class).invoke(null, difficulty.getValue());
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return enumDifficulty;
    }

    public static Object getWorldType(World world) {
        Object worldType = null;
        try {
            Class worldTypeClass = getNMSClass("WorldType");
            worldType = worldTypeClass.getMethod("getType", String.class).invoke(null, world.getWorldType().getName());
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return worldType;
    }

    public static Object getNameFromString(String name) {
        Object string = null;
        try {
            Class craftChatMessage = getCraftbukkitClass("util.CraftChatMessage");
            string = ((Object[])craftChatMessage.getMethod("fromString", String.class).invoke(null, name))[0];
        } catch (Exception ex) {
            TagBase.log(ex);
        }
        return string;
    }

}
