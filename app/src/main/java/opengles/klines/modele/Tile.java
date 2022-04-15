package opengles.klines.modele;


import java.util.Objects;

public class Tile {

    private static int global_id = 0;
    private final int id;
    private final int type;

    public Tile(int type){
        this.id = global_id;
        global_id += 1;
        this.type = type;
    }

    public int getType() {
        return type;
    }
    public int getId() {
        return id;
    }

    public boolean sameType(Tile tile){
        return type == tile.type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tile tile = (Tile) o;
        return id == tile.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
