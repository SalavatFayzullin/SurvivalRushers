package ru.reactioner.survival_rushers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import ru.reactioner.survival_rushers.db.PlayerManager;
import ru.reactioner.survival_rushers.db.PlayerRepository;
import ru.reactioner.survival_rushers.db.PostgreSqlPlayerRepository;
import ru.reactioner.survival_rushers.game.GamesManager;
import ru.reactioner.survival_rushers.game.WorldsManager;
import ru.reactioner.survival_rushers.items.ItemsManager;

import java.sql.SQLException;

public class MainPlugin extends JavaPlugin {
    private static MainPlugin instance;

    public static MainPlugin getInstance() {
        return instance;
    }

    private ItemsManager itemsManager;
    private WorldsManager worldsManager;
    private GamesManager gamesManager;
    private World lobby;
    private int firstPhaseDuration;
    private int secondPhaseDuration;
    private int thirdPhaseDuration;
    private int deathmatchDuration;
    private int restartingDuration;
    private int firstPhaseDiameter;
    private int secondPhaseDiameter;
    private int thirdPhaseDiameter;
    private int chestSpawnInterval;
    private String createSql;
    private String readSql;
    private String updateSql;
    private String deleteSql;
    private PlayerRepository repository;
    private String dbName;
    private String userName;
    private String password;
    private String host;
    private PlayerManager playerManager;

    @Override
    public void onEnable() {
        instance = this;
        firstPhaseDuration = getConfig().getInt("first-phase-duration");
        secondPhaseDuration = getConfig().getInt("second-phase-duration");
        thirdPhaseDuration = getConfig().getInt("third-phase-duration");
        deathmatchDuration = getConfig().getInt("deathmatch-duration");
        restartingDuration = getConfig().getInt("restarting-duration");
        firstPhaseDiameter = getConfig().getInt("first-phase-radius");
        secondPhaseDiameter = getConfig().getInt("second-phase-radius");
        thirdPhaseDiameter = getConfig().getInt("third-phase-radius");
        chestSpawnInterval = getConfig().getInt("chest-spawn-interval");
        createSql = getConfig().getString("db.sql-queries.create-sql");
        readSql = getConfig().getString("db.sql-queries.read-sql");
        updateSql = getConfig().getString("db.sql-queries.update-sql");
        deleteSql = getConfig().getString("db.sql-queries.delete-sql");
        dbName = getConfig().getString("db.connection.db-name");
        userName = getConfig().getString("db.connection.username");
        password = getConfig().getString("db.connection.password");
        host = getConfig().getString("db.connection.host");
        saveDefaultConfig();
        lobby = Bukkit.getWorld(getConfig().getString("lobby-world"));
        itemsManager = new ItemsManager();
        worldsManager = new WorldsManager(getConfig().getInt("games-count"));
        try {
            repository = new PostgreSqlPlayerRepository(dbName, userName, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        playerManager = new PlayerManager(repository);
        getServer().getPluginManager().registerEvents(playerManager, this);
        gamesManager = new GamesManager(playerManager, 2, 16, getConfig().getInt("games-count"), itemsManager, worldsManager, lobby.getSpawnLocation());
        getServer().getPluginManager().registerEvents(gamesManager, this);
        getServer().getPluginManager().registerEvents(itemsManager, this);
    }

    public String getHost() {
        return host;
    }

    public String getCreateSql() {
        return createSql;
    }

    public String getReadSql() {
        return readSql;
    }

    public String getUpdateSql() {
        return updateSql;
    }

    public String getDeleteSql() {
        return deleteSql;
    }

    public int getChestSpawnInterval() {
        return chestSpawnInterval;
    }

    @Override
    public void onDisable() {
        worldsManager.deleteWorlds();
        if (repository != null) {
            try {
                repository.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public int getFirstPhaseDuration() {
        return firstPhaseDuration;
    }

    public int getSecondPhaseDuration() {
        return secondPhaseDuration;
    }

    public int getThirdPhaseDuration() {
        return thirdPhaseDuration;
    }

    public int getDeathmatchDuration() {
        return deathmatchDuration;
    }

    public int getRestartingDuration() {
        return restartingDuration;
    }

    public int getFirstPhaseDiameter() {
        return firstPhaseDiameter;
    }

    public int getSecondPhaseDiameter() {
        return secondPhaseDiameter;
    }

    public int getThirdPhaseDiameter() {
        return thirdPhaseDiameter;
    }
}
