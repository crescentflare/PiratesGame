//
//  NavigationBarComponent.swift
//  Component utility: navigation bar components should subclass this
//  Note: a bar having light content means that a text will be more readable with a white color than black
//

import UIKit

protocol NavigationBarComponent: class {
    
    var isLightContent: Bool { get }
    var isTranslucent: Bool { get }
    
}
