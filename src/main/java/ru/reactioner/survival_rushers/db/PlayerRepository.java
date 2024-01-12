package ru.reactioner.survival_rushers.db;

import java.sql.SQLException;
import java.util.Optional;

public interface PlayerRepository {
    PlayerStats create(String name) throws SQLException;
    Optional<PlayerStats> read(String name) throws SQLException;
    void update(String name, PlayerStats stats) throws SQLException;
    void delete(String name) throws SQLException;
    void close() throws SQLException;
}
