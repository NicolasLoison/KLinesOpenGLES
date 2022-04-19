package opengles.klines.modele;

import android.content.SharedPreferences;

import opengles.klines.exception.NoPossiblePath;
import opengles.klines.exception.TargetNotEmpty;
import opengles.klines.exception.TilesNotInGrid;
import opengles.klines.view.Camera;

public class Facade {

    private static Grid g;
    private final Drawer drawer;
    private final Score score;

    public Facade(Score s, Drawer d){
        this.score = s;
        this.drawer = d;
    }

    public Grid createGrid(int type, SharedPreferences prefs){
        g = new Grid(prefs, score, drawer);
        Camera.main.setSize(type);
        return g;
    }

    public void moove(Position from, Position to) throws TargetNotEmpty, NoPossiblePath {
        g.moove(from.getX(), from.getY(), to.getX(), to.getY());
    }

    public void moove(Tile from, Position to) throws TargetNotEmpty, NoPossiblePath, TilesNotInGrid {
        Position p  = g.getPosition(from);
        if(p == null){
            throw new TilesNotInGrid();
        }
        moove(p, to);
    }

}
