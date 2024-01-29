# Aimstar Messaging SDK
## Requirements

MinSDK - 23

CompileSDK - 33

TargetSDK - 33

## 制限事項

- 後述する `5.ページ閲覧イベントの送出` 実行後にポップアップが表示される可能性がありますが、現時点では表示を抑制する機能が存在しないため、ポップアップ表示時には実行しないなどの排他制御を行なっていただく必要がございます

## SDKで提供する機能

- アプリのページ閲覧イベントを送信する
- 対象と判定されたユーザーに、ポップアップを表示する
- ポップアップ表示、ユーザー操作による非表示、コンバージョンボタンのタップの各イベントを送信する

## 用語

| 用語 | 説明 |
| --- | --- |
| API Key | AimstarMessagingSDK を利用するために必要な API キーで、Aimstar 側で事前にアプリ開発者に発行されます。 |
| Tenant ID | AimstarMessagingSDK を利用するために必要なテナント ID で、Aimstar 側で事前にアプリ開発者に発行されます。 |
| Customer ID | アプリ開発者がユーザーを識別する ID で、アプリ開発者が独自に発行、生成、または利用します。 |
| ScreenName | アプリ側で設定するトリガーの一種（アプリ開発者が任意に設定する識別名）で、ユーザーが特定の画面を表示したり、またはアクションを行うなどの条件を満たした場合に、識別名を使ってメッセージを呼び出すために利用されます。 |

## 導入手順

### 1.SDKをアプリに追加する

このリポジトリの `app/libs/AimstarMessagingSDK.aar` をダウンロードし、aar ファイルをプロジェクトに含めて実装を進めてください。

### 2.SDKのイベントリスナーと初期化コードを追加する

`onCreate` 内にイベントハンドラと初期化コードを追加します。

```kotlin
class YourApplication : Application() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // リスナーの設定を行う
        AimstarInAppMessaging.listener = object : AimstarInAppMessagingListener {
            override fun messageDismissed(message: InAppMessage) {
                // ポップアップが非表示になったタイミングで実行
            }
            override fun messageClicked(message: InAppMessage) {
                // ポップアップ内のボタンがタップされたタイミングで実行
            }
            override fun messageDetectedForDisplay(message: InAppMessage) {
                // 表示対象のメッセージが見つかった際に実行。この後ポップアップが表示される
            }
            override fun messageError(message: InAppMessage?, error: AimstarException) {
                // SDK内でエラーが発生した際に実行
            }
        }

        // SDKの初期化を行う
        val API_KEY = "発行されたAPI KEY"
        val TENANT_ID = "発行されたテナントID"
        AimstarInAppMessaging.setup(context = applicationContext, apiKey = API_KEY, tenantId = TENANT_ID)
    }
}

```

### 3.Customer IDの設定

SDKの初期化後に必要に応じてCustomer IDを設定してください。

```kotlin
// アプリにユーザーがログインした後など、ユーザーが識別できるようになった後に実行
AimstarInAppMessaging.customerId = "ユーザーを識別するID"

```

### ユーザーのログイン・ログアウト状態の判定

`AimstarInAppMessaging.customerId` にユーザーIDが設定されているかどうかで状態が判定されます。従って、ユーザーがログアウトを実行した際には `null` を設定いただく必要がございます。

```kotlin
// アプリからユーザーがログアウト後に実行
AimstarInAppMessaging.customerId = null

```

### 4.isStrictLoginフラグの設定

ユーザーのログイン・ログアウト状態を厳密に判定する場合は `true` を設定していただく必要があります。初期値は `false` です。

```kotlin
AimstarInAppMessaging.isStrictLogin = true

```

### 5.ページ閲覧イベントの送出

スクリーン名を設定して `fetch` メソッドを実行します。

```kotlin
val screenName = "スクリーン名"
AimstarInAppMessaging.fetch(activity = this@MainActivity, screenName = screenName)

```

---

# SDK References

## AimstarInAppMessaging

```kotlin
object AimstarInAppMessaging

```

SDKのエントリーポイントです。

setupメソッドを通じて初期化を行います。初期化が行われていない場合は、SDKの機能が利用できません。

### Properties

### listener: AimstarInAppMessagingListener? (任意)

```kotlin
var listener: AimstarInAppMessagingListener? = null

```

SDK側で発生したイベントをアプリ側に通知するためのリスナーオブジェクトです。イベントをアプリ側で補足したい場合に使います。

### isStrictLogin: Boolean

```kotlin
var isStrictLogin = false

```

このメンバを`true`にすると、ユーザーのログイン・ログアウト状態を厳密に判定するようになります。

### customerId: String?

```kotlin
var customerId: String? = null

```

ユーザーのIDを設定します。

nullが設定されている場合は、ログアウト状態として扱われます。

### Functions

### setup(context: Context, apiKey: String, tenantId: String)

```kotlin
fun setup(context: Context, apiKey: String, tenantId: String)

```

SDKの初期化を行います。

### fetch(activity, screenName)

```kotlin
fun fetch(activity: Activity, screenName: String)

```

任意のタイミングでこのメソッドを呼び出すと、SDKが指定されたscreenNameでメッセージを取得します。メッセージが取得できた場合、指定されたactivityが表示されているウインドウ上にメッセージUIが表示されます。

## AimstarInAppMessagingListener

interfaceです。このリスナーを実装したオブジェクトを作成し、以下の4つのメソッドをオーバーライドすることで利用します。

### Functions

### messageDismissed(InAppMessage)

```kotlin
fun messageDismissed(message: InAppMessage)

```

メッセージUIが表示された後、閉じられる際にコールされます。

### messageClicked(InAppMessage)

```kotlin
fun messageClicked(message: InAppMessage)

```

メッセージUIでユーザーによるポジティブなタップアクション（OKボタンのタップ）を行った際にコールされます。

### messageDetectedForDisplay(InAppMessage)

```kotlin
fun messageDetectedForDisplay(message: InAppMessage)

```

メッセージUIを表示すべき対象のメッセージが取得された際にコールされます。

### messageError(InAppMessage?, AimstarException)

```kotlin
fun messageError(message: InAppMessage?, error: AimstarException)

```

SDK内でメッセージの取得や表示の際にエラーが発生した場合にコールされます。