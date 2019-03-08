//
//  AppEventLabeledSender.swift
//  Event system: a protocol to implement for components sending events which can be tagged by label
//

protocol AppEventLabeledSender: class {
    
    var senderLabel: String? { get }
    
}
