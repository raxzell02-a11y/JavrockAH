package id.javrock.ahbroadcast;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandListener implements Listener {

    private final JavrockAHBroadcastPlugin plugin;
    private final AuctionBroadcastService broadcastService;

    public PlayerCommandListener(JavrockAHBroadcastPlugin plugin, AuctionBroadcastService broadcastService) {
        this.plugin = plugin;
        this.broadcastService = broadcastService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        broadcastService.handleCommand(player, message);
    }
}
