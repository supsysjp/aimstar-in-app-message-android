# AIMSTAR In-App Messaging Android SDK

AIMSTAR で設定されたアプリ内メッセージを Android アプリ上で表示するための SDK です。

## Requirements

| 項目 | バージョン |
| - | - |
| minSdk | 28 以上 |
| compileSdk / targetSdk | 36 |
| Java | 17 |
| Kotlin | 2.2 以上（SDK は 2.3.21 でビルドしており、AAR には Kotlin 2.3 のメタデータが含まれます） |

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

    // SDK が内部で使用するライブラリ（必須）
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    // Jetpack Compose 版の埋め込みバナー（IAMEmbeddedBanner）を使用する場合に必須
    implementation(platform("androidx.compose:compose-bom:2026.05.01"))
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
}
```

AAR を `fileTree` で取り込む方式では SDK の依存ライブラリが自動では解決されないため、上記をアプリ側で宣言する必要があります。バージョンは記載値以上を指定してください。

**依存が不足していてもビルドは成功します。** 問題は対象のメッセージを表示する時点で初めて顕在化し、症状はライブラリによって異なります（例: Coroutines が無いと初期化時にクラッシュ、ConstraintLayout が無いとポップアップモーダルが何も表示されずエラーも通知されない）。既にアプリ側で宣言済みのライブラリがある場合、重複して宣言しても問題ありません。

本リポジトリの `app/` は、この手順どおりに SDK を組み込んだ動作するサンプルアプリです。実際の依存の宣言例は `app/build.gradle.kts` を参照してください。

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

#### 1 つの Activity 内で画面を切り替える場合（Fragment / Navigation / Compose）

Fragment や Navigation Compose などで 1 つの Activity の中に複数の画面を持つ場合、画面が入れ替わっても Activity は変わりません。同じ `screenName` の画面に新しく入り直したときに新しい表示サイクルとして扱われるよう、`owner` に画面インスタンスを識別するオブジェクトを渡してください。`owner` は画面が同じインスタンスかどうかの判定だけに使われ、SDK は弱参照で保持します。省略した場合は `activity` が使われ、Activity 単位で判定されます。詳細は「[表示サイクルと owner](#表示サイクルと-owner)」を参照してください。

```kotlin
// Fragment: Fragment 自身を渡す
override fun onResume() {
    super.onResume()
    AimstarInAppMessaging.screenView(
        activity = requireActivity(),
        screenName = "Product Detail",
        owner = this
    )
}
```

```kotlin
// Navigation Compose: NavBackStackEntry を渡す
composable("product/{id}") { backStackEntry ->
    val activity = LocalContext.current as Activity
    // ポップアップバナーの上下端オフセット（px）。ここではアプリバーの高さ 56dp とナビゲーションバーの高さ 80dp を例にしている
    val topBarHeightPx = with(LocalDensity.current) { 56.dp.roundToPx() }
    val bottomBarHeightPx = with(LocalDensity.current) { 80.dp.roundToPx() }
    LaunchedEffect(backStackEntry) {
        AimstarInAppMessaging.screenView(
            activity = activity,
            screenName = "Product Detail",
            topOffsetPx = topBarHeightPx,
            bottomOffsetPx = bottomBarHeightPx,
            owner = backStackEntry
        )
    }
}
```

```kotlin
// Compose（Navigation を使わない場合）: 画面ごとに remember したオブジェクトを渡す
@Composable
fun ProductDetailScreen() {
    val activity = LocalContext.current as Activity
    val topBarHeightPx = with(LocalDensity.current) { 56.dp.roundToPx() }
    val bottomBarHeightPx = with(LocalDensity.current) { 80.dp.roundToPx() }
    val screenOwner = remember { Any() }
    LaunchedEffect(screenOwner) {
        AimstarInAppMessaging.screenView(
            activity = activity,
            screenName = "Product Detail",
            topOffsetPx = topBarHeightPx,
            bottomOffsetPx = bottomBarHeightPx,
            owner = screenOwner
        )
    }
}
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

### 7. 商品コンテキストの送信

`screenView` には、表示中の商品や商品グループを「商品コンテキスト」（`productId` / `productGroups`）として渡せます。AIMSTAR 管理画面で商品軸レコメンド（閲覧中の商品を起点に商品を推薦する配信）を設定している場合、この値が起点の決定に使われます。

`screenView` を呼ぶと画面表示のリクエストが送られ、その後メッセージの表示・ユーザー操作による非表示・コンバージョンボタンのタップが起きるたびにイベントが送られます。前項のカスタムパラメータ（`customBlock`）とは、どちらに含まれるかが異なります。

- **カスタムパラメータ**: `screenView` のリクエストにのみ含まれます
- **商品コンテキスト**: `screenView` のリクエストに加え、次に `screenView` を呼ぶまでに発生するイベントにも含まれます

起点となる商品は、渡した商品コンテキストと管理画面のコンテンツ設定（どの枠名を使うか）の組み合わせで決まります。カスタムパラメータに同じ値を入れても起点の決定には使われません。

#### 商品詳細画面: productId

表示中の商品が1つに定まる画面（商品詳細画面など）では `productId` を渡してください。

**View:**

```kotlin
AimstarInAppMessaging.screenView(
    activity = this,
    screenName = "Product Detail",
    productId = product.id
)
```

**Jetpack Compose:**

```kotlin
AimstarInAppMessaging.screenView(
    activity = this,
    screenName = "Product Detail",
    topOffsetPx = topBarHeightPx,
    bottomOffsetPx = bottomBarHeightPx,
    productId = product.id
)
```

#### カテゴリ・一覧画面: productGroups

複数の商品グループを同時に表示する画面（カテゴリ・一覧画面など）では `productGroups` を渡してください。キーは AIMSTAR 管理画面のコンテンツ設定で指定した枠名（`product_group_id_01`〜`product_group_id_10`）、値はその枠に表示しているグループの値です。

**View:**

```kotlin
AimstarInAppMessaging.screenView(
    activity = this,
    screenName = "Category List",
    productGroups = mapOf(
        "product_group_id_01" to category.id,
        "product_group_id_02" to brand.id
    )
)
```

**Jetpack Compose:**

```kotlin
AimstarInAppMessaging.screenView(
    activity = this,
    screenName = "Category List",
    topOffsetPx = topBarHeightPx,
    bottomOffsetPx = bottomBarHeightPx,
    productGroups = mapOf(
        "product_group_id_01" to category.id,
        "product_group_id_02" to brand.id
    )
)
```

#### 注意点

- 商品画面以外（トップ画面・カート画面など、商品軸に紐付かない画面）では `productId` / `productGroups` を省略してください
- 省略した場合、送信データに当該キー自体が含まれません（未指定時のリクエストは従来と同一です）
- SDK は枠名（`product_group_id_01`〜`product_group_id_10`）や値の検証・補完を行いません。**管理画面で枠名を指定しているレコメンドでは、その枠名の値が届かないと配信そのものが行われません。** AIMSTAR 管理画面のコンテンツ設定と一致する枠名を呼び出し側で指定してください
- `productGroups` に渡した `Map` は呼び出し時点でコピーされます。呼び出し後に元の `Map` を変更しても、送信される値には反映されません

---

# 利用ガイド

## screenView 呼び出しのタイミング制御

SDK にはポップアップの表示を抑制する機能がありません。メッセージを表示したくないタイミングがある場合は、`messageDetectedForDisplay` / `messageDismissed` リスナーを活用し、アプリ側で `screenView` 呼び出しを制御してください。

`screenName` または画面（`owner`、省略時は Activity）が変わる呼び出しでは、表示中のポップアップが撤去されます（詳細は「[同一画面で再度 screenView を呼び出した場合](#同一画面で再度-screenview-を呼び出した場合)」を参照）。画面遷移で表示中のメッセージを消したくない場合や、そもそもメッセージを出したくないタイミングがある場合は、以下のように `screenView` の呼び出し自体を制御してください。

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

SDK は `screenName` と画面（`owner`、省略時は Activity）の組で「同じ画面かどうか」を判定し、同じ画面での `screenView` 呼び出しを 1 つの表示サイクルとして扱います（表示サイクルについては「[表示サイクルと owner](#表示サイクルと-owner)」を参照）。同じ `screenName`・同じ画面で再度 `screenView` を呼び出した場合（コンテンツの更新、pull-to-refresh、画面復帰など）、表示中のポップアップは原則として維持されます。ポップアップバナーとポップアップモーダルで挙動が異なります。

**ポップアップバナー**

- **表示中のバナー**: そのまま表示され続けます。ユーザーが閉じた時点で、新しい取得結果に含まれる同じ位置のバナーが表示されます
- **同一シナリオの再取得**: 表示中のものと同じシナリオが再度取得された場合、二重に表示されることはありません
- **配信が止まった位置**: 新しい取得結果にその位置のバナーが含まれない場合、表示中のバナーは撤去されます

**ポップアップモーダル**

- **配信が止まった場合**: 新しい取得結果にモーダルが含まれない場合でも、表示中のモーダルは**撤去されません**。ユーザーが閉じるまで表示され続けます
- **新しいモーダルの取得**: 表示中のモーダルは撤去され、新しいモーダルに差し替わります。同一シナリオが再度取得された場合も表示し直されるため、モーダルが重なって表示されることはありません

**共通**

- **`screenName` または画面（`owner`、省略時は Activity）が変わる呼び出し**: 表示中のポップアップはバナー・モーダルともすべて撤去されます

`screenName` と画面（`owner`）は「同じ画面かどうか」の判定に使われます。**内容が変わる画面に同一の `screenName` を使い回すと、前の内容向けのバナーが次の内容の画面に残る場合があります。** 論理的な画面ごとに一意な `screenName` を指定してください。商品詳細のように、同じ `screenName` の画面へ何度も入り直す構成では、画面インスタンスごとに異なる `owner` を渡してください（「[表示サイクルと owner](#表示サイクルと-owner)」を参照）。

### 表示サイクルと owner

表示サイクルとは、メッセージの表示・OPEN の送信を 1 まとまりとして管理する単位です。`screenName` と画面（`owner`）の組が変わると新しい表示サイクルが始まり、表示中のポップアップが撤去され、ポップアップや埋め込みバナーの OPEN は新しい画面として再送されます。同じ組での `screenView` は同じ表示サイクルのままで、表示中のメッセージと OPEN 送信済みの記録を維持します。

| 状況 | `owner` を渡した場合 | `owner` を省略した場合 |
| - | - | - |
| 同じ画面インスタンスへ戻る（戻るボタン、遷移先からの復帰）・同じ画面での再取得 | 同じ表示サイクル | 同じ表示サイクル |
| 同じ `screenName` の新しい画面インスタンスに入る（Fragment を作り直す、同じ宛先へ再度 navigate するなど） | 新しい表示サイクル | 同じ表示サイクル（Activity が同じため） |
| 別の `screenName` の画面に入る | 新しい表示サイクル | 新しい表示サイクル |
| 別の Activity に入る | 新しい表示サイクル | 新しい表示サイクル |

- `owner` には、画面インスタンスごとに 1 つ決まるオブジェクト（Fragment、NavBackStackEntry、Compose で `remember` したオブジェクトなど）を渡します。同じ画面の間は常に同じオブジェクトを渡してください。呼び出しのたびに新しいオブジェクトを作って渡すと、毎回新しい表示サイクルになります
- 画面回転などの構成変更で Activity が再生成された場合、再生成された画面が表示された直後の最初の `screenView` は、同じ `screenName` であれば、`owner` が新しいインスタンス（再生成された Fragment、`remember` し直したオブジェクトなど）でも同じ表示サイクルとして扱われ、表示中のポップアップと OPEN 送信済みの記録を維持します。この引き継ぎは最初の 1 回だけで、その後に別の `owner` で呼ぶと新しい表示サイクルになります。`finish` や戻る操作による破棄、別の画面への遷移では引き継がれず、`owner` が変われば新しい表示サイクルです
- 引き継ぎが効くのは、再生成後に画面が表示された（`onResume` 以降の）直後、数フレームのうちに `screenView` を呼んだ場合です。Fragment なら `onResume`、Compose なら画面表示後に実行される `LaunchedEffect` などから呼んでください。`Activity.onCreate` や Fragment の `onViewCreated` / `onStart` など表示より前に呼んだ場合や、再生成後しばらくしてから呼んだ場合は引き継がれず、`owner` が変われば新しい表示サイクルになります

## 埋め込みバナーの実装時の注意点

- `componentId` は AIMSTAR 管理画面で確認できる識別子です。定数として管理することを推奨します
- 存在しない `componentId` を指定した場合、バナーは表示されません（エラーにはなりません）
- 埋め込みバナーのコンテンツを更新したい場合は、再度 `screenView` を呼び出してください
- デフォルトではロード中にバナー領域が高さ 0 に折りたたまれます。ロード中に shimmer プレースホルダーを表示するには `showsLoadingPlaceholder` を `true` に設定してください（View: `app:showsLoadingPlaceholder` 属性または同名プロパティ、Compose: 同名パラメータ）

## リンク URL の遷移とディープリンク

メッセージのボタン・画像・バナーに設定されたリンク URL は、タップ時に `Intent.ACTION_VIEW` で起動されます。Web ページ（`https`）のほか、`myapp://item/123` のようなカスタムスキームによるディープリンクで自アプリの画面を開くこともできます。

### 開けるスキーム

以下の 9 スキームは、スクリプト実行・ローカルリソース参照・任意コンポーネント起動につながるため開かずに拒否します。これ以外は、`https` などの Web リンクもアプリ独自のカスタムスキームも含めて開きます。

```
javascript / data / vbscript / blob / file / about / content / intent / android-app
```

- スキームの判定は大文字小文字を区別しません（`YourApp://` は `yourapp://` と同じ扱いです）
- スキームを持たない文字列（`example.com/path` など）は開けないため拒否します
- URL に空白・改行が含まれる場合は、除去してから判定・起動します
- 拒否した場合と URL の形式が不正な場合は、`messageError` に `AimstarException.Precondition`（`Invalid link URL: ...`）で通知します。リンクが設定されていない場合は、何も起こらず通知もされません
- 拒否されたときにメッセージが閉じるかどうかは、リンクの有効性ではなくタップ対象によって決まります。画像のタップとポップアップバナーのボタンのタップでは表示が継続しますが、ポップアップモーダルのポジティブボタンはリンクの有効性に関わらずタップ後にモーダルが閉じます

### カスタムスキームで自アプリの画面を開く

リンクを受け取る Activity に intent-filter を設定してください。

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTop">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="myapp" />
    </intent-filter>
</activity>
```

受信した URI は、アプリが未起動の場合は `onCreate`、起動済みの場合は `onNewIntent` に届くため、両方で処理します。

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // ...
    handleDeepLink(intent)
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleDeepLink(intent)
}

private fun handleDeepLink(intent: Intent) {
    val uri = intent.data ?: return
    // uri に応じて画面遷移する
}
```

`launchMode` が `standard` の場合、ディープリンクのたびに Activity の新しいインスタンスが生成され `onNewIntent` は呼ばれません。`singleTop` などの指定を推奨します。

### App Links（https で自アプリを開く）

`https` のリンク URL で自アプリの画面を開きたい場合は、Android の App Links の設定が必要です。intent-filter に `android:autoVerify="true"` を指定し、対象ドメインに `assetlinks.json` を配置してください。検証されていない `https` リンクはブラウザで開かれます。

### 注意点

リンクを処理できるアプリが端末に存在しない場合（カスタムスキームの受け口を設定していない場合など）、**タップしても何も起こらず、`messageError` も通知されません。** リンクが動作しない場合は、まず intent-filter の設定を確認してください。

## ProGuard / R8 による難読化

**利用側アプリで追加の keep ルールを書く必要はありません。** SDK が必要とするルールは AAR に
`proguard.txt` として同梱され、`fileTree` で取り込んだ場合も含めてアプリの R8 に自動的にマージされます。
適用されたルールは、minify を有効にしたビルドの `app/build/outputs/mapping/<variant>/configuration.txt`
で確認できます。

同梱しているのは、`IAMEmbeddedBannerView` を XML レイアウトから inflate するためのコンストラクタ保持のみです。
公開 API 全体の keep やパッケージ単位の keep は意図的に含めていません。利用側アプリが参照する API は
R8 が呼び出し経路から自動的に保持し、使用していない API は削除されるため、アプリの最適化を妨げません。

## トラブルシューティング

### メッセージが表示されない場合

以下の項目を順番に確認してください。

1. `setup()` が `Application.onCreate()` 内で正しく呼び出されているか
2. `screenView` を Main スレッドから呼び出しているか（Main スレッド以外から呼び出した場合、処理は行われず `messageError` に `AimstarException.Precondition` が通知されます）
3. ログイン済みユーザー向けのメッセージを表示したい場合、`customerId` が設定されているか（`customerId` が未設定でも匿名の訪問者としてメッセージの対象になります）
4. `screenView` に渡している `screenName` が AIMSTAR 管理画面の設定と一致しているか
5. ネットワーク接続に問題がないか
6. `messageDetectedForDisplay` リスナーが呼び出されているか（呼び出されていない場合、表示対象のメッセージが存在しないか、条件を満たしていない可能性があります）

### リンクをタップしても遷移しない場合

1. `messageError` に `Invalid link URL: ...` が通知されていないか（通知されている場合、リンク URL のスキームが拒否対象か、スキームを持たない値です。AIMSTAR 管理画面のリンク設定を確認してください）
2. カスタムスキームの場合、リンクを受け取る Activity に intent-filter を設定しているか（受け口が無い場合、タップしても何も起こらず `messageError` も通知されません）
3. `https` で自アプリを開きたい場合、App Links（`android:autoVerify="true"` と `assetlinks.json`）を設定しているか

詳細は「[リンク URL の遷移とディープリンク](#リンク-url-の遷移とディープリンク)」を参照してください。

### エラーが発生する場合

`messageError` リスナーで受け取った `AimstarException` の内容を確認してください。

- `ClientError`: API Key または Tenant ID が正しく設定されているか確認してください
- `ServerError` / `NetworkError`: 時間をおいて再試行してください
- `Precondition`: 前提条件エラーです。原因は `msg` プロパティに含まれるため、まずその内容を確認してください
  - `Received a message with an unsupported value: ...`: SDK が対応していない値を受信しました。対象のメッセージは表示されません。SDK を最新版へ更新してください
  - `Invalid link URL: ...`: メッセージに設定されたリンク URL が遷移先として無効です。リンクは開かれません。AIMSTAR 管理画面のリンク設定を確認してください
  - `SDK is not configured. ...`: `setup()` より前に `screenView` を呼び出しています
  - `apiHost is invalid: ...`: `setup()` に渡した `apiHost` が有効な `http` / `https` URL ではありません
  - `screenView() must be called on the main thread.`: `screenView` を Main スレッド以外から呼び出しています

---

# SDK References

## AimstarInAppMessaging

```kotlin
object AimstarInAppMessaging
```

SDKのエントリーポイントです。`setup` メソッドを通じて初期化を行います。初期化が行われていない場合は、SDKの機能が利用できません。初期化前に `screenView` を呼び出した場合、処理は行われず `messageError` に `AimstarException.Precondition` が通知されます。

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

このフラグが効くのは、`customerId` が未設定（ログアウト）の状態で 2 回目以降の訪問が行われたときだけです。`true` の場合、そのユーザーは過去にログインした顧客としてではなく匿名の訪問者として扱われます。`false` の場合は以前の顧客 ID が引き継がれ、過去にログインした顧客として扱われ続けます。`customerId` が設定されている場合は、本フラグの値に関わらず顧客として同定されます。

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

SDK はリスナーをシングルトンとして保持します。そのため **`Activity` / `Fragment` 自身をリスナーの実装にした場合は、破棄時に必ず解除してください**。解除しないと、そのインスタンスがプロセスの生存中参照され続けます。

```kotlin
override fun onDestroy() {
    AimstarInAppMessaging.setListener(null)
    super.onDestroy()
}
```

`Application` スコープのオブジェクトをリスナーにしている場合は解除不要です。

#### screenView(activity, screenName, topAnchor?, bottomAnchor?, productId?, productGroups?, owner?, customBlock?)

```kotlin
fun screenView(
    activity: Activity,
    screenName: String,
    topAnchor: View? = null,
    bottomAnchor: View? = null,
    productId: String? = null,
    productGroups: Map<String, String>? = null,
    owner: Any = activity,
    customBlock: CustomParameterBuilder.() -> Unit = {}
)
```

指定した screenName でメッセージを取得・表示します（View アンカー指定）。`topAnchor` はポップアップバナー（TOP）の上端基準となる View、`bottomAnchor` はポップアップバナー（BOTTOM）の下端基準となる View です。

`productId` は表示中の商品ID（商品軸レコメンド用）、`productGroups` は表示中の商品グループ（キーは枠名 `product_group_id_01`〜`product_group_id_10`、値はグループの値）です。いずれも商品画面以外では省略してください。省略した場合、送信データに含まれません（詳細は「[商品コンテキストの送信](#7-商品コンテキストの送信)」を参照）。

`owner` は画面インスタンスを識別するオブジェクトです。同じ `screenName` でも `owner` が変われば新しい表示サイクルとして扱い、OPEN を再送します。同じ `owner` での呼び出し（画面復帰・再取得）は同じ表示サイクルのままです。Fragment、NavBackStackEntry、Compose で `remember` したオブジェクトをそのまま渡せます（同一性の判定だけに使い、SDK は弱参照で保持します）。省略した場合は `activity` が使われ、Activity 単位で判定されます（詳細は「[表示サイクルと owner](#表示サイクルと-owner)」を参照）。

Java から呼ぶ場合は名前付き引数がなく、`owner` が `customBlock` の前に位置します。`customBlock` を渡すときは `owner` も省略せずに明示してください（引数の順序は `owner`、`customBlock` の順です）。関数型（`customBlock`）が `owner` に渡された場合は取り違えとみなし、`messageError` に `AimstarException.Precondition` が通知されて呼び出しは無視されます。

`screenView` は必ず Main スレッドから呼び出してください（両オーバーロード共通）。Main スレッド以外から呼び出した場合、処理は行われず `messageError` に `AimstarException.Precondition` が通知されます。

#### screenView(activity, screenName, topOffsetPx, bottomOffsetPx, productId?, productGroups?, owner?, customBlock?)

```kotlin
fun screenView(
    activity: Activity,
    screenName: String,
    topOffsetPx: Int,
    bottomOffsetPx: Int,
    productId: String? = null,
    productGroups: Map<String, String>? = null,
    owner: Any = activity,
    customBlock: CustomParameterBuilder.() -> Unit = {}
)
```

px オフセット指定でメッセージを取得・表示します（Compose 向け）。`topOffsetPx` はウィンドウ上端からの絶対距離、`bottomOffsetPx` はウィンドウ下端からの絶対距離です。`productId` / `productGroups` / `owner` の扱いは上記と同じです。

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

ユーザー操作によってメッセージUIが閉じられた際にコールされます。閉じるトリガーはメッセージ種別によって異なります。

- **ポップアップバナー**: 閉じるボタンのタップのみです。ボタンや画像をタップしてもバナーは閉じないため、このコールバックもコールされません
- **ポップアップモーダル**: 閉じるボタンのタップに加え、**ポジティブボタンのタップ**でもモーダルが閉じるためコールされます。ポジティブボタンのタップでは `messageClicked` と `messageDismissed` の両方がコールされます。画像をタップしてもモーダルは閉じないため、このコールバックはコールされません

以下のプログラム的な撤去ではコールされません。

- `screenName` または画面（`owner`、省略時は Activity）が変わる `screenView` 呼び出しに伴う撤去
- 新しい取得結果に含まれない位置のポップアップバナーの撤去
- 新しいポップアップモーダルへの差し替えに伴う、表示中モーダルの撤去
- 画像ロード失敗で一度も可視状態にならなかった場合

また、埋め込みバナーは閉じる操作を持たないため、このコールバックの対象になりません。

#### messageClicked(message)

```kotlin
fun messageClicked(message: InAppMessage)
```

メッセージUIでユーザーによるポジティブなタップアクション（ボタンまたは画像のタップ）を行った際にコールされます。発火条件はタップ対象によって異なります。

- **ボタンのタップ**: リンク URL の有無・有効性に関わらず常にコールされます
- **画像のタップ**: 画像にリンク URL が設定され、かつ遷移先として有効な場合のみコールされます（リンクなし・無効なリンクの画像タップではコールされません）
- **埋め込みバナー**: バナー領域全体がタップ対象です。リンク URL の有無・有効性に関わらず常にコールされます

#### messageDetectedForDisplay(message)

```kotlin
fun messageDetectedForDisplay(message: InAppMessage)
```

メッセージUIを表示すべき対象のメッセージが検出された際にコールされます。発火タイミングはメッセージ種別によって異なります。

- **ポップアップバナー**: 表示直前。表示中のバナーがある位置に、再取得で別のシナリオが届いた場合は、先行バナーが閉じられて表示順が来た時点でコールされます
- **ポップアップモーダル**: 表示準備を開始した時点
- **埋め込みバナー**: 取得結果を各コンポーネントへ配信した時点。**アプリ側に該当する `componentId` の埋め込みバナーが配置されていない場合もコールされます**（この場合バナーは表示されませんが、`messageError` もコールされません）

このコールバックが呼ばれた時点では、まだメッセージがユーザーに可視とは限りません。OPEN リアクションは、メッセージがユーザーに可視になった時点で別途サーバーに送信されます。

画面回転で Activity が再生成された場合、回転前に表示中だったメッセージは再表示されますが、既に通知済みのため本コールバックは再度コールされません（1 回の配信につき 1 回だけ通知されます）。

#### messageError(message, error)

```kotlin
fun messageError(message: InAppMessage?, error: AimstarException)
```

SDK内でメッセージの取得や表示の際にエラーが発生した場合にコールされます。`message` が `null` の場合はネットワーク通信などメッセージ特定前のエラーを示します。

画像ロードに失敗し、タイトル・本文（ポップアップ系はボタンも）も無いために表示できるコンテンツが残らなかった場合も、本メソッドが `AimstarException.NetworkError` で通知します（テキストが表示できる場合は、そのテキストで表示を成立させるため通知しません）。この場合メッセージは表示されず、OPEN / CLOSE リアクションと `messageDismissed` は発火しません。

`AimstarException.Precondition` は以下の場合に通知されます。

- SDK が対応していない値（未知の `action_type` や表示位置）を受信した場合。対象のメッセージは表示されません
- メッセージに設定されたリンク URL が遷移先として無効な場合（スキームを持たない、または拒否スキーム。一覧は「[開けるスキーム](#開けるスキーム)」を参照してください）。リンクは開かれません（メッセージが閉じるかどうかはタップ対象によって決まります。「[リンク URL の遷移とディープリンク](#リンク-url-の遷移とディープリンク)」を参照してください）
- `setup()` を呼ぶ前に `screenView` を呼び出した場合
- `setup()` に無効な `apiHost` を渡した後に `screenView` を呼び出した場合
- `screenView` を Main スレッド以外から呼び出した場合

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
sealed class AimstarException(message: String) : Exception(message)
```

| サブクラス | プロパティ | 説明 |
| - | - | - |
| `Precondition(msg)` | `msg: String` | 内部不整合などの前提条件エラー。リトライ不可。 |
| `ClientError(httpStatusCode)` | `httpStatusCode: Int?` | 4xx レスポンスに起因するクライアントエラー。リトライ不可。 |
| `ServerError(httpStatusCode)` | `httpStatusCode: Int?` | サーバー起因のエラー。リトライ可能。5xx レスポンスの場合はステータスコードが入り、レスポンスの解析失敗など特定の HTTP ステータスに紐づかない失敗の場合は `null` になります。 |
| `NetworkError(exception)` | `exception: Exception` | ネットワーク通信または画像デコードのエラー。多くはリトライ可能ですが、下記のとおり例外があります。 |

`NetworkError` は画像ロード経路では配信データ起因の失敗も含みます（画像 URL が `https` でない、リダイレクトが上限を超えた、リダイレクト先が非 HTTPS、レスポンスがサイズ上限を超過）。これらはリトライしても解消しないため、リトライの判断に使う場合は `exception` の型まで確認してください（`IllegalArgumentException` / `IllegalStateException` は再試行不能）。

いずれのサブクラスも失敗理由を `Exception` 標準の `message` に設定します（`Precondition` は `msg` の値、`ClientError` / `ServerError` は `"Client error (status: 404)"` のような文字列、`NetworkError` は包んだ例外の `toString()`）。そのため `e.message` や、クラッシュレポーターが収集する `getMessage()` からも理由を取得できます。一方 `cause` は設定されません。

個別の値が必要な場合は、各サブクラスのプロパティ（`Precondition.msg`、`ClientError.httpStatusCode`、`ServerError.httpStatusCode`、`NetworkError.exception`）から取得してください。

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
