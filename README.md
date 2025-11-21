# Aimstar Messaging SDK

## Requirements

MinSDK - 23

CompileSDK - 36

TargetSDK - 36

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

### 1. SDKをアプリに追加する

このリポジトリの `app/libs/AimstarMessagingSDK.aar` をダウンロードし、aar ファイルをプロジェクトに含めて実装を進めてください。

### 2. SDKのイベントリスナーと初期化コードを追加する

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

### 3. Customer IDの設定

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

### 4. isStrictLoginフラグの設定

ユーザーのログイン・ログアウト状態を厳密に判定する場合は `true` を設定していただく必要があります。初期値は `false` です。

```kotlin
AimstarInAppMessaging.isStrictLogin = true
```

### 5. ページ閲覧イベントの送出

スクリーン名を設定して `fetch` メソッドを実行します。

```kotlin
val screenName = "スクリーン名"
AimstarInAppMessaging.fetch(activity = this@MainActivity, screenName = screenName)
```

---

# 利用ガイド

## 基本的な使い方

### 実装の流れ

1. **アプリ起動時にSDKを初期化**
   - `Application` クラスの `onCreate()` 内で `setup(context:apiKey:tenantId:)` を呼び出します
   - 必要に応じて `listener` を設定してイベントを受け取ります

2. **ユーザー識別情報の設定**
   - ユーザーがログインした際に `customerId` を設定します
   - ログアウト時には `customerId` を `null` に設定します

3. **ページ閲覧イベントの送信**
   - 各画面の `onCreate()` など、適切なタイミングで `fetch(activity:screenName:)` を呼び出します
   - ScreenNameは画面ごとに一意の識別子を設定してください

### 実装例

```kotlin
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ページ閲覧イベントを送信
        AimstarInAppMessaging.fetch(activity = this, screenName = "home")
    }
    
    private fun onLoginSuccess(userId: String) {
        // ログイン処理...
        
        // ログイン成功後にCustomer IDを設定
        AimstarInAppMessaging.customerId = userId
    }
    
    private fun onLogout() {
        // ログアウト処理...
        
        // ログアウト時にCustomer IDをクリア
        AimstarInAppMessaging.customerId = null
    }
}
```

## ポップアップの仕様

### 表示タイミング

ポップアップは以下の条件を満たした場合に自動的に表示されます：

1. **ページ閲覧イベントの送信後**
   - `fetch(screenName:)` メソッドを呼び出した後、サーバーからメッセージが取得できた場合
   - ユーザーが表示対象として判定された場合

2. **表示判定のタイミング**
   - `messageDetectedForDisplay(message:)` リスナーメソッドが呼び出された直後
   - このメソッドが呼ばれた時点で、ポップアップが表示されることが確定します

### 表示方法

- **新しいWindowを生成して表示**
  - SDKはアプリの既存のWindowとは別に、専用のWindowを生成します
  - このWindowは最前面に表示され、アプリの他のUIの上に重なって表示されます
  - ポップアップが表示されている間、ユーザーはアプリの他の部分と操作できません

### UI仕様

#### レイアウト

- **モーダル表示**
  - 画面全体を覆う半透明の背景（オーバーレイ）が表示されます
  - 中央にポップアップダイアログが表示されます

#### コンポーネント

- **タイトル**
  - メッセージのタイトルが表示されます（設定されている場合）

- **画像**
  - メッセージの画像が表示されます（設定されている場合）

- **本文**
  - メッセージの本文テキストが表示されます

- **ボタン**
  - コンバージョンボタン（OKボタンなど）が表示されます
  - ボタンのテキストやスタイルはサーバー側で設定された内容が反映されます

- **閉じるボタン**
  - ポップアップを閉じるためのボタンが表示されます（通常は×ボタンなど）

#### スタイリング

- ポップアップのデザイン（色、フォント、サイズなど）はサーバー側で設定された内容が反映されます
- アプリ側でカスタマイズすることはできません

### 動作仕様

#### 表示の制御

- **表示の抑制機能**
  - 現時点では、ポップアップの表示を抑制する機能は提供されていません
  - ポップアップが表示される可能性があるタイミングでは、`fetch(activity:screenName:)` の呼び出しを制御してください

#### ユーザー操作

- **ボタンタップ**
  - コンバージョンボタンをタップすると、`messageClicked(message:)` リスナーメソッドが呼び出されます
  - その後、ポップアップが自動的に閉じられます

- **閉じる操作**
  - 閉じるボタンをタップ、または背景をタップすることでポップアップを閉じることができます
  - ポップアップが閉じられると、`messageDismissed(message:)` リスナーメソッドが呼び出されます

#### 複数メッセージの処理

- 同一の `fetch(activity:screenName:)` 呼び出しで複数のメッセージが取得された場合、最初の1件のみが表示されます
- 複数のメッセージを順次表示する機能は現時点では提供されていません

### イベントの送信

SDKは以下のタイミングでイベントをサーバーに送信します：

1. **ページ閲覧イベント**
   - `fetch(activity:screenName:)` が呼び出された時点で送信されます

2. **ポップアップ表示イベント**
   - ポップアップが実際に表示された時点で送信されます

3. **ボタンタップイベント**
   - コンバージョンボタンがタップされた時点で送信されます

4. **ポップアップ非表示イベント**
   - ポップアップが閉じられた時点で送信されます

## ポップアップバナーの仕様

### 概要

ポップアップバナーは、ポップアップと同様に `fetch(screenName:)` メソッドの呼び出しに応じて、APIからのレスポンス内容に基づいて自動的に表示されるバナーコンポーネントです。ポップアップとは異なり、画面全体を覆うモーダルではなく、画面の特定の位置にフローティング表示されます。

### 表示タイミング

ポップアップバナーは以下の条件を満たした場合に自動的に表示されます：

1. **ページ閲覧イベントの送信後**
   - `fetch(activity:screenName:)` メソッドを呼び出した後、サーバーからバナーメッセージが取得できた場合
   - ユーザーが表示対象として判定された場合
   - 管理画面でバナーの表示が設定されている場合

2. **表示判定のタイミング**
   - `messageDetectedForDisplay(message:)` リスナーメソッドが呼び出された直後
   - このメソッドが呼ばれた時点で、バナーが表示されることが確定します

### 表示方法

- **フローティング表示**
  - SDKはアプリの既存のWindow上に、バナーをフローティング表示します
  - 管理画面で指定した位置（上下・左右の組み合わせ）に表示されます
  - 画面をスクロールしても、指定された位置に固定されて表示されます

### UI仕様

#### レイアウト

- **位置指定**
  - 管理画面で以下の組み合わせから位置を指定できます：
    - **上下**: 上部 / 中央 / 下部
    - **左右**: 左 / 中央 / 右
  - 例: 上部中央、下部右など

- **固定表示**
  - 画面をスクロールしても、指定された位置に固定されて表示されます
  - アプリの他のUI要素の上に重なって表示されます

#### コンポーネント

- **メッセージ内容**
  - 画像、タイトルなど、管理画面で設定された内容が表示されます

- **閉じるボタン**
  - バナーを閉じるためのボタンが表示されます
  - ボタンをタップすることで、バナーを非表示にできます

#### スタイリング

- バナーのデザイン（色、フォント、サイズ、角丸など）はサーバー側で設定された内容が反映されます
- アプリ側でカスタマイズすることはできません

### 動作仕様

#### ユーザー操作

- **閉じる操作**
  - 閉じるボタンをタップすることでバナーを非表示にできます
  - バナーが閉じられると、`messageDismissed(message:)` リスナーメソッドが呼び出されます

- **コンテンツタップ**
  - バナーのコンテンツ部分をタップした場合の動作は、管理画面で設定された内容に従います
  - コンバージョンアクションが設定されている場合、`messageClicked(message:)` リスナーメソッドが呼び出されます

#### 表示の制御

- **表示の抑制機能**
  - 現時点では、ポップアップバナーの表示を抑制する機能は提供されていません
  - バナーが表示される可能性があるタイミングでは、`fetch(activity:screenName:)` の呼び出しを制御してください

#### 複数メッセージの処理

- 同一の `fetch(activity:screenName:)` 呼び出しで複数のバナーメッセージが取得された場合、表示位置が重ならないように、表示位置ごとに最初の1件のみが表示されます

### イベントの送信

SDKは以下のタイミングでイベントをサーバーに送信します：

1. **ページ閲覧イベント**
   - `fetch(activity:screenName:)` が呼び出された時点で送信されます

2. **バナー表示イベント**
   - バナーが実際に表示された時点で送信されます

3. **コンテンツタップイベント**
   - バナーのコンテンツがタップされた時点で送信されます（設定されている場合）

4. **バナー非表示イベント**
   - バナーが閉じられた時点で送信されます

## 埋め込みバナーの仕様

### 概要

埋め込みバナーは、SDK側で作成されているコンポーネントを、アプリの任意のUI内に埋め込んで使用するバナーコンポーネントです。ポップアップやポップアップバナーとは異なり、`fetch(activity:screenName:)` による自動表示ではなく、開発者が明示的にUIに配置します。

### 使用方法

#### 基本的な実装

埋め込みバナーを使用するには、`componentId` をパラメータとして指定する必要があります。

XMLレイアウトを用いた場合の実装例

```kotlin
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // コンテンツを読み込むためにfetchを呼び出す
        AimstarInAppMessaging.fetch(activity = this, screenName = "your_screen_name")
    }
}
```

対応するXMLレイアウトファイル（`activity_main.xml`）の例：

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <jp.co.aimstar.messaging.android.components.EmbeddedBannerView
        android:id="@+id/embedded_banner"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:componentId="your_component_id" />

    <!-- その他のUI要素 -->
</LinearLayout>
```

Compose での実装例

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YourAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EmbeddedBanner(
                        componentId = componentId,
                        modifier = ...
                    )
                }
            }
        }
    }
}
```

### パラメータ

#### componentId（必須）

- **型**: `String`
- **説明**: 管理画面で設定されたバナーコンポーネントの識別子
- **注意**: 存在しない `componentId` を指定した場合、バナー内のコンテンツは表示されません

### UI仕様

#### レイアウト

- **埋め込み表示**
  - アプリの既存のUI要素として、指定した位置に配置されます
  - XMLレイアウトやプログラム的なレイアウトに対応しています
  - 親ビューのサイズや制約に従って表示されます

#### コンポーネント

- **メッセージ内容**
  - タイトル、本文、画像など、管理画面で設定された内容が表示されます
  - `componentId` に対応するコンテンツが自動的に読み込まれます

- **インタラクティブ要素**
  - ボタンやリンクなど、管理画面で設定されたインタラクティブ要素が表示されます
  - タップ時の動作は管理画面で設定された内容に従います

#### スタイリング

- バナーのデザイン（色、フォント、サイズなど）は管理画面で設定された内容が反映されます
- アプリ側でサイズや位置は制御できますが、デザインのカスタマイズはできません

### 動作仕様

#### データの取得

- **明示的なfetch呼び出し**
  - 埋め込みバナーのコンテンツを読み込むには、クライアント側で明示的に `fetch(activity:screenName:)` メソッドを呼び出す必要があります
  - `componentId` を設定したバナービューに対して、`fetch` メソッドを呼び出すことで、サーバーからコンテンツを取得します
  - 取得に失敗した場合、バナーは表示されないか、エラー状態になります
  - コンテンツの更新が必要な場合も、再度 `fetch(activity:screenName:)` メソッドを呼び出すことで最新のコンテンツを取得できます

#### ユーザー操作

- **コンテンツタップ**
  - バナーのコンテンツ部分をタップした場合の動作は、管理画面で設定された内容に従います
  - コンバージョンアクションが設定されている場合、`messageClicked(message:)` リスナーメソッドが呼び出されます

#### ライフサイクル

- **表示タイミング**
  - バナーコンポーネントがビュー階層に追加された時点で表示されます
  - 親ビューが非表示になった場合、バナーも非表示になります

- **更新**
  - サーバー側でコンテンツが更新された場合、再度 `fetch(activity:screenName:)` メソッドを呼び出すことで新しいコンテンツを取得できます
  - コンテンツの更新は自動的には行われないため、必要に応じてクライアント側で `fetch` を呼び出す必要があります

### イベントの送信

SDKは以下のタイミングでイベントをサーバーに送信します：

1. **バナー表示イベント**
   - バナーが実際に表示された時点で送信されます

2. **コンテンツタップイベント**
   - バナーのコンテンツがタップされた時点で送信されます（設定されている場合）

### 実装時の注意点

- **componentIdの管理**
  - `componentId` は管理画面で確認できる識別子です
  - ハードコードする場合は、定数として管理することを推奨します
  - 複数の画面で同じバナーを表示する場合は、共通のコンポーネントとして再利用できます

- **エラーハンドリング**
  - 存在しない `componentId` を指定した場合や、ネットワークエラーが発生した場合の処理を実装してください
  - `messageError(message:error:)` リスナーメソッドでエラーを検知できます

- **レイアウトの考慮**
  - 埋め込みバナーはアプリのUIの一部として配置されるため、レイアウトを適切に設計してください
  - バナーのサイズは管理画面で設定された内容に依存するため、十分なスペースを確保してください

## イベントハンドリング

### リスナーメソッドの実装

SDKのイベントを適切に処理するために、`AimstarInAppMessagingListener` を実装してください。

```kotlin
import android.util.Log
import jp.co.aimstar.messaging.android.AimstarInAppMessaging
import jp.co.aimstar.messaging.android.AimstarInAppMessagingListener
import jp.co.aimstar.messaging.android.data.http.AimstarException
import jp.co.aimstar.messaging.android.data.model.InAppMessage

class YourApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // リスナーの設定
        AimstarInAppMessaging.listener = object : AimstarInAppMessagingListener {
            override fun messageDetectedForDisplay(message: InAppMessage) {
                // ポップアップが表示される前に実行される処理
                // 例: アプリの状態を保存、アニメーションを一時停止など
                Log.d("AimstarMessaging", "メッセージが検出されました: $message")
            }
            
            override fun messageClicked(message: InAppMessage) {
                // コンバージョンボタンがタップされた時の処理
                // 例: 特定の画面に遷移、アナリティクスに送信など
                Log.d("AimstarMessaging", "メッセージがクリックされました: $message")
            }
            
            override fun messageDismissed(message: InAppMessage) {
                // ポップアップが閉じられた時の処理
                // 例: アプリの状態を復元、次の処理を実行など
                Log.d("AimstarMessaging", "メッセージが閉じられました: $message")
            }
            
            override fun messageError(message: InAppMessage?, error: AimstarException) {
                // エラーが発生した時の処理
                // 例: エラーログの記録、ユーザーへの通知など
                Log.e("AimstarMessaging", "エラーが発生しました: $error", error)
            }
        }
    }
}
```

### 実装時の注意点

- **排他制御**
  - `fetch(activity:screenName:)` を呼び出すと、ポップアップが表示される可能性があります
  - 既にポップアップが表示されている場合は、新しい `fetch(activity:screenName:)` の呼び出しを避けてください
  - `messageDetectedForDisplay(message:)` や `messageDismissed(message:)` を活用して、ポップアップの表示状態を管理してください

- **エラーハンドリング**
  - `messageError(message:error:)` でエラーを適切に処理してください
  - ネットワークエラーやサーバーエラーが発生した場合でも、アプリの動作に影響を与えないようにしてください

## ユーザー状態の管理

### Customer IDの設定

- **ログイン時**

```kotlin
AimstarInAppMessaging.customerId = "user_unique_id"
```

- **ログアウト時**

```kotlin
AimstarInAppMessaging.customerId = null
```

### isStrictLoginフラグ

- **通常モード（デフォルト）**
  - `isStrictLogin = false`（初期値）
  - Customer IDが設定されていない場合でも、一部の機能が動作する場合があります

- **厳密モード**
  - `isStrictLogin = true` に設定
  - Customer IDが設定されていない場合は、メッセージの取得や表示が制限されます
  - ログイン必須のアプリで使用することを推奨します

```kotlin
// アプリ起動時に設定
AimstarInAppMessaging.isStrictLogin = true
```

## トラブルシューティング

### ポップアップが表示されない場合

1. **SDKの初期化を確認**
   - `setup(context:apiKey:tenantId:)` が正しく呼び出されているか確認してください

2. **Customer IDの設定を確認**
   - `isStrictLogin = true` の場合、Customer IDが設定されている必要があります

3. **ScreenNameの確認**
   - `fetch(activity:screenName:)` に渡しているScreenNameが、サーバー側で設定されているものと一致しているか確認してください

4. **ネットワーク接続を確認**
   - インターネット接続が正常か確認してください
   - `messageError(message:error:)` でエラーが発生していないか確認してください

5. **リスナーの設定を確認**
   - `listener` が正しく設定されているか確認してください
   - `messageDetectedForDisplay(message:)` が呼び出されているか確認してください

### エラーが発生する場合

- **エラーの種類を確認**
  - `messageError(message:error:)` で受け取った `AimstarException` の内容を確認してください

- **API KeyとTenant IDの確認**
  - 正しいAPI KeyとTenant IDが設定されているか確認してください

- **ログの確認**
  - リスナーメソッド内でログを出力し、SDKの動作を確認してください

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
