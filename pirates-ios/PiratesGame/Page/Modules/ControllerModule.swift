//
//  ControllerModule.swift
//  Page module: provides a protocol for separating controller logic into modules
//

import UIKit
import JsonInflator

protocol ControllerModule: class {
    
    var eventType: String { get }
    
    func didCreate(viewController: UIViewController)
    func didUpdatePage(page: Page, binder: InflatorDictBinder)
    func catchEvent(_ event: AppEvent, sender: Any?) -> Bool

}
