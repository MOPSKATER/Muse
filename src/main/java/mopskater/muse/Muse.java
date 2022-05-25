package mopskater.muse;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public final class Muse extends JavaPlugin implements Listener {

    private final ArrayList<Player> musicianMode = new ArrayList<>();
    private final HashMap<UUID, ItemStack[]> playerHotbar = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("You must be a Player");
            return true;
        }

        if (!player.hasPermission("muse.musemode")) {
            player.sendMessage("Insufficient permissions");
            return true;
        }

        if (musicianMode.contains(player)) {
            musicianMode.remove(player);
            player.sendMessage("§cYou left musician mode");
        }
        else {
            musicianMode.add(player);
            player.sendMessage("§aYou entered musician mode");
        }
        return true;
    }

    @EventHandler
    public void onPlayerBlockPlace(BlockPlaceEvent event){
    }

}
