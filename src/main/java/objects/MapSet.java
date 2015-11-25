package objects;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import exodus.Constants.MapType;
import exodus.Constants.Maps;
import util.Utils;

@XmlRootElement(name = "maps")
public class MapSet {

    private List<BaseMap> maps = null;

    @XmlElement(name = "map")
    public List<BaseMap> getMaps() {
        return maps;
    }

    public void setMaps(List<BaseMap> maps) {
        this.maps = maps;
    }

    public void init(TileSet ts) {
        for (BaseMap m : maps) {

            Maps map = Maps.get(m.getId());
            map.setMap(m);
          
            try {
                Utils.setMapTiles(m, ts);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (m.getType() == MapType.world || m.getType() == MapType.city) {
                
                m.setObjects();

                float[][] shadowMap = new float[m.getWidth()][m.getHeight()];
                for (int y = 0; y < m.getHeight(); y++) {
                    for (int x = 0; x < m.getWidth(); x++) {
                        shadowMap[x][y] = (m.getTile(x, y).isOpaque() ? 1 : 0);
                    }
                }

                m.setShadownMap(shadowMap);
            }

        }
    }
    
    public void init(MapType mt, TileSet ts) {
        for (BaseMap m : maps) {
            if (m.getType() != mt) {
                continue;
            }
            Maps map = Maps.get(m.getId());
            map.setMap(m);
            try {
                Utils.setMapTiles(m, ts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
