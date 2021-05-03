# Maven Repos 登録手順メモ

## version 設定

- version 名に `-SNAPSHOT` を付与
    - ex: `1.11.20210503b-SNAPSHOT`

## git を clean に

git を cloean にする。

## mvn release:prepare

```sh
mvn release:prepare
```

- new development version には以下を
    - ex: `1.11.20210503c-SNAPSHOT`

## mvn release:release

```sh
mvn release:perform
```

## Sonatype リリース操作

リリース操作.

- https://oss.sonatype.org/
- Staging Repositories
- Close
- Release
- `しばらく` 待つ。目安は 60分

# MEMO

- Maven デフォルトのデプロイを選択する方向性
- Sonatype のデプロイプラグインだと GUI 操作抜きで そのまま Central までいってしまうぽい。
