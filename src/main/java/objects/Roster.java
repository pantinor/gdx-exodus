package objects;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import objects.SaveGame.CharacterRecord;

public class Roster {

    private static final CharacterRecord[] records = new CharacterRecord[20];

    static {
        for (int x = 0; x < 20; x++) {
            records[x] = new CharacterRecord();
        }
    }

    public static void read(String strFilePath) throws Exception {

        InputStream is;
        LittleEndianDataInputStream dis = null;
        try {
            is = new FileInputStream(strFilePath);
            dis = new LittleEndianDataInputStream(is);
        } catch (Exception e) {
            throw new Exception("Cannot read save file");
        }

        for (int x = 0; x < 20; x++) {
            CharacterRecord rec = new CharacterRecord();
            rec.read(dis);
            records[x] = rec;
        }

    }

    public static void write(String strFilePath) throws Exception {
        FileOutputStream fos = new FileOutputStream(strFilePath);
        LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(fos);
        for (int x = 0; x < 20; x++) {
            if (records[x] == null) {
                records[x] = new CharacterRecord();
            }
            records[x].write(dos);
        }
    }

    public static void addCharacter(CharacterRecord rec, int index) {
        records[index] = rec;
    }

    public static void removeCharacter(int index) {
        records[index] = new CharacterRecord();
    }

}
