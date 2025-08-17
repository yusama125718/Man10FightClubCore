package yusama125718.man10FightClubCore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static yusama125718.man10FightClubCore.Man10FightClubCore.mfc_core;
import static yusama125718.man10FightClubCore.Man10FightClubCore.vaultapi;

public class Bet {
    private Map<UUID, String> players;
    private Map<UUID, Float> odds;
    private Map<UUID, Double> total_bets;
    // <賭け先, <賭けてる人>, <賭け額>>
    private Map<UUID, Map<UUID, Double>> bets;
    private Map<UUID, String> better;

    private String chat_prefix;
    private String sys_name;
    private String display_name;
    private final UUID BetID;
    private float prise_ratio;

    public ScoreBoardManager score_board;
    public BossBarManager boss_bar;
    public boolean isBet;

    // Standbyから値を引き継ぐ場合
    // p : Map<UUID, MCID>
    public Bet(Map<UUID, String> p, Standby st){
        players = p;
        score_board = st.score_board;
        boss_bar = st.boss_bar;
        chat_prefix = st.getChatPrefix();
        sys_name = st.getSystemName();
        display_name = st.getDisplayName();
        odds = new HashMap<>();
        total_bets = new HashMap<>();
        bets = new HashMap<>();
        better = new HashMap<>();
        for (UUID u : players.keySet()){
            total_bets.put(u, 0.0);
            bets.put(u, new HashMap<>());
            odds.put(u, 1.0F);
        }
        isBet = false;
        BetID = UUID.randomUUID();
        prise_ratio = 0.9F;
    }

    // 新規の場合
    public Bet(Map<UUID, String> p, String SystemName, String DisplayName, String BossBarTitle){
        sys_name = SystemName;
        display_name = DisplayName;
        setChatPrefix(DisplayName);
        score_board = new ScoreBoardManager(sys_name, BossBarTitle);
        score_board = new ScoreBoardManager(sys_name, display_name);
        odds = new HashMap<>();
        total_bets = new HashMap<>();
        bets = new HashMap<>();
        better = new HashMap<>();
        for (UUID u : players.keySet()){
            total_bets.put(u, 0.0);
            bets.put(u, new HashMap<>());
            odds.put(u, 1.0F);
        }
        isBet = false;
        BetID = UUID.randomUUID();
        prise_ratio = 0.9F;
    }

    public void StartBet(int wait_time){
        isBet = true;
        boss_bar.StartCountDown(wait_time, () -> {
            // １秒後にイベントを発火
            Bet bet = this;
            Bukkit.getScheduler().runTaskLater(mfc_core, () -> Bukkit.getPluginManager().callEvent(new MFCBetEndEvent(bet, sys_name)), 20L);
        });
    }

    // chat_prefix修正
    public void setChatPrefix(String s){
        chat_prefix = s + "§r";
    }

    public void setDisplay_name(String s){
        display_name = s;
    }

    public void setPriseRatio(float ratio){
        prise_ratio = ratio;
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

    public void PlayerBet(Player p, double price, UUID target){
        if (!isBet){
            p.sendMessage(Component.text(chat_prefix + "§c現在はベットできません"));
            return;
        }
        Thread th = new Thread(() -> {
            if (vaultapi.getBalance(p.getUniqueId()) < price){
                p.sendMessage(Component.text(chat_prefix + "§c資金が足りません"));
                return;
            }
            MySQLManager mysql = new MySQLManager(mfc_core, "man10_mfc_core");
            if (!mysql.execute("INSERT INTO money_log (time, system_name, mcid, uuid, pay_amount, out_amount, note) VALUES ('" + LocalDateTime.now() + "', '" + sys_name + "', '" + p.getName() + "', '" + p.getUniqueId() + "', " + price + ", 0, 'Action:Bet, BetID:" + BetID + "')")){
                p.sendMessage(Component.text(chat_prefix + "§cDBの保存に失敗しました"));
                return;
            }
            if (!vaultapi.withdraw(p.getUniqueId(), price)) {
                p.sendMessage(Component.text(chat_prefix + "§c出金に失敗しました"));
                return;
            }
            // オッズ・総額はメインスレッドでいじる
            Bukkit.getScheduler().runTask(mfc_core, () -> {
                double total = 0;
                // 掛け金総額計算・追加
                for (UUID u : total_bets.keySet()){
                    if (u == target){
                        double target_bet = total_bets.get(u);
                        target_bet += price;
                        total_bets.put(u, target_bet);
                    }
                    total += total_bets.get(u);
                }
                Map<UUID, Double> t_bet = bets.get(target);
                double bet_amount = 0;
                if (t_bet.containsKey(p.getUniqueId())) bet_amount = t_bet.get(p.getUniqueId());
                bet_amount += price;
                t_bet.put(p.getUniqueId(), bet_amount);
                bets.put(target, t_bet);
                better.put(p.getUniqueId(), p.getName());
                // オッズ再計算
                for (UUID u : odds.keySet()){
                    float o = (float) (total * (1 - prise_ratio) / total_bets.get(u));
                    odds.put(u, o);
                }
                RefreshScoreBoard(total);
                p.sendMessage(Component.text(chat_prefix + "§eベットしました"));
                score_board.BroadCast(chat_prefix + "§c§l" + p.getName() + "§rが§b§l" + players.get(target) + "§rに§e§l" + price + "円ベットしました");
            });
        });
        th.start();
    }

    public void SystemBet(double price, UUID target){
        Thread th = new Thread(() -> {
            MySQLManager mysql = new MySQLManager(mfc_core, "man10_mfc_core");
            if (!mysql.execute("INSERT INTO money_log (time, system_name, mcid, uuid, pay_amount, out_amount, note) VALUES ('" + LocalDateTime.now() + "', '" + sys_name + "', '[SERVER]', '[SERVER]', " + price + ", 0, 'Action:Bet, BetID:" + BetID + "')")){
                score_board.BroadCast(chat_prefix + "§cDBの保存に失敗しました");
                return;
            }
            // オッズ・総額はメインスレッドでいじる
            Bukkit.getScheduler().runTask(mfc_core, () -> {
                double total = 0;
                // 掛け金総額計算・追加
                for (UUID u : total_bets.keySet()){
                    if (u == target){
                        double target_bet = total_bets.get(u);
                        target_bet += price;
                        total_bets.put(u, target_bet);
                    }
                    total += total_bets.get(u);
                }
                Map<UUID, Double> t_bet = bets.get(target);
                double bet_amount = 0;
                if (t_bet.containsKey(null)) bet_amount = t_bet.get(null);
                bet_amount += price;
                t_bet.put(null, bet_amount);
                bets.put(target, t_bet);
                better.put(null, "[SERVER]");
                // オッズ再計算
                for (UUID u : odds.keySet()){
                    float o = (float) (total * (1 - prise_ratio) / total_bets.get(u));
                    odds.put(u, o);
                }
                RefreshScoreBoard(total);
                score_board.BroadCast(chat_prefix + "§c§l[SERVER]§rが§b§l" + players.get(target) + "§rに§e§l" + price + "円ベットしました");
            });
        });
        th.start();
    }

    public void Payout(UUID win_player, double additional_prise){
        Thread th = new Thread(() -> {
            float win_odds = 0F;
            for (UUID id : odds.keySet()){
                if (id == win_player){
                    win_odds = odds.get(id);
                    break;
                }
            }
            MySQLManager mysql = new MySQLManager(mfc_core, "man10_mfc_core");
            for (UUID id : bets.get(win_player).keySet()){
                double out_money = bets.get(win_player).get(id) * win_odds * (1 - prise_ratio);
                String uuid = "";
                if (id == null) uuid = "[SERVER]";
                else uuid = id.toString();
                if (!mysql.execute("INSERT INTO money_log (time, system_name, mcid, uuid, pay_amount, out_amount, note) VALUES ('" + LocalDateTime.now() + "', '" + sys_name + "', '" + better.get(id) + "', '" + uuid + "', 0, " + out_money + ", 'Action:Payout, BetID:" + BetID + "')")){
                    Bukkit.broadcast(Component.text(chat_prefix + "§cDBの保存に失敗しました"));
                    return;
                }
                if (id == null) return;
                vaultapi.deposit(id, out_money);
                score_board.BroadCast(chat_prefix + "§c" + better.get(id) + "§rは§e" + out_money + "円§r獲得しました");
            }
            // 賞金計算
            double total = 0;
            for (UUID u : total_bets.keySet()){
                total += total_bets.get(u);
            }
            double prise = total * prise_ratio + additional_prise;
            if (!mysql.execute("INSERT INTO money_log (time, system_name, mcid, uuid, pay_amount, out_amount, note) VALUES ('" + LocalDateTime.now() + "', '" + sys_name + "', '" + players.get(win_player) + "', '" + win_player + "', 0, " + prise + ", 'Action:WinPrise, BetID:" + BetID + "')")){
                Bukkit.broadcast(Component.text(chat_prefix + "§cDBの保存に失敗しました"));
                return;
            }
            vaultapi.deposit(win_player, prise);
            score_board.BroadCast(chat_prefix + "§c" + players.get(win_player) + "§rは賞金として§e" + prise + "円§r獲得しました");
            // オッズ・総額はメインスレッドでいじる
            Bukkit.getScheduler().runTask(mfc_core, () -> {
                // 念の為配列初期化
                odds = new HashMap<>();
                total_bets = new HashMap<>();
                bets = new HashMap<>();
                better = new HashMap<>();
            });
        });
        th.start();
    }

    public void cancelAll(double entry_prise){
        Thread th = new Thread(() -> {
            MySQLManager mysql = new MySQLManager(mfc_core, "man10_mfc_core");
            for (UUID p : bets.keySet()){
                for (UUID id : bets.get(p).keySet()){
                    double out_money = bets.get(p).get(id);
                    String uuid = "";
                    if (id == null) uuid = "[SERVER]";
                    else uuid = id.toString();
                    if (!mysql.execute("INSERT INTO money_log (time, system_name, mcid, uuid, pay_amount, out_amount, note) VALUES ('" + LocalDateTime.now() + "', '" + sys_name + "', '" + better.get(id) + "', '" + uuid + "', 0, " + bets.get(p).get(id) + ", 'Action:CancelBet, BetID:" + BetID + "')")){
                        Bukkit.broadcast(Component.text(chat_prefix + "§cDBの保存に失敗しました"));
                        return;
                    }
                    if (id == null) return;
                    vaultapi.deposit(id, bets.get(p).get(id));
                }
            }
            for (UUID id : players.keySet()){
                if (!mysql.execute("INSERT INTO money_log (time, system_name, mcid, uuid, pay_amount, out_amount, note) VALUES ('" + LocalDateTime.now() + "', '" + sys_name + "', '" + players.get(id) + "', '" + id + "', 0, " + entry_prise + ", 'Action:CancelMatch, BetID:" + BetID + "')")){
                    Bukkit.broadcast(Component.text(chat_prefix + "§cDBの保存に失敗しました"));
                    return;
                }
                vaultapi.deposit(id, entry_prise);
            }
            score_board.BroadCast(chat_prefix + "マッチがキャンセルされたので返金処理を実施しました");
            // オッズ・総額はメインスレッドでいじる
            Bukkit.getScheduler().runTask(mfc_core, () -> {
                // 配列初期化
                odds = new HashMap<>();
                total_bets = new HashMap<>();
                bets = new HashMap<>();
                better = new HashMap<>();
            });
        });
        th.start();
    }

    private void RefreshScoreBoard(double total){
        score_board.RemoveAll();
        score_board.SetContent("§b===ベット受付中===", 0);
        int cnt = 1;
        for (UUID u : players.keySet()){
            score_board.SetContent(players.get(u) + " : " + odds.get(u) + "倍", cnt);
            cnt++;
            if (cnt >= 15) break;
        }
    }

    // エントリー締め切りイベント
    public static class MFCBetEndEvent extends Event {

        private static final HandlerList HANDLERS = new HandlerList();
        private Bet bet;
        private String sys_name;

        public MFCBetEndEvent(Bet bet, String sys_name) {
            this.bet = bet;
            this.sys_name = sys_name;
        }

        public Bet getBet(){
            return bet;
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
