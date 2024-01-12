package ru.reactioner.survival_rushers.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import ru.reactioner.survival_rushers.MainPlugin;
import ru.reactioner.survival_rushers.db.PlayerManager;
import ru.reactioner.survival_rushers.db.PlayerStats;
import ru.reactioner.survival_rushers.game.state.RestartingState;
import ru.reactioner.survival_rushers.game.state.State;
import ru.reactioner.survival_rushers.game.state.WaitingState;
import ru.reactioner.survival_rushers.items.ItemsManager;

import java.util.*;

public class Game implements Listener {
    private Set<Player> players;
    private Set<Player> spectators;
    private State currentState;
    private BossBar bar;
    private int minPlayerCount;
    private int maxPlayerCount;
    private int secondsBeforeStart;
    private GamesManager gamesManager;
    private int index;
    private WorldsManager worldsManager;
    private ItemsManager itemsManager;
    private Map<String, PlayerStats> snapshots;
    private PlayerManager playerManager;

    public Game(PlayerManager playerManager, ItemsManager itemsManager, WorldsManager worldsManager, int index, int minPlayerCount, int maxPlayerCount, int secondsBeforeStart, GamesManager gamesManager) {
        this.playerManager = playerManager;
        this.itemsManager = itemsManager;
        bar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
        this.secondsBeforeStart = secondsBeforeStart;
        this.worldsManager = worldsManager;
        this.index = index;
        this.minPlayerCount = minPlayerCount;
        this.maxPlayerCount = maxPlayerCount;
        this.gamesManager = gamesManager;
        resetGlobally();
    }

    public ItemsManager getItemsManager() {
        return itemsManager;
    }

    public void setState(State newState) {
        if (currentState != null) currentState.cancel();
        currentState = newState;
        currentState.start();
    }

    public int getSecondsBeforeStart() {
        return secondsBeforeStart;
    }

    public void setBarTitle(String title) {
        bar.setTitle(title);
    }

    public void setBarProgress(double progress) {
        bar.setProgress(progress);
    }

    public boolean addPlayer(Player player) {
        if (spectators.contains(player) || players.size() == maxPlayerCount || players.contains(player) || !currentState.canJoin()) return false;
        players.add(player);
        unleashPlayer(player);
        bar.addPlayer(player);
        player.teleport(worldsManager.getWorld(index).getSpawnLocation());
        currentState.addPlayer(player);
        players.forEach(worldPlayer -> worldPlayer.sendMessage(Component.text("%s присоединился к игре!".formatted(player.getName())).color(TextColor.color(0, 255, 0))));
        snapshots.put(player.getName(), new PlayerStats(0, 0, 0, 0, 0));
        return true;
    }

    public boolean removePlayer(Player player) {
        if (!spectators.contains(player) && !players.contains(player)) return false;
        unleashPlayer(player);
        bar.removePlayer(player);
        players.remove(player);
        spectators.remove(player);
        players.forEach(player1 -> player1.sendMessage(Component.text("Игрок %s вышел из игры".formatted(player.getName()))));
        spectators.forEach(player1 -> player1.sendMessage(Component.text("Игрок %s вышел из игры".formatted(player.getName()))));
        currentState.removePlayer(player);
        player.sendMessage(Component.text("Вы успешно вышли из игры").color(TextColor.color(0, 255, 0)));
        snapshots.remove(player.getName());
        return true;
    }

    public Map<String, PlayerStats> getSnapshots() {
        return snapshots;
    }

    public void updateBar() {
        setBarTitle("Ожидание %d/%d".formatted(getOnline(), getMaxPlayerCount()));
        setBarProgress((double) getOnline() / getMaxPlayerCount());
    }

    public GamePhase getPhase() {
        return currentState.getPhase();
    }

    public int getOnline() {
        return players.size();
    }

    public int getMinPlayerCount() {
        return minPlayerCount;
    }

    public int getMaxPlayerCount() {
        return maxPlayerCount;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public void checkForWinner() {
        if (getOnline() == 1) onPlayerWon();
    }

    public void onPlayerWon() {
        Optional<Player> optionalPlayer = players.stream().findAny();
        if (optionalPlayer.isEmpty()) return;
        Player winner = optionalPlayer.get();
        snapshots.get(winner.getName()).increase(1, 0, 0, 0, 0);
        players.forEach(player -> player.sendMessage("Уррраа %s победил!!!".formatted(winner.getName())));
        setState(new RestartingState(this));
    }

    public void resetGame() {
        getPlayers().forEach(p -> {
            PlayerStats snapshot = getSnapshots().get(p.getName());
            snapshot.increase(0, 0, 1, 0, 0);
            playerManager.increasePlayerStats(p.getName(), snapshot);
            gamesManager.teleportOnSpawn(p);
            bar.removePlayer(p);
        });
        getSpectators().forEach(p -> {
            PlayerStats snapshot = getSnapshots().get(p.getName());
            snapshot.increase(0, 0, 1, 0, 0);
            playerManager.increasePlayerStats(p.getName(), snapshot);
            gamesManager.teleportOnSpawn(p);
            bar.removePlayer(p);
        });
        gamesManager.resetWorld(index);
        resetGlobally();
    }

    public Set<Player> getSpectators() {
        return spectators;
    }

    public void onPlayerKilled(Player killed, Player killer) {
        if (killer == null || !getPlayers().contains(killer)) players.forEach(onlinePlayer -> onlinePlayer.sendMessage(ChatColor.RED + killed.getName() + " died!"));
        else {
            players.forEach(onlinePlayer -> onlinePlayer.sendMessage(ChatColor.RED + killed.getName() + " was killed by " + killer.getName() + "!"));
            snapshots.get(killer.getName()).increase(0, 1, 0, 0, 0);
        }
        snapshots.get(killed.getName()).increase(0, 0, 0, 1, 0);
        makePlayerSpectator(killed);
        if (players.size() == 1) onPlayerWon();
    }

    public void onDeathmatchStarted() {
        getPlayers().forEach(p -> p.sendMessage("Дезматч начался!"));
        getSpectators().forEach(p -> p.sendMessage("Дезматч начался!"));
    }

    public void unleashPlayer(Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setExp(0f);
        player.setLevel(0);
    }

    public void makePlayerSpectator(Player player) {
        unleashPlayer(player);
        player.teleport(worldsManager.getWorld(index).getSpawnLocation());
        player.setGameMode(GameMode.SPECTATOR);
        players.remove(player);
        spectators.add(player);
    }

    public void resetGlobally() {
        snapshots = new HashMap<>();
        players = new HashSet<>();
        spectators = new HashSet<>();
        worldsManager.getWorld(index).setAutoSave(false);
        worldsManager.getWorld(index).getWorldBorder().setCenter(worldsManager.getWorld(index).getSpawnLocation());
        worldsManager.getWorld(index).getWorldBorder().setSize(50.0);
        currentState = new WaitingState(this);
    }

    public void setBarColor(BarColor color) {
        bar.setColor(color);
    }

    public void onSpectatorJoined(Player spectator) {
        getPlayers().forEach(p -> p.sendMessage(Component.text("%s присоединился в качестве наблюдателя".formatted(spectator.getName()))));
        getSpectators().forEach(p -> p.sendMessage(Component.text("%s присоединился в качестве наблюдателя".formatted(spectator.getName()))));
        makePlayerSpectator(spectator);
        spectator.sendMessage(Component.text("Вы увпешно присоединились в качестве наблюдателя"));
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent event) {
        event.deathMessage(null);
        onPlayerKilled(event.getEntity(), event.getEntity().getKiller());
    }

    @EventHandler
    private void onDamage(EntityDamageEvent event) {
        if (currentState.getPhase() == GamePhase.ACTIVE || currentState.getPhase() == GamePhase.DEATHMATCH) return;
        else event.setCancelled(true);
    }

    public World getWorld() {
        return worldsManager.getWorld(index);
    }
}
