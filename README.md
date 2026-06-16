# AIMSTAR In-App Messaging Android SDK

AIMSTAR で設定されたアプリ内メッセージを Android アプリ上で表示するための SDK です。

## Requirements

| 項目 | バージョン |
| - | - |
| minSdk | 26 以上 |
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

        // イベントリスナーを設定します（任意）
        AimstarInAppMessaging.setListener(object : AimstarInAppMessagingListener {
            override fun messageDismissed(message: InAppMessage) {
                // メッセージが非表示になったタイミングで実行
            }
            override fun messageClicked(message: InAppMessage) {
                // メッセージ内のボタンがタップされたタイミングで実行
            }
            override fun messageDetectedForDisplay(message: InAppMessage) {
                // 表示対象のメッセージが見つかった際に実行。この後メッセージが表示される
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

**View (XMLレイアウト):**

```xml
<jp.co.aimstar.messaging.android.ui.view.IAMEmbeddedBannerView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:componentId="your_component_id" />
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
                .height(100.dp)
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

以下はポップアップ表示中に新たな `screenView` を呼び出さないようにするパターンの例です。

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

## 埋め込みバナーの実装時の注意点

- `componentId` は AIMSTAR 管理画面で確認できる識別子です。定数として管理することを推奨します
- 存在しない `componentId` を指定した場合、バナーは表示されません（エラーにはなりません）
- 埋め込みバナーのコンテンツを更新したい場合は、再度 `screenView` を呼び出してください

## トラブルシューティング

### メッセージが表示されない場合

以下の項目を順番に確認してください。

1. `setup()` が `Application.onCreate()` 内で正しく呼び出されているか
2. `isStrictLogin = true` に設定している場合、`customerId` が設定されているか
3. `screenView` に渡している `screenName` が AIMSTAR 管理画面の設定と一致しているか
4. ネットワーク接続に問題がないか
5. `messageDetectedForDisplay` リスナーが呼び出されているか（呼び出されていない場合、表示対象のメッセージが存在しないか、条件を満たしていない可能性があります）

### エラーが発生する場合

`messageError` リスナーで受け取った `AimstarException` の内容を確認してください。

- `ClientError`: API Key または Tenant ID が正しく設定されているか確認してください
- `ServerError` / `NetworkError`: 時間をおいて再試行してください

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

SDKの初期化を行います。`apiHost` を省略または `null` にすると本番エンドポイントを使用します。`apiHost` を指定する場合は `https://` または `http://` で始まる URL を渡してください。スキームなしの文字列を渡した場合は `IllegalArgumentException` がスローされます。

#### setListener(listener)

```kotlin
fun setListener(listener: AimstarInAppMessagingListener)
```

イベント通知用リスナーを設定します。

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

ユーザー操作によってメッセージUIが閉じられた際にコールされます。次の `screenView` 呼び出しによるプログラム的な撤去や、画像ロード失敗で一度も可視状態にならなかった場合はコールされません。

#### messageClicked(message)

```kotlin
fun messageClicked(message: InAppMessage)
```

メッセージUIでユーザーによるポジティブなタップアクション（OKボタンのタップ）を行った際にコールされます。

#### messageDetectedForDisplay(message)

```kotlin
fun messageDetectedForDisplay(message: InAppMessage)
```

メッセージUIを表示すべき対象のメッセージが取得された際にコールされます。このコールバックが呼ばれた時点で OPEN リアクションがサーバーに送信されます（メッセージがユーザーに可視になった時点）。

#### messageError(message, error)

```kotlin
fun messageError(message: InAppMessage?, error: AimstarException)
```

SDK内でメッセージの取得や表示の際にエラーが発生した場合にコールされます。`message` が `null` の場合はネットワーク通信などメッセージ特定前のエラーを示します。

## IAMEmbeddedBannerView (View)

```kotlin
class IAMEmbeddedBannerView : FrameLayout
```

View環境（XML レイアウト）用の埋め込みバナーコンポーネントです。`app:componentId` 属性で ComponentID を指定します。

## IAMEmbeddedBanner (Compose)

```kotlin
@Composable
fun IAMEmbeddedBanner(componentId: String, modifier: Modifier = Modifier)
```

Jetpack Compose 用の埋め込みバナーコンポーネントです。`componentId` を指定して利用します。

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
| `NetworkError(exception)` | ネットワーク通信エラー。リトライ可能。 |

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
| `Unsupported(rawType: String)` | 未知の `action_type` を受信した際のフォールバック。将来のバックエンド拡張に対する前方互換ケースです。`messageError` 経由ではなく `messageDetectedForDisplay` などで通常通り渡されます。`rawType` には受信した生の type 文字列が入ります。 |

### PopupModalMessage

リスナー経由で受け取った後は内部表示用としてのみ使われます。公開プロパティはありません。

### PopupBannerMessage

| プロパティ | 型 | 説明 |
| - | - | - |
| `margin` | `Margin` | バナーの外側余白。デフォルトは `Margin()`（全方向 0）。 |

### EmbeddedBannerMessage

| プロパティ | 型 | 説明 |
| - | - | - |
| `componentId` | `String` | この埋め込みバナーに対応する ComponentID。 |
