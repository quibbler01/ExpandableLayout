package cn.quibbler.expandablelayout

class Section<P, C> {

    var expanded = false

    var parent: P? = null

    var children: List<C> = ArrayList()

}