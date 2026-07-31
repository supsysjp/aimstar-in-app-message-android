# AIMSTAR In-App Messaging Android SDK

AIMSTAR で設定されたアプリ内メッセージを Android アプリ上で表示するための SDK です。

## Requirements

| 項目 | バージョン |
| - | - |
| minSdk | 28 以上 |
| compileSdk / targetSdk | 36 |

## SDKで提供する機能について

- アプリのページ閲覧イベントを送信する
- 対象と判定されたユーザーに、以下の3種類のメッセージを表示する
  - **ポップアップモーダル**: 画面中央に表示されるモーダルダイアログ
  - **ポップアップバナー**: 画面上部/中央/下部に表示されるバナー（位置は9箇所から指定可能）
  - **埋め込みバナー**: アプリの任意の場所に埋め込み表示するバナー
- メッセージの表示、ユーザー操作による非表示、コンバージョンボタンのタップの各イベントを送信する
- View / Jetpack Compose の両方に対応

## 用語

| 用語 | 説明 |
| - | - |
| API Key | AimstarInAppMessaging を利用するために必要な API キーで、AIMSTAR 側で事前にアプリ開発者に発行されます。 |
| Tenant ID | AimstarInAppMessaging を利用するために必要なテナント ID で、AIMSTAR 側で事前にアプリ開発者に発行されます。 |
| Customer ID | アプリ開発者がユーザーを識別する ID で、アプリ開発者が独自に発行、生成、または利用します。 |
| ScreenName | アプリ側で設定するトリガーの一種（アプリ開発者が任意に設定する識別名）で、ユーザーが特定の画面を表示したり、またはアクションを行うなどの条件を満たした場合に、識別名を使ってメッセージを呼び出すために利用されます。 |
| ComponentID | 埋め込みバナーを識別するための文字列 ID です。AIMSTAR 管理画面で設定した値と、アプリ側で指定する値を一致させる必要があります。 |

## 導入手順

### 1. SDKをアプリに追加する

[Releases](https://github.com/supsysjp/aimstar-in-app-message-android/releases) から `AimstarMessagingSdk.aar` をダウンロードし、プロジェクトの `app/libs/` ディレクトリに配置してください。

次に、`app/build.gradle.kts` に以下を追記します。

```kotlin
dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to arrayOf("*.aar"))))
}
```

### 2. SDKの初期化とイベントリスナーを設定する

`Application` クラスの `onCreate()` 内に初期化コードを追加します。

```kotlin
import jp.co.aimstar.messaging.android.AimstarInAppMessaging
import jp.co.aimstar.messaging.android.AimstarInAppMessagingListener
import jp.co.aimstar.messaging.android.AimstarException
import jp.co.aimstar.messaging.android.model.InAppMessage

class YourApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // イベントリスナーを設定します（任意）。全メソッドにデフォルト実装があるため、必要なメソッドのみ実装できます
        AimstarInAppMessaging.setListener(object : AimstarInAppMessagingListener {
            override fun messageDismissed(message: InAppMessage) {
                // メッセージが非表示になったタイミングで実行
            }
            override fun messageClicked(message: InAppMessage) {
                // メッセージ内のボタンがタップされたタイミングで実行
            }
            override fun messageDetectedForDisplay(message: InAppMessage) {
                // 表示対象のメッセージが検出された際に実行
            }
            override fun messageError(message: InAppMessage?, error: AimstarException) {
                // SDK内でエラーが発生した際に実行
            }
        })

        // SDKの初期化を行います
        val API_KEY = "発行されたAPI KEY"
        val TENANT_ID = "発行されたテナントID"
        AimstarInAppMessaging.setup(context = applicationContext, apiKey = API_KEY, tenantId = TENANT_ID)
    }
}
```

### 3. Customer IDの設定

SDKの初期化後に必要に応じてCustomer IDを設定してください。

```kotlin
// アプリにユーザーがログインした後など、ユーザーが識別できるようになった後に実行
AimstarInAppMessaging.customerId = "ユーザーを識別するID"
```

#### ユーザーのログイン・ログアウト状態の判定

`AimstarInAppMessaging.customerId` にユーザーIDが設定されているかどうかで状態が判定されます。従って、ユーザーがログアウトを実行した際には `null` を設定してください。

```kotlin
// アプリからユーザーがログアウト後に実行
AimstarInAppMessaging.customerId = null
```

### 4. isStrictLoginフラグの設定

ユーザーのログイン・ログアウト状態を厳密に判定する場合は `true` を設定してください。初期値は `false` です。

```kotlin
AimstarInAppMessaging.isStrictLogin = true
```

### 5. メッセージの取得と表示

スクリーン名を設定して `screenView` メソッドを実行します。

#### View

```kotlin
// シンプルな呼び出し
AimstarInAppMessaging.screenView(activity = this, screenName = "Your Screen Name")
```

ポップアップバナーの表示位置をカスタムヘッダーやナビゲーションバーなどのUI要素に合わせて調整するには、`topAnchor` / `bottomAnchor` を指定してください。

```kotlin
// ヘッダーの下端〜ナビゲーションバーの上端の範囲内にバナーを表示する
AimstarInAppMessaging.screenView(
    activity = this,
    screenName = "Your Screen Name",
    topAnchor = headerView,
    bottomAnchor = navigationBarView
)
```

#### Jetpack Compose

Compose環境では、`topOffsetPx` / `bottomOffsetPx` でバナーの表示位置をpx単位で指定します。

```kotlin
AimstarInAppMessaging.screenView(
    activity = this,
    screenName = "Your Screen Name",
    topOffsetPx = topBarHeightPx,
    bottomOffsetPx = bottomBarHeightPx
)
```

#### 埋め込みバナー

アプリの任意の場所にバナーを埋め込む場合は、埋め込みバナーコンポーネントを使用します。埋め込みバナーのコンテンツを読み込むには、`screenView` の呼び出しが必要です。

デフォルトでは、コンテンツのロード中はバナー領域が高さ 0 に折りたたまれ、表示可能になった時点でバナーが現れます。ロード中にプレースホルダー（shimmer）を表示したい場合は、`showsLoadingPlaceholder` を `true` に設定してください。

**View (XMLレイアウト):**

```xml
<jp.co.aimstar.messaging.android.ui.view.IAMEmbeddedBannerView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:componentId="your_component_id"
    app:showsLoadingPlaceholder="true" />
```

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AimstarInAppMessaging.screenView(activity = this, screenName = "Your Screen Name")
    }
}
```

**Jetpack Compose:**

```kotlin
import jp.co.aimstar.messaging.android.ui.view.IAMEmbeddedBanner

@Composable
fun SomeScreen() {
    Column {
        IAMEmbeddedBanner(
            componentId = "your_component_id",
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            showsLoadingPlaceholder = true
        )
    }
}
```

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AimstarInAppMessaging.screenView(activity = this, screenName = "Your Screen Name")

        setContent {
            YourAppTheme {
                SomeScreen()
            }
        }
    }
}
```

### 6. カスタムパラメータの送信

`screenView` メソッドにカスタムパラメータを付与できます。カスタムパラメータは、管理画面側でのメッセージの表示条件などに使用できます。

```kotlin
AimstarInAppMessaging.screenView(activity = this, screenName = "Your Screen Name") {
    putString("category", "electronics")
    putInteger("age", 25)
    putBoolean("is_premium", true)
    putDouble("price", 99.9)
    putStringList("tags", listOf("sale", "new"))
}
```

対応する型: `putString`, `putInteger`, `putDouble`, `putBoolean`, `putStringList`, `putIntegerList`, `putDoubleList`, `putBooleanList`

---

# 利用ガイド

## screenView 呼び出しのタイミング制御

SDK にはポップアップの表示を抑制する機能がありません。メッセージを表示したくないタイミングがある場合は、`messageDetectedForDisplay` / `messageDismissed` リスナーを活用し、アプリ側で `screenView` 呼び出しを制御してください。

`screenName` または Activity が変わる呼び出しでは、表示中のポップアップが撤去されます（詳細は「[同一画面で再度 screenView を呼び出した場合](#同一画面で再度-screenview-を呼び出した場合)」を参照）。画面遷移で表示中のメッセージを消したくない場合や、そもそもメッセージを出したくないタイミングがある場合は、以下のように `screenView` の呼び出し自体を制御してください。

```kotlin
class YourApplication : Application() {
    private var isMessageDisplayed = false

    override fun onCreate() {
        super.onCreate()

        AimstarInAppMessaging.setListener(object : AimstarInAppMessagingListener {
            override fun messageDetectedForDisplay(message: InAppMessage) {
                isMessageDisplayed = true
            }
            override fun messageDismissed(message: InAppMessage) {
                isMessageDisplayed = false
            }
        })

        AimstarInAppMessaging.setup(context = applicationContext, apiKey = API_KEY, tenantId = TENANT_ID)
    }

    fun screenViewIfReady(activity: Activity, screenName: String) {
        if (!isMessageDisplayed) {
            AimstarInAppMessaging.screenView(activity = activity, screenName = screenName)
        }
    }
}
```

## 複数メッセージの処理

同一の `screenView` 呼び出しで複数のメッセージが取得された場合、表示ルールは以下のとおりです。

- **ポップアップモーダル**: 最初の1件のみ表示されます
- **ポップアップバナー**: 表示位置が重ならないよう、位置ごとに最初の1件のみ表示されます
- **埋め込みバナー**: `componentId` で指定した位置に対応するメッセージが表示されます

## 同一画面で再度 screenView を呼び出した場合

同じ `screenName`・同じ Activity で再度 `screenView` を呼び出した場合（コンテンツの更新、pull-to-refresh、画面復帰など）、表示中のポップアップは原則として維持されます。ポップアップバナーとポップアップモーダルで挙動が異なります。

**ポップアップバナー**

- **表示中のバナー**: そのまま表示され続けます。ユーザーが閉じた時点で、新しい取得結果に含まれる同じ位置のバナーが表示されます
- **同一シナリオの再取得**: 表示中のものと同じシナリオが再度取得された場合、二重に表示されることはありません
- **配信が止まった位置**: 新しい取得結果にその位置のバナーが含まれない場合、表示中のバナーは撤去されます

**ポップアップモーダル**

- **配信が止まった場合**: 新しい取得結果にモーダルが含まれない場合でも、表示中のモーダルは**撤去されません**。ユーザーが閉じるまで表示され続けます
- **新しいモーダルの取得**: 表示中のモーダルは撤去され、新しいモーダルに差し替わります。同一シナリオが再度取得された場合も表示し直されるため、モーダルが重なって表示されることはありません

**共通**

- **`screenName` または Activity が変わる呼び出し**: 表示中のポップアップはバナー・モーダルともすべて撤去されます

`screenName` は「同じ画面かどうか」の判定に使われます。**内容が変わる画面に同一の `screenName` を使い回すと、前の内容向けのバナーが次の内容の画面に残る場合があります。** 論理的な画面ごとに一意な `screenName` を指定してください。

## 埋め込みバナーの実装時の注意点

- `componentId` は AIMSTAR 管理画面で確認できる識別子です。定数として管理することを推奨します
- 存在しない `componentId` を指定した場合、バナーは表示されません（エラーにはなりません）
- 埋め込みバナーのコンテンツを更新したい場合は、再度 `screenView` を呼び出してください
- デフォルトではロード中にバナー領域が高さ 0 に折りたたまれます。ロード中に shimmer プレースホルダーを表示するには `showsLoadingPlaceholder` を `true` に設定してください（View: `app:showsLoadingPlaceholder` 属性または同名プロパティ、Compose: 同名パラメータ）

## トラブルシューティング

### メッセージが表示されない場合

以下の項目を順番に確認してください。

1. `setup()` が `Application.onCreate()` 内で正しく呼び出されているか
2. `screenView` を Main スレッドから呼び出しているか（Main スレッド以外から呼び出した場合、処理は行われず `messageError` に `AimstarException.Precondition` が通知されます）
3. `isStrictLogin = true` に設定している場合、`customerId` が設定されているか
4. `screenView` に渡している `screenName` が AIMSTAR 管理画面の設定と一致しているか
5. ネットワーク接続に問題がないか
6. `messageDetectedForDisplay` リスナーが呼び出されているか（呼び出されていない場合、表示対象のメッセージが存在しないか、条件を満たしていない可能性があります）

### エラーが発生する場合

`messageError` リスナーで受け取った `AimstarException` の内容を確認してください。

- `ClientError`: API Key または Tenant ID が正しく設定されているか確認してください
- `ServerError` / `NetworkError`: 時間をおいて再試行してください
- `Precondition`: SDK が対応していない値を受信した場合にもコールされます（`msg` プロパティに受信した値が含まれます）。この場合、対象のメッセージは表示されません。SDK を最新版へ更新してください

---

# SDK References

## AimstarInAppMessaging

```kotlin
object AimstarInAppMessaging
```

SDKのエントリーポイントです。`setup` メソッドを通じて初期化を行います。初期化が行われていない場合は、SDKの機能が利用できません。

### Properties

#### customerId: String?

```kotlin
var customerId: String? = null
```

ユーザーのIDを設定します。`null` が設定されている場合は、ログアウト状態として扱われます。

#### isStrictLogin: Boolean

```kotlin
var isStrictLogin: Boolean = false
```

このプロパティを `true` にすると、ユーザーのログイン・ログアウト状態を厳密に判定するようになります。初期値は `false` です。

### Functions

#### setup(context, apiKey, tenantId, apiHost?)

```kotlin
fun setup(context: Context, apiKey: String, tenantId: String, apiHost: String? = null)
```

SDKの初期化を行います。`apiHost` を省略または `null` にすると本番エンドポイントを使用します。`apiHost` を指定する場合は有効な `http` / `https` URL を渡してください。有効な URL でない値を渡した場合は例外を投げず、新しい構成は適用されません（後続の `screenView` で `messageError` に `AimstarException.Precondition` が通知されます）。

Main スレッドからの呼び出しを想定しています。Main スレッド以外から呼び出した場合は crash せず Main スレッドへ移送して適用されますが、メソッドの return 直後にはまだ構成が適用されていないことがあります。複数回呼び出した場合は、最後に呼んだ `setup` の構成が有効になります。

#### setListener(listener)

```kotlin
fun setListener(listener: AimstarInAppMessagingListener?)
```

イベント通知用リスナーを設定します。`null` を設定するとリスナーを解除できます。

#### screenView(activity, screenName, topAnchor?, bottomAnchor?, customBlock?)

```kotlin
fun screenView(
    activity: Activity,
    screenName: String,
    topAnchor: View? = null,
    bottomAnchor: View? = null,
    customBlock: CustomParameterBuilder.() -> Unit = {}
)
```

指定した screenName でメッセージを取得・表示します（View アンカー指定）。`topAnchor` はポップアップバナー（TOP）の上端基準となる View、`bottomAnchor` はポップアップバナー（BOTTOM）の下端基準となる View です。

`screenView` は必ず Main スレッドから呼び出してください（両オーバーロード共通）。Main スレッド以外から呼び出した場合、処理は行われず `messageError` に `AimstarException.Precondition` が通知されます。

#### screenView(activity, screenName, topOffsetPx, bottomOffsetPx, customBlock?)

```kotlin
fun screenView(
    activity: Activity,
    screenName: String,
    topOffsetPx: Int,
    bottomOffsetPx: Int,
    customBlock: CustomParameterBuilder.() -> Unit = {}
)
```

px オフセット指定でメッセージを取得・表示します（Compose 向け）。`topOffsetPx` はウィンドウ上端からの絶対距離、`bottomOffsetPx` はウィンドウ下端からの絶対距離です。

## AimstarInAppMessagingListener

```kotlin
interface AimstarInAppMessagingListener
```

このリスナーを実装したオブジェクトを `setListener` で設定することで、SDK側で発生したイベントをアプリ側に通知することができます。すべてのメソッドにデフォルト実装が提供されているため、必要なメソッドのみ実装できます。

### Functions

#### messageDismissed(message)

```kotlin
fun messageDismissed(message: InAppMessage)
```

ユーザー操作によってメッセージUIが閉じられた際にコールされます。以下のプログラム的な撤去ではコールされません。

- `screenName` または Activity が変わる `screenView` 呼び出しに伴う撤去
- 新しい取得結果に含まれない位置のポップアップバナーの撤去
- 新しいポップアップモーダルへの差し替えに伴う、表示中モーダルの撤去
- 画像ロード失敗で一度も可視状態にならなかった場合

また、埋め込みバナーは閉じる操作を持たないため、このコールバックの対象になりません。

#### messageClicked(message)

```kotlin
fun messageClicked(message: InAppMessage)
```

メッセージUIでユーザーによるポジティブなタップアクション（OKボタンのタップ）を行った際にコールされます。

#### messageDetectedForDisplay(message)

```kotlin
fun messageDetectedForDisplay(message: InAppMessage)
```

メッセージUIを表示すべき対象のメッセージが検出された際にコールされます。発火タイミングはメッセージ種別によって異なり、ポップアップバナーは表示直前（同一位置に複数のバナーが取得された場合、待機中のバナーは先行バナーが閉じられ表示順が来た時点）、ポップアップモーダルと埋め込みバナーは表示準備を開始した時点でコールされます。このコールバックが呼ばれた時点ではまだメッセージはユーザーに可視とは限りません。OPEN リアクションは、メッセージがユーザーに可視になった時点で別途サーバーに送信されます。

#### messageError(message, error)

```kotlin
fun messageError(message: InAppMessage?, error: AimstarException)
```

SDK内でメッセージの取得や表示の際にエラーが発生した場合にコールされます。`message` が `null` の場合はネットワーク通信などメッセージ特定前のエラーを示します。画像ロードに失敗し、タイトル・本文（ポップアップ系はボタンも）も無いために表示できるコンテンツが残らなかった場合も、本メソッドが `AimstarException.NetworkError` で通知します（テキストが表示できる場合は、そのテキストで表示を成立させるため通知しません）。この場合メッセージは表示されず、OPEN / CLOSE リアクションと `messageDismissed` は発火しません。

## IAMEmbeddedBannerView (View)

```kotlin
class IAMEmbeddedBannerView : FrameLayout
```

View環境（XML レイアウト）用の埋め込みバナーコンポーネントです。

| 属性 | 型 | デフォルト | 説明 |
| - | - | - | - |
| `app:componentId` | `String` | - | 表示対象の ComponentID。 |
| `app:showsLoadingPlaceholder` | `Boolean` | `false` | 取得・画像ロード中にプレースホルダー（shimmer）を表示するかどうか。`false` の場合は高さ 0 に折りたたまれ、表示可能になった時点でバナーが現れます。 |

`showsLoadingPlaceholder` は同名の Kotlin プロパティとしても公開されており、Main スレッドから実行時に変更できます（ロード中の変更は表示へ即時反映されます）。`componentId` は XML 属性でのみ指定できます。

## IAMEmbeddedBanner (Compose)

```kotlin
@Composable
fun IAMEmbeddedBanner(
    componentId: String,
    modifier: Modifier = Modifier,
    showsLoadingPlaceholder: Boolean = false,
)
```

Jetpack Compose 用の埋め込みバナーコンポーネントです。`componentId` を指定して利用します。

| パラメータ | 型 | デフォルト | 説明 |
| - | - | - | - |
| `componentId` | `String` | - | 表示対象の ComponentID。 |
| `modifier` | `Modifier` | `Modifier` | 画像は内部で高さを占有するため、`height(N.dp)` などで高さを確定させてください（未確定だと画像が高さ 0 になることがあります）。 |
| `showsLoadingPlaceholder` | `Boolean` | `false` | 取得・画像ロード中にプレースホルダー（shimmer）を表示するかどうか。`false` の場合は高さ 0 に折りたたまれ、表示可能になった時点でバナーが現れます。 |

## CustomParameterBuilder

`screenView()` の `customBlock` ラムダで使用するカスタムパラメータビルダーです。

| メソッド | 説明 |
| - | - |
| `putString(key, value)` | 文字列パラメータを追加する。 |
| `putInteger(key, value)` | 整数パラメータを追加する。 |
| `putDouble(key, value)` | 浮動小数点パラメータを追加する。 |
| `putBoolean(key, value)` | 真偽値パラメータを追加する。 |
| `putStringList(key, value)` | 文字列リストパラメータを追加する。 |
| `putIntegerList(key, value)` | 整数リストパラメータを追加する。 |
| `putDoubleList(key, value)` | 浮動小数点リストパラメータを追加する。 |
| `putBooleanList(key, value)` | 真偽値リストパラメータを追加する。 |

## AimstarException

エラー種別を表す sealed class です。

```kotlin
sealed class AimstarException : Exception()
```

| サブクラス | 説明 |
| - | - |
| `Precondition(msg)` | 内部不整合などの前提条件エラー。リトライ不可。 |
| `ClientError(httpStatusCode)` | 4xx レスポンスに起因するクライアントエラー。リトライ不可。 |
| `ServerError(httpStatusCode)` | 5xx レスポンスに起因するサーバーエラー。リトライ可能。 |
| `NetworkError(exception)` | ネットワーク通信または画像デコードのエラー。リトライ可能。 |

いずれのサブクラスも `Exception` 標準の `message` / `cause` は設定されません。エラー内容は各サブクラスのプロパティ（`Precondition.msg`、`ClientError.httpStatusCode`、`ServerError.httpStatusCode`、`NetworkError.exception`）から取得してください。

## InAppMessage

```kotlin
sealed class InAppMessage
```

SDKが取り扱うメッセージの型です。リスナーメソッドの引数として渡されます。

| サブクラス | 説明 |
| - | - |
| `PopupModalMessage` | ポップアップモーダルのメッセージ。 |
| `PopupBannerMessage` | ポップアップバナーのメッセージ。 |
| `EmbeddedBannerMessage` | 埋め込みバナーのメッセージ。 |
| `Unsupported(rawType: String)` | SDK が対応していない値（未知の `action_type` や表示位置）を受信した際のフォールバック。将来のバックエンド拡張に対する前方互換ケースで、配信全体を失わせないために用意されています。**表示は行われず、`messageError` に `AimstarException.Precondition` として通知されます。** `rawType` には受信した生の値が入ります。 |

### PopupModalMessage

リスナー経由で受け取った後は内部表示用としてのみ使われます。公開プロパティはありません。

### PopupBannerMessage

リスナー経由で受け取った後は内部表示用としてのみ使われます。公開プロパティはありません。

### EmbeddedBannerMessage

| プロパティ | 型 | 説明 |
| - | - | - |
| `componentId` | `String` | この埋め込みバナーに対応する ComponentID。 |
