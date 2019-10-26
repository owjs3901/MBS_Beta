package mbs.beta.event_message

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.Arrays
import java.util.Vector

public class EventMessage : JavaPlugin(), Listener {

    val placeholderAppliers: List<PlaceholderApplier> = Arrays.asList(
            object:PlaceholderApplier(
                    object:Placeholder() {
                        override fun Replace(event: Event, string: String?): String? {
                            val playerEvent = event as PlayerEvent
                            return string?.replace("<name>", playerEvent.player.name)
                        }
                    }
            ) {
                override fun isAppliable(event: Event): Boolean {
                    return event is PlayerEvent
                }
            }
    )
    val variables: HashMap<Class<out Event>, HashMap<Field, String?>> = HashMap()
    val eventExecutor: EventExecutor = EventExecutor { _, event ->
        val clazz: Class<out Event> = event.javaClass;
        if (variables.containsKey(clazz)) {
            val fields = variables.get(clazz)
            for (entry in fields?.entries!!) {
                var value = entry.value
                val field = entry.key;
                if (!value?.isEmpty()!!) {
                    for (placeholderApplier in placeholderAppliers) {
                        value = placeholderApplier.Apply(event, value)
                    }
                    field.isAccessible = true
                    field.set(event, value)
                }
            }
        }
    }
    var join: String = "${ChatColor.YELLOW}<name>님이 들어왔습니다!"
    var quit: String = "${ChatColor.YELLOW}<name>님이 나갔습니다!"

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)

        val classes: Field? = ClassLoader::class.java.getDeclaredField("classes");
        classes?.isAccessible = true;

        @Suppress("UNCHECKED_CAST") // Field 'classes' must be type 'Vector'
        for (clazz in ArrayList(classes?.get(ClassLoader.getSystemClassLoader()) as Vector<Class<*>>)) {
            if (Event::class.java.isAssignableFrom(clazz)) {
                try {
                    clazz.getDeclaredField("handlers")
                    val fields: HashMap<Field, String?> = HashMap()
                    val eventClass = clazz as Class<out Event>
                    variables.put(eventClass, fields)
                    for (field in clazz.declaredFields) {
                        val modifiers: Int = field.modifiers
                        if (!Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers) && String::class.java.isAssignableFrom(field.type)) {
                            val path: String = clazz.name + "." + field.name;
                            var value: String?
                            if (config.contains(path)) {
                                value = config.getString(path)
                            } else {
                                value = ""
                                config.set(path, value)
                            }
                            fields.put(field, value)
                        }
                    }
                    if (!fields.isEmpty()) {
                        Bukkit.getPluginManager().registerEvent(eventClass, this, EventPriority.HIGH, eventExecutor, this)
                    }
                } catch(e: NoSuchFieldException) {}
            }
        }
        saveConfig()
    }

}

abstract class PlaceholderApplier constructor(vararg val placeholders: Placeholder) {

    abstract fun isAppliable(event: Event): Boolean

    fun Apply(event: Event, string: String?): String? {
        var result = string
        if (isAppliable(event)) {
            for (placeholder in placeholders) {
                result = placeholder.Replace(event, result)
            }
        }
        return result
    }

}

abstract class Placeholder { abstract fun Replace(event: Event, string: String?): String? }
