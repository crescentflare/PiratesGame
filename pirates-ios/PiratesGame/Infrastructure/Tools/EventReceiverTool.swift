//
//  EventReceiverTool.swift
//  Tools: receive events through the command console of the dev server
//

import UIKit
import Alamofire

fileprivate class EventReceiverSafeObserver {
    
    weak var item: AppEventObserver?
    
    init(_ item: AppEventObserver) {
        self.item = item
    }
    
}

class EventReceiverTool {
    
    // --
    // MARK: Singleton instance
    // --
    
    public static let shared = EventReceiverTool()


    // --
    // MARK: Members
    // --

    private var observers = [EventReceiverSafeObserver]()
    private var events = [AppEvent]()
    private var token = ""
    private var lastUpdate = "0"
    private var busy = false
    private var waiting = false


    // --
    // MARK: Handle observers
    // --
    
    func addObserver(addObserver: AppEventObserver) {
        for observer in observers {
            if observer.item === addObserver {
                return
            }
        }
        observers.append(EventReceiverSafeObserver(addObserver))
        dispatchEvents()
        pollIfNeeded()
    }
    
    func removeObserver(removeObserver: AppEventObserver) {
        cleanDanglingObservers()
        for index in observers.indices {
            if observers[index].item === removeObserver {
                observers.remove(at: index)
                return
            }
        }
    }
    
    private func cleanDanglingObservers() {
        var indicesToRemove = [Int]()
        for index in observers.indices {
            if observers[index].item == nil {
                indicesToRemove.append(index)
            }
        }
        for index in indicesToRemove.indices.reversed() {
            observers.remove(at: index)
        }
    }
    
    
    // --
    // MARK: Server polling
    // --
    
    private func pollIfNeeded() {
        cleanDanglingObservers()
        if !busy && observers.count > 0 && !CustomAppConfigManager.currentConfig().devServerUrl.isEmpty && CustomAppConfigManager.currentConfig().enableEventReceiver {
            callServer(completion: { events, error in
                var waitingTime = 2.0
                if let events = events {
                    self.events.append(contentsOf: events)
                    self.dispatchEvents()
                    waitingTime = 0.1
                }
                self.waiting = true
                DispatchQueue.main.asyncAfter(deadline: .now() + waitingTime, execute: {
                    self.waiting = false
                    self.pollIfNeeded()
                })
            })
        }
    }
    
    private func dispatchEvents() {
        for observer in observers {
            let checkObserver = observer.item
            for event in events {
                checkObserver?.observedEvent(event, sender: self)
            }
        }
        events.removeAll()
    }
    
    private func callServer(completion: @escaping (_ events: [AppEvent]?, _ error: Error?) -> Void) {
        let deviceName = UIDevice.current.name
        var serverAddress = CustomAppConfigManager.currentConfig().devServerUrl
        if !serverAddress.hasPrefix("http") {
            serverAddress = "http://\(serverAddress)"
        }
        busy = true
        Alamofire.request("\(serverAddress)/commandconsole?name=\(deviceName.urlEncode())&token=\(token)&waitUpdate=\(lastUpdate)").responseJSON { response in
            self.busy = false
            if let dictionary = response.value as? [String: Any] {
                var events = [AppEvent]()
                let commands = dictionary["commands"] as? [Any]
                for command in commands ?? [] {
                    if let commandInfo = command as? [String: Any] {
                        let received = commandInfo["received"] as? Bool ?? false
                        if !received {
                            if let event = AppEvent(value: commandInfo["command"]) {
                                events.append(event)
                            }
                        }
                    }
                }
                self.token = dictionary["token"] as? String ?? ""
                self.lastUpdate = dictionary["lastUpdate"] as? String ?? "0"
                completion(events, nil)
            } else {
                completion(nil, response.error)
            }
        }
    }

}
