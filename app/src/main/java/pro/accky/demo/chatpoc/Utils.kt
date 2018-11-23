package pro.accky.demo.chatpoc

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class KeyProperty: ReadOnlyProperty<Any, String> {
    override fun getValue(thisRef: Any, property: KProperty<*>): String = property.name
}

fun key() = KeyProperty()