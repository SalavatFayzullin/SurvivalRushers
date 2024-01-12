package ru.reactioner.survival_rushers.game.state;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import ru.reactioner.survival_rushers.MainPlugin;
import ru.reactioner.survival_rushers.game.Game;
import ru.reactioner.survival_rushers.game.GamePhase;

import java.util.Random;

public class ActiveState implements State {
    private Game game;
    private int currentPhase;
    private int timer;
    private BukkitTask task;
    private BukkitTask chestSpawnTask;
    private BukkitTask worldBorderChangeTask;
    private int worldBorderChangeTaskTimer;
    private int chestSpawnIntervalTimer;

    public ActiveState(Game game) {
        this.game = game;
        timer = MainPlugin.getInstance().getFirstPhaseDuration();
    }

    @Override
    public void start() {
        changeWorldBorderRadius(MainPlugin.getInstance().getFirstPhaseDiameter(), 20 * 10);
        game.getWorld().setDifficulty(Difficulty.NORMAL);
        game.setBarColor(BarColor.PINK);
        game.getPlayers().forEach(player -> {
            player.getInventory().clear();
            game.unleashPlayer(player);
            player.sendMessage(Component.text("Игра началась!").color(TextColor.color(255, 0, 0)));
            player.setGameMode(GameMode.SURVIVAL);
        });
        currentPhase = 1;
        game.setBarProgress(0D);
        game.setBarTitle("Фаза %d(%d/%d)".formatted(currentPhase, MainPlugin.getInstance().getFirstPhaseDuration() - timer, MainPlugin.getInstance().getFirstPhaseDuration()));
        chestSpawnIntervalTimer = MainPlugin.getInstance().getChestSpawnInterval();
        chestSpawnTask = Bukkit.getScheduler().runTaskTimer(MainPlugin.getInstance(), () -> {
            chestSpawnIntervalTimer--;
            if (chestSpawnIntervalTimer <= 0) {
                int diameter;
                if (currentPhase == 1) diameter = MainPlugin.getInstance().getFirstPhaseDiameter();
                else if (currentPhase == 2) diameter = MainPlugin.getInstance().getSecondPhaseDiameter();
                else diameter = MainPlugin.getInstance().getThirdPhaseDiameter();
                int x = game.getWorld().getWorldBorder().getCenter().getBlockX(), z = game.getWorld().getWorldBorder().getCenter().getBlockZ();
                Random random = new Random();
                x += random.nextInt(-diameter / 2, diameter / 2);
                z += random.nextInt(-diameter / 2, diameter / 2);
                int y = game.getWorld().getHighestBlockYAt(x, z) + 1;
                game.getWorld().setType(x, y, z, Material.CHEST);
                Chest chest = (Chest) game.getWorld().getBlockState(x, y, z);
                int cur = random.nextInt(0, 9);
                while (cur < 27) {
                    ItemStack itemStack = game.getItemsManager().getItemForChest();
                    if (itemStack != null) chest.getBlockInventory().setItem(cur, itemStack);
                    cur += random.nextInt(0, 9);
                }
                chestSpawnIntervalTimer = MainPlugin.getInstance().getChestSpawnInterval();
                int finalX = x;
                int finalZ = z;
                game.getPlayers().forEach(player -> player.sendMessage(Component.text("Сундук появился на X: %d, Y: %d, Z: %d".formatted(finalX, y, finalZ))));
            }
        }, 0, 20);
        task = Bukkit.getScheduler().runTaskTimer(MainPlugin.getInstance(), () -> {
            if (currentPhase == 1) {
                timer--;
                if (timer <= 0) {
                    currentPhase = 2;
                    timer = MainPlugin.getInstance().getSecondPhaseDuration();
                    game.setBarColor(BarColor.BLUE);
                    changeWorldBorderRadius(MainPlugin.getInstance().getSecondPhaseDuration(), 20 * 10);
                } else {
                    game.setBarProgress((double) (MainPlugin.getInstance().getFirstPhaseDuration() - timer) / MainPlugin.getInstance().getFirstPhaseDuration());
                    game.setBarTitle("Фаза %d(%d/%d)".formatted(currentPhase, MainPlugin.getInstance().getFirstPhaseDuration() - timer, MainPlugin.getInstance().getFirstPhaseDuration()));
                }
            } else if (currentPhase == 2) {
                timer--;
                if (timer <= 0) {
                    currentPhase = 3;
                    timer = MainPlugin.getInstance().getThirdPhaseDuration();
                    game.setBarColor(BarColor.PURPLE);
                    changeWorldBorderRadius(MainPlugin.getInstance().getThirdPhaseDiameter(), 20 * 10);
                } else {
                    game.setBarProgress((double) (MainPlugin.getInstance().getSecondPhaseDuration() - timer) / MainPlugin.getInstance().getSecondPhaseDuration());
                    game.setBarTitle("Фаза %d(%d/%d)".formatted(currentPhase, MainPlugin.getInstance().getSecondPhaseDuration() - timer, MainPlugin.getInstance().getSecondPhaseDuration()));
                }
            } else {
                timer--;
                if (timer <= 0) {
                    game.setState(new DeathmatchState(game));
                } else {
                    game.setBarProgress((double) (MainPlugin.getInstance().getThirdPhaseDuration() - timer) / MainPlugin.getInstance().getThirdPhaseDuration());
                    game.setBarTitle("Фаза %d(%d/%d)".formatted(currentPhase, MainPlugin.getInstance().getThirdPhaseDuration() - timer, MainPlugin.getInstance().getThirdPhaseDuration()));
                }
            }
        }, 0, 20);
    }

    private void changeWorldBorderRadius(double newRadius, int durationInTicks) {
        double delta = (newRadius - game.getWorld().getWorldBorder().getSize()) / durationInTicks;
        worldBorderChangeTaskTimer = durationInTicks;
        worldBorderChangeTask = Bukkit.getScheduler().runTaskTimer(MainPlugin.getInstance(), () -> {
            game.getWorld().getWorldBorder().setSize(game.getWorld().getWorldBorder().getSize() + delta);
            worldBorderChangeTaskTimer--;
            if (worldBorderChangeTaskTimer <= 0) worldBorderChangeTask.cancel();
        }, 0, 1);
    }

    @Override
    public void cancel() {
        if (task != null && !task.isCancelled()) task.cancel();
        task = null;
        if (chestSpawnTask != null && !chestSpawnTask.isCancelled()) chestSpawnTask.cancel();
        chestSpawnTask = null;
        if (worldBorderChangeTask != null && !worldBorderChangeTask.isCancelled()) worldBorderChangeTask.cancel();
        worldBorderChangeTask = null;
    }

    @Override
    public boolean canJoin() {
        return true;
    }

    @Override
    public GamePhase getPhase() {
        return GamePhase.ACTIVE;
    }

    @Override
    public void addPlayer(Player player) {
        game.onSpectatorJoined(player);
    }

    @Override
    public void removePlayer(Player player) {
        game.checkForWinner();
    }
}
