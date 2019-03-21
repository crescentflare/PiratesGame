//
//  LevelEntitiesView.swift
//  LevelView helper: a layer for showing entities, like a boat
//

import UIKit
import UniLayout

class LevelEntitiesView: UniView {
    
    // --
    // MARK: Constants
    // --
    
    private let playerBoatCharacter: Character = "P"
    private let enemyBoatCharacter: Character = "E"


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
        
        // Create entity views
        for mapIndex in characterMap.indices {
            let lineMap = characterMap[mapIndex]
            for characterIndex in lineMap.indices {
                let character = lineMap[characterIndex]
                if character == playerBoatCharacter || character == enemyBoatCharacter {
                    let imageView = UniImageView()
                    imageView.image = UIImage(named: character == playerBoatCharacter ? "entity_boat_side_player" : "entity_boat_side_enemy")
                    imageView.contentMode = .scaleToFill
                    imageView.tag = mapIndex * 1024 + characterIndex
                    addSubview(imageView)
                }
            }
        }
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
                var imageWidth: CGFloat = 0
                var imageHeight: CGFloat = 0
                if let image = (view as? UniImageView)?.image {
                    imageWidth = image.size.width * tileSize / 64
                    imageHeight = image.size.height * tileSize / 64
                }
                if imageWidth > 0 && imageHeight > 0 {
                    UniLayout.setFrame(view: view, frame: CGRect(x: xTile * tileSize + tileSize * 0.5 - imageWidth / 2, y: yTile * tileSize + tileSize * 0.54 - imageHeight / 2, width: imageWidth, height: imageHeight))
                }
            }
        }
    }
    
}
