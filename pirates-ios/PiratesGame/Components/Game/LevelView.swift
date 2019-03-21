//
//  LevelView.swift
//  Game view: a level
//

import UIKit
import UniLayout
import ViewletCreator

class LevelView: FrameContainerView {

    // --
    // MARK: Members
    // --
    
    private let tileMapView = LevelTileMapView()
    private let entitiesView = LevelEntitiesView()
    private let waveAnimationView = LevelWaveAnimationView()


    // --
    // MARK: Viewlet integration
    // --
    
    override class func viewlet() -> Viewlet {
        return ViewletClass()
    }
    
    private class ViewletClass: Viewlet {
        
        func create() -> UIView {
            return LevelView()
        }
        
        func update(view: UIView, attributes: [String : Any], parent: UIView?, binder: ViewletBinder?) -> Bool {
            if let levelView = view as? LevelView {
                // Apply animation
                levelView.waveSpawnInterval = ViewletConvUtil.asFloat(value: attributes["waveSpawnInterval"]) ?? 0.2
                
                // Set tiles
                levelView.tileMap = ViewletConvUtil.asStringArray(value: attributes["tileMap"])

                // Generic view properties
                ViewletUtil.applyGenericViewAttributes(view: view, attributes: attributes)

                // Event handling
                levelView.tapEvent = AppEvent(value: attributes["tapEvent"])

                // Forward event observer
                if let eventObserver = parent as? AppEventObserver {
                    levelView.eventObserver = eventObserver
                }
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
        // Add tile map view
        tileMapView.layoutProperties.width = UniLayoutProperties.stretchToParent
        tileMapView.layoutProperties.height = UniLayoutProperties.stretchToParent
        addSubview(tileMapView)
        
        // Add wave animation view
        waveAnimationView.layoutProperties.width = UniLayoutProperties.stretchToParent
        waveAnimationView.layoutProperties.height = UniLayoutProperties.stretchToParent
        waveAnimationView.tileMapView = tileMapView
        addSubview(waveAnimationView)
        
        // Add entities view
        entitiesView.layoutProperties.width = UniLayoutProperties.stretchToParent
        entitiesView.layoutProperties.height = UniLayoutProperties.stretchToParent
        addSubview(entitiesView)
        
        // Start spawning wave particles
        continueWaveSpawning()
    }
    

    // --
    // MARK: Configurable values
    // --
    
    var waveSpawnInterval: Float = 0.2
    
    var tileMap: [String] {
        set {
            tileMapView.tiles = newValue
            entitiesView.tiles = newValue
        }
        get { return tileMapView.tiles }
    }
   

    // --
    // MARK: Spawning wave particles
    // --
    
    private func continueWaveSpawning() {
        DispatchQueue.main.asyncAfter(deadline: .now() + Double(waveSpawnInterval), execute: { [weak self] in
            if let level = self {
                level.waveAnimationView.spawnRandomWave()
                level.continueWaveSpawning()
            }
        })
    }

}
