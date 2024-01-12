package ru.reactioner.survival_rushers.game.state;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ru.reactioner.survival_rushers.MainPlugin;
import ru.reactioner.survival_rushers.game.Game;
import ru.reactioner.survival_rushers.game.GamePhase;

public class DeathmatchState implements State {
    private Game game;
    private BukkitTask worldBorderChangeTask;
    private BukkitTask damagerTask;
    private double damage;

    public DeathmatchState(Game game) {
        this.game = game;
    }

    @Override
    public void start() {
        game.onDeathmatchStarted();
        damage = 0.5;
        worldBorderChangeTask = Bukkit.getScheduler().runTaskTimer(MainPlugin.getInstance(), () -> {
            if (game.getWorld().getWorldBorder().getSize() <= 0) worldBorderChangeTask.cancel();
            else game.getWorld().getWorldBorder().setSize(game.getWorld().getWorldBorder().getSize() - 1.0);
        }, 0, 20);
        damagerTask = Bukkit.getScheduler().runTaskTimer(MainPlugin.getInstance(), () -> {
            Player[] players = new Player[game.getPlayers().size()];
            game.getPlayers().toArray(players);
            for (Player p : players) p.damage(damage);
            damage += 0.5;
        }, 0, 20 * 5);
    }

    @Override
    public void cancel() {
        if (worldBorderChangeTask != null && !worldBorderChangeTask.isCancelled()) worldBorderChangeTask.cancel();
        if (damagerTask != null && !damagerTask.isCancelled()) damagerTask.cancel();
        worldBorderChangeTask = null;
        damagerTask = null;
    }

    @Override
    public boolean canJoin() {
        return true;
    }

    @Override
    public GamePhase getPhase() {
        return GamePhase.DEATHMATCH;
    }

    @Override
    public void addPlayer(Player player) {
        game.makePlayerSpectator(player);
    }

    @Override
    public void removePlayer(Player player) {
        game.checkForWinner();
    }
}
