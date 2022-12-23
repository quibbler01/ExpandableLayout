package cn.quibbler.expandablelayout

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private lateinit var layoutInflater: LayoutInflater

    @LayoutRes
    private var parentLayout = -1

    @LayoutRes
    private var childLayout = -1

    private var renderer: Renderer<Any?, Any?>? = null

    lateinit var sections: MutableList<Section<Any?, Any?>>

    private var expandListener: ExpandCollapseListener.ExpandListener<Any?>? = null

    private var collapseListener: ExpandCollapseListener.CollapseListener<Any?>? = null

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
        orientation = VERTICAL
        sections = ArrayList()
        var typedArray: TypedArray? = null
        try {
            typedArray = context?.obtainStyledAttributes(attrs, R.styleable.ExpandableLayout)
            typedArray?.let {
                parentLayout = it.getResourceId(R.styleable.ExpandableLayout_parentLayout, NO_RES)
                childLayout = it.getResourceId(R.styleable.ExpandableLayout_childLayout, NO_RES)
                layoutInflater = LayoutInflater.from(context)
            }
        } finally {
            typedArray?.recycle()
        }
    }

    fun <P> setExpandListener(expandListener: ExpandCollapseListener.ExpandListener<Any?>?) {
        this.expandListener = expandListener
    }

    fun <P> setCollapseListener(collapseListener: ExpandCollapseListener.CollapseListener<Any?>?) {
        this.collapseListener = collapseListener
    }

    fun setRenderer(renderer: Renderer<Any?, Any?>) {
        this.renderer = renderer
    }

    fun addSection(section: Section<Any?, Any?>) {
        sections.add(section)
        notifySectionAdded(section)
    }

    fun <P : View, C : View> addChild(parent: P, child: C) {
        var parentIndex = NO_INDEX
        for (i in sections.indices) {
            if (sections[i].parent == parent) {
                if (sections[i].children.contains(child)) {
                    sections[i].children.add(child)
                }
                parentIndex = i
            }
        }
        if (parentIndex != NO_INDEX) {
            notifyItemAdded(parentIndex, child);
            if (sections[parentIndex].expanded) {
                expand(parent)
            }
        }
    }

    fun <P, C> addChildern(parent: P, children: List<C>) {
        var parentIndex = NO_INDEX
        for (i in sections.indices) {
            if (sections[i].parent == parent) {
                if (!sections[i].children.containsAll(children)) {
                    sections[i].children.addAll(children)
                }
                parentIndex = i
            }
        }
        if (parentIndex != NO_INDEX) {
            notifyItemAdded(parentIndex, children);
            if (sections[parentIndex].expanded) {
                expand(parent)
            }
        }
    }

    fun filterParent(op: Operation) {
        for (section in sections) {
            val parent = section.parent
            val contains = op.apply(parent)
            getChildAt(sections.indexOf(section)).visibility = if (contains) VISIBLE else GONE
        }
    }

    fun filterChildren(op: Operation) {
        currentFilter = op
        for (section in sections) {
            val children = section.children
            var keepParentVisible = false
            val childrenViews = getChildAt(sections.indexOf(section)) as ViewGroup
            for (i in children.indices) {
                val child = children[i]
                val contains = op.apply(child)
                childrenViews.getChildAt(children.indexOf(child) + 1).visibility = if (contains) VISIBLE else GONE
                if (!keepParentVisible && contains) {
                    keepParentVisible = true
                }
            }
            childrenViews.visibility = if (keepParentVisible) VISIBLE else GONE
        }
    }

    private fun <C : View> notifyItemAdded(parentIndex: Int, child: C) {
        if (renderer == null) return
        val parentView: ViewGroup = getChildAt(parentIndex) as ViewGroup
        val childView: View = layoutInflater.inflate(childLayout, null, false)
        renderer?.renderChild(childView, child, parentIndex, sections[parentIndex].children.size - 1)
        parentView.addView(childView)
    }

    private fun <C> notifyItemAdded(parentIndex: Int, children: List<C>) {
        if (renderer == null) return
        val parentView: ViewGroup = getChildAt(parentIndex) as ViewGroup
        for (i in children.indices) {
            val childView = layoutInflater.inflate(childLayout, null)
            renderer?.renderChild(childView, children[i], parentIndex, i)
            parentView.addView(childView)
        }
    }

    private fun notifySectionAdded(section: Section<*, *>) {
        if (renderer == null) return
        val sectionLayout = LinearLayout(context)
        sectionLayout.layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
        sectionLayout.orientation = LinearLayout.VERTICAL

        val parentView = layoutInflater.inflate(parentLayout, null, false)
        parentView.setOnClickListener {
            if (section.expanded) {
                collapse(section.parent)
            } else {
                expand(section.parent)
            }
        }
        renderer?.renderParent(parentView, section.parent, section.expanded, sections.size - 1)
        sectionLayout.addView(parentView)

        if (section.expanded) {
            for (i in section.children.indices) {
                val child = section.children[i]
                val childView: View = layoutInflater.inflate(childLayout, null, false)
                renderer?.renderChild(childView, child, sections.size - 1, i)
                sectionLayout.addView(childView)
            }
        }
        addView(sectionLayout)
    }

    fun notifyParentChanged(position: Int) {
        if (position > childCount - 1) {
            return
        }
        val viewGroup = getChildAt(position) as ViewGroup?
        if (viewGroup != null && viewGroup.childCount > 0) {
            val parentView = viewGroup.getChildAt(0)
            renderer?.renderParent(parentView, sections[position].parent, sections[position].expanded, position)
        }
    }

    private fun <P> expand(parent: P) {
        for (i in sections.indices) {
            if (parent == sections[i].parent) {
                val sectionView = getChildAt(i) as ViewGroup
                for (j in 1 until sectionView.childCount) {
                    val childView = sectionView.getChildAt(i)
                    val childType = sections[i].children[j - 1]
                    childView.visibility = if (currentFilter.apply(childType)) VISIBLE else GONE
                }
                sections[i].expanded = true
                expandListener?.onExpanded(i, sections[i].parent, sectionView.getChildAt(0))
                break
            }
        }
    }

    private fun <P> collapse(parent: P) {
        for (i in sections.indices) {
            if (parent == sections[i].parent) {
                val sectionView = getChildAt(i) as ViewGroup
                sections[i].expanded = false
                for (j in 1 until sectionView.childCount) {
                    sectionView.getChildAt(j).visibility = GONE
                }
                collapseListener?.onCollapsed(i, sections[i].parent, sectionView.getChildAt(0))
                break
            }
        }
    }

}