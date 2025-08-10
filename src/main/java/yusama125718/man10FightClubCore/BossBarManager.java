package yusama125718.man10FightClubCore;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

import static yusama125718.man10FightClubCore.Man10FightClubCore.mfc_core;

public class BossBarManager implements Listener {
    private BossBar boss_bar;
    private BukkitTask task;
    private String title;
    private List<String> show_players;
    private List<String> show_worlds;
    private boolean show_all;

    // %time%はカウントダウンの残り秒数に置き換えられる
    public BossBarManager(String BarTitle, BarColor Color, double Progress, BarStyle Style){
        title = BarTitle;
        boss_bar = Bukkit.createBossBar(title.replace("%time%", "0"), Color, Style);
        boss_bar.setProgress(Progress);
    }

    public BossBarManager(){}

    public void ShowToWorld(World world){
        if (show_worlds.contains(world.getName())) return;
        show_worlds.add(world.getName());
        for (Player p : world.getPlayers()){
            boss_bar.addPlayer(p);
        }
    }

    public void ShowToPlayer(Player p){
        if (show_players.contains(p.getName())) return;
        show_players.add(p.getName());
        boss_bar.addPlayer(p);
    }

    public void ShowAll(){
        if (show_all) return;
        show_all = true;
        for (Player p : Bukkit.getOnlinePlayers()){
            boss_bar.addPlayer(p);
        }
    }

    public void HideToWorld(World world){
        if (!show_worlds.contains(world.getName())) return;
        show_worlds.remove(world.getName());
        for (Player p : world.getPlayers()){
            boss_bar.removePlayer(p);
        }
    }

    public void HideToPlayer(Player p){
        if (!show_players.contains(p.getName())) return;
        show_players.remove(p.getName());
        boss_bar.removePlayer(p);
    }

    public void RemoveAll(){
        if (!show_all) return;
        show_all = false;
        boss_bar.removeAll();
    }

    public void ChangeColor(BarColor color){
        boss_bar.setColor(color);
    }

    public void ChangeStyle(BarStyle style){
        boss_bar.setStyle(style);
    }

    public void ChangeTitle(String Title){
        boss_bar.setTitle(Title);
    }

    public void ChangeProgress(Double progress){
        boss_bar.setProgress(progress);
    }

    public void StartCountDown(int second, Runnable complete){
        // 進行中のタスクがあればキャンセル
        if (task != null && !task.isCancelled()) task.cancel();
        task = new BukkitRunnable() {
            int timer = second;

            @Override
            public void run() {
                // %time%があれば置き換え
                if (title.contains("%time%")){
                    boss_bar.setTitle(title.replace("%time%", String.valueOf(timer)));
                }
                ChangeProgress((double) timer / second);

                if (timer <= 0){
                    if (complete != null) complete.run();
                    this.cancel();
                    return;
                }
                timer--;
            }
        }.runTaskTimer(mfc_core, 0L, 20L);
    }

    public void EndTimer(){
        task.cancel();
    }

    @EventHandler
    public void MoveWorld(PlayerChangedWorldEvent e){
        if (!show_worlds.contains(e.getFrom().getName()) && !show_worlds.contains(e.getPlayer().getWorld().getName())) return;
        // 表示ワールドに入る場合
        if (!show_worlds.contains(e.getFrom().getName())) boss_bar.addPlayer(e.getPlayer());
        // 表示ワールドから出た場合
        else boss_bar.removePlayer(e.getPlayer());
    }

    @EventHandler
    public void PlayerJoin(PlayerJoinEvent e){
        if (show_all) boss_bar.addPlayer(e.getPlayer());
    }

    @EventHandler
    public void PlayerLeft(PlayerQuitEvent e){
        if (show_players.contains(e.getPlayer().getName())) show_players.remove(e.getPlayer().getName());
    }
}
