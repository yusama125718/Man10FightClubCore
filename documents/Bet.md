# Bet

ベット機能を提供するクラスです。

## インスタンス作成
```java
new Bet(Map<UUID, String> players, Standby standby)
new Bet(Map<UUID, String> players, String systemName, String displayName, String bossBarTitle)
```
* **players** : 参加者のUUIDとプレイヤー名の対応表
* **standby** : Standbyインスタンスから状態を引き継ぐ場合に使用
* **systemName** : システムの内部名
* **displayName** : 表示名
* **bossBarTitle** : ボスバーのタイトル

## 公開変数
* `ScoreBoardManager score_board` : 表示用スコアボード
* `BossBarManager boss_bar` : カウントダウン用ボスバー
* `boolean isBet` : ベット受付中かどうか

## 公開メソッド
### void StartBet(int wait_time)
カウントダウンを開始し、指定秒数後に `MFCBetEndEvent` を発火します。

### void setChatPrefix(String s)
チャットメッセージのプレフィックスを設定します。

### void setDisplay_name(String s)
表示名を設定します。

### void setPriseRatio(float ratio)
総ベット額に対する賞金の割合を設定します。

### String getSystemName()
システム名を取得します。

### String getDisplayName()
表示名を取得します。

### String getChatPrefix()
チャットプレフィックスを取得します。

### void PlayerBet(Player p, double price, UUID target)
指定プレイヤーがターゲットにベットします。

### void SystemBet(double price, UUID target)
システムからターゲットにベットします。

### void Payout(UUID win_player, double additional_prise)
勝者に賞金を支払い、ベット払い戻し処理後にベット状況を初期化します。
賞金はベット総額から指定した割合の他、additional_priseが賞金に上乗せされます。

### void cancelAll(double entry_prise)
試合中止などで全てのベットを返金し、処理後にベット状況を初期化します。

## カスタムイベント
### MFCBetEndEvent
`StartBet` のカウントダウンが終了した際に発火するイベントです。

#### 利用可能なメソッド
* `Bet getBet()` : 対象の `Bet` インスタンスを取得します。
* `String getSystemName()` : システム名を取得します。
