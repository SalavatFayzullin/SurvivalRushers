package ru.reactioner.survival_rushers.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ItemsManager implements Listener {
    private static final ChestItemEnchantment PROTECTION;
    private static final ChestItemEnchantment SHARPNESS;
    private static final ChestItemEnchantment DURABILITY;
    private static final List<ChestItem> chestItems;

    static {
        PROTECTION = new ChestItemEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
        PROTECTION.addLevel(1, 0.3);
        PROTECTION.addLevel(2, 0.2);
        PROTECTION.addLevel(3, 0.1);
        SHARPNESS = new ChestItemEnchantment(Enchantment.DAMAGE_ALL);
        SHARPNESS.addLevel(1, 0.3);
        SHARPNESS.addLevel(2, 0.2);
        SHARPNESS.addLevel(3, 0.1);
        DURABILITY = new ChestItemEnchantment(Enchantment.DURABILITY);
        DURABILITY.addLevel(1, 0.3);
        DURABILITY.addLevel(2, 0.2);
        DURABILITY.addLevel(3, 0.1);
        chestItems = new ArrayList<>();
        chestItems.add(new ChestItem(Material.LEATHER_HELMET, 0.1, PROTECTION, DURABILITY));
        chestItems.add(new ChestItem(Material.LEATHER_CHESTPLATE, 0.1, PROTECTION, DURABILITY));
        chestItems.add(new ChestItem(Material.LEATHER_LEGGINGS, 0.1, PROTECTION, DURABILITY));
        chestItems.add(new ChestItem(Material.LEATHER_BOOTS, 0.1, PROTECTION, DURABILITY));
        chestItems.add(new ChestItem(Material.WOODEN_SWORD, 0.1, SHARPNESS));
        chestItems.add(new ChestItem(Material.STONE_SWORD, 0.1, SHARPNESS));
    }

    private List<SurvivalItem> items;

    public ItemsManager() {
        items = new LinkedList<>();
    }

    public void addItemAction(SurvivalItem item) {
        items.add(item);
    }

    public ItemStack getItemForChest() {
        double currentProba = 0.0, neededProba = Math.random();
        for (ChestItem item : chestItems) {
            if (currentProba <= neededProba && neededProba <= currentProba + item.getChance()) return item.getItem();
            currentProba += item.getChance();
        }
        return null;
    }

    @EventHandler
    private void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        for (SurvivalItem item : items) {
            if (item.identify(event.getCurrentItem())) {
                event.setCancelled(true);
                item.doAction((Player) event.getWhoClicked());
                break;
            }
        }
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        for (SurvivalItem item : items) {
            if (item.identify(event.getItem())) {
                event.setCancelled(true);
                item.doAction(event.getPlayer());
                break;
            }
        }
    }

    @EventHandler
    private void onDrop(PlayerDropItemEvent event) {
        for (SurvivalItem item : items) {
            if (item.identify(event.getItemDrop().getItemStack())) {
                event.setCancelled(true);
                item.doAction(event.getPlayer());
                break;
            }
        }
    }
}
