//
//  LevelTileMapView.swift
//  LevelView helper: a map for rendering tiles
//

import UIKit
import UniLayout

class LevelTileMapView: UniView {
    
    // --
    // MARK: Constants
    // --
    
    private let rockTileCharacter: Character = "#"
    

    // --
    // MARK: Members
    // --

    var mapWidth = 0
    
    var tiles: [String] = [] {
        didSet {
            recreateTileViews()
        }
    }
    

    // --
    // MARK: Creating tiles
    // --

    private func recreateTileViews() {
        // Clear existing tiles
        for view in subviews {
            view.removeFromSuperview()
        }
        
        // Convert to 2-dimensional character array, determine map width
        var characterMap = [[Character]]()
        mapWidth = 0
        for string in tiles {
            let characters = Array(string)
            characterMap.append(characters)
            mapWidth = max(mapWidth, characters.count)
        }
        
        // Create rock tile views
        for mapIndex in characterMap.indices {
            let lineMap = characterMap[mapIndex]
            for characterIndex in lineMap.indices {
                let character = lineMap[characterIndex]
                if character == rockTileCharacter {
                    let imageView = UniImageView()
                    imageView.image = UIImage(named: "tile_rock")
                    imageView.contentMode = .scaleToFill
                    imageView.tag = mapIndex * 1024 + characterIndex
                    addSubview(imageView)
                }
            }
        }
    }
    

    // --
    // MARK: Check for a blocking tile
    // --

    func isOccupied(tileX: Int, tileY: Int) -> Bool {
        if tileY >= 0 && tileY < tiles.count {
            let characters = Array(tiles[tileY])
            if tileX >= 0 && tileX < characters.count {
                return characters[tileX] == rockTileCharacter
            }
        }
        return false
    }
    

    // --
    // MARK: Custom layout
    // --

    override func layoutSubviews() {
        if mapWidth > 0 {
            let tileSize = bounds.width / CGFloat(mapWidth)
            for view in subviews {
                let xTile = CGFloat(view.tag % 1024)
                let yTile = CGFloat(view.tag / 1024)
                var ratio: CGFloat = 1
                if let image = (view as? UniImageView)?.image, image.size.width > 0 {
                    ratio = image.size.height / image.size.width
                }
                UniLayout.setFrame(view: view, frame: CGRect(x: xTile * tileSize, y: yTile * tileSize, width: tileSize, height: tileSize * ratio))
            }
        }
    }
    
}
