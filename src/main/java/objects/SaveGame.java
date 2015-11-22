package objects;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import exodus.Constants;
import util.Utils;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import exodus.Party;
import util.XORShiftRandom;

public class SaveGame implements Constants {

    public CharacterRecord[] players = new CharacterRecord[4];

    public int transport;
    public int location;
    public int moves;
    public int numberInParty;
    public int partyX;
    public int partyY;
    public int dnglevel;
    public int orientation;
    public int shiphull;
    public int pc1;
    public int pc2;
    public int pc3;
    public int pc4;

    public byte[] monster_save_tileids = new byte[8];
    public byte[] monster_save_x = new byte[8];
    public byte[] monster_save_y = new byte[8];

    public byte[] objects_save_tileids = new byte[24];
    public byte[] objects_save_x = new byte[24];
    public byte[] objects_save_y = new byte[24];

    private static final Random rand = new XORShiftRandom();

    public void resetMonsters() {
        monster_save_tileids = new byte[8];
        monster_save_x = new byte[8];
        monster_save_y = new byte[8];
        objects_save_tileids = new byte[24];
        objects_save_x = new byte[24];
        objects_save_y = new byte[24];
    }

    public void write(String strFilePath) throws Exception {

        FileOutputStream fos = new FileOutputStream(strFilePath);
        LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(fos);

        dos.writeByte(transport);
        dos.writeByte(location);
        dos.writeInt(moves);
        dos.writeByte(numberInParty);
        dos.writeByte(partyX);
        dos.writeByte(partyY);
        dos.writeByte(dnglevel);
        dos.writeByte(orientation);
        dos.writeByte(shiphull);

        dos.writeByte(pc1);
        dos.writeByte(pc2);
        dos.writeByte(pc3);
        dos.writeByte(pc4);

        for (int i = 0; i < 4; i++) {
            if (players[i] == null) {
                players[i] = new CharacterRecord();
            }
            players[i].write(dos);
        }

        for (byte b : monster_save_tileids) {
            dos.writeByte(b);
        }
        for (byte b : objects_save_tileids) {
            dos.writeByte(b);
        }
        for (byte b : monster_save_x) {
            dos.writeByte(b);
        }
        for (byte b : objects_save_x) {
            dos.writeByte(b);
        }
        for (byte b : monster_save_y) {
            dos.writeByte(b);
        }
        for (byte b : objects_save_y) {
            dos.writeByte(b);
        }

        dos.close();

    }

    public void read(String strFilePath) throws Exception {
        InputStream is;
        LittleEndianDataInputStream dis = null;
        try {
            is = new FileInputStream(strFilePath);
            dis = new LittleEndianDataInputStream(is);
        } catch (Exception e) {
            throw new Exception("Cannot read save file");
        }
        read(dis);
    }

    public void read(LittleEndianDataInputStream dis) throws Exception {

        transport = dis.readByte() & 0xff;
        location = dis.readByte() & 0xff;
        moves = dis.readInt();
        numberInParty = dis.readByte() & 0xff;
        partyX = dis.readByte() & 0xff;
        partyY = dis.readByte() & 0xff;
        dnglevel = dis.readByte() & 0xff;
        orientation = dis.readByte() & 0xff;
        shiphull = dis.readByte() & 0xff;

        pc1 = dis.readByte() & 0xff;
        pc2 = dis.readByte() & 0xff;
        pc3 = dis.readByte() & 0xff;
        pc4 = dis.readByte() & 0xff;

        for (int i = 0; i < 4; i++) {
            players[i] = new CharacterRecord();
            players[i].read(dis);
        }

        try {
            for (int i = 0; i < monster_save_tileids.length; i++) {
                monster_save_tileids[i] = dis.readByte();
            }
            for (int i = 0; i < objects_save_tileids.length; i++) {
                objects_save_tileids[i] = dis.readByte();
            }
            for (int i = 0; i < monster_save_x.length; i++) {
                monster_save_x[i] = dis.readByte();
            }
            for (int i = 0; i < objects_save_x.length; i++) {
                objects_save_x[i] = dis.readByte();
            }
            for (int i = 0; i < monster_save_y.length; i++) {
                monster_save_y[i] = dis.readByte();
            }
            for (int i = 0; i < objects_save_y.length; i++) {
                objects_save_y[i] = dis.readByte();
            }
        } catch (Exception e) {

        }

        //set initial start
        if (partyX == 0 && partyY == 0) {
            partyX = 182;
            partyY = 73;
        }

        dis.close();

    }

    public static class CharacterRecord {

        public String name = null;
        public int portaitIndex = 11 + 2 * 16;
        public int markKings;
        public int markSnake;
        public int markFire;
        public int markForce;
        public int cardDeath;
        public int cardSol;
        public int cardLove;
        public int cardMoons;
        public int torches;
        public int inParty;
        public StatusType status = StatusType.GOOD;
        public int str;
        public int dex;
        public int intell;
        public int wis;
        public ClassType race = ClassType.HUMAN;
        public Profession profession = Profession.FIGHTER;
        public SexType sex = SexType.MALE;
        public int mana;
        public int health;
        public int maxHealth;
        public int exp;
        public int food;
        public int submorsels = 100;
        public int gold;
        public int gems;
        public int keys;
        public int powder;
        public ArmorType armor = ArmorType.NONE;
        public WeaponType weapon = WeaponType.NONE;

        public int[] weapons = new int[WeaponType.values().length];
        public int[] armors = new int[ArmorType.values().length];

        public int write(LittleEndianDataOutputStream dos) throws Exception {
            if (name == null || name.length() < 1) {
                for (int i = 0; i < 16; i++) {
                    dos.writeByte(0);
                }
            } else {
                String paddedName = StringUtils.rightPad(name, 16);
                byte[] nameArray = paddedName.getBytes();
                for (int i = 0; i < 16; i++) {
                    if (nameArray[i] == 32) {
                        nameArray[i] = 0;
                    }
                    dos.writeByte(nameArray[i]);
                }
            }

            dos.writeByte(markKings);
            dos.writeByte(markSnake);
            dos.writeByte(markFire);
            dos.writeByte(markForce);
            dos.writeByte(cardDeath);
            dos.writeByte(cardSol);
            dos.writeByte(cardLove);
            dos.writeByte(cardMoons);
            dos.writeByte(torches);
            dos.writeByte(inParty);
            dos.writeByte(status.ordinal());
            dos.writeByte(str);
            dos.writeByte(dex);
            dos.writeByte(intell);
            dos.writeByte(wis);
            dos.writeByte(race.ordinal());
            dos.writeByte(profession.ordinal());
            dos.writeByte(sex.ordinal());
            dos.writeShort(mana);
            dos.writeShort(health);
            dos.writeShort(maxHealth);
            dos.writeShort(exp);
            dos.writeShort(food);
            dos.writeShort(gold);
            dos.writeByte(gems);
            dos.writeByte(keys);
            dos.writeByte(powder);
            dos.writeByte(armor.ordinal());
            dos.writeByte(weapon.ordinal());

            for (ArmorType t : ArmorType.values()) {
                if (t == ArmorType.NONE) {
                    continue;
                }
                dos.writeByte(armors[t.ordinal()]);
            }

            for (WeaponType t : WeaponType.values()) {
                if (t == WeaponType.NONE) {
                    continue;
                }
                dos.writeByte(weapons[t.ordinal()]);
            }

            dos.writeShort(portaitIndex);
            dos.writeByte(0);
            dos.writeInt(0);

            return 1;
        }

        public int read(LittleEndianDataInputStream dis) throws Exception {

            byte[] nameArray = new byte[16];
            boolean end = false;
            for (int i = 0; i < 16; i++) {
                byte b = dis.readByte();
                if (b == 0) {
                    end = true;
                };
                if (!end) {
                    nameArray[i] = b;
                }
            }
            name = new String(nameArray).trim();

            markKings = dis.readByte();
            markSnake = dis.readByte();
            markFire = dis.readByte();
            markForce = dis.readByte();
            cardDeath = dis.readByte();
            cardSol = dis.readByte();
            cardLove = dis.readByte();
            cardMoons = dis.readByte();
            torches = dis.readByte();
            inParty = dis.readByte();
            status = StatusType.get(dis.readByte());
            str = dis.readByte();
            dex = dis.readByte();
            intell = dis.readByte();
            wis = dis.readByte();
            race = ClassType.get(dis.readByte());
            profession = Profession.get(dis.readByte());
            sex = SexType.get(dis.readByte());
            mana = dis.readShort();
            health = dis.readShort();
            maxHealth = dis.readShort();
            exp = dis.readShort();
            food = dis.readShort();
            gold = dis.readShort();
            gems = dis.readByte();
            keys = dis.readByte();
            powder = dis.readByte();
            armor = ArmorType.get(dis.readByte());
            weapon = WeaponType.get(dis.readByte());

            for (ArmorType t : ArmorType.values()) {
                if (t == ArmorType.NONE) {
                    continue;
                }
                armors[t.ordinal()] = dis.readByte();
            }

            for (WeaponType t : WeaponType.values()) {
                if (t == WeaponType.NONE) {
                    continue;
                }
                weapons[t.ordinal()] = dis.readByte();
            }

            portaitIndex = dis.readShort();
            dis.readByte();
            dis.readInt();

            return 1;
        }

        public int getMaxMana() {
            int maxMana = 0;
            switch (profession) {
                case BARBARIAN:
                case FIGHTER:
                case THIEF:
                    break;
                case DRUID:
                    maxMana = wis / 2 > intell / 2 ? wis / 2 : intell / 2;
                    break;
                case ALCHEMIST:
                    maxMana = intell / 2;
                    break;
                case RANGER:
                    maxMana = wis / 2 > intell / 2 ? intell / 2 : wis / 2;
                    break;
                case WIZARD:
                    maxMana = intell;
                    break;
                case LARK:
                    maxMana = intell / 2;
                    break;
                case ILLUSIONIST:
                    maxMana = wis / 2;
                    break;
                case CLERIC:
                    maxMana = wis;
                    break;
                case PALADIN:
                    maxMana = wis / 2;
                    break;
                default:

            }
            return maxMana;
        }

        public int getLevel() {
            return exp / 100;
        }

        public int getMaxLevel() {
            int level = 1;
            int next = 100;

            while (exp >= next && level < 25) {
                level++;
                next <<= 1;
            }

            return level;
        }

        public void adjustMp(int pts) {
            mana = Utils.adjustValueMax(mana, pts, getMaxMana());
        }

        public void adjustGold(int v) {
            gold = Utils.adjustValue(gold, v, 9999, 0);
        }

        public boolean advanceLevel() {
            if (getLevel() == getMaxLevel()) {
                return false;
            }

            status = StatusType.GOOD;
            maxHealth = getMaxLevel() * 100;
            health = maxHealth;

            /* improve stats by 1-8 each */
            str += rand.nextInt(8) + 1;
            dex += rand.nextInt(8) + 1;
            intell += rand.nextInt(8) + 1;
            wis += rand.nextInt(8) + 1;

            if (str > this.race.getMaxStr()) {
                str = this.race.getMaxStr();
            }
            if (dex > this.race.getMaxDex()) {
                dex = this.race.getMaxDex();
            }
            if (intell > this.race.getMaxInt()) {
                intell = this.race.getMaxInt();
            }
            if (wis > this.race.getMaxWis()) {
                wis = this.race.getMaxWis();
            }
            return true;

        }

    }
}
