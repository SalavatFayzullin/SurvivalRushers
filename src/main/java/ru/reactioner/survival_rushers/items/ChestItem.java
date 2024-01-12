package ru.reactioner.survival_rushers.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;


public class ChestItem {
    private Material type;
    private List<ChestItemEnchantment> enchantments;
    private double chance;

    public ChestItem(Material type, double chance, ChestItemEnchantment... enchantments) {
        this.type = type;
        this.enchantments = List.of(enchantments);
        this.chance = chance;
    }

    public double getChance() {
        return chance;
    }

    public void addEnchantment(ChestItemEnchantment enchantment) {
        enchantments.add(enchantment);
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(type);
        ItemMeta meta = item.getItemMeta();
        enchantments.forEach(enchantment -> {
            int level = enchantment.getLevel();
            if (level == 0) return;
            meta.addEnchant(enchantment.getEnchantment(), level, true);
        });
        item.setItemMeta(meta);
        return item;
    }
}
