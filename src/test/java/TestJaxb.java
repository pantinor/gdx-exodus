
import exodus.Constants.CreatureType;
import exodus.Constants.InventoryType;
import exodus.Constants.Maps;
import static exodus.Constants.PARTY_SAV_BASE_FILENAME;
import exodus.Context;
import exodus.Party;
import java.io.File;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import objects.BaseMap;
import objects.MapSet;
import objects.SaveGame;
import objects.SaveGame.CharacterRecord;
import objects.Tile;
import objects.TileSet;
import org.testng.Assert;
import vendor.OracleService;
import vendor.VendorClassSet;

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
        try {
            sg.read(PARTY_SAV_BASE_FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int x = 0;

//        sg.pc1 = 0xff;
//        sg.pc2 = 0xff;
//        sg.pc3 = 0xff;
//        sg.pc4 = 0xff;
//
//        for (int x = 0; x < 4; x++) {
//            CharacterRecord avatar = new CharacterRecord();
//            avatar.name = "char_" + x;
//            avatar.health = 199;
//            avatar.food = 30000;
//            avatar.gold = 200;
//            avatar.torches = 2;
//            avatar.qtyWeapons[WeaponType.EXOTIC.ordinal()] = 0xFE;
//            sg.players[x] = avatar;
//        }
        //sg.write(Constants.PARTY_SAV_BASE_FILENAME);
        //sg.write("test.sav");
    }

    //@Test
    public void testAdvanceLevel() throws Exception {

        CharacterRecord rec = new CharacterRecord();
        rec.name = "Adventurer";
        rec.exp = 0;

        rec.meetLordBritish();
        Assert.assertEquals(rec.getLevel(), 1);

        rec.exp = 110;
        Assert.assertEquals(rec.getLevel(), 1);

        rec.meetLordBritish();
        Assert.assertEquals(rec.getLevel(), 2);

        rec.exp = 175;
        Assert.assertEquals(rec.getLevel(), 2);

        rec.meetLordBritish();
        Assert.assertEquals(rec.getLevel(), 2);

        rec.exp = 230;
        Assert.assertEquals(rec.getLevel(), 2);

        rec.meetLordBritish();
        Assert.assertEquals(rec.getLevel(), 3);

        rec.exp = 415;
        Assert.assertEquals(rec.getLevel(), 3);

        rec.meetLordBritish();
        Assert.assertEquals(rec.getLevel(), 5);

        rec.exp = 515;
        rec.meetLordBritish();
        Assert.assertEquals(rec.getLevel(), 5);

        rec.exp = 615;
        rec.meetLordBritish();
        Assert.assertEquals(rec.getLevel(), 5);

        rec.marks[0] = 1;
        rec.meetLordBritish();
        Assert.assertEquals(rec.getLevel(), 7);

    }

    //@Test
    public void testFoodVendor() throws Exception {

        File file = new File("target/classes/assets/xml/vendor.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(VendorClassSet.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        VendorClassSet vcs = (VendorClassSet) jaxbUnmarshaller.unmarshal(file);
        vcs.init();

        CharacterRecord rec = new CharacterRecord();
        rec.name = "avatar";
        rec.health = 200;

        SaveGame sg = new SaveGame();
        sg.players[0] = rec;

        Party party = new Party(sg);
        Context context = new Context();
        context.setParty(party);
        party.addMember(rec);

        rec.gold = 2000;

        OracleService v = new OracleService(vcs.getVendor(InventoryType.ORACLEINFO, Maps.DAWN), context, party.getMember(0));

        while (true) {

            if (!v.nextDialog()) {
                break;
            }

            String answer = JOptionPane.showInputDialog(null, "");

            if (answer != null && answer.equals("bye")) {
                break;
            }

            v.setResponse(answer);

        }

        System.err.println("sg gold = " + rec.gold);

    }

    //@Test
    public void testRandDung() throws Exception {
        Random rand = new Random();
        int currentLevel = 8;

        for (int i = 0; i < 20; i++) {

            int total = 0;
            for (CreatureType ct : CreatureType.values()) {
                total += (ct.getSpawnLevel() <= currentLevel) ? ct.getSpawnWeight() : 0;
            }

            int thresh = rand.nextInt(total);
            CreatureType monster = null;

            for (CreatureType ct : CreatureType.values()) {
                thresh -= (ct.getSpawnLevel() <= currentLevel) ? ct.getSpawnWeight() : 0;
                if (thresh < 0) {
                    monster = ct;
                    break;
                }
            }

            System.out.printf("spawned on level %s : %s\n", currentLevel, monster.toString());
        }
    }


}
