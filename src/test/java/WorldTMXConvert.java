
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import java.io.File;
import java.util.List;
import objects.BaseMap;
import objects.Moongate;
import objects.Portal;
import objects.Tile;
import org.apache.commons.io.FileUtils;
import exodus.Constants.Maps;
import exodus.Exodus;
import java.util.Random;

public class WorldTMXConvert implements ApplicationListener {

    //tile ids from uf_map
    public final int GRASS1 = 146 + 1;
    public final int GRASS2 = 147 + 1;
    public final int FOREST = 185 + 1;
    public final int MEADOW = 178 + 1;
    public final int MOUNTAIN = 407 + 1;
    public final int WATER = 92 + 1;
    public final int TOWN = 386 + 1;
    public final int CASTLE = 265 + 1;
    public final int DUNGEON = 393 + 1;
    public final int LAVA = 67 + 1;

    public static void main(String[] args) throws Exception {
        new LwjglApplication(new WorldTMXConvert());
    }

    @Override
    public void create() {

        try {

            Exodus ult = new Exodus();
            ult.create();

            Random random = new Random();

            BaseMap map = Maps.SOSARIA.getMap();
            Tile[] tiles = map.getTiles();

            // map layers
            StringBuilder props = new StringBuilder();
            StringBuilder mountains = new StringBuilder();
            StringBuilder forest = new StringBuilder();
            StringBuilder meadow = new StringBuilder();
            StringBuilder water = new StringBuilder();
            StringBuilder grass = new StringBuilder();
            StringBuilder lava = new StringBuilder();

            int count = 1;
            for (Tile t : tiles) {
                int i = t.getIndex();

                if (i == 0) {//water
                    props.append("0").append(",");
                    mountains.append("0").append(",");
                    forest.append("0").append(",");
                    meadow.append("0").append(",");
                    water.append(WATER).append(",");
                    grass.append("0").append(",");
                    lava.append("0").append(",");
                } else if (i == 1) {//grass
                    props.append("0").append(",");
                    mountains.append("0").append(",");
                    forest.append("0").append(",");
                    meadow.append("0").append(",");
                    water.append("0").append(",");
                    grass.append(random.nextBoolean() ? GRASS1 : GRASS2).append(",");
                    lava.append("0").append(",");
                } else if (i == 2) {//meadow
                    props.append("0").append(",");
                    mountains.append("0").append(",");
                    forest.append("0").append(",");
                    meadow.append(MEADOW).append(",");
                    water.append("0").append(",");
                    grass.append(random.nextBoolean() ? GRASS1 : GRASS2).append(",");
                    lava.append("0").append(",");
                } else if (i == 3) {//forest
                    props.append("0").append(",");
                    mountains.append("0").append(",");
                    forest.append(FOREST).append(",");
                    meadow.append("0").append(",");
                    water.append("0").append(",");
                    grass.append(random.nextBoolean() ? GRASS1 : GRASS2).append(",");
                    lava.append("0").append(",");
                } else if (i == 4) {//mountains
                    props.append("0").append(",");
                    mountains.append(MOUNTAIN).append(",");
                    forest.append("0").append(",");
                    meadow.append("0").append(",");
                    water.append("0").append(",");
                    grass.append("0").append(",");
                    lava.append("0").append(",");
                } else if (i == 5) {//dungeon
                    props.append(DUNGEON).append(",");
                    mountains.append("0").append(",");
                    forest.append("0").append(",");
                    meadow.append("0").append(",");
                    water.append("0").append(",");
                    grass.append(random.nextBoolean() ? GRASS1 : GRASS2).append(",");
                    lava.append("0").append(",");
                } else if (i == 6) {//town
                    props.append(TOWN).append(",");
                    mountains.append("0").append(",");
                    forest.append("0").append(",");
                    meadow.append("0").append(",");
                    water.append("0").append(",");
                    grass.append(random.nextBoolean() ? GRASS1 : GRASS2).append(",");
                    lava.append("0").append(",");
                } else if (i == 7) {//castle
                    props.append(CASTLE).append(",");
                    mountains.append("0").append(",");
                    forest.append("0").append(",");
                    meadow.append("0").append(",");
                    water.append("0").append(",");
                    grass.append(random.nextBoolean() ? GRASS1 : GRASS2).append(",");
                    lava.append("0").append(",");
                } else if (i == 33) {//lava
                    props.append("0").append(",");
                    mountains.append("0").append(",");
                    forest.append("0").append(",");
                    meadow.append("0").append(",");
                    water.append("0").append(",");
                    grass.append("0").append(",");
                    lava.append(LAVA).append(",");
                } else {
                    props.append("0").append(",");
                    mountains.append("0").append(",");
                    forest.append("0").append(",");
                    meadow.append("0").append(",");
                    water.append(WATER).append(",");
                    grass.append("0").append(",");
                    lava.append("0").append(",");
                }

                count++;
                if (count > 64) {
                    props.append("\n");
                    mountains.append("\n");
                    forest.append("\n");
                    meadow.append("\n");
                    water.append("\n");
                    grass.append("\n");
                    lava.append("\n");
                    count = 1;
                }
            }

            Formatter c = new Formatter(map.getFname(), "u3ega-shapes.png",
                    map.getWidth(), map.getHeight(),
                    24, 24,
                    props.toString(), mountains.toString(), forest.toString(), meadow.toString(), water.toString(), grass.toString(), lava.toString(),
                    map.getPortals(), map.getMoongates());

            FileUtils.writeStringToFile(new File("tmx/map_" + map.getId() + "_Andius.tmx"), c.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("DONE");
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

    private static class Formatter {

        private String tilesetName;
        private String imageSource;
        private int mapWidth;
        private int mapHeight;
        private int tileWidth;
        private int tileHeight;

        private String props;
        private String mountains;
        private String forest;
        private String meadow;
        private String water;
        private String grass;
        private String lava;

        private List<Portal> portals;
        private List<Moongate> moongates;

        public Formatter(String tilesetName, String imageSource,
                int mapWidth, int mapHeight,
                int tileWidth, int tileHeight,
                String props,
                String mountains,
                String forest,
                String meadow,
                String water,
                String grass,
                String lava,
                List<Portal> portals,
                List<Moongate> moongates) {

            this.tilesetName = tilesetName;
            this.imageSource = imageSource;
            this.mapWidth = mapWidth;
            this.mapHeight = mapHeight;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;

            this.props = props.substring(0, props.length() - 2);
            this.mountains = mountains.substring(0, mountains.length() - 2);
            this.forest = forest.substring(0, forest.length() - 2);
            this.meadow = meadow.substring(0, meadow.length() - 2);
            this.water = water.substring(0, water.length() - 2);
            this.grass = grass.substring(0, grass.length() - 2);
            this.lava = lava.substring(0, lava.length() - 2);

            this.portals = portals;
            this.moongates = moongates;
        }

        @Override
        public String toString() {

            StringBuilder portalString = new StringBuilder();
            if (portals != null) {
                for (Portal p : portals) {
                    if (p == null) {
                        continue;
                    }
                    portalString.append(p.toString());
                }
            }

            StringBuilder moongateString = new StringBuilder();
            if (moongates != null) {
                for (Moongate p : moongates) {
                    if (p == null) {
                        continue;
                    }
                    moongateString.append(p.toString());
                }
            }

            String template = """
                <?xml version="1.0" encoding="UTF-8"?>
                <map version="1.10" tiledversion="1.11.0" orientation="orthogonal" renderorder="right-down" width="64" height="64" tilewidth="24" tileheight="24" infinite="0" backgroundcolor="#000000" nextlayerid="14" nextobjectid="33">
                 <properties>
                  <property name="startX" value="44"/>
                  <property name="startY" value="20"/>
                 </properties>
                 <tileset firstgid="1" name="uf_map" tilewidth="24" tileheight="24" tilecount="525" columns="25">
                  <image source="uf_map.png" width="600" height="504"/>
                  <tile id="17">
                   <animation>
                    <frame tileid="17" duration="500"/>
                    <frame tileid="18" duration="500"/>
                    <frame tileid="19" duration="500"/>
                    <frame tileid="20" duration="500"/>
                    <frame tileid="19" duration="500"/>
                    <frame tileid="18" duration="500"/>
                   </animation>
                  </tile>
                  <tile id="67">
                   <animation>
                    <frame tileid="67" duration="500"/>
                    <frame tileid="68" duration="500"/>
                    <frame tileid="69" duration="500"/>
                    <frame tileid="70" duration="500"/>
                    <frame tileid="69" duration="500"/>
                    <frame tileid="68" duration="500"/>
                   </animation>
                  </tile>
                  <tile id="92">
                   <animation>
                    <frame tileid="92" duration="500"/>
                    <frame tileid="93" duration="500"/>
                    <frame tileid="94" duration="500"/>
                    <frame tileid="95" duration="500"/>
                    <frame tileid="94" duration="500"/>
                    <frame tileid="93" duration="500"/>
                   </animation>
                  </tile>
                  <tile id="117">
                   <animation>
                    <frame tileid="117" duration="500"/>
                    <frame tileid="118" duration="500"/>
                    <frame tileid="119" duration="500"/>
                    <frame tileid="120" duration="500"/>
                    <frame tileid="119" duration="500"/>
                    <frame tileid="118" duration="500"/>
                   </animation>
                  </tile>
                  <tile id="260">
                   <animation>
                    <frame tileid="260" duration="200"/>
                    <frame tileid="261" duration="200"/>
                    <frame tileid="262" duration="200"/>
                    <frame tileid="263" duration="200"/>
                   </animation>
                  </tile>
                  <tile id="265">
                   <animation>
                    <frame tileid="265" duration="200"/>
                    <frame tileid="266" duration="200"/>
                    <frame tileid="267" duration="200"/>
                    <frame tileid="268" duration="200"/>
                   </animation>
                  </tile>
                  <tile id="285">
                   <animation>
                    <frame tileid="285" duration="200"/>
                    <frame tileid="286" duration="200"/>
                    <frame tileid="287" duration="200"/>
                    <frame tileid="288" duration="200"/>
                   </animation>
                  </tile>
                  <tile id="290">
                   <animation>
                    <frame tileid="290" duration="200"/>
                    <frame tileid="291" duration="200"/>
                    <frame tileid="292" duration="200"/>
                    <frame tileid="293" duration="200"/>
                   </animation>
                  </tile>
                  <tile id="388">
                   <animation>
                    <frame tileid="388" duration="300"/>
                    <frame tileid="389" duration="300"/>
                   </animation>
                  </tile>
                  <tile id="390">
                   <animation>
                    <frame tileid="390" duration="500"/>
                    <frame tileid="391" duration="500"/>
                   </animation>
                  </tile>
                  <tile id="410">
                   <animation>
                    <frame tileid="410" duration="400"/>
                    <frame tileid="411" duration="400"/>
                   </animation>
                  </tile>
                  <tile id="412">
                   <animation>
                    <frame tileid="412" duration="400"/>
                    <frame tileid="413" duration="400"/>
                   </animation>
                  </tile>
                  <tile id="414">
                   <animation>
                    <frame tileid="414" duration="400"/>
                    <frame tileid="415" duration="400"/>
                   </animation>
                  </tile>
                  <tile id="416">
                   <animation>
                    <frame tileid="416" duration="400"/>
                    <frame tileid="417" duration="400"/>
                   </animation>
                  </tile>
                  <tile id="443">
                   <animation>
                    <frame tileid="443" duration="200"/>
                    <frame tileid="444" duration="200"/>
                    <frame tileid="445" duration="200"/>
                    <frame tileid="446" duration="200"/>
                   </animation>
                  </tile>
                 </tileset>

                <layer name="lava" width="64" height="64">
                <data encoding="csv">
                %s
                </data>
                </layer>

                <layer name="grass" width="64" height="64">
                <data encoding="csv">
                %s
                </data>
                </layer>

                <layer name="water" width="64" height="64">
                <data encoding="csv">
                %s
                </data>
                </layer>

                <layer name="meadow" width="64" height="64">
                <data encoding="csv">
                %s
                </data>
                </layer>

                <layer name="forest" width="64" height="64">
                <data encoding="csv">
                %s
                </data>
                </layer>

                <layer name="mountains" width="64" height="64">
                <data encoding="csv">
                %s
                </data>
                </layer>

                <layer name="props" width="64" height="64">
                <data encoding="csv">
                %s
                </data>
                </layer>

                <objectgroup name="portals" width="64" height="64">
                %s
                </objectgroup>

                <objectgroup name="moongates" width="64" height="64">
                %s
                </objectgroup>
                              
                </map>""";

            return String.format(template,
                    lava, grass, water, meadow, forest, mountains, props,
                    portalString.toString(),
                    moongateString.toString()
            );

        }
    }

}
