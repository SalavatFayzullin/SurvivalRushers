package ru.reactioner.survival_rushers.db;

import org.bukkit.Bukkit;
import ru.reactioner.survival_rushers.MainPlugin;

import java.sql.*;
import java.util.Optional;

public class PostgreSqlPlayerRepository implements PlayerRepository {
    private Connection connection;

    public PostgreSqlPlayerRepository(String dbName, String userName, String password) throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        connection = DriverManager.getConnection("jdbc:postgresql://%s/%s".formatted(MainPlugin.getInstance().getHost(), dbName), userName, password);
    }

    @Override
    public PlayerStats create(String name) throws SQLException {
        String query = MainPlugin.getInstance().getCreateSql().replace("${player.name}", name);
        try (Statement statement = connection.createStatement()) {
            statement.execute(query);
        }
        return new PlayerStats(0, 0, 0, 0, 0);
    }

    @Override
    public Optional<PlayerStats> read(String name) throws SQLException {
        String query = MainPlugin.getInstance().getReadSql().replace("${player.name}", name);
        PlayerStats stats;
        try (Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(query);
            if (set.next()) {
                int kills = set.getInt(1);
                int wins = set.getInt(2);
                int games = set.getInt(3);
                int deaths = set.getInt(4);
                int chestsLooten = set.getInt(5);
                stats = new PlayerStats(wins, kills, games, deaths, chestsLooten);
            } else stats = null;
        }
        if (stats == null) return Optional.empty();
        else return Optional.of(stats);
    }

    @Override
    public void update(String name, PlayerStats stats) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String query = MainPlugin.getInstance().getUpdateSql();
            query = query.replace("${player.kills}", String.valueOf(stats.getKills()));
            query = query.replace("${player.games}", String.valueOf(stats.getGames()));
            query = query.replace("${player.wins}", String.valueOf(stats.getWins()));
            query = query.replace("${player.deaths}", String.valueOf(stats.getDeaths()));
            query = query.replace("${player.chests-looten}", String.valueOf(stats.getChestsLooten()));
            query = query.replace("${player.name}", String.valueOf(name));
            statement.execute(query);
        }
    }

    @Override
    public void delete(String name) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String query = MainPlugin.getInstance().getDeleteSql().replace("${player.name}", name);
            statement.execute(query);
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) connection.close();
    }
}
