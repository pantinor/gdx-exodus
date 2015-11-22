
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import objects.BaseMap;
import objects.MapSet;
import objects.TileSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import exodus.Constants.DungeonTile;
import exodus.Constants.MapType;
import exodus.Constants.Maps;
import java.util.Random;
import objects.Tile;
import static util.Utils.rand;
import util.XORShiftRandom;

public class UltIsometricTmxConvert implements ApplicationListener {

    private static enum LayerType {

        BKGDN, COLLISION, OBJECT, AUTOMAP
    }

    public static void main(String[] args) throws Exception {

        new LwjglApplication(new UltIsometricTmxConvert());
    }

    @Override
    public void create() {

        try {

            File file2 = new File("target/classes/assets/xml/u4-tileset-base.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(TileSet.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            TileSet ts = (TileSet) jaxbUnmarshaller.unmarshal(file2);
            ts.setMaps();

            File file3 = new File("target/classes/assets/xml/maps.xml");
            jaxbContext = JAXBContext.newInstance(MapSet.class);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            MapSet ms = (MapSet) jaxbUnmarshaller.unmarshal(file3);
            ms.init(MapType.combat, ts);

            for (BaseMap map : ms.getMaps()) {

                if (map.getType() != MapType.combat) {
                    continue;
                }

                FileInputStream is = new FileInputStream("target/classes/assets/data/" + map.getFname());
                byte[] bytes = IOUtils.toByteArray(is);

                String[][] bkgrnd = new String[33][33];
                String[][] collision = new String[33][33];
                String[][] objects = new String[33][33];

                int pos = 0x40;
                for (int y = 0; y < 11; y++) {
                    for (int x = 0; x < 11; x++) {
                        int index = bytes[pos] & 0xff;
                        pos++;
                        Tile tile = ts.getTileByIndex(index);
                        for (int c = 0; c < 3; c++) {
                            for (int d = 0; d < 3; d++) {
                                bkgrnd[y * 3 + c][x * 3 + d] = setBackgroundTile(tile);
                                collision[y * 3 + c][x * 3 + d] = setCollisionTile(tile);
                                objects[y * 3 + c][x * 3 + d] = setObjectTile(tile);
                            }
                        }
                    }
                }

                StringBuilder[] bksb = new StringBuilder[1];
                StringBuilder[] colsb = new StringBuilder[1];
                StringBuilder[] objsb = new StringBuilder[1];

                bksb[0] = new StringBuilder();
                colsb[0] = new StringBuilder();
                objsb[0] = new StringBuilder();

                for (int y = 0; y < 33; y++) {
                    for (int x = 0; x < 33; x++) {
                        bksb[0].append(bkgrnd[y][x]).append(",");
                        colsb[0].append(collision[y][x]).append(",");
                        objsb[0].append(objects[y][x]).append(",");
                    }
                    bksb[0].append("\n");
                    colsb[0].append("\n");
                    objsb[0].append("\n");
                }
                bksb[0].delete(bksb[0].length() - 2, bksb[0].length());
                colsb[0].delete(colsb[0].length() - 2, colsb[0].length());
                objsb[0].delete(objsb[0].length() - 2, objsb[0].length());

                String template = FileUtils.readFileToString(new File("./src/main/resources/assets/iso-tmx/combat_template.tmx"));
                String data = String.format(template, map.getFname(), bksb[0], objsb[0], colsb[0]);
                String tmxFName = String.format("./src/main/resources/assets/iso-tmx/iso_combat_%s_%s.tmx", map.getId(), map.getFname().replace(".ult", ""));
                FileUtils.writeStringToFile(new File(tmxFName), data);
                System.out.printf("Wrote: %s\n", tmxFName);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("DONE");
    }
    public static Random rand = new XORShiftRandom();

    private String setBackgroundTile(Tile tile) {
        if (tile == null) {
            return "0";
        }
        if (tile.getName().equals("grass")) {
            int r = rand.nextInt(16) + 16;
            return "" + r;
        } else if (tile.getName().equals("brush")) {
            int r = rand.nextInt(16) + 16;
            return "" + r;
        } else if (tile.getName().equals("forest")) {
            int r = rand.nextInt(16) + 16;
            return "" + r;
        } else if (tile.getName().equals("water")) {
            int r = rand.nextInt(16) + 144 + 32;
            return "" + r;
        } else if (tile.getName().equals("swamp")) {
            int r = rand.nextInt(16) + 16;
            return "" + r;
        } else if (tile.getName().equals("hills")) {
            int r = rand.nextInt(16) + 16 + 16;
            return "" + r;
        } else if (tile.getName().equals("rocks")) {
            int r = rand.nextInt(16) + 16 + 16;
            return "" + r;
        }
        return "0";
    }

    private String setCollisionTile(Tile tile) {
        if (tile.getName().equals("water")) {
            return "3";
        } else if (tile.getName().equals("mountains")) {
            return "3";
        } else if (tile.getName().equals("rocks")) {
            return "3";
        }
        return "0";
    }

    private String setObjectTile(Tile tile) {

        if (tile.getName().equals("brush")) {
            int r = rand.nextInt(16) + 16 + 96;
            return "" + r;
        } else if (tile.getName().equals("forest")) {
            int r = rand.nextInt(32) + 208;
            return "" + r;
        } else if (tile.getName().equals("rocks")) {
            int r = rand.nextInt(3) + 467 + 134;
            return "" + r;
        } else if (tile.getName().equals("hills")) {
            int r = rand.nextInt(3) + 467 + 134;
            return "" + r;
        } else if (tile.getName().equals("mountains")) {
            int r = 0 + 16 + 32;
            return "" + r;
        }
        return "0";
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

    private enum IsoData {

        NONE(DungeonTile.FLOOR, "");

        private final String[] ids = new String[16];
        private final DungeonTile dt;

        private IsoData(DungeonTile dt, String i) {
            this.dt = dt;
        }

    }

}
