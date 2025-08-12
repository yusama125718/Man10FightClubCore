# Man10FightClubCore
Man10FightClub（MFC）のコアシステムです。\
ライブラリとして活用することで様々な個人戦のベット可能なミニゲームを作成することができます。\
理論上1vs1だけでなく1v1v1v1なども出来る仕様です。

## やれること
このシステムでは以下のことができます
* エントリー管理
* スコアボード（画面右のやつ）管理
* ボスバー管理
* ベット管理
* 賞金・払戻金支払処理

## DBについて
ゲームの流れで発生するお金のやり取りは全てCore側で完結するように作成しているのでログの保存などを機にする必要はありません。\
DBが無いとログを保存できず処理が進まないので必ず作成してください。\
開発環境ではMySQLで動作確認を実施しています。サーバーではconfig.ymlの以下の値を確認・修正してください。
```yaml
mysql:
  host: <db接続先>
  port: '<dbポート番号>'
  user: <dbユーザー名>
  pass: <dbログインパスワード>
  db: <db名>
```
<db名>で指定したdbは自動では作成されないので手動で作成してください。

## 導入方法
mavenの場合はdependencies配下に以下を記述します
```xml
<dependency>
    <groupId>yusama125718</groupId>
    <artifactId>Man10FightClubCore</artifactId>
    <version>1.0</version>
    <scope>system</scope>
    <systemPath>${basedir}/Man10FightClubCore.jar</systemPath>
</dependency>
```


## 使用方法
各機能の使用方法はdocuments配下にクラスごとに書いてあります。