# BossBarManager
ボスバーの管理を楽にするための機能です。

## インスタンス作成
以下のようにインスタンスを作成できます
```access transformers
new BossBarManager(String BarTitle, BarColor Color, double Progress, BarStyle Style)
```
* **BarTitle** : ボスバーの上部に表示される名前を設定します（%time%を挿入することでカウントダウンの残り時間を設定可能）
* **Color** : ボスバーの色を指定します
* **Progress** : ボスバーの充填率を設定します（0〜1）
* **Style** : ボスバーの見た目を指定します

## クラスメソッド
以下のメソッドが使用可能です

### void ShowToWorld(World world)
引数で指定したワールドにいるプレイヤーにボスバーを表示します。\
追加したワールドから離れると自動で非表示にします。

### void ShowToPlayer(Player p)
引数で指定したプレイヤーにボスバーを表示するようにします。

### void ShowAll()
全てのプレイヤーにボスバーを表示するようにします。

### void HideToWorld(World world)
ボスバーを表示に設定しているワールドから引数で指定したワールドを除外します

### public void HideToPlayer(Player p)
ボスバーを表示に設定しているプレイヤーから引数で指定したプレイヤーを除外します

### void RemoveAll()
全てのプレイヤーから非表示にします