package mbs.beta.economy

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

class Economy : JavaPlugin(), Listener {

    private var time: Long = 10
    private var timeMoney = 10
    private var data: FileConfiguration? = null;
    private val dataFile = File("plugins/Economy/money.yml")

    private val moneyMap = HashMap<UUID, Int>()
    private val timeMap = HashMap<UUID, Int>()


    override fun onEnable() {
        var b = false
        if (config.contains("timeMoney"))
            timeMoney = config.getInt("timeMoney")
        else {
            b = true
            config.set("timeMoney", timeMoney)
        }

        if (config.contains("time"))
            time = config.getLong("time")
        else {
            b = true
            config.set("timeMoney", time)
        }
        if (b) saveConfig()
        if (!dataFile.exists()) {
            dataFile.createNewFile()
        }
        data = YamlConfiguration.loadConfiguration(dataFile)
        Bukkit.getPluginManager().registerEvents(this, this)

		Bukkit.getOnlinePlayers().forEach({ p: Player ->
			moneyMap.put(p.uniqueId,config.getInt(p.uniqueId.toString()))
		})

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, {
            Bukkit.getOnlinePlayers().forEach({ p: Player ->
                val t: Int = timeMap.get(p.uniqueId)?.let { it }?:timeMap.put(p.uniqueId,0)?:0
                if (t?.toLong() == time) {
                    timeMap.put(p.uniqueId, 0)
                    moneyMap.get(p.uniqueId)?.plus(timeMoney)?.let { moneyMap.put(p.uniqueId, it); saveData(p.uniqueId) }
                } else
					timeMap.get(p.uniqueId)?.plus(1)?.let { timeMap.put(p.uniqueId, it) }
            })
        }, 20L*60, 20L*60);
    }

    override fun onDisable() {}

    fun saveData(uuid: UUID) {
        data?.set(uuid.toString(), moneyMap.get(uuid))
        data?.save(dataFile)
    }

    @EventHandler
    fun onPlayerJoinEvent(e: PlayerJoinEvent) {
        if (!moneyMap.containsKey(e.player.uniqueId)) {
            moneyMap.put(e.player.uniqueId, 0)
            saveData(e.player.uniqueId)
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player)
            moneyMap.get(sender.uniqueId)?.let { sender.sendMessage("현재 소지금은 $it 원입니다") }?:sender.sendMessage("데이터가 없습니다")


        return super.onCommand(sender, command, label, args)
    }
}
