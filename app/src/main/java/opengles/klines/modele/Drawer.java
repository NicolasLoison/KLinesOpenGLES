package opengles.klines.modele;

import java.util.List;

public interface Drawer {
    void drawGrid(Grid g);
    void drawNext(List<Tile> tileList);
    void gameOver();
}
