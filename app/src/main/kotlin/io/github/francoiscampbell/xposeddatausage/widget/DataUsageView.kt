package io.github.francoiscampbell.xposeddatausage.widget

import android.view.View

/**
 * Created by francois on 16-03-14.
 */
interface DataUsageView {
    val androidView: View
    var bytesText: String
    var twoLines: Boolean
    var visible: Boolean
    var colorOverride: Int?
    var position: Position
    var alignment: Alignment
    var textSize: Float

    fun update()
}