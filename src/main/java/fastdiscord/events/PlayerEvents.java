package fastdiscord.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import fastdiscord.fastDiscord;

import java.util.ArrayList;
import java.util.List;

public class PlayerEvents implements Listener {
    fastDiscord plugin;
    private final List<Player> onlinePlayers = new ArrayList<Player>();

    public PlayerEvents(fastDiscord plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        onlinePlayers.add(event.getPlayer());
        plugin.updateStatus();
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        onlinePlayers.remove(event.getPlayer());
        plugin.updateStatus();
    }
    public List<Player> getOnlinePlayers(){
        return onlinePlayers;
    }
}
