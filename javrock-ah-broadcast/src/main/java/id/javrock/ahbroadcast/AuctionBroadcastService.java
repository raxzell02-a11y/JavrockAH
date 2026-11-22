package id.javrock.ahbroadcast;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionBroadcastService {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final JavrockAHBroadcastPlugin plugin;
    private final Map<UUID, Long> lastBroadcastMillis = new ConcurrentHashMap<>();

    private Set<String> watchedCommands = new HashSet<>();
    private Set<String> sellSubcommands = new HashSet<>();
    private long cooldownMillis;
    private String currencySymbol;
    private boolean preferCustomItemName;
    private boolean smallcapsItemName;
    private String broadcastTemplate;

    public AuctionBroadcastService(JavrockAHBroadcastPlugin plugin) {
        this.plugin = plugin;
        reloadSettings();
    }

    public void reloadSettings() {
        List<String> cmds = plugin.getConfig().getStringList("settings.watched-commands");
        this.watchedCommands = new HashSet<>();
        for (String c : cmds) {
            this.watchedCommands.add(c.toLowerCase(Locale.ROOT));
        }

        List<String> subs = plugin.getConfig().getStringList("settings.sell-subcommands");
        this.sellSubcommands = new HashSet<>();
        for (String s : subs) {
            this.sellSubcommands.add(s.toLowerCase(Locale.ROOT));
        }

        int cdSec = Math.max(0, plugin.getConfig().getInt("settings.cooldown-seconds", 3));
        this.cooldownMillis = cdSec * 1000L;
        this.currencySymbol = plugin.getConfig().getString("settings.currency-symbol", "$");
        this.preferCustomItemName = plugin.getConfig().getBoolean("settings.prefer-custom-item-name", true);
        this.smallcapsItemName = plugin.getConfig().getBoolean("settings.smallcaps-item-name", true);
        this.broadcastTemplate = plugin.getConfig().getString(
                "messages.sell-broadcast",
                "&9&lᴊᴀᴠʀᴏᴄᴋ &8» &b%player% &7ᴛᴇʟᴀʜ ᴍᴇɴᴊᴜᴀʟ ʙᴀʀᴀɴɢ &3%item% &7ꜱᴇʜᴀʀɢᴀ &b%price% &7ᴅɪ &3/ᴀʜ"
        );
    }

    public void handleCommand(Player player, String rawCommand) {
        if (player == null || rawCommand == null || !rawCommand.startsWith("/")) {
            return;
        }

        String trimmed = rawCommand.substring(1).trim();
        if (trimmed.isEmpty()) {
            return;
        }

        String[] parts = trimmed.split("\s+");
        if (parts.length < 3) {
            return;
        }

        String baseCmd = parts[0].toLowerCase(Locale.ROOT);
        if (baseCmd.contains(":")) {
            baseCmd = baseCmd.substring(baseCmd.indexOf(':') + 1);
        }

        String subCmd = parts[1].toLowerCase(Locale.ROOT);
        if (!watchedCommands.contains(baseCmd) || !sellSubcommands.contains(subCmd)) {
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR || itemInHand.getAmount() <= 0) {
            return;
        }

        long now = System.currentTimeMillis();
        Long last = lastBroadcastMillis.get(player.getUniqueId());
        if (last != null && (now - last) < cooldownMillis) {
            return;
        }
        lastBroadcastMillis.put(player.getUniqueId(), now);

        String rawPrice = parts[2].replace(",", "").trim();
        String formattedPrice = formatPrice(rawPrice);
        String itemName = resolveItemName(itemInHand);
        int amount = itemInHand.getAmount();

        broadcastSale(player.getName(), itemName, amount, formattedPrice);
    }

    public void broadcastSale(String playerName, String itemName, int amount, String formattedPrice) {
        String rendered = broadcastTemplate
                .replace("%player%", playerName)
                .replace("%item%", itemName)
                .replace("%amount%", String.valueOf(amount))
                .replace("%price%", formattedPrice);

        Component component = LEGACY.deserialize(rendered);
        Bukkit.getServer().sendMessage(component);
    }

    private String formatPrice(String rawPrice) {
        if (rawPrice.startsWith(currencySymbol)) {
            return rawPrice;
        }
        return currencySymbol + rawPrice;
    }

    private String resolveItemName(ItemStack item) {
        String name = null;
        if (preferCustomItemName && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                Component display = meta.displayName();
                if (display != null) {
                    String plain = PLAIN.serialize(display).trim();
                    if (!plain.isEmpty()) {
                        name = plain;
                    }
                }
            }
        }
        if (name == null) {
            name = item.getType().name().toLowerCase(Locale.ROOT).replace('_', ' ');
        }
        return smallcapsItemName ? toSmallCaps(name) : name;
    }

    public static String toSmallCaps(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = Character.toLowerCase(input.charAt(i));
            switch (c) {
                case 'a' -> sb.append('ᴀ');
                case 'b' -> sb.append('ʙ');
                case 'c' -> sb.append('ᴄ');
                case 'd' -> sb.append('ᴅ');
                case 'e' -> sb.append('ᴇ');
                case 'f' -> sb.append('ꜰ');
                case 'g' -> sb.append('ɢ');
                case 'h' -> sb.append('ʜ');
                case 'i' -> sb.append('ɪ');
                case 'j' -> sb.append('ᴊ');
                case 'k' -> sb.append('ᴋ');
                case 'l' -> sb.append('ʟ');
                case 'm' -> sb.append('ᴍ');
                case 'n' -> sb.append('ɴ');
                case 'o' -> sb.append('ᴏ');
                case 'p' -> sb.append('ᴘ');
                case 'q' -> sb.append('ǫ');
                case 'r' -> sb.append('ʀ');
                case 's' -> sb.append('ꜱ');
                case 't' -> sb.append('ᴛ');
                case 'u' -> sb.append('ᴜ');
                case 'v' -> sb.append('ᴠ');
                case 'w' -> sb.append('ᴡ');
                case 'x' -> sb.append('x');
                case 'y' -> sb.append('ʏ');
                case 'z' -> sb.append('ᴢ');
                default -> sb.append(input.charAt(i));
            }
        }
        return sb.toString();
    }
}
