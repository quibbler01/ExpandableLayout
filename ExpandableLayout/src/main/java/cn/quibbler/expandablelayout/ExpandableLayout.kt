package cn.quibbler.expandablelayout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.LayoutRes

class ExpandableLayout : LinearLayout {

    companion object {

        private const val NO_RES = 0

        private const val NO_INDEX = -1

        private val DEFAULT_FILTER: Operation = object : Operation {
            override fun apply(obj: Any?): Boolean = true
        }

    }

    interface Renderer<P, C> {
        fun renderParent(view: View, model: P, isExpand: Boolean, parentPosition: Int)

        fun renderChild(view: View, model: C, parentPosition: Int, childPosition: Int)
    }

    private var layoutInflater: LayoutInflater? = null

    @LayoutRes
    private var parentLayout = -1

    @LayoutRes
    private var childLayout = -1

    private var renderer: Renderer<*, *>? = null

    private var sections: List<Section<*, *>>? = null

    private var expandListener: ExpandCollapseListener.ExpandListener<*>? = null

    private var collapseListener: ExpandCollapseListener.CollapseListener<*>? = null

    private var currentFilter: Operation = DEFAULT_FILTER

    constructor(context: Context?) : super(context) {
        init(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context?, attrs: AttributeSet?) {

    }

}