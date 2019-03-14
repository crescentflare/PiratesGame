//
//  LevelView.swift
//  Game view: a level
//

import UIKit
import UniLayout
import ViewletCreator

class LevelView: UniView {

    // --
    // MARK: Viewlet integration
    // --
    
    class func viewlet() -> Viewlet {
        return ViewletClass()
    }
    
    private class ViewletClass: Viewlet {
        
        func create() -> UIView {
            return LevelView()
        }
        
        func update(view: UIView, attributes: [String : Any], parent: UIView?, binder: ViewletBinder?) -> Bool {
            if let levelView = view as? LevelView {
                // Size properties
                levelView.gridWidth = ViewletConvUtil.asInt(value: attributes["gridWidth"]) ?? 7
                levelView.spawnSpeed = ViewletConvUtil.asFloat(value: attributes["spawnSpeed"]) ?? 1

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(view: view, attributes: attributes)
                return true
            }
            return false
        }
        
        func canRecycle(view: UIView, attributes: [String : Any]) -> Bool {
            return view is LevelView
        }
        
    }


    // --
    // MARK: Initialization
    // --

    override init(frame: CGRect) {
        super.init(frame: frame)
        setup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setup()
    }
    
    fileprivate func setup() {
        continueSpawning()
    }
    

    // --
    // MARK: Configurable values
    // --
    
    var gridWidth: Int = 7 {
        didSet {
            // TODO: recreate waves etc.?
        }
    }
    
    var spawnSpeed: Float = 1
   

    // --
    // MARK: Handle spawn timer
    // --
    
    private func continueSpawning() {
        DispatchQueue.main.asyncAfter(deadline: .now() + Double(spawnSpeed), execute: { [weak self] in
            if let level = self {
                level.spawnWave()
                level.continueSpawning()
            }
        })
    }
    
    private func spawnWave() {
        let particle = UIImageView()
        particle.image = UIImage(named: "particle_wave")?.withRenderingMode(.alwaysTemplate)
        particle.tintColor = UIColor.white
        particle.frame = CGRect(x: CGFloat(drand48()) * (bounds.width + 64) - 64, y: CGFloat(drand48()) * (bounds.height + 16) - 16, width: 64, height: 16)
        particle.transform = CGAffineTransform(translationX: 0, y: 0).scaledBy(x: 1.5, y: 0.1)
        particle.alpha = 0
        
        let distance = CGFloat(32 + drand48() * 8)
        let speed: Double = 4 + drand48() * 1
        UIView.animateKeyframes(withDuration: speed, delay: 0, options: [.calculationModeLinear], animations: {
            UIView.addKeyframe(withRelativeStartTime: 0, relativeDuration: 0.5, animations: {
                let scale = CGFloat(1 - drand48() * 0.5)
                particle.transform = CGAffineTransform(translationX: -distance / 2, y: -0.75 * 16 * scale).scaledBy(x: 1, y: scale)
                particle.alpha = 1
            })
            UIView.addKeyframe(withRelativeStartTime: 0.5, relativeDuration: 0.5, animations: {
                particle.transform = CGAffineTransform(translationX: -distance, y: 0).scaledBy(x: 1.5, y: 0.1)
                particle.alpha = 0
            })
        }, completion: { (finished) -> Void in
            particle.removeFromSuperview()
        })
        
        addSubview(particle)
    }
    
}
