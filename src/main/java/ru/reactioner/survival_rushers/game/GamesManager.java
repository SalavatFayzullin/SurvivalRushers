package ru.reactioner.survival_rushers.game;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.reactioner.survival_rushers.MainPlugin;
import ru.reactioner.survival_rushers.db.PlayerManager;
import ru.reactioner.survival_rushers.items.ItemsManager;
import ru.reactioner.survival_rushers.items.SurvivalItem;

import java.util.ArrayList;
import java.util.List;

public class GamesManager implements Listener {
    private List<Game> games;
    private Inventory view;
    private int minPlayersCount;
    private int maxPlayersCount;
    private ItemStack gameSelector;
    private ItemStack backToLobby;
    private Location spawn;
    private WorldsManager worldsManager;
    private ItemsManager itemsManager;
    private PlayerManager playerManager;

    public GamesManager(PlayerManager playerManager, int minPlayersCount, int maxPlayersCount, int gamesCount, ItemsManager itemsManager, WorldsManager worldsManager, Location spawn) {
        this.playerManager = playerManager;
        this.worldsManager = worldsManager;
        this.minPlayersCount = minPlayersCount;
        this.itemsManager = itemsManager;
        this.maxPlayersCount = maxPlayersCount;
        games = new ArrayList<>(gamesCount);
        for (int i = 0; i < gamesCount; i++) {
            Game game = new Game(playerManager, itemsManager, worldsManager, i, 2, 16, 20, this);
            Bukkit.getServer().getPluginManager().registerEvents(game, MainPlugin.getInstance());
            games.add(game);
        }
        view = Bukkit.createInventory(null, 27, Component.text("Выберите сервер"));
        gameSelector = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = gameSelector.getItemMeta();
        meta.displayName(Component.text("Выберите игру"));
        gameSelector.setItemMeta(meta);
        SurvivalItem item = new SurvivalItem(gameSelector, this::openView);
        itemsManager.addItemAction(item);
        this.spawn = spawn;
        updateContent();
        backToLobby = new ItemStack(Material.MAGMA_CREAM);
        meta = backToLobby.getItemMeta();
        meta.displayName(Component.text("Вернуться в лобби"));
        backToLobby.setItemMeta(meta);
        itemsManager.addItemAction(new SurvivalItem(backToLobby, player -> {
            for (Game game : games) {
                if (game.getPlayers().contains(player)) {
                    game.removePlayer(player);
                    break;
                }
            }
            teleportOnSpawn(player);
        }));
        for (int i = 0; i < gamesCount; i++) {
            int finalI = i;
            item = new SurvivalItem(view.getItem(i), player -> {
                if (!games.get(finalI).addPlayer(player)) return;
                player.getInventory().clear();
                player.getInventory().addItem(backToLobby);
            });
            itemsManager.addItemAction(item);
        }
    }

    public void openView(Player player) {
        updateContent();
        player.openInventory(view);
    }

    private void updateContent() {
        for (int i = 0; i < games.size(); i++) {
            ItemStack item = view.getItem(i);
            if (item == null) item = new ItemStack(Material.SLIME_BALL);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("Игра №%d".formatted(i + 1)));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Онлайн: %d/%d".formatted(games.get(i).getOnline(), maxPlayersCount)));
            lore.add(Component.text("Фаза: %s".formatted(games.get(i).getPhase().name())));
            meta.lore(lore);
            item.setItemMeta(meta);
            view.setItem(i, item);
        }
    }

    public void resetWorld(int worldIndex) {
        worldsManager.resetWorld(worldIndex);
    }

    public void teleportOnSpawn(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().addItem(gameSelector);
        player.teleport(spawn);
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() == view) event.setCancelled(true);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        teleportOnSpawn(event.getPlayer());
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
        for (Game game : games) {
            if (game.removePlayer(event.getPlayer())) break;
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        for (Game game : games) {
            if (game.getPlayers().contains(event.getEntity())) {
                if ((game.getPhase() == GamePhase.WAITING || game.getPhase() == GamePhase.STARTING) && game.getPlayers().contains(event.getEntity())) break;
                return;
            }
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        for (Game game : games) {
            if (game.getPlayers().contains(event.getPlayer())) {
                if ((game.getPhase() == GamePhase.WAITING || game.getPhase() == GamePhase.STARTING) && game.getPlayers().contains(event.getPlayer())) break;
                return;
            }
        }
        event.setCancelled(true);
    }
}
