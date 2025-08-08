package yusama125718.man10FightClubCore;

import org.bukkit.plugin.java.JavaPlugin;

public final class Man10FightClubCore extends JavaPlugin {

    public static JavaPlugin mfc_core;

    @Override
    public void onEnable() {
        mfc_core = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
