package ru.reactioner.survival_rushers.game.state;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ru.reactioner.survival_rushers.MainPlugin;
import ru.reactioner.survival_rushers.game.Game;
import ru.reactioner.survival_rushers.game.GamePhase;

public class StartingState implements State {
    private Game game;
    private int secondsBeforeStart;
    private BukkitTask task;

    public StartingState(Game game) {
        this.game = game;
        secondsBeforeStart = game.getSecondsBeforeStart();
    }

    @Override
    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(MainPlugin.getInstance(), () -> {
            secondsBeforeStart--;
            if (secondsBeforeStart <= 0) {
                game.setState(new ActiveState(game));
            } else {
                game.getPlayers().forEach(player -> {
                    player.setExp((float) secondsBeforeStart / game.getSecondsBeforeStart());
                    player.setLevel(secondsBeforeStart);
                });
            }
        }, 0, 20);
    }

    @Override
    public void cancel() {
        game.getPlayers().forEach(p -> {
            game.unleashPlayer(p);
        });
        if (task == null) return;
        task.cancel();
        task = null;
    }

    @Override
    public boolean canJoin() {
        return true;
    }

    @Override
    public GamePhase getPhase() {
        return GamePhase.STARTING;
    }

    @Override
    public void addPlayer(Player player) {
        game.updateBar();
        player.setGameMode(GameMode.ADVENTURE);
    }

    @Override
    public void removePlayer(Player player) {
        game.updateBar();
        if (game.getOnline() < game.getMinPlayerCount()) game.setState(new WaitingState(game));
    }
}
