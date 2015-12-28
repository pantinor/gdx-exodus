package exodus;

import java.util.Random;

import objects.Aura;
import objects.BaseMap;
import objects.Creature;
import objects.Drawable;
import objects.Portal;
import objects.Tile;

import com.badlogic.gdx.maps.tiled.TiledMap;
import exodus.Party.PartyMember;
import java.util.List;
import util.PartyDeathException;
import util.XORShiftRandom;

public class Context implements Constants {

    private Party party;
    private BaseMap currentMap;
    private TiledMap currentTiledMap;
    private int locationMask;
    private int line, col;
    private int moonPhase = 0;
    private Direction windDirection = Direction.NORTH;
    private int windCounter;
    private Aura aura = new Aura();
    private int horseSpeed;
    private int opacity;
    private Transport transportContext;
    private Drawable lastShip;
    private Drawable currentShip;

    private long lastCommandTime = System.currentTimeMillis();

    private final Random rand = new XORShiftRandom();

    public int getLine() {
        return line;
    }

    public int getCol() {
        return col;
    }

    public int getMoonPhase() {
        return moonPhase;
    }

    public Direction getWindDirection() {
        return windDirection;
    }

    public int incrementWindCounter() {
        return ++windCounter;
    }

    public void setWindCounter(int v) {
        windCounter = v;
    }

    public int getHorseSpeed() {
        return horseSpeed;
    }

    public int getOpacity() {
        return opacity;
    }

    public Transport getTransport() {
        return transportContext;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void setMoonPhase(int moonPhase) {
        this.moonPhase = moonPhase;
    }

    public void setWindDirection(Direction windDirection) {
        this.windDirection = windDirection;
    }

    public void setHorseSpeed(int horseSpeed) {
        this.horseSpeed = horseSpeed;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public void setTransport(int idx) {
        if (idx == 1) {
            this.transportContext = Transport.SHIP;
        } else if (idx == 2) {
            this.transportContext = Transport.HORSE;
        } else {
            this.transportContext = Transport.FOOT;
        }
    }

    public void setTransport(Transport transportContext) {
        this.transportContext = transportContext;
    }

    public TiledMap getCurrentTiledMap() {
        return currentTiledMap;
    }

    public void setCurrentTiledMap(TiledMap currentTiledMap) {
        this.currentTiledMap = currentTiledMap;
    }

    public BaseMap getCurrentMap() {
        return currentMap;
    }

    public void setCurrentMap(BaseMap currentMap) {
        this.currentMap = currentMap;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
        party.setContext(this);
    }

    public void saveGame(float x, float y, float z, Direction orientation, Maps map) {

        if (map == Maps.SOSARIA) {
            party.getSaveGame().partyX = (int) x;
            party.getSaveGame().partyY = (int) y;
            party.getSaveGame().dnglevel = 0;
            party.getSaveGame().orientation = 0;
        } else {
            Portal p = Maps.SOSARIA.getMap().getPortal(map.getId());
            party.getSaveGame().partyX = (int) x;
            party.getSaveGame().partyY = (int) y;
            party.getSaveGame().dnglevel = (int) z;
            party.getSaveGame().orientation = orientation.getVal();
        }

        party.getSaveGame().transport = this.transportContext.ordinal();
        party.getSaveGame().location = map.getId();

        party.getSaveGame().resetMonsters();

        Drawable[] objects = new Drawable[24];
        int count = 0;
        for (Drawable d : Maps.SOSARIA.getMap().getObjects()) {
            objects[count] = d;
            if (count > 23) {
                break;
            }
            count++;
        }

        for (int i = 0; i < 24; i++) {
            if (objects[i] != null) {
                party.getSaveGame().objects_save_tileids[i] = (byte) objects[i].getTile().getIndex();
                party.getSaveGame().objects_save_x[i] = (byte) objects[i].getCx();
                party.getSaveGame().objects_save_y[i] = (byte) objects[i].getCy();
            }
        }

        List<Creature> monsters = Maps.SOSARIA.getMap().getCreatures();
        for (int i = 0; i < 8 && monsters.size() > i; i++) {
            Tile tile = Exodus.baseTileSet.getTileByName(monsters.get(i).getTile().toString());
            if (tile == null || tile.getName().equals("whirlpool")) {
                continue;
            }
            party.getSaveGame().monster_save_tileids[i] = (byte) tile.getIndex();
            party.getSaveGame().monster_save_x[i] = (byte) monsters.get(i).currentX;
            party.getSaveGame().monster_save_y[i] = (byte) monsters.get(i).currentY;
        }

        try {
            party.getSaveGame().write(PARTY_SAV_BASE_FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int getLocationMask() {
        return locationMask;
    }

    public void setLocationMask(int locationMask) {
        this.locationMask = locationMask;
    }

    public Aura getAura() {
        return aura;
    }

    public void setAura(AuraType t, int duration) {
        this.aura.set(t, duration);
    }

    /**
     * Default handler for slowing movement. Returns true if slowed, false if
     * not slowed
     */
    public boolean slowedByTile(Tile tile) {
        boolean slow;

        TileRule ts = tile.getRule();
        if (ts == null) {
            return false;
        }

        switch (ts.getSpeed()) {
            case SLOW:
                slow = rand.nextInt(8) == 0;
                break;
            case VSLOW:
                slow = rand.nextInt(4) == 0;
                break;
            case VVSLOW:
                slow = rand.nextInt(2) == 0;
                break;
            case FAST:
            default:
                slow = false;
                break;
        }

        return slow;
    }

    public void damageShip(int minDamage, int maxDamage) {
        if (transportContext == Transport.SHIP) {
            int damage = minDamage >= 0 && minDamage < maxDamage ? rand.nextInt(maxDamage + 1 - minDamage) + minDamage : maxDamage;
            party.adjustShipHull(-damage);
        }
    }

    public void getChestTrapHandler(PartyMember pm) throws PartyDeathException {

        TileEffect trapType;
        boolean hasTrap = (rand.nextInt(2) == 0);
        int whichTrap = rand.nextInt(4);

        if (hasTrap) {
            if (pm.getPlayer().dex + 25 < rand.nextInt(100)) {

                switch (whichTrap) {
                    case 2:
                        trapType = TileEffect.POISON;
                        Exodus.hud.add("Poison Trap!");
                        Sounds.play(Sound.POISON_EFFECT);
                        break;
                    case 3:
                        trapType = TileEffect.LAVA;
                        Exodus.hud.add("Gas Trap!");
                        break;
                    default:
                        trapType = TileEffect.FIRE;
                        Exodus.hud.add("Acid Trap!");
                        Sounds.play(Sound.ACID);
                        break;
                }

                if (trapType == TileEffect.LAVA) {
                    party.applyEffect(trapType);
                } else {
                    pm.applyEffect(trapType);
                }
                
            } else {
                Sounds.play(Sound.EVADE);
                Exodus.hud.add("Evaded!");
            }
        }
    }

    public Drawable getCurrentShip() {
        return currentShip;
    }

    public void setCurrentShip(Drawable currentShip) {
        this.currentShip = currentShip;
    }

    public Drawable getLastShip() {
        return lastShip;
    }

    public void setLastShip(Drawable lastShip) {
        this.lastShip = lastShip;
    }

    public Maps getCombatMap(Creature c, BaseMap bm, int creatureX, int creatureY, int avatarX, int avatarY) {

        Maps cm = bm.getTile(creatureX, creatureY).getCombatMap();
        TileRule ptr = bm.getTile(avatarX, avatarY).getRule();

        if (c.getSwims() && !ptr.has(TileAttrib.unwalkable)) {
            cm = Maps.SHORE_CON;
        } else if (c.getSails() && !ptr.has(TileAttrib.unwalkable)) {
            cm = Maps.SHORSHIP_CON;
        }

        if (transportContext == Transport.SHIP) {
            if (c.getSwims()) {
                cm = Maps.SHIPSEA_CON;
            } else if (c.getSails()) {
                cm = Maps.SHIPSHIP_CON;
            } else {
                cm = Maps.SHIPSHOR_CON;
            }
        }

        return cm;

    }

    public long getLastCommandTime() {
        return lastCommandTime;
    }

    public void setLastCommandTime(long lastCommandTime) {
        this.lastCommandTime = lastCommandTime;
    }

}
