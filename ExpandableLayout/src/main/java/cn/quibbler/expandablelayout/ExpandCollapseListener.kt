package cn.quibbler.expandablelayout

import android.view.View

interface ExpandCollapseListener {

    interface ExpandListener<P> {
        fun onExpanded(parentIndex: Int, parent: P, view: View)
    }

    interface CollapseListener<P> {
        fun onCollapsed(parentIndex: Int, parent: P, view: View)
    }

}