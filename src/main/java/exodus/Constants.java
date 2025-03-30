package exodus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import objects.Armor;
import objects.BaseMap;
import objects.Creature;
import objects.Drawable;
import objects.Weapon;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import exodus.Party.PartyMember;
import java.util.HashMap;
import java.util.Map;
import util.XORShiftRandom;

public interface Constants {

    public enum ScreenType {

        MAIN, COMBAT, DUNGEON, SHRINE, CODEX, RANDOMDNG, TMXDUNGEON;
    }

    public static int tilePixelWidth = 32;
    public static int tilePixelHeight = 32;
    public static final String PARTY_SAV_BASE_FILENAME = "party.sav";
    public static final int STATS_NONE = 0;
    public static final int STATS_PLAYER1 = 1;
    public static final int STATS_PLAYER2 = 2;
    public static final int STATS_PLAYER3 = 3;
    public static final int STATS_PLAYER4 = 4;
    public static int MOON_PHASES = 24;
    public static int MOON_SECONDS_PER_PHASE = 4;
    public static int MOON_CHAR = 20;
    public static final int MAX_CREATURES_ON_MAP = 10;
    public static final int MAX_WANDERING_CREATURES_IN_DUNGEON = 2;
    public static final int MAX_CREATURE_DISTANCE = 24;

    public enum Maps {

        SOSARIA(0, "Sosaria"),
        BRITISH(1, "Castle of Lord British"),
        EXODUS(2, "Castle of Death"),
        LCB(3, "Britain"),
        YEW(4, "Yew"),
        MOON(5, "Moon"),
        GREY(6, "Grey"),
        DEVIL(7, "Devil Guard"),
        MONTOR_E(8, "Montor East"),
        MONTOR_W(9, "Montor West"),
        DEATH(10, "Death Gulch"),
        DAWN(11, "Dawn"),
        FAWN(12, "Fawn"),
        AMBROSIA(13, "Ambrosia"),
        PERINIAN(14, "Perinian Depths"),
        DARDIN(15, "Dardin's Pit"),
        MINE(16, "Mines of Morinia"),
        FIRE(17, "Dungeon of Fire"),
        DOOM(18, "Dungeon of Doom"),
        SNAKE(19, "Dungeon of the Snake"),
        TIME(20, "Dungeon of Time"),
        BRICK_CON(33, ""),
        BRIDGE_CON(34, ""),
        BRUSH_CON(35, ""),
        DNG0_CON(37, ""),
        DNG1_CON(38, ""),
        DNG2_CON(39, ""),
        DNG3_CON(40, ""),
        DNG5_CON(42, ""),
        DNG6_CON(43, ""),
        DUNGEON_CON(44, ""),
        FOREST_CON(45, ""),
        GRASS_CON(46, ""),
        HILL_CON(47, ""),
        MARSH_CON(49, ""),
        SHIPSEA_CON(50, ""),
        SHIPSHIP_CON(51, ""),
        SHIPSHOR_CON(52, ""),
        SHORE_CON(53, ""),
        SHORSHIP_CON(54, ""),
        SHRINE_OF_WISDOM(55, "Shrine of Wisdom"),
        SHRINE_OF_DEXTERITY(56, "Shrine of Dexterity"),
        SHRINE_OF_INTELLIGENCE(57, "Shrine of Intelligence"),
        SHRINE_OF_STRENGTH(58, "Shrine of Sterngth"),;

        private int id;
        private String label;
        private BaseMap baseMap;

        private Maps(int id, String label) {
            this.id = id;
            this.label = label;
        }

        public int getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public static Maps get(int id) {
            for (Maps m : Maps.values()) {
                if (m.getId() == id) {
                    return m;
                }
            }
            return null;
        }

        public BaseMap getMap() {
            return baseMap;
        }

        public void setMap(BaseMap baseMap) {
            this.baseMap = baseMap;
        }

    }

    public enum ObjectMovementBehavior {

        FIXED,
        WANDER,
        FOLLOW_AVATAR,
        ATTACK_AVATAR;
    }

    public enum SlowedType {

        SLOWED_BY_NOTHING,
        SLOWED_BY_TILE,
        SLOWED_BY_WIND;
    };

    public enum TileSpeed {

        FAST,
        SLOW,
        VSLOW,
        VVSLOW;
    }

    public enum TileEffect {

        NONE,
        FIRE,
        POISON,
        POISONFIELD,
        ELECTRICITY,
        GREMLINS,
        LAVA;
    }

    public enum TileAttrib {

        unwalkable(0x000001),
        creatureunwalkable(0x000002),
        swimmable(0x000004),
        sailable(0x000008),
        unflyable(0x000010),
        rangeattackover(0x000020),
        chest(0x000080),
        dispelable(0x000100),
        door(0x000200),
        horse(0x000400),
        ship(0x000800),
        replacement(0x002000),
        snake(0x004000),
        livingthing(0x008000),
        lockeddoor(0x010000),
        secretdoor(0x040000);
        private int val;

        private TileAttrib(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }
    }

    public enum TileRule {

        none(0),
        water(TileAttrib.unwalkable.getVal() | TileAttrib.swimmable.getVal() | TileAttrib.sailable.getVal() | TileAttrib.rangeattackover.getVal()),
        shallows(TileAttrib.unwalkable.getVal() | TileAttrib.swimmable.getVal() | TileAttrib.rangeattackover.getVal()),
        swamp(0, TileSpeed.SLOW, TileEffect.POISON),
        grass(TileAttrib.replacement.getVal()),
        brush(0, TileSpeed.VSLOW, TileEffect.NONE),
        hills(0, TileSpeed.VVSLOW, TileEffect.NONE),
        mountains(TileAttrib.unwalkable.getVal() | TileAttrib.unflyable.getVal() | TileAttrib.creatureunwalkable.getVal()),
        lcb(TileAttrib.unwalkable.getVal() | TileAttrib.creatureunwalkable.getVal()),
        lcb_entrance(TileAttrib.creatureunwalkable.getVal()),
        ship(TileAttrib.ship.getVal() | TileAttrib.creatureunwalkable.getVal()),
        horse(TileAttrib.horse.getVal() | TileAttrib.creatureunwalkable.getVal()),
        floors(TileAttrib.replacement.getVal()),
        person(TileAttrib.livingthing.getVal() | TileAttrib.unwalkable.getVal()),
        solid(TileAttrib.unwalkable.getVal() | TileAttrib.rangeattackover.getVal()),
        walls(TileAttrib.unwalkable.getVal() | TileAttrib.unflyable.getVal()),
        locked_door(TileAttrib.unwalkable.getVal() | TileAttrib.lockeddoor.getVal() | TileAttrib.creatureunwalkable.getVal() | TileAttrib.unflyable.getVal()),
        door(TileAttrib.unwalkable.getVal() | TileAttrib.door.getVal() | TileAttrib.creatureunwalkable.getVal() | TileAttrib.unflyable.getVal()),
        secret_door(TileAttrib.secretdoor.getVal() | TileAttrib.creatureunwalkable.getVal() | TileAttrib.unflyable.getVal()),
        chest(TileAttrib.chest.getVal()),
        poison_field(TileAttrib.dispelable.getVal(), TileSpeed.VVSLOW, TileEffect.POISON),
        energy_field(TileAttrib.dispelable.getVal() | TileAttrib.unflyable.getVal() | TileAttrib.unwalkable.getVal() | TileAttrib.creatureunwalkable.getVal(), TileSpeed.VVSLOW, TileEffect.ELECTRICITY),
        fire_field(TileAttrib.dispelable.getVal(), TileSpeed.VVSLOW, TileEffect.FIRE),
        lava(TileAttrib.replacement.getVal(), TileSpeed.VVSLOW, TileEffect.LAVA),
        signs(TileAttrib.unwalkable.getVal() | TileAttrib.unflyable.getVal() | TileAttrib.rangeattackover.getVal()),
        spacers(TileAttrib.unwalkable.getVal() | TileAttrib.rangeattackover.getVal()),
        snake(TileAttrib.snake.getVal() | TileAttrib.unwalkable.getVal() | TileAttrib.swimmable.getVal() | TileAttrib.sailable.getVal()),
        monster(TileAttrib.livingthing.getVal() & TileAttrib.unwalkable.getVal());

        private int attribs = 0;
        private TileSpeed speed = TileSpeed.FAST;
        private TileEffect effect = TileEffect.NONE;

        private TileRule(int attribs, TileSpeed speed, TileEffect effect) {
            this.attribs = attribs;
            this.speed = speed;
            this.effect = effect;
        }

        private TileRule(int attribs) {
            this.attribs = attribs;
        }

        public boolean has(TileAttrib attrib) {
            return (attrib.getVal() & attribs) > 0;
        }

        public int getAttribs() {
            return attribs;
        }

        public TileSpeed getSpeed() {
            return speed;
        }

        public TileEffect getEffect() {
            return effect;
        }

    }

    public enum MapType {

        world(0x01),
        combat(0x02),
        city(0x04),
        dungeon(0x08),
        shrine(0x10);

        private int val;

        private MapType(int val) {
            this.val = val;
        }

        public int val() {
            return val;
        }

    }

    public enum MapBorderBehavior {

        wrap,
        exit,
        fixed;
    }

    enum Direction {

        WEST(1, 0x1),
        NORTH(2, 0x2),
        EAST(3, 0x4),
        SOUTH(4, 0x8);

        private int val;
        private int mask;

        private Direction(int v, int mask) {
            this.val = v;
            this.mask = mask;
        }

        public int getVal() {
            return val;
        }

        public int getMask() {
            return mask;
        }

        public static boolean isDirInMask(Direction dir, int mask) {
            int v = (mask & dir.mask);
            return (v > 0);
        }

        public static boolean isDirInMask(int dir, int mask) {
            int v = (mask & dir);
            return (v > 0);
        }

        public static int addToMask(Direction dir, int mask) {
            return (dir.mask | mask);
        }

        public static int removeFromMask(int mask, Direction... dirs) {
            for (Direction dir : dirs) {
                mask &= ~dir.getMask();
            }
            return mask;
        }

        public static Direction getRandomValidDirection(int mask) {
            int n = 0;
            Direction d[] = new Direction[4];
            for (Direction dir : values()) {
                if (isDirInMask(dir, mask)) {
                    d[n] = dir;
                    n++;
                }
            }
            if (n == 0) {
                return null;
            }
            int rand = new XORShiftRandom().nextInt(n);
            return d[rand];
        }

        public static Direction reverse(Direction dir) {
            switch (dir) {
                case WEST:
                    return EAST;
                case NORTH:
                    return SOUTH;
                case EAST:
                    return WEST;
                case SOUTH:
                    return NORTH;
            }
            return null;
        }

        public static Direction getByValue(int val) {
            Direction ret = null;
            for (Direction d : Direction.values()) {
                if (val == d.getVal()) {
                    ret = d;
                    break;
                }
            }
            return ret;
        }

        public static Direction getByMask(int mask) {
            Direction ret = null;
            for (Direction d : Direction.values()) {
                if (mask == d.mask) {
                    ret = d;
                    break;
                }
            }
            return ret;
        }

        public static int getBroadsidesDirectionMask(Direction dir) {
            int mask = 0;
            switch (dir) {
                case EAST:
                case WEST:
                    mask = (Direction.NORTH.mask | Direction.SOUTH.mask);
                    break;
                case NORTH:
                case SOUTH:
                    mask = (Direction.EAST.mask | Direction.WEST.mask);
                    break;
            }
            return mask;
        }

        public static Direction goBroadsides(int broadsidesMask) {
            Direction ret = null;
            for (Direction d : Direction.values()) {
                if ((broadsidesMask & d.mask) > 0) {
                    ret = d;
                    break;
                }
            }
            return ret;
        }

    };

    public enum Transport {

        FOOT,
        SHIP,
        HORSE;
    }

    public enum DungeonTile {

        NOTHING((byte) 0x00, "Nothing", "brick_floor", true),
        FLOOR((byte) 0x00, "Floor", "brick_floor", true),
        WATER((byte) 0x00, "Water", "water", true),
        CEILING((byte) 0x00, "Ceiling", "none", false),
        LADDER_UP((byte) 0x10, "Ladder Up", "up_ladder", true, Maps.DNG1_CON),
        LADDER_DOWN((byte) 0x20, "Ladder Down", "down_ladder", true, Maps.DNG2_CON),
        LADDER_UP_DOWN((byte) 0x30, "Ladder Up & Down", "down_ladder", true, Maps.DNG3_CON),
        CEILING_HOLE((byte) 0x00, "Ceiling Hole", "solid", false),
        FLOOR_HOLE((byte) 0x00, "Floor Hole", "solid", false),
        ORB((byte) 0x00, "Magic Orb", "magic_flash", true),
        LIGHT((byte) 0, "Light", "miss_flash", true),
        MOONGATE((byte) 0, "Moongate", "moongate", true),
        ALTAR((byte) 0, "Altar", "altar", true),
        CHEST((byte) 0x40, "Treasure Chest", "chest", true),
        MARK((byte) 0x05, "Mark", "whirlpool", true),
        MARK_SNAKE((byte) 0x00, "Mark of the Snake", "whirlpool", true),
        MARK_KINGS((byte) 0x00, "Mark of Kings", "whirlpool", true),
        MARK_FORCE((byte) 0x00, "Mark of Force", "whirlpool", true),
        MARK_FIRE((byte) 0x00, "Mark of Fire", "whirlpool", true),
        WIND_TRAP((byte) 0x03, "Winds/Darknes Trap", "magic_flash", true),
        GREMLINS((byte) 0x06, "Gremlins", "jester", true),
        PIT_TRAP((byte) 0x04, "Pit Trap", "hit_flash", true),
        FOUNTAIN_PLAIN((byte) 0x00, "Plain Fountain", "Z", true),
        FOUNTAIN_HEAL((byte) 0x02, "Healing Fountain", "H", true),
        FOUNTAIN_ACID((byte) 0x00, "Acid Fountain", "A", true),
        FOUNTAIN_CURE((byte) 0x00, "Cure Fountain", "C", true),
        FOUNTAIN_POISON((byte) 0x00, "Poison Fountain", "P", true),
        FIELD_POISON((byte) 0x00, "Poison Field", "poison_field", false),
        FIELD_ENERGY((byte) 0x00, "Energy Field", "energy_field", false),
        FIELD_FIRE((byte) 0x00, "Fire Field", "fire_field", false),
        DOOR((byte) 0xC0, "Door", "door", true, Maps.DNG5_CON),
        LOCKED_DOOR((byte) 0, "Locked Door", "locked_door", false, Maps.DNG5_CON),
        SECRET_DOOR((byte) 0xA0, "Secret Door", "secret_door", false, Maps.DNG6_CON),
        TIME_LORD((byte) 0x01, "Time Lord", "lord_british", false),
        MISTY_WRITINGS((byte) 0x08, "Misty Writings", "shrine", true),
        WALL((byte) 0x80, "Wall ", "brick_wall", false);

        private byte value;
        private String type;
        private String tileName;
        private Maps combatMap = Maps.DNG0_CON;
        private boolean creatureWalkable;

        private DungeonTile(byte value, String type, String tileName, boolean cw) {
            this.value = value;
            this.type = type;
            this.tileName = tileName;
            this.creatureWalkable = cw;
        }

        private DungeonTile(byte value, String type, String tileName, boolean cw, Maps combatMap) {
            this.value = value;
            this.type = type;
            this.tileName = tileName;
            this.combatMap = combatMap;
            this.creatureWalkable = cw;
        }

        public int getValue() {
            return value;
        }

        public boolean getCreatureWalkable() {
            return creatureWalkable;
        }

        public String getType() {
            return type;
        }

        public String getTileName() {
            return tileName;
        }

        public static DungeonTile getTileByValue(byte val) {
            DungeonTile ret = DungeonTile.NOTHING;
            for (DungeonTile d : DungeonTile.values()) {
                if (val == d.getValue()) {
                    ret = d;
                    break;
                }
            }
            return ret;
        }

        public static DungeonTile getTileByName(String name) {
            DungeonTile ret = null;
            for (DungeonTile d : DungeonTile.values()) {
                if (StringUtils.equals(name, d.getTileName())) {
                    ret = d;
                    break;
                }
            }
            return ret;
        }

        public Maps getCombatMap() {
            return combatMap;
        }

        public void setCombatMap(Maps combatMap) {
            this.combatMap = combatMap;
        }

    }

    public enum WeaponType {

        NONE,
        DAGGER,
        MACE,
        SLING,
        AXE,
        BOW,
        SWORD,
        SWORD_2H,
        AXE_P2,
        BOW_P2,
        SWORD_P2,
        GLOVES,
        AXE_P4,
        BOW_P4,
        SWORD_P4,
        EXOTIC;

        private Weapon weapon;

        public static WeaponType get(int v) {
            for (WeaponType x : values()) {
                if (x.ordinal() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
        }

        public Weapon getWeapon() {
            return weapon;
        }

        public void setWeapon(Weapon weapon) {
            this.weapon = weapon;
        }

    }

    public enum ArmorType {

        NONE,
        CLOTH,
        LEATHER,
        CHAIN,
        PLATE,
        CHAIN_P2,
        PLATE_P2,
        EXOTIC;

        private Armor armor;

        public static ArmorType get(int v) {
            for (ArmorType x : values()) {
                if (x.ordinal() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
        }

        public Armor getArmor() {
            return armor;
        }

        public void setArmor(Armor armor) {
            this.armor = armor;
        }
    }

    public enum Profession {

        BARBARIAN("fighter", 0x001),
        DRUID("shepherd", 0x002),
        ALCHEMIST("tinker", 0x004),
        RANGER("ranger", 0x008),
        FIGHTER("fighter", 0x010),
        WIZARD("mage", 0x020),
        THIEF("rogue", 0x0040),
        LARK("jester", 0x080),
        ILLUSIONIST("mage", 0x100),
        CLERIC("cleric", 0x200),
        PALADIN("paladin", 0x400);

        private final String tile;
        private final int val;

        private Profession(String tile, int val) {
            this.tile = tile;
            this.val = val;
        }

        public String getTile() {
            return tile;
        }

        public int val() {
            return this.val;
        }

    }

    public enum ClassType {

        HUMAN("H", 75, 75, 75, 75),
        ELF("E", 75, 99, 75, 50),
        DWARF("D", 99, 75, 50, 75),
        BOBIT("B", 75, 50, 75, 99),
        FUZZY("F", 25, 99, 99, 75);

        private final String id;
        private final int maxStr, maxDex, maxInt, maxWis;

        private ClassType(String id, int mxSt, int mxDx, int mxIn, int mxWi) {
            this.id = id;
            this.maxStr = mxSt;
            this.maxDex = mxDx;
            this.maxInt = mxIn;
            this.maxWis = mxWi;
        }

        public String getId() {
            return id;
        }

        public int getMaxStr() {
            return maxStr;
        }

        public int getMaxDex() {
            return maxDex;
        }

        public int getMaxInt() {
            return maxInt;
        }

        public int getMaxWis() {
            return maxWis;
        }

    }

    public enum SexType {

        MALE(0xB, "Male"),
        FEMALE(0xC, "Female");

        private int b;
        private String desc;

        private SexType(int value, String d) {
            b = value;
            desc = d;
        }

        public int getValue() {
            return b;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum StatusType {

        GOOD("G"),
        POISONED("P"),
        ASH("A"),
        DEAD("D");

        private String id;

        private StatusType(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

    }

    public enum Spell {

        REPOND("A", 0, "Destroy orcs, gremlins, trolls", Profession.ALCHEMIST.val() | Profession.DRUID.val() | Profession.LARK.val() | Profession.RANGER.val() | Profession.WIZARD.val(), MapType.combat.val()),
        MITTAR("B", 5, "Magic missile", Profession.ALCHEMIST.val() | Profession.DRUID.val() | Profession.LARK.val() | Profession.RANGER.val() | Profession.WIZARD.val(), MapType.combat.val()),
        LORUM("C", 10, "Minor light spell", Profession.ALCHEMIST.val() | Profession.DRUID.val() | Profession.LARK.val() | Profession.RANGER.val() | Profession.WIZARD.val(), MapType.dungeon.val()),
        DOR_ACRON("D", 15, "Down one dungeon level", Profession.ALCHEMIST.val() | Profession.DRUID.val() | Profession.LARK.val() | Profession.RANGER.val() | Profession.WIZARD.val(), MapType.dungeon.val()),
        SUR_ACRON("E", 20, "Up one dungeon level", Profession.ALCHEMIST.val() | Profession.DRUID.val() | Profession.LARK.val() | Profession.RANGER.val() | Profession.WIZARD.val(), MapType.dungeon.val()),
        FULGAR("F", 25, "Fireball", Profession.ALCHEMIST.val() | Profession.DRUID.val() | Profession.LARK.val() | Profession.RANGER.val() | Profession.WIZARD.val(), MapType.combat.val()),
        DAG_ACRON("G", 30, "Random teleport on surface", Profession.ALCHEMIST.val() | Profession.DRUID.val() | Profession.LARK.val() | Profession.RANGER.val() | Profession.WIZARD.val(), MapType.world.val()),
        MENTAR("H", 35, "Magic missile", Profession.ALCHEMIST.val() | Profession.DRUID.val() | Profession.LARK.val() | Profession.RANGER.val() | Profession.WIZARD.val(), MapType.combat.val()),
        PABULUM("I", 40, "Feeds the many", Profession.ALCHEMIST.val() | Profession.DRUID.val() | Profession.LARK.val() | Profession.WIZARD.val(), 0xff),
        FAL_DIVI("J", 45, "Cast a cleric spell", Profession.ALCHEMIST.val() | Profession.DRUID.val() | Profession.LARK.val() | Profession.WIZARD.val(), 0xff),
        NOXUM("K", 50, "Multi-fireball", Profession.WIZARD.val(), MapType.combat.val()),
        DECORP("L", 55, "Death bolt", Profession.WIZARD.val(), MapType.combat.val()),
        ALTAIR("M", 60, "Stop time", Profession.WIZARD.val(), 0xff),
        DAG_MENTAR("N", 65, "Major multi-fireball", Profession.WIZARD.val(), MapType.combat.val()),
        NECORP("O", 70, "Weakens enemies", Profession.WIZARD.val(), MapType.combat.val()),
        UNSPEAKABLE("P", 75, "Multi-death bolt", Profession.WIZARD.val(), MapType.combat.val()),
        PONTORI("A", 0, "Turn the undead", Profession.CLERIC.val() | Profession.ILLUSIONIST.val() | Profession.PALADIN.val() | Profession.RANGER.val() | Profession.DRUID.val(), MapType.combat.val()),
        APPAR_UNEM("B", 5, "Disarm chest safely", Profession.CLERIC.val() | Profession.ILLUSIONIST.val() | Profession.PALADIN.val() | Profession.RANGER.val() | Profession.DRUID.val(), MapType.city.val() | MapType.world.val() | MapType.dungeon.val()),
        SANCTU("C", 10, "Minor healing", Profession.CLERIC.val() | Profession.ILLUSIONIST.val() | Profession.PALADIN.val() | Profession.RANGER.val() | Profession.DRUID.val(), 0xff),
        LUMINAE("D", 15, "Minor light spell", Profession.CLERIC.val() | Profession.ILLUSIONIST.val() | Profession.PALADIN.val() | Profession.RANGER.val() | Profession.DRUID.val(), MapType.dungeon.val()),
        REC_SU("E", 20, "Up one dungeon level", Profession.CLERIC.val() | Profession.ILLUSIONIST.val() | Profession.PALADIN.val() | Profession.RANGER.val() | Profession.DRUID.val(), MapType.dungeon.val()),
        REC_DU("F", 25, "Down one dungeon level", Profession.CLERIC.val() | Profession.ILLUSIONIST.val() | Profession.PALADIN.val() | Profession.RANGER.val() | Profession.DRUID.val(), MapType.dungeon.val()),
        LIB_REC("G", 30, "Random teleport in dungeon", Profession.CLERIC.val() | Profession.ILLUSIONIST.val() | Profession.PALADIN.val() | Profession.RANGER.val() | Profession.DRUID.val(), MapType.dungeon.val()),
        ALCORT("H", 35, "Cure poison", Profession.CLERIC.val() | Profession.ILLUSIONIST.val() | Profession.PALADIN.val() | Profession.RANGER.val() | Profession.DRUID.val(), 0xff),
        SEQUITU("I", 40, "Exit from dungeon", Profession.CLERIC.val() | Profession.ILLUSIONIST.val() | Profession.PALADIN.val() | Profession.RANGER.val() | Profession.DRUID.val(), MapType.dungeon.val()),
        SOMINAE("J", 45, "Major light spell", Profession.CLERIC.val() | Profession.ILLUSIONIST.val() | Profession.PALADIN.val() | Profession.DRUID.val(), MapType.dungeon.val()),
        SANCTU_MANI("K", 50, "Major healing", Profession.CLERIC.val() | Profession.ILLUSIONIST.val() | Profession.PALADIN.val() | Profession.DRUID.val(), 0xff),
        VIEDA("L", 55, "Overhead view of surroundings", Profession.CLERIC.val(), MapType.world.val() | MapType.city.val()),
        EXCUUN("M", 60, "Death bolt", Profession.CLERIC.val(), MapType.combat.val()),
        SURMANDUM("N", 65, "Resurrection", Profession.CLERIC.val(), 0xff),
        ZXKUQYB("O", 70, "Multi-death bolt", Profession.CLERIC.val(), MapType.combat.val()),
        ANJU_SERMANI("P", 75, "Recall from ashes", Profession.CLERIC.val(), 0xff);

        private final String id;
        private final int cost;
        private final String desc;
        private final Sound sound;
        private final int castableMask;
        private final int locationMask;

        private Spell(String id, int cost, String desc, int castableMask, int locationMask) {
            this.id = id;
            this.cost = cost;
            this.desc = desc;
            this.castableMask = castableMask;
            this.locationMask = locationMask;
            this.sound = Sound.MAGIC;
        }

        private Spell(String id, int cost, String desc, int castableMask, int locationMask, Sound sound) {
            this.id = id;
            this.cost = cost;
            this.desc = desc;
            this.castableMask = castableMask;
            this.locationMask = locationMask;
            this.sound = sound;
        }

        public static Map<String, Spell> getCastables(PartyMember pm, MapType mt) {
            Map<String, Spell> ret = new HashMap<>();
            Profession prof = pm.getPlayer().profession;
            int count = Keys.A;
            for (Spell s : Spell.values()) {
                if (s.canCast(prof) && s.canCastLocation(mt) && s.getCost() <= pm.getPlayer().mana) {
                    ret.put(Keys.toString(count), s);
                    count++;
                }
            }
            return ret;
        }

        public String getId() {
            return id;
        }

        public int getCost() {
            return cost;
        }

        public String getDesc() {
            return desc;
        }

        public Sound getSound() {
            return sound;
        }

        public boolean canCast(Profession prof) {
            return (prof.val() & this.castableMask) > 0;
        }

        public boolean canCastLocation(MapType mt) {
            return (mt.val() & this.locationMask) > 0;
        }

    }

    public enum ItemMapLabels {

        EXOTIC_ARMOR("Exotic Armour", 0),
        EXOTIC_WEAPON("Exotic Weapon", 0),
        CARD_OF_DEATH("Card of Death", 0),
        CARD_OF_SOL("Card of Sol", 0),
        CARD_OF_MOONS("Card of Moons", 0),
        CARD_OF_LOVE("Card of Love", 0);

        private String desc;
        private int conditions;

        private ItemMapLabels(String desc, int cond) {
            this.desc = desc;
            this.conditions = cond;
        }

        public String getDesc() {
            return this.desc;
        }

        public int getConditions() {
            return this.conditions;
        }
    }

    public enum GuildItemType {

        gem,
        key,
        powder,
        torch;
    }

    public enum HealType {

        NONE,
        CURE,
        FULLHEAL,
        RESURRECT,
        HEAL,
        RECALL;
    }

    public enum InventoryType {

        WEAPON,
        ARMOR,
        FOOD,
        HEALER,
        GUILDITEM,
        TAVERNINFO,
        ORACLEINFO,
        HORSE;
    }

    public enum CombatAction {

        ATTACK,
        ADVANCE,
        RANGED,
        FLEE,
        TELEPORT;
    }

    public enum PartyEvent {

        ADVANCED_LEVEL,
        STARVING,
        POISON_DAMAGE,
        TRANSPORT_CHANGED,
        PARTY_DEATH,
        ACTIVE_PLAYER_CHANGED,
        PARTY_REVIVED,
        INVENTORY_ADDED,
    };

    public enum CreatureStatus {

        FINE,
        DEAD,
        FLEEING,
        CRITICAL,
        HEAVILYWOUNDED,
        LIGHTLYWOUNDED,
        BARELYWOUNDED;
    }

    public enum AuraType {

        NONE,
        HORN,
        JINX,
        NEGATE,
        PROTECTION,
        QUICKNESS;
    }

    public enum CreatureType {

        horse(0),
        horse2(1),
        mage(2),
        bard(3),
        fighter(4),
        druid(5),
        tinker(6),
        paladin(7),
        ranger(8),
        shepherd(9),
        guard(10),
        merchant(11),
        bard_singing(12),
        jester(13),
        beggar(14),
        child(15),
        bull(16),
        lord_british(17),
        pirate_ship(18),
        nixie(19),
        giant_squid(20),
        sea_serpent(21),
        sea_horse(22),
        twister(23),
        whirlpool(24),
        rat(25,         5,      1),
        bat(26,         5,      1),
        spider(27,      5,      1),
        ghost(28,       5,      2),
        slime(29,       5,      1),
        troll(30,       5,      2),
        gremlin(31,     5,      3),
        mimic(32,       5,      4),
        insect_swarm(34),
        gazer(35,       5,      4),
        phantom(36,     5,      3),
        orc(37,         5,      1),
        skeleton(38,    5,      1),
        rogue(39),
        brigand(40),
        ettin(41,       5,      3),
        headless(42,    5,      2),
        cyclops(43,     5,      4),
        wisp(44,        5,      4),
        evil_mage(45,   5,      4),
        liche(46,       5,      5),
        lava_lizard(47),
        zorn(48,        5,      7),
        daemon(49,      5,      5),
        hydra(50,       5,      6),
        dragon(51,      5,      7),
        balron(52,      5,      8),
        grass(60),
        chest(61),
        brick_floor(62),
        cleric(63);

        private final int intValue;
        private final int dungeonSpawnWeight;
        private int dungeonSpawnLevel;
        private Creature creature;

        private CreatureType(int value) {
            intValue = value;
            dungeonSpawnWeight = 0;
        }

        private CreatureType(int value, int dsw, int dsl) {
            intValue = value;
            dungeonSpawnWeight = dsw;
            dungeonSpawnLevel = dsl;
        }

        public int getValue() {
            return intValue;
        }

        public int getSpawnWeight() {
            return dungeonSpawnWeight;
        }

        public int getSpawnLevel() {
            return dungeonSpawnLevel;
        }

        public static CreatureType get(int v) {
            for (CreatureType x : values()) {
                if (x.getValue() == v) {
                    return x;
                }
            }
            return null;
        }

        public static CreatureType get(String v) {
            for (CreatureType x : values()) {
                if (x.toString().equals(v)) {
                    return x;
                }
            }
            return null;
        }

        public Creature getCreature() {
            return creature;
        }

        public void setCreature(Creature creature) {
            this.creature = creature;
        }
    }

    public enum AttackResult {

        NONE,
        HIT,
        MISS;
    }

    public class AttackVector {

        public int x;
        public int y;
        public int distance;

        public AttackResult result;
        public String leaveTileName;

        public Creature impactedCreature;
        public Drawable impactedDrawable;

        public AttackVector(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public class AddActorAction implements Runnable {

        private final Actor actor;
        private final Stage stage;

        public AddActorAction(Stage stage, Actor actor) {
            this.actor = actor;
            this.stage = stage;
        }

        @Override
        public void run() {
            stage.addActor(actor);
        }
    }

    public class PlaySoundAction implements Runnable {

        private Sound s;

        public PlaySoundAction(Sound s) {
            this.s = s;
        }

        @Override
        public void run() {
            Sounds.play(s);
        }
    }

    public class ClasspathResolver implements FileHandleResolver {

        @Override
        public FileHandle resolve(String fileName) {
            return Gdx.files.classpath(fileName);
        }

    }

}
