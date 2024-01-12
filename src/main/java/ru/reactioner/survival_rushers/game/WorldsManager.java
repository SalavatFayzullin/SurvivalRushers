package ru.reactioner.survival_rushers.game;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldsManager {
    private List<World> worlds;
    private int gamesCount;

    public WorldsManager(int gamesCount) {
        this.gamesCount = gamesCount;
        worlds = new ArrayList<>(gamesCount);
        for (int i = 0; i < gamesCount; i++) {
            worlds.add(null);
            resetWorld(i);
        }
    }

    public void resetWorld(int index) {
        deleteWorld(index);
        createWorld(index);
    }

    public World getWorld(int index) {
        return worlds.get(index);
    }

    public void createWorld(int index) {
        WorldCreator creator = new WorldCreator(String.valueOf(index));
        worlds.set(index, creator.createWorld());
    }

    public void deleteWorld(int index) {
        String worldName = String.valueOf(index);
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().equals(worldName)) {
                Bukkit.unloadWorld(world, false);
                break;
            }
        }
        worlds.set(index, null);
        for (File file : Bukkit.getServer().getWorldContainer().listFiles()) {
            if (file.getName().equals(worldName)) {
                delete(file);
                break;
            }
        }
    }

    public void deleteWorlds() {
        for (int i = 0; i < gamesCount; i++) deleteWorld(i);
    }

    private void delete(File fileToDelete) {
        if (fileToDelete.isDirectory()) {
            for (File file : fileToDelete.listFiles()) delete(file);
        }
        fileToDelete.delete();
    }
}
