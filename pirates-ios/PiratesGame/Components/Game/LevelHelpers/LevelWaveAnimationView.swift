//
//  LevelWaveAnimationView.swift
//  LevelView helper: a view containing animated wave particles
//

import UIKit
import UniLayout

class LevelWaveAnimationView: UniView {
    
    // --
    // MARK: Members
    // --

    var tileMapView: LevelTileMapView?
    private var visibleWaves = [CGPoint]()
    

    // --
    // MARK: Spawn a wave
    // --

    func spawnRandomWave(maxIterations: Int = 4) {
        let mapWidth: Int = tileMapView?.mapWidth ?? 0
        let mapHeight: Int = Int(ceil(CGFloat(mapWidth) * bounds.height / bounds.width))
        if mapWidth > 0 {
            let halfTileX = -1 + Int(floor(drand48() * Double(mapWidth * 2 + 3)))
            let halfTileY = Int(floor(drand48() * Double(mapHeight * 2)))
            if !isOccupied(halfTileX: halfTileX, halfTileY: halfTileY) {
                spawnWave(halfTileX: halfTileX, halfTileY: halfTileY)
                return
            }
        }
        if maxIterations > 0 {
            spawnRandomWave(maxIterations: maxIterations - 1)
        }
    }
    
    private func spawnWave(halfTileX: Int, halfTileY: Int) {
        let mapWidth: Int = tileMapView?.mapWidth ?? 0
        if let particleImage = UIImage(named: "particle_wave")?.withRenderingMode(.alwaysTemplate), mapWidth > 0 {
            // Prepare the particle
            let tileSize = bounds.width / CGFloat(mapWidth)
            let halfTileSize = tileSize / 2
            let x = CGFloat(halfTileX) * halfTileSize
            let y = CGFloat(halfTileY) * halfTileSize
            let waveHeight = tileSize * particleImage.size.height / particleImage.size.width
            let particle = UIImageView()
            particle.image = particleImage
            particle.tintColor = UIColor.white
            particle.frame = CGRect(x: x, y: y + waveHeight * 1.25, width: tileSize, height: waveHeight)
            particle.transform = CGAffineTransform(translationX: 0, y: 0).scaledBy(x: 1.5, y: 0.1)
            particle.alpha = 0
            
            // Add to the visible wave list to prevent overlapping waves
            visibleWaves.append(CGPoint(x: halfTileX, y: halfTileY))

            // Prepare animation
            let distance = tileSize
            let duration: Double = 4 + drand48() * 1
            UIView.animateKeyframes(withDuration: duration, delay: 0, options: [.calculationModeLinear], animations: {
                UIView.addKeyframe(withRelativeStartTime: 0, relativeDuration: 0.5, animations: {
                    let scale = CGFloat(1 - drand48() * 0.5)
                    particle.transform = CGAffineTransform(translationX: -distance / 2, y: -0.75 * waveHeight * scale).scaledBy(x: 1, y: scale)
                    particle.alpha = 1
                })
                UIView.addKeyframe(withRelativeStartTime: 0.5, relativeDuration: 0.5, animations: {
                    particle.transform = CGAffineTransform(translationX: -distance, y: 0).scaledBy(x: 1.5, y: 0.1)
                    particle.alpha = 0
                })
            }, completion: { (finished) -> Void in
                particle.removeFromSuperview()
                self.removeVisibleWave(halfTileX: halfTileX, halfTileY: halfTileY)
            })
            
            // Add view
            addSubview(particle)
        }
    }
    
    
    // --
    // MARK: Check for spawning in available places
    // --
    
    private func isOccupied(halfTileX: Int, halfTileY: Int) -> Bool {
        // The animation should not be overlapping with a tile
        if let tileMapView = tileMapView {
            let checkPositions = [ halfTileX - 2, halfTileX - 1, halfTileX, halfTileX + 1 ]
            for checkX in checkPositions {
                if tileMapView.isOccupied(tileX: checkX / 2, tileY: halfTileY / 2) {
                    return true
                }
            }
        }
        
        // The animation should not be overlapping an existing wave
        for visibleWave in visibleWaves {
            if Int(visibleWave.y) == halfTileY {
                let checkPositions = [ halfTileX - 2, halfTileX - 1, halfTileX, halfTileX + 1 ]
                let checkPositions2 = [ Int(visibleWave.x - 2), Int(visibleWave.x - 1), Int(visibleWave.x), Int(visibleWave.x + 1) ]
                for checkX in checkPositions {
                    for checkX2 in checkPositions2 {
                        if checkX == checkX2 {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }
    
    private func removeVisibleWave(halfTileX: Int, halfTileY: Int) {
        for waveIndex in self.visibleWaves.indices {
            let checkWave = self.visibleWaves[waveIndex]
            if Int(checkWave.x) == halfTileX && Int(checkWave.y) == halfTileY {
                self.visibleWaves.remove(at: waveIndex)
                break
            }
        }
    }
    
}
