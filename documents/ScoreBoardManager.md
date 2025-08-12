# ScoreBoardManager

スコアボードの表示を管理するクラスです。

## インスタンス作成
```java
new ScoreBoardManager(String name, String title)
new ScoreBoardManager()
```
* **name** : スコアボードの内部名
* **title** : 表示タイトル

## 公開変数
このクラスにpublic変数はありません。

## 公開メソッド
### void SetContent(Map<Integer, String> content)
スコアボードの内容を一括設定します。

### void SetContent(String content, int row)
指定行にテキストを設定します。

### void ShowToWorld(World world)
指定ワールドにいるプレイヤーにスコアボードを表示します。プレイヤーがそのワールドを離れると自動的に非表示になります。

### void ShowToPlayer(Player p)
指定プレイヤーにスコアボードを表示します。

### void ShowAll()
全てのプレイヤーにスコアボードを表示します。

### void HideToWorld(World world)
指定ワールドのプレイヤーからスコアボードを非表示にします。

### void HideToPlayer(Player p)
指定プレイヤーからスコアボードを非表示にします。

### void RemoveAll()
全てのプレイヤーからスコアボードを取り除きます。

### void BroadCast(String message)
スコアボード表示対象者にメッセージを送信します。

## カスタムイベント
このクラスにカスタムイベントはありません。
