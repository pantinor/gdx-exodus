package objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import exodus.Constants.Maps;

@XmlRootElement(name = "tileset")
public class TileSet {

    private List<Tile> tiles = null;
    private final Map<String, Tile> nameMap = new HashMap<>();
    private final Map<Integer, Tile> indexMap = new HashMap<>();

    @XmlElement(name = "tile")
    public List<Tile> getTiles() {
        return tiles;
    }

    public void setTiles(List<Tile> tiles) {
        this.tiles = tiles;
    }

    public void setMaps() {
        for (Tile t : tiles) {
            nameMap.put(t.getName(), t);
            indexMap.put(t.getIndex(), t);

            if (t.getName().equals("horse")) {
                t.setCombatMap(Maps.CONFLICT_GRASS);
            } else if (t.getName().equals("grass")) {
                t.setCombatMap(Maps.CONFLICT_GRASS);
            } else if (t.getName().equals("brush")) {
                t.setCombatMap(Maps.CONFLICT_GRASS_BRUSH);
            } else if (t.getName().equals("forest")) {
                t.setCombatMap(Maps.CONFLICT_GRASS_FOREST);
            } else if (t.getName().equals("dungeon")) {
                t.setCombatMap(Maps.CONFLICT_BRICK_FLOOR);
            } else if (t.getName().equals("city")) {
                t.setCombatMap(Maps.CONFLICT_BRICK_FLOOR);
            } else if (t.getName().equals("castle")) {
                t.setCombatMap(Maps.CONFLICT_BRICK_FLOOR);
            } else if (t.getName().equals("towne")) {
                t.setCombatMap(Maps.CONFLICT_BRICK_FLOOR);
            } else if (t.getName().equals("lcb_entrance")) {
                t.setCombatMap(Maps.CONFLICT_BRICK_FLOOR);
            } else if (t.getName().equals("shrine")) {
                t.setCombatMap(Maps.CONFLICT_GRASS_BRUSH);
            } else if (t.getName().equals("chest")) {
                t.setCombatMap(Maps.CONFLICT_BRICK_FLOOR);
            } else if (t.getName().equals("floor")) {
                t.setCombatMap(Maps.CONFLICT_BRICK_FLOOR);
            } else if (t.getName().equals("moongate")) {
                t.setCombatMap(Maps.CONFLICT_GRASS);
            } else if (t.getName().equals("door")) {
                t.setCombatMap(Maps.CONFLICT_BRICK_FLOOR);
            } else if (t.getName().equals("secret_door")) {
                t.setCombatMap(Maps.CONFLICT_BRICK_FLOOR);
            } else if (t.getName().equals("locked_door")) {
                t.setCombatMap(Maps.CONFLICT_BRICK_FLOOR);
            } else if (t.getName().equals("water")) {
                t.setCombatMap(Maps.CONFLICT_BOTH_SHIPS);
            } else if (t.getName().equals("sea")) {
                t.setCombatMap(Maps.CONFLICT_BOTH_SHIPS);
            } else if (t.getName().equals("energy_field")) {
                t.setCombatMap(Maps.CONFLICT_BRICK_FLOOR);
            } else if (t.getName().equals("lava")) {
                t.setCombatMap(Maps.CONFLICT_BRICK_FLOOR);
            } else {
                //System.err.printf("could not find combat map for %s\n",t.getName());
            }
        }
    }

    public Tile getTileByName(String name) {
        return nameMap.get(name);
    }

    public Tile getTileByIndex(int index) {
        return indexMap.get(index);
    }

}
