# Standby

試合参加者の募集と待機状態を管理するクラスです。

## インスタンス作成
```java
new Standby(String systemName, String displayName, String bossBarTitle, int price)
```
* **systemName** : システムの内部名
* **displayName** : 表示名
* **bossBarTitle** : ボスバーのタイトル
* **price** : エントリー費用

## 公開変数
* `BossBarManager boss_bar` : 募集カウントダウン用ボスバー
* `ScoreBoardManager score_board` : 募集状況表示用スコアボード
* `boolean isEntry` : 現在受付中かどうか

## 公開メソッド
### void StartStandby(int wait_time)
指定秒数のカウントダウンを開始し、終了時に `MFCStandbyEndEvent` を発火します。

### Map<String, UUID> getPlayers()
登録済みプレイヤーの一覧を取得します。

### void setChatPrefix(String s)
チャットメッセージのプレフィックスを設定します。

### void setDisplay_name(String s)
表示名を設定します。

### String getSystemName()
システム名を取得します。

### String getDisplayName()
表示名を取得します。

### String getChatPrefix()
チャットプレフィックスを取得します。

### void Entry(Player p)
プレイヤーを募集に登録します。

### void Cancel(Player p)
プレイヤーの登録を取り消します。

### void AllCancel()
全てのプレイヤーの登録を取り消します。

### void RemovePlayer(String mcid)
指定したプレイヤーを登録から外します。
マッチング時にマッチングしたプレイヤー必ず外してください。
（無料で参加登録できることになってしまいます）

## カスタムイベント
### MFCStandbyEndEvent
`StartStandby` のカウントダウン終了時に発火し、募集の締め切りを通知します。

#### 利用可能なメソッド
* `Map<String, UUID> getPlayers()` : 登録済みプレイヤー一覧を取得します。
* `Standby getStanby()` : 対象の `Standby` インスタンスを取得します。
* `String getSystemName()` : システム名を取得します。
