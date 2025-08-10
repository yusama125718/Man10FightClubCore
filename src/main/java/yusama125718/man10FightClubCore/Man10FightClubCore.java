package yusama125718.man10FightClubCore;

import org.bukkit.plugin.java.JavaPlugin;

public final class Man10FightClubCore extends JavaPlugin {

    public static JavaPlugin mfc_core;
    public static VaultAPI vaultapi;

    @Override
    public void onEnable() {
        mfc_core = this;
        mfc_core.saveDefaultConfig();
        vaultapi = new VaultAPI();
        getServer().getPluginManager().registerEvents(new BossBarManager(), this);
        getServer().getPluginManager().registerEvents(new ScoreBoardManager(), this);
        Thread th = new Thread(() -> {
            MySQLManager mysql = new MySQLManager(mfc_core, "man10_mfc_core");
            mysql.execute("create table if not exists money_log(id int auto_increment,time datetime,system_name varchar(120),mcid varchar(16),uuid varchar(36),pay_amount integer, out_amount integer, note varchar(120),primary key(id))");
        });
        th.start();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
