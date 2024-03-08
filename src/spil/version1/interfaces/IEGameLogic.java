package spil.version1.interfaces;

import spil.version1.gamefiles.Player;
import spil.version1.gamefiles.pair;

import java.util.Queue;

public interface IEGameLogic {

    Player makePlayer(String name);

    void makeVirtualPlayer();

    pair getRandomFreePosition();

    void updatePlayer(Player me, int delta_x, int delta_y, String direction);

    void movePlayers(Queue<String> actions);

    Player getPlayer(String name);

    Player getPlayerAt(int x, int y);


}
