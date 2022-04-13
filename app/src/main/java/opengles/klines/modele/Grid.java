package opengles.klines.modele;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

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
    private int pionsAligne;
    private final ArrayList<Pions> grid;
    private final ArrayList<Pions> next;

    public Grid(int type, Score s, Drawer d){
        this.drawer = d;
        this.score = s;
        switch(type){
            case 9:
                this.gridSize = 9;
                this.pionsAligne = 5;
                break;
            case 7:
                this.gridSize = 7;
                this.pionsAligne = 4;
        }
        this.grid = new ArrayList<>(this.gridSize * this.gridSize);
        for(int i = 0; i<getGridSize()*getGridSize();i++){
            this.grid.add(null);
        }
        this.next = new ArrayList<>(3);
        for(int i = 0; i<3;i++){
            this.next.add(null);
        }
        this.populateNext();
    }

    public int getGridSize() {
        return gridSize;
    }

    public Position getPosition(Pions p){
        if(!grid.contains(p)){
            return null;
        }
        int index = grid.indexOf(p);
        int x = index / gridSize;
        int y = index % gridSize;
        return new Position(x, y);
    }

    public Pions getPions(int x, int y) throws OutofBounds {
        if(x >= 0 && y >= 0 && x*this.gridSize+y<gridSize*gridSize){
            return this.grid.get(x*this.gridSize+y);
        }
        throw new OutofBounds();
    }

    public boolean containsPions(Pions p){
        return this.grid.contains(p);
    }

    private void setPions(Pions p, int x, int y){
        this.grid.set(x*this.gridSize+y, p);
    }

    private Pions randomPions(){
        int rand = new Random().nextInt(7);
        return new Pions(rand);
    }

    private void populateNext(){
        for(int i = 0; i<3; i++){
            this.next.set(i, this.randomPions());
        }
        this.drawer.drawNext(this.next);
    }

    private List<Position> getVide(){
        List<Position> res = new ArrayList<>();
        for(int i = 0; i<getGridSize(); i++){
            for(int j = 0; j<getGridSize(); j++){
                Pions p = null;
                try {
                    p = getPions(i,j);
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
        // on fait spawn les 3 pions à 3 endroits vide de la grille puis on re populate
        for(int i = 0; i<3; i++){
            List<Position> vide = this.getVide();
            if(vide.size() == 0){
                this.drawer.gameOver(this);
                break;
            }
            int index = new Random().nextInt(vide.size());
            this.setPions(this.next.get(i), vide.get(index));
            this.checkAlignement(vide.get(index));
        }
        this.draw();
        this.populateNext();
        this.drawer.drawNext(this.next);
    }

    public void removeAllPositions(List<Position> listPos){
        listPos.sort(new PositionComparator());
        for(Position p : listPos){
            this.setPions(null, p);
            this.draw();
        }
    }

    public void draw(){
        this.drawer.drawGrid(this);
    }

    public void checkAlignement(Position p){
        Pions base = null;
        try {
            base = this.getPions(p.getX(), p.getY());
        } catch (OutofBounds outofBounds) {
            outofBounds.printStackTrace();
        }
        ArrayList<Position> alignedX = new ArrayList<>(Collections.singletonList(p));
        ArrayList<Position> alignedY = new ArrayList<>(Collections.singletonList(p));
        int[][] directionsX = {{-1,0}, {1,0}};
        int[][] directionsY = {{0,-1}, {0,1}};

        for(int[] dir : directionsX){
            for(int i = 1; p.getX()+dir[0]*i<this.gridSize && p.getX()+dir[0]*i>0; i++){
                Position pion_pos = new Position(p.getX()+dir[0]*i, p.getY());
                Pions pions = null;
                try {
                    pions = this.getPions(pion_pos);
                } catch (OutofBounds outofBounds) {
                    outofBounds.printStackTrace();
                }
                if(pions == null || base == null || !pions.sameType(base)){
                    break;
                }
                alignedX.add(pion_pos);
            }
        }
        for(int[] dir : directionsY){
            for(int i = 1; p.getY()+dir[1]*i<this.gridSize && p.getY()+dir[1]*i>0; i++){
                Position pion_pos = new Position(p.getX(), p.getY()+dir[1]*i);
                Pions pions = null;
                try {
                    pions = this.getPions(pion_pos);
                } catch (OutofBounds outofBounds) {
                    outofBounds.printStackTrace();
                }
                if(pions == null || base == null || !pions.sameType(base)){
                    break;
                }
                alignedY.add(pion_pos);
            }
        }
        if(alignedX.size()>=this.pionsAligne){
            this.score.notifyScoreChanged(10+alignedX.size()-this.pionsAligne);
            this.removeAllPositions(alignedX);
        }
        if(alignedY.size()>=this.pionsAligne){
            this.score.notifyScoreChanged(10+alignedY.size()-this.pionsAligne);
            this.removeAllPositions(alignedY);
        }
    }

    private Pions getPions(Position pion_pos) throws OutofBounds {
        return this.getPions(pion_pos.getX(), pion_pos.getY());
    }

    private void setPions(Pions pions, Position position) {
        this.setPions(pions, position.getX(), position.getY());
    }

    public void moove(int departX, int departY, int targetX, int targetY) throws TargetNotEmpty, NoPossiblePath {
        Pions depart = null;
        try {
            depart = this.getPions(departX, departY);
        } catch (OutofBounds outofBounds) {
            outofBounds.printStackTrace();
        }
        Position lastPos = new Position(departX, departY);
        try {
            if (this.getPions(targetX, targetY) != null) {
                throw new TargetNotEmpty();
            }
        } catch (OutofBounds outofBounds) {
            outofBounds.printStackTrace();
        }

        List<Position> path = this.aStar(new Position(departX, departY), new Position(targetX, targetY));
        for(Position p : path){
            this.setPions(depart, p);
            this.setPions(null, lastPos);
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
                if(grid.getPions(this.etat.getX()+1, this.etat.getY()) == null){
                    AstarNode new_node = new AstarNode(this.grid, new Position(this.etat.getX() + 1, this.etat.getY()), newParents, this.target);
                    if(!newParents.contains(new_node))
                        res.add(new_node);
                }
            } catch (OutofBounds outofBounds) {
                // don't care
            }
            try {
                if(grid.getPions(this.etat.getX()-1, this.etat.getY()) == null){
                    AstarNode new_node = new AstarNode(this.grid, new Position(this.etat.getX() - 1, this.etat.getY()), newParents, this.target);
                    if(!newParents.contains(new_node))
                        res.add(new_node);
                }
            } catch (OutofBounds outofBounds) {
                // don't care
            }
            try {
                if(grid.getPions(this.etat.getX(), this.etat.getY()+1) == null){
                    AstarNode new_node = new AstarNode(this.grid, new Position(this.etat.getX(), this.etat.getY() + 1), newParents, this.target);
                    if(!newParents.contains(new_node))
                        res.add(new_node);
                }
            } catch (OutofBounds outofBounds) {
                // don't care
            }
            try {
                if(grid.getPions(this.etat.getX(), this.etat.getY()-1) == null){
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
