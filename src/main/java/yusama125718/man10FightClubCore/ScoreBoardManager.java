package yusama125718.man10FightClubCore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreBoardManager implements Listener {
    private Map<Integer, String> system_txt;
    private String title;
    private String name;
    private Scoreboard board;
    private Objective obj;
    private List<String> show_players;
    private List<String> show_worlds;
    private boolean show_all;

    public ScoreBoardManager(String Name, String Title){
        system_txt = new HashMap<>();
        title = Title;
        name = Name;
        show_players = new ArrayList<>();
        show_worlds = new ArrayList<>();
        show_all = false;
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        obj = board.registerNewObjective(name, Criteria.DUMMY, title);
    }

    public ScoreBoardManager() {}

    public void SetContent(Map<Integer, String> content){
        system_txt = content;
        for (int i = 0; i < 15; i++){
            obj.getScore(system_txt.getOrDefault(14 - i, "")).setScore(i);
        }
    }

    public void SetContent(String content, int row){
        system_txt.put(14 - row, content);
        obj.getScore(content).setScore(14 - row);
    }

    public void ShowToWorld(World world){
        if (show_worlds.contains(world.getName())) return;
        show_worlds.add(world.getName());
        for (Player p : world.getPlayers()){
            p.setScoreboard(board);
        }
    }

    public void ShowToPlayer(Player p){
        if (show_players.contains(p.getName())) return;
        show_players.add(p.getName());
        p.setScoreboard(board);
    }

    public void ShowAll(){
        if (show_all) return;
        show_all = true;
        for (Player p : Bukkit.getOnlinePlayers()){
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    public void HideToWorld(World world){
        if (!show_worlds.contains(world.getName())) return;
        show_worlds.remove(world.getName());
        for (Player p : world.getPlayers()){
            if (p.getScoreboard() != board || show_players.contains(p.getName())) continue;
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    public void HideToPlayer(Player p){
        if (!show_players.contains(p.getName())) return;
        show_players.remove(p.getName());
        if (p.getScoreboard() != board) return;
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public void RemoveAll(){
        if (!show_all) return;
        show_all = false;
        for (Player p : Bukkit.getOnlinePlayers()){
            if (p.getScoreboard() != board) continue;
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    public void BroadCast(String message){
        if (show_all) {
            Bukkit.broadcast(Component.text(message));
            return;
        }
        for (String s : show_players){
            Player p = Bukkit.getPlayer(s);
            if (p != null) p.sendMessage(Component.text(message));
        }
        for (String s : show_worlds){
            World w = Bukkit.getWorld(s);
            if (w == null) continue;
            for (Player p : w.getPlayers()){
                if (show_players.contains(p.getName())) continue;
                p.sendMessage(Component.text(message));
            }
        }
    }

    @EventHandler
    public void MoveWorld(PlayerChangedWorldEvent e){
        if (!show_worlds.contains(e.getFrom().getName()) && !show_worlds.contains(e.getPlayer().getWorld().getName())) return;
        // 表示ワールドに入る場合
        if (!show_worlds.contains(e.getFrom().getName())) e.getPlayer().setScoreboard(board);
            // 表示ワールドから出た場合
        else e.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    @EventHandler
    public void PlayerJoin(PlayerJoinEvent e){
        if (show_all) e.getPlayer().setScoreboard(board);
    }

    @EventHandler
    public void PlayerLeft(PlayerQuitEvent e){
        if (show_players.contains(e.getPlayer().getName())) show_players.remove(e.getPlayer().getName());
    }
}
