package ru.reactioner.survival_rushers.items;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SurvivalItem {
    private ItemStack item;
    private Consumer<Player> action;

    public SurvivalItem(ItemStack item, Consumer<Player> action) {
        this.item = item;
        this.action = action;
    }

    public boolean identify(ItemStack other) {
        if (other.getItemMeta() == null) return false;
        if (Objects.equals(item.getItemMeta().displayName(), other.getItemMeta().displayName())) return true;
        else return false;
    }

    public void doAction(Player player) {
        action.accept(player);
    }

    public void changeName(Component name) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        item.setItemMeta(meta);
    }

    public void changeName(List<Component> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.lore(lore);
        item.setItemMeta(meta);
    }
}
