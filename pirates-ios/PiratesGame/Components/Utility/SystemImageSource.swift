//
//  SystemImageSource.swift
//  Component utility: provides system image sources
//

import UIKit
import UniLayout

class SystemImageSource {
    
    static func load(path: String) -> UIImage? {
        switch path {
        case "spinner":
            let bundle = Bundle(for: ImageSource.self)
            return UIImage(named: "icon_spinner", in: bundle, compatibleWith: nil)
        case "navigate_back":
            return ImageSource(string: "generate://strokedPath?color=#ffffff&strokeSize=3&points=9,0;0,9;9,18&caching=always")?.getImage()
        case "navigate_close":
            return ImageSource(dict: [
                "type": "generate",
                "name": "strokedPath",
                "color": "#ffffff",
                "strokeSize": 2.5,
                "points": [ 0, 0, 14, 14 ],
                "caching": "always",
                "otherSources": [[
                    "type": "generate",
                    "name": "strokedPath",
                    "color": "#ffffff",
                    "strokeSize": 2.5,
                    "points": [ 14, 0, 0, 14 ]
                ]]
            ]).getImage()
        default:
            break
        }
        return nil
    }
    
}
