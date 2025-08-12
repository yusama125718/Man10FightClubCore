package yusama125718.man10FightClubCore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;

import static yusama125718.man10FightClubCore.Man10FightClubCore.mfc_core;
import static yusama125718.man10FightClubCore.Man10FightClubCore.vaultapi;

public class Standby {
    // システム名
    private String sys_name;
    // 表示名
    private String display_name;
    // ボスバーのタイトル
    private String boss_bar_title;
    // チャットのプレフィックス
    private String chat_prefix;
    // 待機中ユーザー名リスト
    private Map<String, UUID> players;
    // エントリー費用
    private int price;
    // ボスバー
    public BossBarManager boss_bar;
    // スコアボード
    public ScoreBoardManager score_board;
    // 募集中か
    public boolean isEntry = false;

    // 初期化・募集開始処理
    public Standby(String SysName, String DisplayName, String BossBarTitle, int Price){
        sys_name = SysName;
        display_name = DisplayName;
        chat_prefix = DisplayName + "§r";
        boss_bar_title = BossBarTitle;
        players = new HashMap<>();
        price = Price;
        boss_bar = new BossBarManager(boss_bar_title, BarColor.WHITE, 1.0, BarStyle.SOLID);
        score_board = new ScoreBoardManager(sys_name, display_name);
    }

    // 受付開始処理
    public void StartStandby(int wait_time){
        isEntry = true;
        score_board.SetContent("§c§l==参加者募集中==", 0);
        score_board.SetContent("0人登録中", 1);
        boss_bar.StartCountDown(wait_time, () -> {
            Standby standby = this;
            // １秒後にイベントを発火
            Bukkit.getScheduler().runTaskLater(mfc_core, () -> {
                Bukkit.getPluginManager().callEvent(new Standby.MFCStandbyEndEvent(players, standby, sys_name));
                score_board.RemoveAll();
            }, 20L);
        });
    }

    // 登録プレイヤー取得処理
    public Map<String, UUID> getPlayers() {
        return players;
    }

    // chat_prefix修正
    public void setChatPrefix(String s){
        chat_prefix = s + "§r";
    }

    public void setDisplay_name(String s){
        display_name = s;
    }

    public String getSystemName(){
        return sys_name;
    }

    public String getDisplayName(){
        return display_name;
    }

    public String getChatPrefix(){
        return chat_prefix;
    }

    // エントリー処理
    public void Entry(Player p){
        if (!isEntry) {
            p.sendMessage(Component.text(chat_prefix + "§c参加募集中ではありません"));
            return;
        }
        if (players.containsKey(p.getName())) {
            p.sendMessage(Component.text(chat_prefix + "§cすでに登録済みです"));
            return;
        }
        if (price != 0 && vaultapi.getBalance(p.getUniqueId()) < price) {
            p.sendMessage(Component.text(chat_prefix + "§c資金が足りません"));
            return;
        }
        Thread th = new Thread(() -> {
            MySQLManager mysql = new MySQLManager(mfc_core, "man10_mfc_core");
            if (!mysql.execute("INSERT INTO money_log (time, system_name, mcid, uuid, pay_amount, out_amount, note) VALUES ('" + LocalDateTime.now() + "', '" + sys_name + "', '" + p.getName() + "', '" + p.getUniqueId() + "', " + price + ", 0, 'Action:Entry')")){
                p.sendMessage(Component.text(chat_prefix + "§cDBの保存に失敗しました"));
                return;
            }
            if (!vaultapi.withdraw(p.getUniqueId(), price)) {
                p.sendMessage(Component.text(chat_prefix + "§c出金に失敗しました"));
                return;
            }
            // マップはメインスレッドでいじる
            Bukkit.getScheduler().runTask(mfc_core, () -> {
                players.put(p.getName(), p.getUniqueId());
                score_board.SetContent(players.size() + "人登録中", 1);
                p.sendMessage(Component.text(chat_prefix + "§e参加登録をしました"));
            });
        });
        th.start();
    }

    public void Cancel(Player p){
        if (players.containsKey(p.getName())){
            p.sendMessage(chat_prefix + "§c登録していません");
            return;
        }
        Thread th = new Thread(() -> {
            MySQLManager mysql = new MySQLManager(mfc_core, "man10_mfc_core");
            if (!mysql.execute("INSERT INTO money_log (time, system_name, mcid, uuid, pay_amount, out_amount, note) VALUES ('" + LocalDateTime.now() + "', '" + sys_name + "', '" + p.getName() + "', '" + p.getUniqueId() + "', 0, " + price + ", 'Entry')")){
                p.sendMessage(Component.text(chat_prefix + "§cDBの保存に失敗しました"));
                return;
            }
            if (!vaultapi.deposit(p.getUniqueId(), price)) {
                p.sendMessage(Component.text(chat_prefix + "§c入金に失敗しました"));
                return;
            }
            // マップはメインスレッドでいじる
            Bukkit.getScheduler().runTask(mfc_core, () -> {
                players.remove(p.getName());
                p.sendMessage(Component.text(chat_prefix + "§e参加登録を取り消しました"));
            });
        });
        th.start();
    }

    public void AllCancel(){
        Thread th = new Thread(() -> {
            for(String mcid : players.keySet()){
                MySQLManager mysql = new MySQLManager(mfc_core, "man10_mfc_core");
                if (!mysql.execute("INSERT INTO money_log (time, system_name, mcid, uuid, pay_amount, out_amount, note) VALUES ('" + LocalDateTime.now() + "', '" + sys_name + "', '" + mcid + "', '" + players.get(mcid) + "', 0, " + price + ", 'Entry')")) return;
                vaultapi.deposit(players.get(mcid), price);
            }
            // 配列はメインスレッドでいじる
            Bukkit.getScheduler().runTask(mfc_core, () -> {
                players = new HashMap<>();
            });
        });
        th.start();
    }

    // エントリー締め切りイベント
    public static class MFCStandbyEndEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();
        private final Map<String, UUID> players;
        private Standby st;
        private String sys_name;

        public MFCStandbyEndEvent(Map<String, UUID> ps, Standby st, String sys_name) {
            this.players = ps;
            this.st = st;
            this.sys_name = sys_name;
        }

        public Map<String, UUID> getPlayers() {
            return players;
        }

        public Standby getStanby(){
            return st;
        }

        public String getSystemName() {
            return sys_name;
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            return HANDLERS;
        }
    }
}
