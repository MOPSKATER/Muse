package mopskater.muse;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class Muse extends JavaPlugin implements Listener {

    private final ArrayList<Player> musicianMode = new ArrayList<>();

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
    public void onItemSwitch(@NotNull PlayerItemHeldEvent event){
        if (!musicianMode.contains(event.getPlayer()))
            return;

        if (event.getPlayer().isSneaking()){
            PlayerInventory inventory = event.getPlayer().getInventory();
            ItemStack[] savedItems = new ItemStack[9];

            int direction;

            int res = event.getNewSlot() - event.getPreviousSlot();
            direction = res == 1 || res == -8 ? -9 : 9;

            for (int i = 0; i < 9; i++){
                savedItems[i] = inventory.getItem(i);
                inventory.setItem(modulus(i, 36), inventory.getItem(modulus(i + direction, 36)));
                inventory.setItem(modulus(i + direction, 36), inventory.getItem(modulus(i + 2 * direction, 36)));
                inventory.setItem(modulus(i + 2 * direction, 36), inventory.getItem(modulus(i + 3 * direction, 36)));
                inventory.setItem(modulus(i + 3 * direction, 36), savedItems[i]);
            }
        }
    }

    private int modulus(int x, int modulus){
        if (x < 0) {
            return modulus + x;
        }
        return x % modulus;
    }

    @EventHandler
    private void onPlayerInteract(@NotNull PlayerInteractEvent event){
        if (!musicianMode.contains(event.getPlayer()))
            return;

        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.NOTE_BLOCK)
            return;

        if (!event.getPlayer().isSneaking())
            return;

        if (event.getAction().isLeftClick()){
            ItemStack noteBlock = new ItemStack(Material.NOTE_BLOCK, 1);
            ItemMeta meta = noteBlock.getItemMeta();
            int note = ((NoteBlock)clicked.getBlockData()).getNote().getId();
            meta.displayName(Component.text("§5Note: " + note));
            meta.getPersistentDataContainer().set(new NamespacedKey(this, "note"), PersistentDataType.INTEGER, note);
            noteBlock.setItemMeta(meta);
            PlayerInventory inventory = event.getPlayer().getInventory();
            inventory.setItem(inventory.getHeldItemSlot(), noteBlock);
            event.setCancelled(true);
        }
        else{
            Bukkit.getScheduler().runTaskLater(this, () -> {
                Block block = clicked.getWorld().getBlockAt(clicked.getLocation());
                NoteBlock noteBlock = (NoteBlock) block.getBlockData();
                noteBlock.setNote(new Note(modulus(noteBlock.getNote().getId() - 2, 25)));
                block.setBlockData(noteBlock);
            }, 1L);
        }

    }

    @EventHandler
    public void onPlayerBlockPlace(@NotNull BlockPlaceEvent event){
        if (!musicianMode.contains(event.getPlayer()))
            return;

        ItemStack item = event.getItemInHand();
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().has(new NamespacedKey(this, "note"))){
            Block noteblock = event.getBlockPlaced();
            NoteBlock noteBlockData = (NoteBlock) noteblock.getBlockData();
            //noinspection ConstantConditions
            noteBlockData.setNote(new Note(meta.getPersistentDataContainer().get(new NamespacedKey(this, "note"), PersistentDataType.INTEGER)));
            noteblock.setBlockData(noteBlockData);
        }
    }

}
