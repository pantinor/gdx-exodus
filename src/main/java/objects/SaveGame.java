package objects;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import exodus.Constants;
import util.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import util.XORShiftRandom;

public class SaveGame implements Constants {

    public CharacterRecord[] players = new CharacterRecord[4];

    public int transport;
    public int location;
    public int moves;
    public int numberInParty;
    public int partyX;
    public int partyY;
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

    Texture zstatsBox;

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
        dos.writeByte(pc1);
        dos.writeByte(pc2);
        dos.writeByte(pc3);
        dos.writeByte(pc4);

        dos.writeByte(0);
        dos.writeByte(0);
        dos.writeByte(0);

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
            is = new FileInputStream(Gdx.files.internal(strFilePath).file());
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
        pc1 = dis.readByte() & 0xff;
        pc2 = dis.readByte() & 0xff;
        pc3 = dis.readByte() & 0xff;
        pc4 = dis.readByte() & 0xff;

        dis.readByte();
        dis.readByte();
        dis.readByte();

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
        
        if (partyX == 0 && partyY == 0) {
            partyX = 47;
            partyY = 20;
        }

        dis.close();

    }

    public static class CharacterRecord {

        public String name = null;
        public int portaitIndex = 11+2*16;
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
        public int gold;
        public int gems;
        public int keys;
        public int powder;
        public ArmorType armor = ArmorType.NONE;
        public WeaponType weapon = WeaponType.NONE;

        public int[] qtyWeapons = new int[WeaponType.values().length];
        public int[] qtyArmors = new int[ArmorType.values().length];

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
                dos.writeByte(qtyArmors[t.ordinal()]);
            }

            for (WeaponType t : WeaponType.values()) {
                if (t == WeaponType.NONE) {
                    continue;
                }
                dos.writeByte(qtyWeapons[t.ordinal()]);
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
                qtyArmors[t.ordinal()] = dis.readByte();
            }

            for (WeaponType t : WeaponType.values()) {
                if (t == WeaponType.NONE) {
                    continue;
                }
                qtyWeapons[t.ordinal()] = dis.readByte();
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

            if (str > 50) {
                str = 50;
            }
            if (dex > 50) {
                dex = 50;
            }
            if (intell > 50) {
                intell = 50;
            }
            return true;

        }

    }

    //to proper case
    public static String pc(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public String[] getZstats() {

        StringBuilder sb1 = new StringBuilder();
        for (int i = 0; i < numberInParty; i++) {
            CharacterRecord p = players[i];

            sb1.append(
                    pc(p.name) + "  "
                    + pc(p.race.toString()) + "|"
                    + pc(p.profession.toString()) + "|"
                    + pc(p.sex.getDesc()) + "  "
                    + p.status.getId() + "|"
            );

            sb1.append(
                    "MANA: " + p.mana + "  LV: " + Math.round(p.maxHealth / 100) + "|"
                    + "STR: " + p.str + "  HP: " + p.health + "|"
                    + "DEX: " + p.dex + "  HM: " + p.maxHealth + "|"
                    + "INT: " + p.intell + "  EX: " + p.exp + "|"
                    + "W: " + pc(p.weapon.toString()) + "|"
                    + "A: " + pc(p.armor.toString()) + "|");

            sb1.append("~");

        }

//        StringBuilder sb4 = new StringBuilder();
//        sb4.append(torches + " - Torches|");
//        sb4.append(gems + " - Gems|");
//        sb4.append(powders + " - Powders|");
//        sb4.append(keys + " - Keys| |");
//
//        for (Item item : Constants.Item.values()) {
//            if (!item.isVisible()) {
//                continue;
//            }
//            sb4.append((this.items & (1 << item.ordinal())) > 0 ? item.getDesc() + "|" : "");
//        }
//
        String[] ret = new String[1];
        ret[0] = sb1.toString();

        return ret;
    }

    public void renderZstats(int showZstats, BitmapFont font, Batch batch, int SCREEN_HEIGHT) {

        if (zstatsBox == null) {
            Pixmap pixmap = new Pixmap(175, 490, Format.RGBA8888);
            pixmap.setColor(0f, 0f, 0f, 0.65f);
            pixmap.fillRectangle(0, 0, 175, 490);
            zstatsBox = new Texture(pixmap);
            pixmap.dispose();
        }

        batch.draw(zstatsBox, 5, SCREEN_HEIGHT - 5 - 490);

        int rx = 10;
        int ry = SCREEN_HEIGHT - 10;

        String[] pages = getZstats();
        if (showZstats >= STATS_PLAYER1 && showZstats <= STATS_PLAYER4) {
            // players
            String[] players = pages[0].split("\\~");
            for (int i = 0; i < players.length; i++) {
                String[] lines = players[i].split("\\|");
                if (i != showZstats - 1) {
                    continue;
                }
                rx = 10;
                ry = SCREEN_HEIGHT - 10;
                font.draw(batch, "Player " + (i + 1), rx, ry);
                ry = ry - 18;
                for (int j = 0; j < lines.length; j++) {
                    if (lines[j] == null || lines[j].length() < 1) {
                        continue;
                    }
                    font.draw(batch, lines[j], rx, ry);
                    ry = ry - 18;
                }
            }
        } else if (showZstats == STATS_WEAPONS) {
            String[] lines = pages[1].split("\\|");
            font.draw(batch, "Weapons", rx, ry);
            ry = ry - 18;
            for (int j = 0; j < lines.length; j++) {
                if (lines[j] == null || lines[j].length() < 1) {
                    continue;
                }
                font.draw(batch, lines[j], rx, ry);
                ry = ry - 18;
            }
        } else if (showZstats == STATS_ARMOR) {
            String[] lines = pages[2].split("\\|");
            font.draw(batch, "Armor", rx, ry);
            ry = ry - 18;
            for (int j = 0; j < lines.length; j++) {
                if (lines[j] == null || lines[j].length() < 1) {
                    continue;
                }
                font.draw(batch, lines[j], rx, ry);
                ry = ry - 18;
            }
        } else if (showZstats == STATS_ITEMS) {
            String[] lines = pages[3].split("\\|");
            font.draw(batch, "Items", rx, ry);
            ry = ry - 18;
            for (int j = 0; j < lines.length; j++) {
                if (lines[j] == null || lines[j].length() < 1) {
                    continue;
                }
                font.draw(batch, lines[j], rx, ry);
                ry = ry - 18;
            }
        } else if (showZstats == STATS_SPELLS) {
            String[] lines = pages[5].split("\\|");
            font.draw(batch, "Spell Mixtures", rx, ry);
            ry = ry - 18;
            for (int j = 0; j < lines.length; j++) {
                if (lines[j] == null || lines[j].length() < 1) {
                    continue;
                }
                font.draw(batch, lines[j], rx, ry);
                ry = ry - 18;
            }
        }

    }

}
