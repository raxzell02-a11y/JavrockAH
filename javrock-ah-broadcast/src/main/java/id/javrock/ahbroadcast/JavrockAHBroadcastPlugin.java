package id.javrock.ahbroadcast;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class JavrockAHBroadcastPlugin extends JavaPlugin {

    private AuctionBroadcastService broadcastService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.broadcastService = new AuctionBroadcastService(this);
        getServer().getPluginManager().registerEvents(new PlayerCommandListener(this, broadcastService), this);

        PluginCommand cmd = getCommand("jahb");
        if (cmd != null) {
            BroadcastCommandExecutor executor = new BroadcastCommandExecutor(this, broadcastService);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        getLogger().info("JavrockAHBroadcast aktif untuk Paper 1.21.11.");
    }

    @Override
    public void onDisable() {
        getLogger().info("JavrockAHBroadcast dinonaktifkan.");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        if (broadcastService != null) {
            broadcastService.reloadSettings();
        }
    }
}
