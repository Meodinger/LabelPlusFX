package info.meodinger.lpfx.util.component

import javafx.scene.Node
import javafx.scene.layout.AnchorPane

/**
 * Author: Meodinger
 * Date: 2021/9/30
 * Location: info.meodinger.lpfx.util
 */

/**
 * Get AnchorPane anchor - Left
 * @return LayoutX if null
 */
var Node.anchorPaneLeft: Double
    get() =  AnchorPane.getLeftAnchor(this) ?: layoutX
    set(value) { AnchorPane.setLeftAnchor(this, value) }

/**
 * Get AnchorPane anchor - Top
 * @return LayoutY if null
 */
var Node.anchorPaneTop: Double
    get() =  AnchorPane.getTopAnchor(this) ?: layoutY
    set(value) { AnchorPane.setTopAnchor(this, value) }

/**
 * Get AnchorPane anchor - Right
 * @return LayoutX + localBoundsWidth if null
 */
var Node.anchorPaneRight: Double
    get() = AnchorPane.getRightAnchor(this) ?: (layoutX + boundsInLocal.width)
    set(value) { AnchorPane.setLeftAnchor(this, value) }

/**
 * Get AnchorPane anchor - Bottom
 * @return LayoutY + localBoundsHeight if null
 */
var Node.anchorPaneBottom: Double
    get() =  AnchorPane.getBottomAnchor(this) ?: (layoutY + boundsInLocal.height)
    set(value) { AnchorPane.setBottomAnchor(this, value) }