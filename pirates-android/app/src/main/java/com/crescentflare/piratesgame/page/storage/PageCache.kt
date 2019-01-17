package com.crescentflare.piratesgame.page.storage

/**
 * Page storage: cached loaded page items
 */
class PageCache {

    // ---
    // Static: singleton instance
    // ---

    companion object {

        var instance = PageCache()

    }


    // ---
    // Members
    // ---

    private var entries = mutableMapOf<String, Page>()


    // ---
    // Cache access
    // ---

    fun hasEntry(cacheKey: String): Boolean {
        return entries[cacheKey] != null
    }

    fun getEntry(cacheKey: String): Page? {
        return entries[cacheKey]
    }

    fun storeEntry(cacheKey: String, page: Page) {
        entries[cacheKey] = page
    }

    fun clear() {
        entries = mutableMapOf()
    }

}
