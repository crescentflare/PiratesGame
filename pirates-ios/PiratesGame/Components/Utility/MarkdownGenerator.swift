//
//  MarkdownGenerator.swift
//  Component utility: apply styles during markdown parsing
//

import SimpleMarkdownParser

class MarkdownGenerator: DefaultMarkdownAttributedStringGenerator {
    
    // --
    // MARK: Members
    // --
    
    private var noColorization: Bool
    private var noBulletIndentation: Bool
    

    // --
    // MARK: Initialization
    // --
    
    init(noColorization: Bool = false, noBulletIndentation: Bool = false) {
        self.noColorization = noColorization
        self.noBulletIndentation = noBulletIndentation
    }
    

    // --
    // MARK: Implementation
    // --

    override func applyAttribute(defaultFont: UIFont, attributedString: NSMutableAttributedString, type: MarkdownTagType, weight: Int, start: Int, length: Int, extra: String) {
        switch type {
        case .header:
            var size = defaultFont.pointSize
            switch weight {
            case 1:
                size = AppDimensions.titleText
            case 2:
                size = AppDimensions.subTitleText
            default:
                break
            }
            if !noColorization {
                attributedString.addAttribute(NSAttributedString.Key.foregroundColor, value: AppColors.primary, range: NSMakeRange(start, length))
            }
            attributedString.addAttribute(NSAttributedString.Key.font, value: AppFonts.titleBold.font(ofSize: size), range: NSMakeRange(start, length))
            return
        case .textStyle:
            if let font = fontForWeight(weight: weight, pointSize: defaultFont.pointSize) {
                attributedString.addAttribute(NSAttributedString.Key.font, value: font, range: NSMakeRange(start, length))
                return
            }
            return
        case .orderedList, .unorderedList:
            if noBulletIndentation {
                let bulletParagraph = NSMutableParagraphStyle()
                let tokenTabStop = NSTextTab(textAlignment: .right, location: 10 + CGFloat(weight - 1) * 15, options: [:])
                let textTabStop = NSTextTab(textAlignment: .left, location: tokenTabStop.location + 5, options: [:])
                bulletParagraph.tabStops = [ tokenTabStop, textTabStop ]
                bulletParagraph.firstLineHeadIndent = 0
                bulletParagraph.headIndent = textTabStop.location
                attributedString.addAttribute(NSAttributedString.Key.paragraphStyle, value: bulletParagraph, range: NSMakeRange(start, length))
                return
            }
            break
        default:
            break
        }
        super.applyAttribute(defaultFont: defaultFont, attributedString: attributedString, type: type, weight: weight, start: start, length: length, extra: extra)
    }
    

    // --
    // MARK: Helper
    // --
    
    private func fontForWeight(weight: Int, pointSize: CGFloat) -> UIFont? {
        switch weight {
        case 0:
            return AppFonts.normal.font(ofSize: pointSize)
        case 1:
            return AppFonts.italics.font(ofSize: pointSize)
        case 2:
            return AppFonts.bold.font(ofSize: pointSize)
        case 3:
            return AppFonts.boldItalics.font(ofSize: pointSize)
        default:
            break
        }
        return nil
    }

}
