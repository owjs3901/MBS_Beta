package mbs.beta.event_message

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

public class EventMessage : JavaPlugin(), Listener {


    var join: String = "${ChatColor.YELLOW}<name>님이 들어왔습니다!";
    var quit: String = "${ChatColor.YELLOW}<name>님이 나갔습니다!";

    override fun onEnable() {
        super.onEnable()
        Bukkit.getPluginManager().registerEvents(this, this);
        var b = false
        if (config.contains("playerJoin"))
            join = config.getString("playerJoin")!!
        else {
            config.set("playerJoin", join)
            b = true
        }
        if (config.contains("playerQuit"))
            quit = config.getString("playerQuit")!!
        else {
            config.set("playerQuit", quit)
            b = true
        }
        if (b) saveConfig()

    }

    @EventHandler
    public fun onJoinEvent(e: PlayerJoinEvent) {
        e.joinMessage = join.replace("<name>", e.player.name)
    }

    @EventHandler
    public fun onQuitEvent(e: PlayerQuitEvent) {
        e.quitMessage = quit.replace("<name>", e.player.name)
    }
}