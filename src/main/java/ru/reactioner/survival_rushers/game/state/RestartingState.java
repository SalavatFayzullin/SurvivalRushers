package ru.reactioner.survival_rushers.game.state;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ru.reactioner.survival_rushers.MainPlugin;
import ru.reactioner.survival_rushers.game.Game;
import ru.reactioner.survival_rushers.game.GamePhase;

public class RestartingState implements State {
    private Game game;
    private int secondsBeforeRestart;
    private BukkitTask task;

    public RestartingState(Game game) {
        this.game = game;
    }

    @Override
    public void start() {
        secondsBeforeRestart = MainPlugin.getInstance().getRestartingDuration();
        game.setBarColor(BarColor.RED);
        game.setBarProgress(0.0);
        game.setBarTitle("Рестарт через %s секунд".formatted(secondsBeforeRestart));
        task = Bukkit.getScheduler().runTaskTimer(MainPlugin.getInstance(), () -> {
            secondsBeforeRestart--;
            if (secondsBeforeRestart <= 0) {
                task.cancel();
                game.resetGame();
            } else {
                game.setBarTitle("Рестарт через %s секунд".formatted(secondsBeforeRestart));
                game.setBarProgress((double) secondsBeforeRestart / MainPlugin.getInstance().getRestartingDuration());
            }
        }, 0, 20);
    }

    @Override
    public void cancel() {
        if (task != null) task.cancel();
        task = null;
    }

    @Override
    public boolean canJoin() {
        return false;
    }

    @Override
    public GamePhase getPhase() {
        return GamePhase.RESTARTING;
    }

    @Override
    public void addPlayer(Player player) {
        game.makePlayerSpectator(player);
    }

    @Override
    public void removePlayer(Player player) {

    }
}
