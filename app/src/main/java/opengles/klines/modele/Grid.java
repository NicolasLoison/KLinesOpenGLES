package opengles.klines.modele;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import opengles.klines.exception.NoPossiblePath;
import opengles.klines.exception.OutofBounds;
import opengles.klines.exception.TargetNotEmpty;

public class Grid {

    private final Score score;
    private final Drawer drawer;

    private int gridSize;
    private int tilesAligne;
    private final int nbNext;
    private final ArrayList<Tile> grid;
    private final ArrayList<Tile> next;

    public Grid(SharedPreferences prefs, Score s, Drawer d){
        this.drawer = d;
        this.score = s;
        int type = prefs.getInt("gridType", 9);
        switch(type){
            case 9:
                this.gridSize = 9;
                this.tilesAligne = prefs.getInt("nbAligned9", 5);
                break;
            case 7:
                this.gridSize = 7;
                this.tilesAligne = prefs.getInt("nbAligned7", 4);
        }
        this.grid = new ArrayList<>(this.gridSize * this.gridSize);
        for(int i = 0; i<getGridSize()*getGridSize();i++){
            this.grid.add(null);
        }
        this.nbNext = prefs.getInt("nbNext", 3);
        this.next = new ArrayList<>(nbNext);
        for(int i = 0; i < nbNext; i++){
            this.next.add(null);
        }
        this.populateNext();
    }

    public int getGridSize() {
        return gridSize;
    }

    public int getNbNext() {
        return nbNext;
    }

    public Position getPosition(Tile p){
        if(!grid.contains(p)){
            return null;
        }
        int index = grid.indexOf(p);
        int x = index / gridSize;
        int y = index % gridSize;
        return new Position(x, y);
    }

    public Tile getTiles(int x, int y) throws OutofBounds {
        if(x >= 0 && y >= 0 && x*this.gridSize+y<gridSize*gridSize){
            return this.grid.get(x*this.gridSize+y);
        }
        throw new OutofBounds();
    }

    public boolean containsTiles(Tile p){
        return this.grid.contains(p);
    }

    private void setTiles(Tile p, int x, int y){
        this.grid.set(x*this.gridSize+y, p);
    }

    private Tile randomTiles(){
        int rand = new Random().nextInt(gridSize == 9 ? 7 : 5);
        return new Tile(rand);
    }

    private void populateNext(){
        for(int i = 0; i < nbNext; i++){
            this.next.set(i, this.randomTiles());
        }
    }

    private List<Position> getVide(){
        List<Position> res = new ArrayList<>();
        for(int i = 0; i<getGridSize(); i++){
            for(int j = 0; j<getGridSize(); j++){
                Tile p = null;
                try {
                    p = getTiles(i,j);
                } catch (OutofBounds outofBounds) {
                    outofBounds.printStackTrace();
                }
                if(p == null){
                    res.add(new Position(i,j));
                }
            }
        }
        return res;
    }

    public void spawnNext(){
        // on fait spawn les 3 tiles à 3 endroits vide de la grille puis on re populate
        List<Position> vide;
        for(int i = 0; i < nbNext; i++){
            vide = this.getVide();
            if (vide.size() == 0){
                this.drawer.gameOver();
                break;
            }
            int index = new Random().nextInt(vide.size());
            this.setTiles(this.next.get(i), vide.get(index));
            this.checkAlignement(vide.get(index));
        }
        vide = this.getVide();
        if (vide.size() == 0){
            this.drawer.gameOver();
        }
        this.draw();
        this.populateNext();
        this.drawer.drawNext(this.next);
    }

    public void removeAllPositions(List<Position> listPos){
        listPos.sort(new PositionComparator());
        for(Position p : listPos){
            this.setTiles(null, p);
            this.draw();
        }
    }

    public void draw(){
        this.drawer.drawGrid(this);
    }

    public void checkAlignement(Position p){
        Tile base = null;
        try {
            base = this.getTiles(p.getX(), p.getY());
        } catch (OutofBounds outofBounds) {
            outofBounds.printStackTrace();
        }
        ArrayList<Position> alignedX = new ArrayList<>(Collections.singletonList(p));
        ArrayList<Position> alignedY = new ArrayList<>(Collections.singletonList(p));
        int[][] directionsX = {{-1,0}, {1,0}};
        int[][] directionsY = {{0,-1}, {0,1}};

        for(int[] dir : directionsX){
            for(int i = 1; p.getX() + dir[0]*i < this.gridSize && p.getX() + dir[0]*i >= 0; i++){
                Position pion_pos = new Position(p.getX() + dir[0]*i, p.getY());
                Tile tiles = null;
                try {
                    tiles = this.getTiles(pion_pos);
                } catch (OutofBounds outofBounds) {
                    outofBounds.printStackTrace();
                }
                if(tiles == null || base == null || !tiles.sameType(base)){
                    break;
                }
                alignedX.add(pion_pos);
            }
        }
        for(int[] dir : directionsY){
            for(int i = 1; p.getY() + dir[1]*i < this.gridSize && p.getY() + dir[1]*i >= 0; i++){
                Position pion_pos = new Position(p.getX(), p.getY()+dir[1]*i);
                Tile tiles = null;
                try {
                    tiles = this.getTiles(pion_pos);
                } catch (OutofBounds outofBounds) {
                    outofBounds.printStackTrace();
                }
                if(tiles == null || base == null || !tiles.sameType(base)){
                    break;
                }
                alignedY.add(pion_pos);
            }
        }
        if(alignedX.size()>=this.tilesAligne){
            this.score.notifyScoreChanged(10+alignedX.size()-this.tilesAligne);
            this.removeAllPositions(alignedX);
        }
        if(alignedY.size()>=this.tilesAligne){
            this.score.notifyScoreChanged(10+alignedY.size()-this.tilesAligne);
            this.removeAllPositions(alignedY);
        }
    }

    private Tile getTiles(Position pion_pos) throws OutofBounds {
        return this.getTiles(pion_pos.getX(), pion_pos.getY());
    }

    private void setTiles(Tile tiles, Position position) {
        this.setTiles(tiles, position.getX(), position.getY());
    }

    public void moove(int departX, int departY, int targetX, int targetY) throws TargetNotEmpty, NoPossiblePath {
        Tile depart = null;
        try {
            depart = this.getTiles(departX, departY);
        } catch (OutofBounds outofBounds) {
            outofBounds.printStackTrace();
        }
        Position lastPos = new Position(departX, departY);
        try {
            if (this.getTiles(targetX, targetY) != null) {
                throw new TargetNotEmpty();
            }
        } catch (OutofBounds outofBounds) {
            outofBounds.printStackTrace();
        }

        List<Position> path = this.aStar(new Position(departX, departY), new Position(targetX, targetY));
        for(Position p : path){
            this.setTiles(depart, p);
            this.setTiles(null, lastPos);
            lastPos = p;
            this.draw();
        }
        this.checkAlignement(path.get(path.size()-1));
        this.spawnNext();
    }

    public static class AstarNode{

        private final List<AstarNode> parent;
        public final Position etat;
        private final Position target;
        private final Grid grid;
        private final double cout;

        public AstarNode(Grid g, Position etat, List<AstarNode> parent, Position target){
            this.etat = etat;
            this.parent = parent;
            this.grid = g;
            this.target = target;
            this.cout = this.getCout();
        }

        public List<Position> getPositions(){
            List<Position> res = new ArrayList<>();
            for(AstarNode p : this.parent){
                res.add(p.etat);
            }
            res.add(this.etat);
            return res;
        }

        private double getCout() {
            double distMan = sqrt(pow(this.target.getX()-this.etat.getX(), 2) + pow(this.target.getY()-this.etat.getY(), 2));
            return this.parent.size()+distMan*2.1f; // volontairement augmenté pour éviter d'avoir à tester toutes les possibilités avec un parent de même taille ( on prendra le noeud final en priorité plutot que testé les autres chemins qui ont le même nombre de parents )
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AstarNode astarNode = (AstarNode) o;
            return etat.equals(astarNode.etat);
        }

        @NonNull
        @Override
        public String toString() {
            return "AstarNode{" +
                    "etat=" + etat +
                    '}';
        }

        @Override
        public int hashCode() {
            return Objects.hash(etat);
        }

        public List<AstarNode> getChilds(){
            ArrayList<AstarNode> res = new ArrayList<>();
            ArrayList<AstarNode> newParents = new ArrayList<>(parent);
            newParents.add(this);

            try {
                if(grid.getTiles(this.etat.getX()+1, this.etat.getY()) == null){
                    AstarNode new_node = new AstarNode(this.grid, new Position(this.etat.getX() + 1, this.etat.getY()), newParents, this.target);
                    if(!newParents.contains(new_node))
                        res.add(new_node);
                }
            } catch (OutofBounds outofBounds) {
                // don't care
            }
            try {
                if(grid.getTiles(this.etat.getX()-1, this.etat.getY()) == null){
                    AstarNode new_node = new AstarNode(this.grid, new Position(this.etat.getX() - 1, this.etat.getY()), newParents, this.target);
                    if(!newParents.contains(new_node))
                        res.add(new_node);
                }
            } catch (OutofBounds outofBounds) {
                // don't care
            }
            try {
                if(grid.getTiles(this.etat.getX(), this.etat.getY()+1) == null){
                    AstarNode new_node = new AstarNode(this.grid, new Position(this.etat.getX(), this.etat.getY() + 1), newParents, this.target);
                    if(!newParents.contains(new_node))
                        res.add(new_node);
                }
            } catch (OutofBounds outofBounds) {
                // don't care
            }
            try {
                if(grid.getTiles(this.etat.getX(), this.etat.getY()-1) == null){
                    AstarNode new_node = new AstarNode(this.grid, new Position(this.etat.getX(), this.etat.getY() - 1), newParents, this.target);
                    if(!newParents.contains(new_node))
                        res.add(new_node);
                }
            } catch (OutofBounds outofBounds) {
                // don't care
            }
            return res;
        }
    }
    public static class AstarNodeComparator implements Comparator<AstarNode>{
        @Override
        public int compare(AstarNode first, AstarNode second) {
            return Double.compare(first.cout, second.cout);
        }
    }

    private List<Position> aStar(Position from, Position to) throws NoPossiblePath {

        AstarNode node = new AstarNode(this, from, new ArrayList<>(), to);
        ArrayList<AstarNode> file = new ArrayList<>();
        AstarNodeComparator comparator = new AstarNodeComparator();

        while(!node.etat.equals(to)){
            for( AstarNode child : node.getChilds()){
                for(int i = 0; i<file.size()+1; i++){
                    if (i == file.size() || comparator.compare(child, file.get(i)) <= 0){ // child a un cout inférieur = plus intéressant que other
                        file.add(i, child);
                        break;
                    }
                }
            }
            if(file.size() == 0){
                throw new NoPossiblePath();
            }

            node = file.remove(0);
        }

        return node.getPositions();
    }


}
