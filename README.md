# HatenaClient-kotlin

under development...

This is a module for accessing Hatena Bookmark by using Kotlin.

### Account

```kt
suspend fun signIn() {
    val certifiedClient = HatenaClient.signIn("userId", "pass")
    val accountInfo = certifiedClient.user.getAccount()
}
```

### Entry

```kt
suspend fun entries() {
    val hotEntries = HatenaClient.entry.getEntries(EntriesType.HOT, Category.ALL)
}
```

### Bookmark

```kt
suspend fun bookmarks() {
    val response = HatenaClient.bookmark.getRecentBookmarks(url = "https://foobarbaz/")
    val bookmarks = response.bookmarks
}
```
