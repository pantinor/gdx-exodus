
import exodus.Constants;
import exodus.Constants.WeaponType;
import exodus.Exodus;
import exodus.StartScreen;
import static exodus.StartScreen.movesCommands;
import static exodus.StartScreen.movesData;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.Collection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import objects.BaseMap;
import objects.MapSet;
import objects.SaveGame;
import objects.SaveGame.CharacterRecord;
import objects.Tile;
import objects.TileSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.testng.annotations.Test;
import util.Utils;

public class TestJaxb {

    //@Test
    public void testTileSetBase() throws Exception {
        File file = new File("target/classes/assets/xml/tileset-base.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(TileSet.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        TileSet ts = (TileSet) jaxbUnmarshaller.unmarshal(file);
        for (Tile t : ts.getTiles()) {
            System.out.println(t);
        }
    }

    //@Test
    public void testMapSet() throws Exception {
        File file = new File("target/classes/assets/xml/maps.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(MapSet.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        MapSet ms = (MapSet) jaxbUnmarshaller.unmarshal(file);
        for (BaseMap m : ms.getMaps()) {
            System.out.println(m);
        }
    }

    //@Test
    public void testReadSaveGame() throws Exception {

        SaveGame sg = new SaveGame();

        sg.pc1 = 0xff;
        sg.pc2 = 0xff;
        sg.pc3 = 0xff;
        sg.pc4 = 0xff;

        for (int x = 0; x < 4; x++) {
            CharacterRecord avatar = new CharacterRecord();
            avatar.name = "char_" + x;
            avatar.health = 199;
            avatar.food = 30000;
            avatar.gold = 200;
            avatar.torches = 2;
            avatar.qtyWeapons[WeaponType.EXOTIC.ordinal()] = 0xFE;
            sg.players[x] = avatar;
        }

        sg.write(Constants.PARTY_SAV_BASE_FILENAME);

        //sg.write("test.sav");
    }

}
