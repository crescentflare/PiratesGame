package com.crescentflare.piratesgame.infrastructure.tools

import android.os.Build
import com.crescentflare.jsoninflator.utility.InflatorMapUtil
import com.crescentflare.piratesgame.infrastructure.appconfig.CustomAppConfigManager
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import java.lang.ref.WeakReference

/**
 * Tools: receive events through the command console of the dev server
 */
object EventReceiverTool {

    // --
    // Members
    // --

    private var observers = mutableListOf<WeakReference<AppEventObserver>>()
    private val events = mutableListOf<AppEvent>()
    private val mapUtil = InflatorMapUtil()
    private var token = ""
    private var lastUpdate = "0"
    private var busy = false
    private var waiting = false


    // --
    // Handle observers
    // --

    fun addObserver(addObserver: AppEventObserver) {
        for (observer in observers) {
            if (observer.get() == addObserver) {
                return
            }
        }
        observers.add(WeakReference(addObserver))
        dispatchEvents()
        pollIfNeeded()
    }

    fun removeObserver(removeObserver: AppEventObserver) {
        cleanDanglingObservers()
        for (observer in observers) {
            if (observer.get() == removeObserver) {
                observers.remove(observer)
                return
            }
        }
    }

    private fun cleanDanglingObservers() {
        val indicesToRemove = mutableListOf<Int>()
        for (index in observers.indices) {
            if (observers[index].get() == null) {
                indicesToRemove.add(index)
            }
        }
        for (index in indicesToRemove.indices.reversed()) {
            observers.removeAt(index)
        }
    }


    // --
    // Server polling
    // --

    private fun pollIfNeeded() {
        cleanDanglingObservers()
        if (!busy && observers.size > 0 && CustomAppConfigManager.currentConfig().devServerUrl.isNotEmpty() && CustomAppConfigManager.currentConfig().enableEventReceiver) {
            callServer { eventList, _ ->
                var waitingTime = 2000
                if (eventList != null) {
                    events.addAll(eventList)
                    dispatchEvents()
                    waitingTime = 100
                }
                waiting = true
                GlobalScope.launch(Dispatchers.Main) {
                    delay(waitingTime.toLong())
                    waiting = false
                    pollIfNeeded()
                }
            }
        }
    }

    private fun dispatchEvents() {
        for (observer in observers) {
            val checkObserver = observer.get()
            for (event in events) {
                checkObserver?.observedEvent(event, this)
            }
        }
        events.clear()
    }

    private fun callServer(completion: (eventList: List<AppEvent>?, exception: Throwable?) -> Unit) {
        val deviceName = Build.BRAND + " " + Build.MODEL
        val client = OkHttpClient()
        var serverAddress = CustomAppConfigManager.currentConfig().devServerUrl
        if (!serverAddress.startsWith("http")) {
            serverAddress = "http://$serverAddress"
        }
        busy = true
        client.newCall(Request.Builder().url("$serverAddress/commandconsole?name=$deviceName&token=$token&waitUpdate=$lastUpdate").build()).enqueue(object : Callback {
            override fun onFailure(call: Call, exception: IOException) {
                GlobalScope.launch(Dispatchers.Main) {
                    busy = false
                    completion(null, exception)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body()?.string()
                if (jsonString != null) {
                    val type = object : TypeToken<Map<String, Any>>() {
                    }.type
                    var newToken: String? = null
                    var newLastUpdate: String? = null
                    val eventList = mutableListOf<AppEvent>()
                    try {
                        val result = Gson().fromJson<Map<String, Any>>(jsonString, type)
                        if (result != null) {
                            val commands: MutableList<Any> = mapUtil.optionalObjectList(result, "commands")
                            for (command in commands) {
                                val commandInfo = mapUtil.asStringObjectMap(command)
                                if (commandInfo != null) {
                                    val received = mapUtil.optionalBoolean(commandInfo, "received", false)
                                    if (!received) {
                                        val event = AppEvent.fromObject(commandInfo["command"])
                                        if (event != null) {
                                            eventList.add(event)
                                        }
                                    }
                                }
                            }
                            newToken = result["token"]?.toString()
                            newLastUpdate = (result["lastUpdate"] as? Double)?.toLong().toString()
                        }
                    } catch (ignored: Exception) {
                        // No implementation
                    }
                    GlobalScope.launch(Dispatchers.Main) {
                        busy = false
                        token = newToken ?: ""
                        lastUpdate = newLastUpdate ?: "0"
                        completion(eventList, null)
                    }
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        busy = false
                        completion(null, null)
                    }
                }
            }
        })
    }

}
