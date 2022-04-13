package opengles.klines.modele;

import opengles.klines.exception.NoPossiblePath;
import opengles.klines.exception.PionsNotInGrid;
import opengles.klines.exception.TargetNotEmpty;

public class Facade {

    private static Grid g;
    private final Drawer drawer;
    private final Score score;

    public Facade(Score s, Drawer d){
        this.score = s;
        this.drawer = d;
    }

    public Grid createGrid(int type){
        g = new Grid(type, score, drawer);
        return g;
    }

    public void moove(Position from, Position to) throws TargetNotEmpty, NoPossiblePath {
        g.moove(from.getX(), from.getY(), to.getX(), to.getY());
    }

    public void moove(Pions from, Position to) throws TargetNotEmpty, NoPossiblePath, PionsNotInGrid {
        Position p  = g.getPosition(from);
        if(p == null){
            throw new PionsNotInGrid();
        }
        moove(p, to);
    }

}
