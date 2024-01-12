package ru.reactioner.survival_rushers.db;

public class PlayerStats {
    private int wins;
    private int kills;
    private int games;
    private int deaths;
    private int chestsLooten;

    public PlayerStats(int wins, int kills, int games, int deaths, int chestsLooten) {
        this.wins = wins;
        this.kills = kills;
        this.games = games;
        this.deaths = deaths;
        this.chestsLooten = chestsLooten;
    }

    public int getWins() {
        return wins;
    }

    public int getKills() {
        return kills;
    }

    public int getGames() {
        return games;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getChestsLooten() {
        return chestsLooten;
    }

    public void increase(int wins, int kills, int games, int deaths, int chestsLooten) {
        this.wins += wins;
        this.kills += kills;
        this.games += games;
        this.deaths += deaths;
        this.chestsLooten += chestsLooten;
    }
}
