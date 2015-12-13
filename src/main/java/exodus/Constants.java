package exodus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import objects.Armor;
import objects.BaseMap;
import objects.Creature;
import objects.Drawable;
import objects.Weapon;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import util.XORShiftRandom;

public interface Constants {

    public enum ScreenType {

        MAIN, COMBAT, DUNGEON, SHRINE, CODEX, RANDOMDNG, TMXDUNGEON;
    }

    public static int tilePixelWidth = 32;
    public static int tilePixelHeight = 32;

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
        SHRINE_OF_STRENGTH(58, "Shrine of Sterngth"),
        ;
        
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
        SLEEP,
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
        canlandballoon(0x000040),
        chest(0x000080),
        dispelable(0x000100),
        door(0x000200),
        horse(0x000400),
        ship(0x000800),
        balloon(0x001000),
        replacement(0x002000),
        onWaterOnlyReplacement(0x004000),
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
        water(TileAttrib.unwalkable.getVal() | TileAttrib.swimmable.getVal() | TileAttrib.sailable.getVal() | TileAttrib.onWaterOnlyReplacement.getVal() | TileAttrib.rangeattackover.getVal()),
        shallows(TileAttrib.unwalkable.getVal() | TileAttrib.swimmable.getVal() | TileAttrib.onWaterOnlyReplacement.getVal() | TileAttrib.rangeattackover.getVal()),
        swamp(0, TileSpeed.SLOW, TileEffect.POISON),
        grass(TileAttrib.canlandballoon.getVal() | TileAttrib.replacement.getVal()),
        brush(0, TileSpeed.VSLOW, TileEffect.NONE),
        hills(0, TileSpeed.VVSLOW, TileEffect.NONE),
        mountains(TileAttrib.unwalkable.getVal() | TileAttrib.unflyable.getVal() | TileAttrib.creatureunwalkable.getVal()),
        lcb(TileAttrib.unwalkable.getVal() | TileAttrib.creatureunwalkable.getVal()),
        lcb_entrance(TileAttrib.creatureunwalkable.getVal()),
        ship(TileAttrib.ship.getVal() | TileAttrib.creatureunwalkable.getVal()),
        horse(TileAttrib.horse.getVal() | TileAttrib.creatureunwalkable.getVal()),
        floors(TileAttrib.replacement.getVal()),
        balloon(TileAttrib.balloon.getVal() | TileAttrib.creatureunwalkable.getVal()),
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
        sleep_field(TileAttrib.dispelable.getVal(), TileSpeed.VVSLOW, TileEffect.SLEEP),
        lava(TileAttrib.replacement.getVal(), TileSpeed.VVSLOW, TileEffect.LAVA),
        signs(TileAttrib.unwalkable.getVal() | TileAttrib.unflyable.getVal() | TileAttrib.rangeattackover.getVal()),
        spacers(TileAttrib.unwalkable.getVal() | TileAttrib.rangeattackover.getVal()),
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

        world(0x1),
        combat(0x2),
        city(0x4),
        dungeon(0x8),
        shrine(0x10);

        private int val;

        private MapType(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }

    }

    public enum MapBorderBehavior {

        wrap,
        exit,
        fixed;
    }

    public enum HeadingDirection {

        NORTH(0),
        NORTH_EAST(2),
        EAST(3),
        SOUTH_EAST(4),
        SOUTH(5),
        SOUTH_WEST(6),
        WEST(7),
        NORTH_WEST(8);
        private int heading;

        private HeadingDirection(int heading) {
            this.heading = heading;
        }

        public int getHeading() {
            return heading;
        }

        public static HeadingDirection getByValue(int val) {
            HeadingDirection ret = HeadingDirection.NORTH;
            for (HeadingDirection d : HeadingDirection.values()) {
                if (val == d.getHeading()) {
                    ret = d;
                    break;
                }
            }
            return ret;
        }

        /**
         * Use Y Down coordinate system with atan2, so negated the values to the
         * function.
         *
         * @param dx delta of the x coords
         * @param dy delta of the y coords
         */
        public static HeadingDirection getDirection(float dx, float dy) {
            double theta = MathUtils.atan2(-dy, -dx);
            double ang = theta * MathUtils.radDeg;
            if (ang < 0) {
                ang = 360 + ang;
            }
            ang = (ang + 90 + 45 + 22.5f) % 360;
            ang /= 45f;
            return HeadingDirection.getByValue((int) ang);
        }
    }

    public static int MOON_PHASES = 24;
    public static int MOON_SECONDS_PER_PHASE = 4;
    public static int MOON_CHAR = 20;

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

    public enum PortalTriggerAction {

        NONE(0x0),
        ENTER(0x1),
        KLIMB(0x2),
        DESCEND(0x4),
        EXIT_NORTH(0x8),
        EXIT_EAST(0x10),
        EXIT_SOUTH(0x20),
        EXIT_WEST(0x40);
        private int intValue;

        private PortalTriggerAction(int i) {
            this.intValue = i;
        }

        public int getIntValue() {
            return intValue;
        }
    }

    public enum TransportContext {

        FOOT(0x1),
        HORSE(0x2),
        SHIP(0x4),
        BALLOON(0x8),
        FOOT_OR_HORSE(0x1 | 0x2),
        ANY(0xffff);

        private int intValue;

        private TransportContext(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }
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
        WIND_TRAP((byte) 0x03, "Winds/Darknes Trap", "hit_flash", true),
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
        FIELD_SLEEP((byte) 0x00, "Sleep Field", "sleep_field", false),
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

    public static final String PARTY_SAV_BASE_FILENAME = "party.sav";

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

        BARBARIAN("fighter"),
        DRUID("shepherd"),
        ALCHEMIST("tinker"),
        RANGER("ranger"),
        FIGHTER("fighter"),
        WIZARD("mage"),
        THIEF("rogue"),
        LARK("jester"),
        ILLUSIONIST("mage"),
        CLERIC("cleric"),
        PALADIN("paladin");

        private final String tile;

        private Profession(String tile) {
            this.tile = tile;
        }

        public static Profession get(int v) {
            for (Profession x : values()) {
                if (x.ordinal() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
        }

        public String getTile() {
            return tile;
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

        public static ClassType get(int v) {
            for (ClassType x : values()) {
                if (x.ordinal() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
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

    public static final int STATS_NONE = 0;
    public static final int STATS_PLAYER1 = 1;
    public static final int STATS_PLAYER2 = 2;
    public static final int STATS_PLAYER3 = 3;
    public static final int STATS_PLAYER4 = 4;
    public static final int STATS_SPELLS = 5;

    //for touching orbs
    public static final int STATSBONUS_INT = 0x1;
    public static final int STATSBONUS_DEX = 0x2;
    public static final int STATSBONUS_STR = 0x4;

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

        public static SexType get(int v) {
            for (SexType x : values()) {
                if (x.ordinal() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
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

        public static StatusType get(int v) {
            for (StatusType x : values()) {
                if (x.ordinal() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
        }
    }

    public enum Spell {

        AWAKEN("Awaken", 0, 5, Sound.HEALING);

        public static Spell get(int i) {
            for (Spell x : values()) {
                if (x.ordinal() == i) {
                    return x;
                }
            }
            return null;
        }

        String desc;
        int mask;
        int mp;
        Sound sound = Sound.MAGIC;

        private Spell(String desc, int mask, int mp) {
            this.desc = desc;
            this.mask = mask;
            this.mp = mp;
        }

        private Spell(String desc, int mask, int mp, Sound snd) {
            this.desc = desc;
            this.mask = mask;
            this.mp = mp;
            this.sound = snd;
        }

        public String getDesc() {
            return desc;
        }

        public int getMask() {
            return mask;
        }

        public int getMp() {
            return mp;
        }

        public Sound getSound() {
            return sound;
        }

        @Override
        public String toString() {
            return this.desc;
        }


    }

    public enum Item {

        SKULL("Skull of Mondain", true, 0x000001),
        SKULL_DESTROYED("Skull Destroyed", false, 0x000002),
        CANDLE("Candle of Love", true, 0x000004),
        BOOK("Book of Truth", true, 0x000008),
        BELL("Bell of Courage", true, 0x000010),
        KEY_C("Key of Courage", true, 0x000020),
        KEY_L("Key of Love", true, 0x000040),
        KEY_T("Key of Truth", true, 0x000080),
        HORN("Silver Horn", true, 0x000100),
        WHEEL("Wheel of HMS Cape", true, 0x000200),
        CANDLE_USED("Candle Used", false, 0x000400),
        BOOK_USED("Book Used", false, 0x000800),
        BELL_USED("Bell Used", false, 0x001000),
        MASK_MINAX("Mask of Minax", true, 0x002000),
        RAGE_GOD("Rage of God", true, 0x004000),
        IRON_ORE("Iron Ore", true, 0x008000),
        RUNE_MOLD("Rune Mold", true, 0x010000),
        IRON_RUNE("Iron Rune", true, 0x020000),
        SONG_HUM("Song of Humility", true, 0x040000),
        PARCH("Magic Parchment", true, 0x080000),
        GREED_RUNE("Rune of Greed", true, 0x100000);

        private boolean visible;
        private String desc;
        private int loc;

        private Item(String desc, boolean v, int loc) {
            this.desc = desc;
            this.visible = v;
            this.loc = loc;
        }

        public static Item get(int v) {
            for (Item x : values()) {
                if (x.ordinal() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
        }

        public boolean isVisible() {
            return this.visible;
        }

        public String getDesc() {
            return this.desc;
        }

        public int getLoc() {
            return loc;
        }

    }

    public static final int SC_NONE = 0x00;
    public static final int SC_NEWMOONS = 0x01;
    public static final int SC_FULLAVATAR = 0x02;
    public static final int SC_REAGENTDELAY = 0x04;

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

    public enum Stone {

        BLUE(0x01),
        YELLOW(0x02),
        RED(0x04),
        GREEN(0x08),
        ORANGE(0x10),
        PURPLE(0x20),
        WHITE(0x40),
        BLACK(0x80);
        private int loc;

        private Stone(int loc) {
            this.loc = loc;
        }

        public static Stone get(int v) {
            for (Stone x : values()) {
                if (x.ordinal() == (v & 0xff)) {
                    return x;
                }
            }
            return null;
        }

        public int getLoc() {
            return loc;
        }
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
        CAST_SLEEP,
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
        villager(11),
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
        rat(25, 5, 0),
        bat(26, 5, 0),
        spider(27, 5, 0),
        ghost(28, 5, 1),
        slime(29, 5, 0),
        troll(30, 5, 2),
        gremlin(31, 5, 3),
        mimic(32, 5, 5),
        reaper(33, 5, 7),
        insect_swarm(34),
        gazer(35, 3, 5),
        phantom(36, 3, 4),
        orc(37, 5, 0),
        skeleton(38, 5, 0),
        rogue(39),
        python(40),
        ettin(41, 5, 3),
        headless(42, 5, 3),
        cyclops(43, 5, 4),
        wisp(44, 5, 5),
        evil_mage(45, 5, 5),
        liche(46, 8, 7),
        lava_lizard(47),
        zorn(48, 5, 8),
        daemon(49, 3, 4),
        hydra(50, 5, 5),
        dragon(51, 10, 7),
        balron(52, 10, 8),
        grass(60),
        brick_floor(61),
        chest(62),
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

    public static final int MAX_CREATURES_ON_MAP = 10;
    public static final int MAX_WANDERING_CREATURES_IN_DUNGEON = 2;
    public static final int MAX_CREATURE_DISTANCE = 24;

    public static String[] deathMsgs = {
        "All is Dark...",
        "But wait...",
        "Where am I?...",
        "Am I dead?...",
        "Afterlife?...",
        "You hear:  %s",
        "I feel motion...",
        "Lord British says: I have pulled thy spirit and some possessions from the void.  Be more careful in the future!"
    };

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

        private Actor actor;
        private Stage stage;

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
