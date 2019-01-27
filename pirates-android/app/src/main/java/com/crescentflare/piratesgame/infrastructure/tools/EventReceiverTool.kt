package com.crescentflare.piratesgame.infrastructure.tools

import android.os.Build
import com.crescentflare.piratesgame.infrastructure.events.AppEvent
import com.crescentflare.piratesgame.infrastructure.events.AppEventObserver
import com.crescentflare.viewletcreator.utility.ViewletMapUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * Tools: receive events through the command console of the dev server
 */
object EventReceiverTool {

    // ---
    // Members
    // ---

    // private var serverAddress = "http://192.168.1.12:1313/commandconsole"
    private var serverAddress = "" // Disabled
    private var observers = mutableListOf<WeakReference<AppEventObserver>>()
    private val events = mutableListOf<AppEvent>()
    private var token = ""
    private var lastUpdate = "0"
    private var busy = false
    private var waiting = false


    // ---
    // Handle observers
    // ---

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


    // ---
    // Server polling
    // ---

    private fun pollIfNeeded() {
        cleanDanglingObservers()
        if (!busy && observers.size > 0 && serverAddress.isNotEmpty()) {
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
        val deviceName = Build.DEVICE
        val client = OkHttpClient()
        busy = true
        client.newCall(Request.Builder().url("$serverAddress?name=$deviceName&token=$token&waitUpdate=$lastUpdate").build()).enqueue(object : Callback {
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
                    val result = Gson().fromJson<Map<String, Any>>(jsonString, type)
                    var newToken: String? = null
                    var newLastUpdate: String? = null
                    val eventList = mutableListOf<AppEvent>()
                    if (result != null) {
                        val commands: MutableList<Any> = ViewletMapUtil.optionalObjectList(result, "commands")
                        for (command in commands) {
                            val commandInfo = ViewletMapUtil.asStringObjectMap(command)
                            if (commandInfo != null) {
                                val received = ViewletMapUtil.optionalBoolean(commandInfo, "received", false)
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
