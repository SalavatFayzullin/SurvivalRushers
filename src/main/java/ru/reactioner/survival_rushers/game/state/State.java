package ru.reactioner.survival_rushers.game.state;

import org.bukkit.entity.Player;
import ru.reactioner.survival_rushers.game.GamePhase;

public interface State {
    void start();
    void cancel();
    boolean canJoin();
    GamePhase getPhase();
    void addPlayer(Player player);
    void removePlayer(Player player);
}
