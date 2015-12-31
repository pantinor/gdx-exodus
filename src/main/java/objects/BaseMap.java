package objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import exodus.Constants;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import exodus.BaseScreen;
import exodus.Context;
import exodus.Exodus;
import exodus.GameScreen;
import exodus.Party;
import exodus.Party.PartyMember;
import exodus.Sound;
import exodus.Sounds;
import java.util.Iterator;
import javax.xml.bind.annotation.XmlTransient;
import util.PartyDeathException;
import util.Utils;

@XmlRootElement(name = "map")
public class BaseMap implements Constants {

    private boolean initialized = false;

    private int id;
    private String fname;
    private MapType type;
    private int width;
    private int height;
    private int levels;
    private String music;
    private MapBorderBehavior borderbehavior;

    private List<Portal> portals;
    private List<Label> labels;
    private Dungeon dungeon;

    private final List<Creature> creatures = new ArrayList<>();
    private List<PartyMember> combatPlayers;

    private List<Moongate> moongates;
    private List<Person> people;
    private final List<Drawable> objects = new ArrayList<>();

    private Tile[] tiles;
    private float[][] shadownMap;

    //used to keep the pace of wandering to every 2 moves instead of every move, 
    //otherwise cannot catch up and talk to the character
    private long wanderFlag = 0;

    private final List<DoorStatus> doors = new ArrayList<>();

    public Moongate getMoongate(int phase) {
        if (moongates == null) {
            return null;
        }
        for (Moongate m : moongates) {
            if (m.getPhase() == phase) {
                return m;
            }
        }
        return null;
    }

    public Moongate getMoongate(String name) {
        if (moongates == null) {
            return null;
        }
        for (Moongate m : moongates) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    public Portal getPortal(int id) {
        if (portals == null) {
            return null;
        }
        for (Portal p : portals) {
            if (p.getDestmapid() == id) {
                return p;
            }
        }
        return null;
    }

    public Portal getPortal(float x, float y, float z) {
        if (portals == null) {
            return null;
        }
        for (Portal p : portals) {
            if (p.getX() == x && p.getY() == y && p.getZ() == z) {
                return p;
            }
        }
        return null;
    }

    public List<Portal> getPortals(int x, int y, int z) {
        List<Portal> ps = new ArrayList<>();
        if (portals == null) {
            return ps;
        }
        for (Portal p : portals) {
            if (p.getX() == x && p.getY() == y && p.getZ() == z) {
                ps.add(p);
            }
        }
        return ps;
    }

    @XmlAttribute
    public int getId() {
        return id;
    }

    @XmlAttribute
    public String getFname() {
        return fname;
    }

    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(MapTypeAdapter.class)
    public MapType getType() {
        return type;
    }

    @XmlAttribute
    public int getWidth() {
        return width;
    }

    @XmlAttribute
    public int getHeight() {
        return height;
    }

    @XmlAttribute
    public int getLevels() {
        return levels;
    }

    @XmlAttribute
    public String getMusic() {
        return music;
    }

    @XmlAttribute(name = "borderbehavior")
    @XmlJavaTypeAdapter(BorderTypeAdapter.class)
    public MapBorderBehavior getBorderbehavior() {
        return borderbehavior;
    }

    @XmlElement(name = "portal")
    public List<Portal> getPortals() {
        return portals;
    }

    @XmlElement(name = "label")
    public List<Label> getLabels() {
        return labels;
    }

    @XmlElement(name = "moongate")
    public List<Moongate> getMoongates() {
        return moongates;
    }

    @XmlElement
    public Dungeon getDungeon() {
        return dungeon;
    }

    @XmlTransient
    public List<Person> getPeople() {
        return people;
    }

    public Person resetTalkingFlags() {
        for (Person p : people) {
            if (p == null) {
                continue;
            }
            p.setTalking(false);
        }
        return null;
    }

    public void setPeople(List<Person> people) {
        this.people = people;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public void setType(MapType type) {
        this.type = type;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setLevels(int levels) {
        this.levels = levels;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public void setBorderbehavior(MapBorderBehavior borderbehavior) {
        this.borderbehavior = borderbehavior;
    }

    public void setPortals(List<Portal> portals) {
        this.portals = portals;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public List<Creature> getCreatures() {
        return creatures;
    }

    public void addCreature(Creature cr) {
        if (cr == null) {
            return;
        }
        creatures.add(cr);
    }

    public void removeCreature(Creature cr) {
        if (cr == null) {
            return;
        }
        creatures.remove(cr);
    }

    public void clearCreatures() {
        creatures.clear();
    }

    public Creature getCreatureAt(int x, int y) {
        for (Creature cre : creatures) {
            if (cre.currentX == x && cre.currentY == y) {
                return cre;
            }
        }
        return null;
    }

    public Drawable getObjectAt(int x, int y) {
        for (Drawable obj : objects) {
            if (obj.getCx() == x && obj.getCy() == y) {
                return obj;
            }
        }
        return null;
    }

    public void setMoongates(List<Moongate> moongate) {
        this.moongates = moongate;
    }

    public void setDungeon(Dungeon dungeon) {
        this.dungeon = dungeon;
    }

    @Override
    public String toString() {
        return String.format("BaseMap [id=%s, fname=%s, portals=%s]", id, fname, portals);
    }

    public Tile[] getTiles() {
        return tiles;
    }

    public void setTiles(Tile[] tiles) {
        this.tiles = tiles;
    }

    public void setTile(Tile tile, int x, int y) {
        if (x < 0 || y < 0) {
            return;
        }
        if (x + (y * width) >= tiles.length) {
            return;
        }
        tiles[x + (y * width)] = tile;
    }

    public synchronized Tile getTile(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return null;
        }
        if (x + (y * width) >= tiles.length) {
            return null;
        }
        return tiles[x + (y * width)];
    }

    public synchronized Tile getTile(Vector3 v) {
        return getTile((int) v.x, (int) v.y);
    }

    public float[][] getShadownMap() {
        return shadownMap;
    }

    public void setShadownMap(float[][] shadownMap) {
        this.shadownMap = shadownMap;
    }

    public Person getPersonAt(int x, int y) {
        for (Person p : people) {
            if (p == null) {
                continue;
            }
            if (p.getX() == x && p.getY() == y) {
                return p;
            }
        }
        return null;
    }

    public void initObjects(GameScreen screen, TextureAtlas atlas1, TextureAtlas atlas2) {

        if (initialized) {

            if (people != null) {
                for (Person p : people) {
                    if (p == null) {
                        continue;
                    }
                    p.setRemovedFromMap(false);
                }
            }

            return;
        }

        if (people != null) {

            for (Person p : people) {
                if (p == null) {
                    continue;
                }
                String tname = p.getTile().getName();

                Array<AtlasRegion> arr = atlas1.findRegions(tname);
                if (arr == null || arr.size == 0) {
                    arr = atlas2.findRegions(tname);
                }

                if (arr.size == 0) {
                    System.err.printf("%s - tname is empty %s", p, tname);
                }

                p.setTextureRegion(arr.first());

                if (arr.size > 1) {
                    //random rate between 1 and 4
                    int frameRate = Utils.getRandomBetween(1, 4);
                    p.setAnim(new Animation(frameRate, arr));
                }

                Vector3 pixelPos = screen.getMapPixelCoords(p.getStart_x(), p.getStart_y());
                p.setCurrentPos(pixelPos);
                p.setX(p.getStart_x());
                p.setY(p.getStart_y());

                CreatureType ct = CreatureType.get(tname);
                if (ct != null) {
                    p.setEmulatingCreature(ct.getCreature());
                } else {
                    System.err.printf("%s - ct is null %s\n", p, tname);
                }

            }

        }

        //set doors
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                Tile t = getTile(x, y);
                if (t.getName().equals("door")) {
                    doors.add(new DoorStatus(x, y, false, 0));
                } else if (t.getName().equals("locked_door")) {
                    doors.add(new DoorStatus(x, y, true, 0));
                }
            }
        }

        initialized = true;
    }

    public void moveObjects(GameScreen screen, int avatarX, int avatarY) throws PartyDeathException {

        if (people != null) {

            wanderFlag++;

            for (Person p : people) {
                if (p == null || p.isRemovedFromMap()) {
                    continue;
                }

                Vector3 pos = null;
                Vector3 pixelPos = null;
                Direction dir = null;

                switch (p.getMovement()) {
                    case ATTACK_AVATAR: {
                        int dist = Utils.movementDistance(borderbehavior, width, height, p.getX(), p.getY(), avatarX, avatarY);
                        if (dist <= 1) {
                            Maps cm = screen.context.getCombatMap(p.getEmulatingCreature(), this, p.getX(), p.getY(), avatarX, avatarY);
                            Creature attacker = Exodus.creatures.getInstance(p.getEmulatingCreature().getTile(), Exodus.standardAtlas);
                            attacker.currentX = p.getX();
                            attacker.currentY = p.getY();
                            attacker.currentPos = screen.getMapPixelCoords(p.getX(), p.getY());
                            screen.attackAt(cm, attacker);
                            p.setRemovedFromMap(true);
                            continue;
                        }
                        int mask = getValidMovesMask(screen.context, p.getX(), p.getY(), p.getEmulatingCreature(), avatarX, avatarY);
                        dir = Utils.getPath(borderbehavior, width, height, avatarX, avatarY, mask, true, p.getX(), p.getY());
                    }
                    break;
                    case FOLLOW_AVATAR: {
                        int mask = getValidMovesMask(screen.context, p.getX(), p.getY(), p.getEmulatingCreature(), avatarX, avatarY);
                        dir = Utils.getPath(borderbehavior, width, height, avatarX, avatarY, mask, true, p.getX(), p.getY());
                    }
                    break;
                    case FIXED:
                        break;
                    case WANDER: {
                        if (wanderFlag % 2 == 0) {
                            continue;
                        }
                        if (p.isTalking()) {
                            continue;
                        }
                        dir = Direction.getRandomValidDirection(getValidMovesMask(screen.context, p.getX(), p.getY(), p.getEmulatingCreature(), avatarX, avatarY));
                    }
                    break;
                    default:
                        break;

                }

                if (dir == null) {
                    continue;
                }
                if (dir == Direction.NORTH) {
                    pos = new Vector3(p.getX(), p.getY() - 1, 0);
                }
                if (dir == Direction.SOUTH) {
                    pos = new Vector3(p.getX(), p.getY() + 1, 0);
                }
                if (dir == Direction.EAST) {
                    pos = new Vector3(p.getX() + 1, p.getY(), 0);
                }
                if (dir == Direction.WEST) {
                    pos = new Vector3(p.getX() - 1, p.getY(), 0);
                }
                pixelPos = screen.getMapPixelCoords((int) pos.x, (int) pos.y);
                p.setCurrentPos(pixelPos);
                p.setX((int) pos.x);
                p.setY((int) pos.y);

            }

        }

        Iterator<Creature> i = creatures.iterator();
        while (i.hasNext()) {

            Creature cr = i.next();

            int dist = Utils.movementDistance(borderbehavior, width, height, cr.currentX, cr.currentY, avatarX, avatarY);
            if (dist > MAX_CREATURE_DISTANCE && cr.getTile() != CreatureType.whirlpool) {
                i.remove();
                continue;
            }

            if (cr.getTile() == CreatureType.pirate_ship) {
                int relDirMask = Utils.getRelativeDirection(borderbehavior, width, height, avatarX, avatarY, cr.currentX, cr.currentY);
                if (avatarX == cr.currentX) {
                    relDirMask = Direction.removeFromMask(relDirMask, Direction.EAST, Direction.WEST);
                } else if (avatarY == cr.currentY) {
                    relDirMask = Direction.removeFromMask(relDirMask, Direction.NORTH, Direction.SOUTH);
                } else {
                    relDirMask = 0;
                }
                int broadsidesDirs = Direction.getBroadsidesDirectionMask(cr.sailDir);
                if (relDirMask > 0 && (dist == 3 || dist == 2) && Direction.isDirInMask(relDirMask, broadsidesDirs)) {
                    Direction fireDir = Direction.getByMask(relDirMask);
                    AttackVector av = Utils.enemyfireCannon(screen.context, this.objects, this, fireDir, cr.currentX, cr.currentY, avatarX, avatarY);
                    Utils.animateCannonFire(screen, screen.projectilesStage, this, av, cr.currentX, cr.currentY, false);
                    continue;
                } else if (relDirMask > 0 && (dist == 3 || dist == 2) && !Direction.isDirInMask(relDirMask, broadsidesDirs) && Utils.rand.nextInt(2) == 0) {
                    cr.sailDir = Direction.goBroadsides(broadsidesDirs);
                    continue;
                } else if (dist <= 1) {
                    if (Direction.isDirInMask(relDirMask, broadsidesDirs)) {
                        Maps cm = screen.context.getCombatMap(cr, this, cr.currentX, cr.currentY, avatarX, avatarY);
                        screen.attackAt(cm, cr);
                        break;
                    } else {
                        cr.sailDir = Direction.goBroadsides(broadsidesDirs);
                        continue;
                    }
                }

            } else if (dist <= 1) {

                if (cr.getWontattack()) {
                    if (cr.getTile() == CreatureType.whirlpool) {
                        Sounds.play(Sound.WAVE);
                        Exodus.hud.add("A huge swirling Whirlpool engulfs you and your ship dragging both to a watery grave!");
                        Exodus.hud.add("As the water enters your lungs you pass into Darkness!");
                        Exodus.hud.add("You awaken on the shores of a forgotten Land.");
                        Exodus.hud.add("Your ship and crew lost to the sea!");
                        screen.context.setTransport(Transport.FOOT);
                        GameScreen.mainAvatar = GameScreen.avatarAnim;
                        screen.loadNextMap(Maps.AMBROSIA, 32, 54);
                        break;
                    } else if (cr.getTile() == CreatureType.twister) {
                        if (screen.context.getTransport() == Transport.SHIP) {
                            screen.context.damageShip(10, 30);
                        }
                        continue;
                    }
                } else {
                    Maps cm = screen.context.getCombatMap(cr, this, cr.currentX, cr.currentY, avatarX, avatarY);
                    screen.attackAt(cm, cr);
                    break;
                }

            }

            int mask = getValidMovesMask(screen.context, cr.currentX, cr.currentY, cr, avatarX, avatarY);
            Direction dir = null;
            if (cr.getWontattack()) {
                dir = Direction.getRandomValidDirection(mask);
            } else {
                dir = Utils.getPath(borderbehavior, width, height, avatarX, avatarY, mask, true, cr.currentX, cr.currentY);
            }
            if (dir == null) {
                continue;
            }

            if (cr.getTile() == CreatureType.pirate_ship) {
                if (cr.sailDir != dir) {
                    cr.sailDir = dir;
                    continue;
                }
            }

            if (borderbehavior == MapBorderBehavior.wrap) {
                if (dir == Direction.NORTH) {
                    cr.currentY = cr.currentY - 1 < 0 ? height - 1 : cr.currentY - 1;
                }
                if (dir == Direction.SOUTH) {
                    cr.currentY = cr.currentY + 1 >= height ? 0 : cr.currentY + 1;
                }
                if (dir == Direction.EAST) {
                    cr.currentX = cr.currentX + 1 >= width ? 0 : cr.currentX + 1;
                }
                if (dir == Direction.WEST) {
                    cr.currentX = cr.currentX - 1 < 0 ? width - 1 : cr.currentX - 1;
                }
            } else {
                if (dir == Direction.NORTH) {
                    cr.currentY = cr.currentY - 1 < 0 ? cr.currentY : cr.currentY - 1;
                }
                if (dir == Direction.SOUTH) {
                    cr.currentY = cr.currentY + 1 >= height ? cr.currentY : cr.currentY + 1;
                }
                if (dir == Direction.EAST) {
                    cr.currentX = cr.currentX + 1 >= width ? cr.currentX : cr.currentX + 1;
                }
                if (dir == Direction.WEST) {
                    cr.currentX = cr.currentX - 1 < 0 ? cr.currentX : cr.currentX - 1;
                }
            }

            cr.currentPos = screen.getMapPixelCoords(cr.currentX, cr.currentY);

        }

    }

    public boolean isTileBlockedForRangedAttack(int x, int y, boolean checkForCreatures) {
        Tile tile = getTile(x, y);
        TileRule rule = tile.getRule();
        boolean blocked = false;
        if (rule != null) {
            //projectiles cannot go thru walls, but can over water or if they can be attacked over, like certain solids
            blocked = rule.has(TileAttrib.unwalkable) && !rule.has(TileAttrib.rangeattackover);
        }
        if (checkForCreatures) {
            for (Creature cre : creatures) {
                if (cre.currentX == x && cre.currentY == y) {
                    blocked = true;
                    break;
                }
            }
        }
        return blocked;
    }

    public int getValidMovesMask(Context context, int x, int y) {
        return getValidMovesMask(context, x, y, null, 0, 0);
    }

    public int getValidMovesMask(Context context, int x, int y, Creature cr, int avatarX, int avatarY) {

        int mask = 0;

        if (this.getBorderbehavior() == MapBorderBehavior.wrap) {

            Tile north = getTile(x, y - 1 < 0 ? height - 1 : y - 1);
            Tile south = getTile(x, y + 1 >= height ? 0 : y + 1);
            Tile east = getTile(x + 1 >= width - 1 ? 0 : x + 1, y);
            Tile west = getTile(x - 1 < 0 ? width - 1 : x - 1, y);

            mask = addToMask(context, Direction.NORTH, mask, north, x, y - 1 < 0 ? height - 1 : y - 1, cr, avatarX, avatarY);
            mask = addToMask(context, Direction.SOUTH, mask, south, x, y + 1 >= height ? 0 : y + 1, cr, avatarX, avatarY);
            mask = addToMask(context, Direction.EAST, mask, east, x + 1 >= width - 1 ? 0 : x + 1, y, cr, avatarX, avatarY);
            mask = addToMask(context, Direction.WEST, mask, west, x - 1 < 0 ? width - 1 : x - 1, y, cr, avatarX, avatarY);

        } else {

            Tile north = getTile(x, y - 1);
            Tile south = getTile(x, y + 1);
            Tile east = getTile(x + 1, y);
            Tile west = getTile(x - 1, y);

            mask = addToMask(context, Direction.NORTH, mask, north, x, y - 1, cr, avatarX, avatarY);
            mask = addToMask(context, Direction.SOUTH, mask, south, x, y + 1, cr, avatarX, avatarY);
            mask = addToMask(context, Direction.EAST, mask, east, x + 1, y, cr, avatarX, avatarY);
            mask = addToMask(context, Direction.WEST, mask, west, x - 1, y, cr, avatarX, avatarY);
        }

        return mask;

    }

    private int addToMask(Context context, Direction dir, int mask, Tile tile, int x, int y, Creature cr, int avatarX, int avatarY) {
        if (tile != null) {

            TileRule rule = tile.getRule();
            boolean canmove = false;
            if (rule != null) {
                if (cr != null) {
                    if (cr.getSails() && rule.has(TileAttrib.sailable)) {
                        canmove = true;
                    } else if (cr.getSails() && !rule.has(TileAttrib.unwalkable)) {
                        canmove = false;
                    } else if (cr.getSwims() && rule.has(TileAttrib.swimmable)) {
                        canmove = true;
                    } else if (cr.getSwims() && !rule.has(TileAttrib.unwalkable)) {
                        canmove = false;
                    } else if (cr.getFlies() && !rule.has(TileAttrib.unflyable)) {
                        canmove = true;
                    } else if (rule.has(TileAttrib.creatureunwalkable)) {
                        canmove = false;
                    } else if (cr.getIncorporeal() || !rule.has(TileAttrib.unwalkable)) {
                        canmove = true;
                    }
                } else {
                    Transport tc = context.getTransport();
                    if (tc == Transport.SHIP && this.type != MapType.combat) {
                        if (rule.has(TileAttrib.sailable)) {
                            canmove = true;
                            if (rule.has(TileAttrib.snake)) {
                                canmove = false;
                            }
                        } else {
                            canmove = false;
                        }
                    } else if (tc == Transport.HORSE && this.type != MapType.combat) {
                        if (!rule.has(TileAttrib.creatureunwalkable) && !rule.has(TileAttrib.unwalkable)) {
                            canmove = true;
                        } else {
                            canmove = false;
                        }
                    } else {
                        if (rule.has(TileAttrib.dispelable)) {
                            canmove = true;
                            for (PartyMember pm : context.getParty().getMembers()) {
                                if (pm.getPlayer().marks[3] == 0) {
                                    canmove = false;
                                }
                            }
                        } else if (!rule.has(TileAttrib.unwalkable) || rule == TileRule.ship || rule.has(TileAttrib.chest) || rule == TileRule.horse) {
                            canmove = true;
                        }
                        for (Drawable dr : this.objects) {
                            if (dr.getTile() != null && dr.getTile().getRule() == TileRule.ship && dr.getCx() == x && dr.getCy() == y) {
                                canmove = true;
                                break;
                            }
                        }
                    }
                }
            } else {
                canmove = false;
            }

            //NPCs cannot go thru the secret doors or walk where the avatar is
            if (cr != null) {
                if ((tile.getIndex() == 73 && !cr.getIncorporeal()) || (avatarX == x && avatarY == y && !cr.getCanMoveOntoAvatar())) {
                    canmove = false;
                }
            }

            //see if another person is there
            if (people != null) {
                for (Person p : people) {
                    if (p == null || p.isRemovedFromMap()) {
                        continue;
                    }
                    if (p.getX() == x && p.getY() == y && !p.getEmulatingCreature().getIsWalkableOver()) {
                        canmove = false;
                        break;
                    }
                }
            }

            for (Creature cre : creatures) {
                if (cre.currentX == x && cre.currentY == y) {
                    canmove = false;
                    break;
                }
            }

            if (combatPlayers != null) {
                for (PartyMember p : combatPlayers) {
                    if (p.combatCr == null || p.fled) {
                        continue;
                    }
                    if (p.combatCr.currentX == x && p.combatCr.currentY == y) {
                        canmove = false;
                        break;
                    }
                }
            }

            if (rule == null || canmove || isDoorOpen(x, y)) {
                mask = Direction.addToMask(dir, mask);
            }
        } else {
            //if the tile is not on the map then it is OOB, 
            //so add this direction anyway so that monster flee operations work.
            if (cr != null && cr.getDamageStatus() == CreatureStatus.FLEEING) {
                mask = Direction.addToMask(dir, mask);
            }
        }
        return mask;
    }

    public DoorStatus getDoor(int x, int y) {
        for (DoorStatus ds : doors) {
            if (ds.x == x && ds.y == y) {
                return ds;
            }
        }
        return null;
    }

    public boolean unlockDoor(int x, int y) {
        DoorStatus ds = getDoor(x, y);
        if (ds != null) {
            ds.locked = false;
            return true;
        }
        return false;
    }

    public boolean openDoor(int x, int y) {
        DoorStatus ds = getDoor(x, y);
        if (ds != null && !ds.locked) {
            ds.openedTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Door will stay open for 10 seconds and then close
     */
    public boolean isDoorOpen(int x, int y) {
        DoorStatus ds = getDoor(x, y);
        if (ds != null && System.currentTimeMillis() - ds.openedTime < 10000) {
            return true;
        }
        return false;
    }

    public boolean isDoorOpen(DoorStatus ds) {
        if (ds != null && System.currentTimeMillis() - ds.openedTime < 10000) {
            return true;
        }
        return false;
    }

    public class DoorStatus {

        public int x;
        public int y;
        public long openedTime;
        public boolean locked = false;

        private DoorStatus(int x, int y, boolean locked, long openedTime) {
            this.x = x;
            this.y = y;
            this.openedTime = openedTime;
            this.locked = locked;
        }
    }

    public ItemMapLabels searchLocation(BaseScreen screen, Party p, PartyMember pm, int x, int y, int z) {

        SaveGame sg = p.getSaveGame();

        if (labels == null) {
            return null;
        }

        Label tmp = null;
        for (Label l : labels) {
            if (l.getX() == x && l.getY() == y && l.getZ() == z) {
                tmp = l;
            }
        }
        if (tmp == null) {
            return null;
        }

        int expPoints = 0;
        ItemMapLabels label = ItemMapLabels.valueOf(ItemMapLabels.class, tmp.getName());
        boolean added = false;

        switch (label) {
            case CARD_OF_DEATH:
                if (pm.getPlayer().cards[0] > 0) {
                    break;
                }
                pm.getPlayer().cards[0]++;
                expPoints = 100;
                added = true;
                break;
            case CARD_OF_SOL:
                if (pm.getPlayer().cards[1] > 0) {
                    break;
                }
                pm.getPlayer().cards[1]++;
                expPoints = 100;
                added = true;
                break;
            case CARD_OF_MOONS:
                if (pm.getPlayer().cards[2] > 0) {
                    break;
                }
                pm.getPlayer().cards[2]++;
                expPoints = 100;
                added = true;
                break;
            case CARD_OF_LOVE:
                if (pm.getPlayer().cards[3] > 0) {
                    break;
                }
                pm.getPlayer().cards[3]++;
                expPoints = 100;
                added = true;
                break;
            case EXOTIC_ARMOR:
                if (pm.getPlayer().armors[ArmorType.EXOTIC.ordinal()] > 0) {
                    break;
                }
                pm.getPlayer().armors[ArmorType.EXOTIC.ordinal()]++;
                expPoints = 100;
                added = true;
                break;
            case EXOTIC_WEAPON:
                if (pm.getPlayer().weapons[WeaponType.EXOTIC.ordinal()] > 0) {
                    break;
                }
                pm.getPlayer().weapons[WeaponType.EXOTIC.ordinal()]++;
                expPoints = 100;
                added = true;
                break;
        }

        if (expPoints > 0) {
            pm.awardXP(expPoints);
        }

        if (added) {
            return label;
        }

        return null;
    }

    @XmlTransient
    public List<PartyMember> getCombatPlayers() {
        return combatPlayers;
    }

    public void setCombatPlayers(List<PartyMember> combatPlayers) {
        this.combatPlayers = combatPlayers;
    }

    public List<Drawable> getObjects() {
        return this.objects;
    }

    public void setObjects() {

        //init any ships or chests etc
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                Tile tile = getTile(x, y);
                if (tile != null) {
                    if (tile.getName().equals("ship")) {
                        addObject(tile, x, y);
                    } else if (tile.getName().equals("chest")) {
                        addObject(tile, x, y);
                    }
                }

            }
        }
    }

    public Drawable addObject(Tile tile, int x, int y) {
        Drawable dr = new Drawable(this, x, y, tile, Exodus.standardAtlas);
        Vector3 v = new Vector3(x * tilePixelWidth, getHeight() * tilePixelHeight - y * tilePixelHeight - tilePixelHeight, 0);
        dr.setX(v.x);
        dr.setY(v.y);
        switch (tile.getName()) {
            case "ship":
                setTile(Exodus.baseTileSet.getTileByName("water"), x, y);
                break;
            case "chest":
                String t = (this.id == Maps.SOSARIA.getId() ? "grass" : "brick_floor");
                setTile(Exodus.baseTileSet.getTileByName(t), x, y);
                break;
        }
        this.objects.add(dr);
        return dr;
    }

}
