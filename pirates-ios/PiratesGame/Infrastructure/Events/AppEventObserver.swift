//
//  AppEventObserver.swift
//  Event system: a protocol to observe for events like button taps and pull to refresh
//

protocol AppEventObserver: class {
    
    func observedEvent(_ event: AppEvent, sender: Any?)
    
}
