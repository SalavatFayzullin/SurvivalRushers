package ru.reactioner.survival_rushers.db;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlayerManager implements Listener {
    private PlayerRepository repository;
    private Map<String, PlayerStats> stats;

    public PlayerManager(PlayerRepository repository) {
        this.repository = repository;
        stats = new HashMap<>();
    }

    public PlayerStats getStats(String name) {
        return stats.get(name);
    }

    public void increasePlayerStats(String name, PlayerStats increment) {
        PlayerStats oldStats = stats.get(name);
        oldStats.increase(increment.getWins(), increment.getKills(), increment.getGames(), increment.getDeaths(), increment.getChestsLooten());
        try {
            repository.update(name, oldStats);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        try {
            Optional<PlayerStats> maybeStats = repository.read(event.getPlayer().getName());
            PlayerStats playerStats;
            if (maybeStats.isEmpty()) playerStats = repository.create(event.getPlayer().getName());
            else playerStats = maybeStats.get();
            stats.put(event.getPlayer().getName(), playerStats);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
