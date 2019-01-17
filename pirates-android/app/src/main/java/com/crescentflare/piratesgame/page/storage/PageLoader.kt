package com.crescentflare.piratesgame.page.storage

import android.content.Context
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * Page storage: handles loading of pages from several sources
 */
class PageLoader(context: Context, location: String) {

    // ---
    // Members
    // ---

    private val context: Context
    private val location: String
    private val loadInternal: Boolean
    private var continuousCompletion : WeakReference<PageLoaderContinuousCompletion>? = null
    private var loading = false
    private var waiting = false


    // ---
    // Initialization
    // ---

    init {
        this.context = context
        this.location = location
        loadInternal = !location.contains("://")
    }


    // ---
    // Loading
    // ---

    fun load(completion: (page: Page?, exception: Throwable?) -> Unit) {
        val cachedPage = PageCache.instance.getEntry(location)
        if (cachedPage != null) {
            completion(cachedPage, null)
        } else if (loadInternal) {
            loadInternal {
                if (it != null) {
                    PageCache.instance.storeEntry(location, it)
                }
                completion(it, null)
            }
        } else {
            loadOnline("ignore") { page, exception ->
                if (page != null) {
                    PageCache.instance.storeEntry(location, page)
                }
                completion(page, exception)
            }
        }
    }

    private fun loadOnline(currentHash: String, completion: (page: Page?, exception: Throwable?) -> Unit) {
        if (!loadInternal) {
            val client = OkHttpClient()
            loading = true
            client.newCall(Request.Builder().url(location).header("X-Mock-Wait-Change-Hash", currentHash).build()).enqueue(object : Callback {
                override fun onFailure(call: Call, exception: IOException) {
                    GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
                        loading = false
                        completion(null, exception)
                    }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    val jsonString = response.body()?.string()
                    if (jsonString != null) {
                        val page = Page(jsonString)
                        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
                            loading = false
                            completion(page, null)
                        }
                    } else {
                        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
                            loading = false
                            completion(null, null)
                        }
                    }
                }
            })
        } else {
            completion(null, null)
        }
    }

    private fun loadInternal(completion: (page: Page?) -> Unit) {
        loading = true
        GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
            val page = loadInternalSync()
            GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
                loading = false
                completion(page)
            }
        }
    }

    fun loadInternalSync(): Page? {
        if (loadInternal) {
            try {
                val stream = context.getAssets().open("pages/${location}")
                val jsonString = stream.bufferedReader().use { it.readText() }
                return Page(jsonString)
            } catch (ignored: IOException) {
            }
        }
        return null
    }


    // ---
    // Continuous loading
    // ---

    fun startLoadingContinuously(completion: PageLoaderContinuousCompletion) {
        continuousCompletion = WeakReference(completion)
        tryNextContinuousLoad()
    }

    fun stopLoadingContinuously() {
        continuousCompletion = null
    }

    private fun tryNextContinuousLoad() {
        if (!loading && !waiting && continuousCompletion != null) {
            if (loadInternal) {
                loadInternal {
                    if (it != null) {
                        PageCache.instance.storeEntry(location, it)
                        continuousCompletion?.get()?.didUpdatePage(it)
                    }
                }
            } else {
                val hash = PageCache.instance.getEntry(location)?.hash ?: "unknown"
                loadOnline(hash) { page, _ ->
                    var waitingTime = 2000
                    if (page != null) {
                        if (hash != page.hash) {
                            PageCache.instance.storeEntry(location, page)
                            continuousCompletion?.get()?.didUpdatePage(page)
                        }
                        waitingTime = 100
                    }
                    waiting = true
                    GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
                        delay(waitingTime.toLong())
                        waiting = false
                        tryNextContinuousLoad()
                    }
                }
            }
        }
    }

}
