package com.crescentflare.piratesgame.page.storage

/**
 * Page storage: an interface for continuous page loading to receive updates of a page refresh
 */
interface PageLoaderContinuousCompletion {

    fun didUpdatePage(page: Page)

}
