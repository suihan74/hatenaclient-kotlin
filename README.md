# HatenaClient-kotlin

[![](https://jitpack.io/v/suihan74/HatenaClient-kotlin.svg)](https://jitpack.io/#suihan74/HatenaClient-kotlin)

under development...

The modules for accessing Hatena Bookmark by using Kotlin.

## How to use

### Android Project

#### build.gradle(:project)

```gradle:build.gradle
allprojects {
    repositories {
        // ...
        // add
        maven { url 'https://jitpack.io' }
    }
}
```

#### build.gradle(:app)

```gradle
dependencies {
    // ...

    // HatenaClient
    implementation 'com.github.suihan74:hatenaclient-kotlin:0.0.17'
}
```

---

## APIs

### User

#### Sign-in

```kt
suspend fun signIn() {
    val rk = "..."

    // 認証情報付きのクライアントを取得する
    val certifiedClient = HatenaClient.signIn(rk)
}
```

`rk`はアプリ内ブラウザなど別の手段でHatenaにサインインし，クッキーを取得する必要があります。

#### Account

```kt
suspend fun account() {
    val account = certifiedClient.user.getAccount()
}
```

#### Notice

##### Get Notices

```kt
suspend fun getNotices() {
    // 通知を取得
    val response = certifiedClient.user.getNotices()
    // 通知の最終確認時刻を更新
    certifiedClient.user.readNotices()
}
```

#### Ignored Users

##### Ignoring / UnIgnoreing

```kt
suspend fun ignoreUser() {
    // userIdを非表示設定
    certifiedClient.user.ignore("userId")
    
    // userIdの非表示設定を解除
    certifiedClient.user.unIgnore("userId")
}
```

##### Get Ignored Users

```kt
suspend fun getIgnoredUsersIncremental() {
    // 適当な件数ずつ取得
    val response = certifiedClient.user.getIgnoredUsers(limit = null, cursor = null)
    val users = response.users
    val cursor = response.cursor
}
```

```kt
suspend fun getIgnoredUsersAll() {
    // 全件取得
    val response = certifiedClient.user.getIgnoredUsersAll()
    val users = response.users
    val cursor = response.cursor  // 途中で失敗した場合null以外の値が入る
}
```

### Entry

#### Get Entries

```kt
suspend fun entries() {
    // 指定カテゴリのホットエントリ一覧を取得
    val hotEntries = HatenaClient.entry.getEntries(EntriesType.HOT, Category.ALL)

    // 指定カテゴリの新着エントリ一覧を取得
    val recentEntries = HatenaClient.entry.getEntries(EntriesType.RECENT, Category.ALL)
}
```

#### Get Issues

```kt
suspend fun issues() {
    // 指定カテゴリの「特集」を取得
    val issues = HatenaClient.entry.getIssues(Category.IT)

    // 特集を指定してエントリ一覧を取得
    val entriesOfFirstItIssue = HatenaClient.entry.getEntries(EntriesType.HOT, issues[0])
}
```

#### My hot entries

```kt
suspend fun myHotEntries() {
    val entries = certifiedClient.entry.getMyHotEntries()
}
```

#### Entries bookmarked by the specified user

```kt
suspend fun userBookmarkedEntries() {
    // 指定ユーザーがブクマしたエントリ
    val suihanEntries = HatenaClient.entry.getBookmarkedEntries(user = "suihan74")
    
    // サインインしているユーザーがブクマしたエントリ
    val userEntries = certifiedClient.entry.getBookmarkedEntries()
    
    // サインインしているユーザーがブクマしたエントリを検索
    val searchResult = certifiedClient.entry.searchBookmarkedEntries(
        SearchType.TAG,
        query = "あとで読む"
    )
}
```

### Bookmark

#### Get Bookmarks

```kt
suspend fun bookmarks() {
    // 新着ブクマを取得
    val recentBookmarksResponse = HatenaClient.bookmark.getRecentBookmarks(
        url = "https://foobarbaz/",
        limit = null,
        cursor = null
    )
    val recentBookmarks = recentBookmarksResponse.bookmarks
    val cursor = recentBookmarksResponse.cursor  // 続きを読み込むときに使用
    
    // 人気ブクマを取得
    val bookmarksDigest = HatenaClient.bookmark.getBookmarksDigest(url = "https://foobarbaz/")
    val popularBookmarks = bookmarksDigest.scoredBookmarks
}
```

### Star

#### Get Stars

```kt
suspend fun stars() {
    val starsEntry = HatenaClient.star.getStarsEntry("url")
    val stars = starsEntry.allStars
    val yellowStars = starsEntry.stars
    val coloredStars = starsEntry.coloredStars
}

suspend fun stars_multi_urls() {
    val starsEntries = HatenaClient.star.getStarsEntries(listOf("url0", "url1", ...))
    starsEntries.forEach { starsEntry ->
        // ...
    }
}
```

#### Get My Recent Stars

```kt
suspend fun getMyRecentStars() {
    val starEntries = certifiedClient.star.getMyRecentStars()
}
```

#### Get Recent Stars Report

```kt
suspend fun getRecentStarsReport() {
    val response = certifiedClient.star.getRecentStarsReport()
    val starEntries = response.entries
}
```

