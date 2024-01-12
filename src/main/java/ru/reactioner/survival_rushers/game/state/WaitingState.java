package ru.reactioner.survival_rushers.game.state;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import ru.reactioner.survival_rushers.game.Game;
import ru.reactioner.survival_rushers.game.GamePhase;

public class WaitingState implements State {
    private Game game;

    public WaitingState(Game game) {
        this.game = game;
    }

    @Override
    public void start() {
        game.getWorld().setDifficulty(Difficulty.PEACEFUL);
        game.setBarTitle("Ожидание %d/%d".formatted(game.getOnline(), game.getMaxPlayerCount()));
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean canJoin() {
        return true;
    }

    @Override
    public GamePhase getPhase() {
        return GamePhase.WAITING;
    }

    @Override
    public void addPlayer(Player player) {
        game.setBarTitle("Ожидание %d/%d".formatted(game.getOnline(), game.getMaxPlayerCount()));
        game.setBarProgress((double) game.getOnline() / game.getMaxPlayerCount());
        player.setGameMode(GameMode.ADVENTURE);
        if (game.getOnline() == game.getMinPlayerCount()) game.setState(new StartingState(game));
    }

    @Override
    public void removePlayer(Player player) {
        game.setBarTitle("Ожидание %d/%d".formatted(game.getOnline(), game.getMaxPlayerCount()));
        game.setBarProgress((double) game.getOnline() / game.getMaxPlayerCount());
    }
}
