package cn.quibbler.expandablelayout

import android.view.View

class Section<P, C> {

    var expanded = false

    var parent: P? = null

    var children: MutableList<C> = ArrayList()

}