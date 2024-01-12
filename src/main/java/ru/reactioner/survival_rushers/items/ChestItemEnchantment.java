package ru.reactioner.survival_rushers.items;

import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

public class ChestItemEnchantment {
    private Enchantment enchantment;
    private Map<Integer, Double> chances;

    public ChestItemEnchantment(Enchantment enchantment) {
        this.enchantment = enchantment;
        chances = new HashMap<>();
    }

    public void addLevel(int level, double chance) {
        chances.put(level, chance);
    }

    public int getLevel() {
        double currentProba = 0.0, neededProba = Math.random();
        for (Map.Entry<Integer, Double> entry : chances.entrySet()) {
            if (currentProba <= neededProba && neededProba <= currentProba + entry.getValue()) return entry.getKey();
            currentProba += entry.getValue();
        }
        return 0;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }
}
