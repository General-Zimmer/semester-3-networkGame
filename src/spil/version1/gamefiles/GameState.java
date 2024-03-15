package spil.version1.gamefiles;

public class GameState implements java.io.Serializable {
    private ConcurrentArrayList players = new ConcurrentArrayList();
    private long tickID = 0;

    public ConcurrentArrayList getPlayers() {
        return players;
    }

    public void setPlayers(ConcurrentArrayList players) {
        this.players = players;
    }

    public long getTickID() {
        return tickID;
    }

    public void setTickID(long tickID) {
        this.tickID = tickID;
    }

    public void incrementTickID() {
        tickID++;
    }
}
