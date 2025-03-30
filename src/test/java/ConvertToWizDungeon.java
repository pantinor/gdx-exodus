
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import objects.BaseMap;
import org.apache.commons.io.IOUtils;
import java.io.FileInputStream;
import java.util.List;
import exodus.Constants.DungeonTile;
import exodus.Constants.MapType;
import exodus.Constants.Maps;
import exodus.Exodus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.EndianUtils;

public class ConvertToWizDungeon implements ApplicationListener {

    //wizardry 1 proving grounds
    private static final String L1 = "555555555500545500040800500000007400005540914500005500944D00501440064000144040066400004000500300001440550100555546555500000001008100545551555500505550559900705550551500B09A50559A00301050550000605750B5990040555001000050155099590054154010400001005414640005005518540015015514A400150254804200150155149400094495105400458454140400494455040400450055020C0001000100410000000404000005002400000055551500D00055D52500D000956015005400952025007500550054581500150025581600054055D3C400010055D3C4005455000C040050000000740000554091450000550094C50050144006400014404006E40000400050010000144055010055554655550000000100810055555155550050555055950050D550551500907A5055990010105055000060555095990040557001000050155099590054154010400055555D555500000015055900014015069500454015056900850015A050004540150D6500015125045500112115054100125115014100114095004200004000405000000001010000010009004000F57F05007400555509007400155805005500254809405D001500155645000540099645000150D53471000140E534710000D8030000D801000CD809000C0000000CC089A860C021B266D81FB166180E0006003E9200000000000050000000000000000C0000000C0000000A0000000A0000B80F0000A00F0000F80F0000F80F00010000000002000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000500000000000000000000000000000000000000000000000000000000000000000000000AAAAAAAA0000A3AAAA4AAAAAAAAA0900A0AAAAAA0A0000000000000000000A0000000000000000000A7065000000000000080A0000000000000000000A0000000000000000000A0000000000000000000A0000000000000000000A0000000000000000000A00000000000000000010B16BBB8B050000000000000200FFFFFFFF01004D00B7FBB6FB00000000000000000000000000000000000007000000030004000B00120018000400000000000000000000000000000000000C00010008000F000400040004000100000000000000000000000000000000000000040000000100000000000500000003000000000003000000";

    public enum WizardryCellType {
        NORMAL, STAIRS, PIT, CHUTE, SPINNER, DARK, TELEPORT, DAMAGE, ELEVATOR, ROCK, NOSPELL, MESSAGE, ENCOUNTER
    }

    public static void main(String[] args) throws Exception {

        new LwjglApplication(new ConvertToWizDungeon());
    }

    @Override
    public void create() {

        try {

            Exodus ult = new Exodus();
            ult.create();

            byte[] initial = DatatypeConverter.parseHexBinary(L1);

            Map<String, List<LevelData>> data = new HashMap<>();

            for (BaseMap map : Exodus.maps.getMaps()) {

                if (!map.getFname().endsWith("ult") || map.getId() == Maps.SOSARIA.getId()) {
                    continue;
                }

                FileInputStream is = new FileInputStream("src/main/resources/assets/data/" + map.getFname());
                byte[] bytes = IOUtils.toByteArray(is);

                if (map.getType() == MapType.dungeon) {

                    List<LevelData> list = new ArrayList<>();

                    data.put(map.getFname().replace(".ult", ""), list);

                    int pos = 0;
                    for (int lvl = 0; lvl < map.getLevels(); lvl++) {

                        DungeonTile[][] tiles = new DungeonTile[16][16];
                        for (int y = 0; y < 16; y++) {
                            for (int x = 0; x < 16; x++) {
                                byte idx = bytes[pos];
                                DungeonTile dt = DungeonTile.getTileByValue(idx);
                                tiles[y][x] = dt;
                                pos++;
                            }
                        }

                        byte[] d = new byte[initial.length];
                        for (int i = 0; i < initial.length; i++) {
                            d[i] = initial[i];
                        }
                        for (int i = 0; i < 0x360; i++) {
                            d[i] = (byte) 0;
                        }

                        LevelData ld = new LevelData(d);
                        list.add(ld);

                        int rockId = ld.addCellInfo(WizardryCellType.ROCK, 0, 0, 0);

                        for (int y = 0; y < 20; y++) {
                            for (int x = 0; x < 20; x++) {
                                ld.cellInfoLocations[y][x] = (byte) rockId;
                            }
                        }

                        for (int y = 0; y < 15; y++) {
                            for (int x = 0; x < 15; x++) {
                                ld.cellInfoLocations[y][x] = 0;
                            }
                        }

                        for (int y = 1; y < 16; y++) {
                            for (int x = 1; x < 16; x++) {

                                DungeonTile dt = tiles[y][x];
                                boolean ww = x - 1 < 0 ? true : tiles[y][x - 1] == DungeonTile.WALL;
                                boolean ew = x + 1 >= 16 ? true : tiles[y][x + 1] == DungeonTile.WALL;
                                boolean nw = y - 1 < 0 ? true : tiles[y - 1][x] == DungeonTile.WALL;
                                boolean sw = y + 1 >= 16 ? true : tiles[y + 1][x] == DungeonTile.WALL;

                                boolean wd = x - 1 < 0 ? false : tiles[y][x - 1] == DungeonTile.DOOR;
                                boolean ed = x + 1 >= 16 ? false : tiles[y][x + 1] == DungeonTile.DOOR;
                                boolean nd = y - 1 < 0 ? false : tiles[y - 1][x] == DungeonTile.DOOR;
                                boolean sd = y + 1 >= 16 ? false : tiles[y + 1][x] == DungeonTile.DOOR;

                                boolean hwd = x - 1 < 0 ? false : tiles[y][x - 1] == DungeonTile.SECRET_DOOR;
                                boolean hed = x + 1 >= 16 ? false : tiles[y][x + 1] == DungeonTile.SECRET_DOOR;
                                boolean hnd = y - 1 < 0 ? false : tiles[y - 1][x] == DungeonTile.SECRET_DOOR;
                                boolean hsd = y + 1 >= 16 ? false : tiles[y + 1][x] == DungeonTile.SECRET_DOOR;

                                int wx = x - 1;
                                int wy = 20 - 4 - y - 1;

                                writeCellWallsDoors(wx, wy, dt,
                                        nw, sw, ew, ww,
                                        nd, sd, ed, wd,
                                        hnd, hsd, hed, hwd,
                                        d);

                                if (dt == DungeonTile.LADDER_UP) {
                                    ld.cellInfoLocations[wx][wy] = (byte) ld.cellInfoIndex;
                                    ld.addCellInfo(WizardryCellType.STAIRS, (short) (lvl + 1 - 1), (short) (wy), (short) (wx));
                                }
                                if (dt == DungeonTile.LADDER_DOWN) {
                                    ld.cellInfoLocations[wx][wy] = (byte) ld.cellInfoIndex;
                                    ld.addCellInfo(WizardryCellType.STAIRS, (short) (lvl + 1 + 1), (short) (wy), (short) (wx));
                                }
                                if (dt == DungeonTile.WALL) {
                                    ld.cellInfoLocations[wx][wy] = (byte) rockId;
                                }
                            }
                        }

                        ld.writeCellInfoLocations();
                    }

                }

                for (String m : data.keySet()) {
                    List<LevelData> list = data.get(m);
                    for (int i = 0; i < list.size(); i++) {
                        byte[] bb = list.get(i).data;
                        System.out.println("private static final String ULT_EX_" + map.getFname().replace(".ult", "").toUpperCase() + "_" + i + " = \"" + DatatypeConverter.printHexBinary(bb) + "\";");
                    }
                    return;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("DONE");
    }

    private void writeCellWallsDoors(int column, int row, DungeonTile tile,
            boolean nw, boolean sw, boolean ew, boolean ww,
            boolean nd, boolean sd, boolean ed, boolean wd,
            boolean hnd, boolean hsd, boolean hed, boolean hwd,
            byte[] data) {

        if (tile == DungeonTile.DOOR || tile == DungeonTile.SECRET_DOOR) {
            if (ew && ww) {
                nd = true;
                sd = true;
            }
            if (nw && sw) {
                ed = true;
                wd = true;
            }
        }

        if (tile == DungeonTile.SECRET_DOOR) {
            nw = true;
            sw = true;
            ew = true;
            ww = true;
        }

        if (hnd) {
            nw = true;
            nd = true;
        }

        if (hsd) {
            sw = true;
            sd = true;
        }

        if (hed) {
            ew = true;
            ed = true;
        }

        if (hwd) {
            ww = true;
            wd = true;
        }

        int offset = column * 6 + row / 4;

        if (tile != DungeonTile.WALL) {
            setWallsDoors(data, offset, row, ww, wd);
            setWallsDoors(data, offset + 120, row, sw, sd);
            setWallsDoors(data, offset + 240, row, ew, ed);
            setWallsDoors(data, offset + 360, row, nw, nd);
        } else {
            //setWallsDoors(data, offset, row, true, false);
            //setWallsDoors(data, offset + 120, row, true, false);
            //setWallsDoors(data, offset + 240, row, true, false);
            //setWallsDoors(data, offset + 360, row, true, false);
        }
    }

    private void setWallsDoors(byte[] buffer, int offset, int row, boolean wall, boolean door) {
        int shift = (row % 4) * 2;

        int currentValue = buffer[offset] & 0xFF;
        currentValue &= ~(3 << shift); // Clear the two bits at the position

        if (wall) {
            currentValue |= (1 << shift);
        }
        if (door) {
            currentValue |= (1 << (shift + 1));
        }

        buffer[offset] = (byte) currentValue;
    }

    private class LevelData {

        final byte[] data;
        final byte[][] cellInfoLocations = new byte[20][20];
        final CellInfo[] cellInfo = new CellInfo[16];
        int cellInfoIndex = 1;

        public LevelData(byte[] d) {
            this.data = d;
            int offset = 0x230;
            for (int col = 0; col < 20; col++) {
                for (int row = 0; row < 20; row += 2) {
                    cellInfoLocations[col][row] = (byte) (data[offset] & 0x0F);
                    cellInfoLocations[col][row + 1] = (byte) ((data[offset] & 0xF0) >>> 4);
                    offset++;
                }
            }
            for (int i = 0; i < 16; i++) {
                cellInfo[i] = new CellInfo();
            }
        }

        private int addCellInfo(WizardryCellType type, int v0, int v1, int v2) {
            int infoId = this.cellInfoIndex;
            this.cellInfoIndex++;

            CellInfo ci = this.cellInfo[infoId];
            ci.type = type;
            ci.val[0] = (short) v0;
            ci.val[1] = (short) v1;
            ci.val[2] = (short) v2;
            ci.write(infoId, this.data, 0x2F8);

            return infoId;
        }

        private void writeCellInfoLocations() {
            int offset = 0x230;
            for (int col = 0; col < 20; col++) {
                for (int row = 0; row < 20; row += 2) {
                    byte combined = (byte) ((cellInfoLocations[col][row + 1] << 4) | (cellInfoLocations[col][row] & 0x0F));
                    data[offset] = combined;
                    offset++;
                }
            }
        }

    }

    private static class CellInfo {

        private WizardryCellType type = WizardryCellType.NORMAL;
        private final short[] val = new short[3];
        private final byte[][] data = new byte[3][2];

        private void write(int index, byte[] buffer, int offset) {
            byte existingByte = buffer[offset + index / 2];
            int typeIndex = this.type.ordinal();

            if (index % 2 == 0) {
                existingByte = (byte) ((existingByte & 0xF0) | (typeIndex & 0x0F));
            } else {
                existingByte = (byte) ((existingByte & 0x0F) | ((typeIndex & 0x0F) << 4));
            }

            buffer[offset + index / 2] = existingByte;

            buffer[offset + 8 + index * 2] = this.data[0][0];
            buffer[offset + 8 + index * 2 + 1] = this.data[0][1];

            buffer[offset + 40 + index * 2] = this.data[1][0];
            buffer[offset + 40 + index * 2 + 1] = this.data[1][1];

            buffer[offset + 72 + index * 2] = this.data[2][0];
            buffer[offset + 72 + index * 2 + 1] = this.data[2][1];

            EndianUtils.writeSwappedShort(buffer, offset + 8 + index * 2, this.val[0]);
            EndianUtils.writeSwappedShort(buffer, offset + 40 + index * 2, this.val[1]);
            EndianUtils.writeSwappedShort(buffer, offset + 72 + index * 2, this.val[2]);
        }
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

}
