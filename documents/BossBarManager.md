# BossBarManager

ボスバーの表示を管理するクラスです。

## インスタンス作成
```java
new BossBarManager(String barTitle, BarColor color, double progress, BarStyle style)
new BossBarManager()
```
* **barTitle** : ボスバーの上部に表示されるタイトル（`%time%` を含めると残り時間に置き換わります）
* **color** : ボスバーの色
* **progress** : 初期の充填率（0〜1）
* **style** : ボスバーのスタイル

## 公開変数
このクラスにpublic変数はありません。

## 公開メソッド
### void ShowToWorld(World world)
指定ワールドにいるプレイヤーにボスバーを表示します。プレイヤーがそのワールドを離れると自動的に非表示になります。

### void ShowToPlayer(Player p)
指定プレイヤーにボスバーを表示します。

### void ShowAll()
全てのプレイヤーにボスバーを表示します。

### void HideToWorld(World world)
指定ワールドのプレイヤーからボスバーを非表示にします。

### void HideToPlayer(Player p)
指定プレイヤーからボスバーを非表示にします。

### void RemoveAll()
全てのプレイヤーからボスバーを取り除きます。

### void ChangeColor(BarColor color)
ボスバーの色を変更します。

### void ChangeStyle(BarStyle style)
ボスバーのスタイルを変更します。

### void ChangeTitle(String title)
ボスバーのタイトルを変更します。

### void ChangeProgress(Double progress)
ボスバーの充填率を変更します。

### void StartCountDown(int second, Runnable complete)
カウントダウンを開始し、完了時に `complete` を実行します。

### void EndTimer()
進行中のカウントダウンを停止します。

## カスタムイベント
このクラスにカスタムイベントはありません。
