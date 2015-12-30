/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import exodus.BaseScreen;
import exodus.CombatScreen;
import exodus.Constants;
import exodus.Context;
import exodus.Exodus;
import exodus.Party.PartyMember;
import exodus.Sound;
import exodus.Sounds;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import objects.BaseMap;
import objects.Creature;
import objects.Drawable;
import objects.Moongate;
import objects.Person;
import objects.Portal;
import objects.ProjectileActor;
import objects.Tile;
import objects.TileSet;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Paul
 */
public class Utils implements Constants {

    public static Random rand = new XORShiftRandom();

    public static String properCase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    //This gives you a random number in between low (inclusive) and high (exclusive)
    public static int getRandomBetween(int low, int high) {
        return rand.nextInt(high - low) + low;
    }

    public static Object loadXml(String fname, Class<?> clazz) throws Exception {
        InputStream is = Utils.class.getResourceAsStream("/" + fname);
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return jaxbUnmarshaller.unmarshal(is);
    }

    public static int adjustValueMax(int v, int val, int max) {
        v += val;
        if (v > max) {
            v = max;
        }
        return v;
    }

    public static int adjustValueMin(int v, int val, int min) {
        v += val;
        if (v < min) {
            v = min;
        }
        return v;
    }

    public static int adjustValue(int v, int val, int max, int min) {
        v += val;
        if (v > max) {
            v = max;
        }
        if (v < min) {
            v = min;
        }
        return v;
    }

    public static void setMapTiles(BaseMap map, TileSet ts) throws Exception {

        String fname = map.getFname().toLowerCase();
        if (fname == null || fname.isEmpty()) {
            return;
        }

        if (fname.endsWith("tmx")) {
            setTilesFromTMX(map, Maps.get(map.getId()), fname, ts);
        } else {

            InputStream is = ClassLoader.class.getResourceAsStream("/assets/data/" + fname);
            byte[] bytes = IOUtils.toByteArray(is);

            Tile[] tiles = new Tile[map.getWidth() * map.getHeight()];

            if (map.getType() == MapType.world || map.getType() == MapType.city) {

                int pos = 0;
                for (int y = 0; y < map.getHeight(); y++) {
                    for (int x = 0; x < map.getWidth(); x++) {
                        int index = (bytes[pos] & 0xff) / 4;
                        pos++;
                        Tile tile = ts.getTileByIndex(index);
                        if (tile == null) {
                            System.out.println("Tile index cannot be found: " + index + " using index 37 for black space.");
                            tile = ts.getTileByIndex(37);
                        }

                        tiles[x + y * map.getWidth()] = tile;
                    }
                }

                //doors
                for (int y = 0; y < map.getHeight(); y++) {
                    for (int x = 0; x < map.getWidth(); x++) {
                        Tile tile = tiles[x + y * map.getWidth()];
                        Tile left = x > 0 ? tiles[(x - 1) + y * map.getWidth()] : null;
                        Tile right = x < map.getWidth() - 1 ? tiles[(x + 1) + y * map.getWidth()] : null;
                        if (tile.getIndex() == 46 && (left != null && left.getRule() != TileRule.signs) && (right != null && right.getRule() != TileRule.signs)) {
                            tiles[x + y * map.getWidth()] = ts.getTileByName("locked_door");
                        }
                    }
                }

                //ambrosia doors
                if (map.getId() == Maps.AMBROSIA.getId()) {
                    tiles[34 + 5 * map.getWidth()] = ts.getTileByName("locked_door");
                    tiles[35 + 5 * map.getWidth()] = ts.getTileByName("locked_door");
                    tiles[36 + 5 * map.getWidth()] = ts.getTileByName("locked_door");
                }

                if (map.getType() == MapType.city) {
                    setPeople(map, tiles, bytes, ts);
                }

                if (map.getType() == MapType.world) {
                    //set a moongate tile to grass here
                    tiles[15 + 29 * map.getWidth()] = ts.getTileByIndex(1);
                }

            } else if (map.getType() == MapType.combat) {

                int pos = 0x40;
                for (int y = 0; y < map.getHeight(); y++) {
                    for (int x = 0; x < map.getWidth(); x++) {
                        int index = bytes[pos] & 0xff;
                        pos++;
                        Tile tile = ts.getTileByIndex(index);
                        if (tile == null) {
                            //System.err.printf("%S Combat Tile index cannot be found: %d using index 127 for black space (%d, %d)\n", map, index, x, y);
                            tile = ts.getTileByIndex(127);
                        }
                        tiles[x + y * map.getWidth()] = tile;
                    }
                }
            } else if (map.getType() == MapType.shrine) {
                int pos = 0;
                for (int y = 0; y < map.getHeight(); y++) {
                    for (int x = 0; x < map.getWidth(); x++) {
                        int index = bytes[pos] & 0xff;
                        pos++;
                        Tile tile = ts.getTileByIndex(index);
                        if (tile == null) {
                            System.out.println("Tile index cannot be found: " + index + " using index 127 for black space.");
                            tile = ts.getTileByIndex(127);
                        }
                        if (tile.getIndex() == 31) { //avatar position
                            tile = ts.getTileByIndex(4);
                        }
                        tiles[x + y * map.getWidth()] = tile;
                    }
                }
            }

            map.setTiles(tiles);

        }

    }

    public static void setPeople(BaseMap map, Tile[] tiles, byte[] bytes, TileSet ts) {
        List<Person> people = new ArrayList<>();

        int pos = 0x1180;
        for (int x = 0; x < 32; x++) {
            int index = (bytes[pos] & 0xff) / 4;
            pos++;
            Tile tile = ts.getTileByIndex(index);
            if (tile == null) {
                System.out.println("Tile index cannot be found: " + index + " using index 37 for black space.");
                tile = ts.getTileByIndex(37);
            }
            Person p = new Person();
            p.setTile(tile);
            people.add(p);
        }

        pos = 0x11C0;
        for (int x = 0; x < 32; x++) {
            int dx = bytes[pos] & 0xff;
            pos++;
            people.get(x).setStart_x(dx);
            people.get(x).setX(dx);
        }

        pos = 0x11E0;
        for (int x = 0; x < 32; x++) {
            int dy = bytes[pos] & 0xff;
            pos++;
            people.get(x).setStart_y(dy);
            people.get(x).setY(dy);
        }

        pos = 0x1200;
        for (int x = 0; x < 32; x++) {
            int dialog = bytes[pos] & 0x0f;
            int m = (bytes[pos] >> 4) & 0x0f;
            ObjectMovementBehavior move = ObjectMovementBehavior.FIXED;
            if (m == 4) {
                move = ObjectMovementBehavior.WANDER;
            }
            if (m == 8) {
                move = ObjectMovementBehavior.FOLLOW_AVATAR;
            }
            if (m == 12) {
                move = ObjectMovementBehavior.ATTACK_AVATAR;
            }
            pos++;
            people.get(x).setMovement(move);
            people.get(x).setDialogId(dialog);
        }

        pos = 0x11A0;
        for (int x = 0; x < 32; x++) {
            int index = (bytes[pos] & 0xff) / 4;
            pos++;
            Tile tile = ts.getTileByIndex(index);
            if (tile == null) {
                System.out.println("Tile index cannot be found: " + index + " using index 37 for black space.");
                tile = ts.getTileByIndex(37);
            }
            int dx = people.get(x).getX();
            int dy = people.get(x).getY();

            tiles[dx + dy * map.getWidth()] = tile;
        }

        pos = 0x1000;
        int[] textOffsets = new int[8];
        for (int x = 0; x < 8; x++) {
            textOffsets[x] = (bytes[pos] & 0xff) + 0x1000;
            pos += 2;
        }

        String[] texts = new String[8];
        for (int x = 0; x < 8; x++) {
            int os = textOffsets[x];
            byte[] b = new byte[64];
            int c = 0;
            while (true) {
                b[c] = bytes[os];
                if (b[c] == 0) {
                    break;
                }
                os++;
                c++;
            }
            texts[x] = new String(b).trim();
            texts[x] = texts[x].replaceAll("[<>]", "");
            texts[x] = texts[x].replaceAll("[\n\r]", " ");

        }

        for (int x = 0; x < 8; x++) {
            for (Person per : people) {
                if (per.getDialogId() == x + 1) {
                    per.setConversation(texts[x]);
                }
            }
        }

        map.setPeople(people);
    }

    public static void setTilesFromTMX(BaseMap map, Maps id, String tmxFile, TileSet ts) {

        Tile[] tiles = new Tile[map.getWidth() * map.getHeight()];

        FileHandleResolver resolver = new Constants.ClasspathResolver();
        TmxMapLoader loader = new TmxMapLoader(resolver);
        TiledMap tm = loader.load("assets/tmx/" + tmxFile);

        TiledMapTileLayer ml = (TiledMapTileLayer) tm.getLayers().get(map.getId() + "-map");
        if (ml != null) {
            FileHandle f = resolver.resolve("assets/graphics/latest-atlas.txt");
            TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(f, f.parent(), false);
            int png_grid_width = 24;
            Tile[] mapTileIds = new Tile[png_grid_width * Constants.tilePixelWidth + 1];
            for (TextureAtlas.TextureAtlasData.Region r : atlas.getRegions()) {
                int x = r.left / r.width;
                int y = r.top / r.height;
                int i = x + (y * png_grid_width) + 1;
                mapTileIds[i] = ts.getTileByName(r.name);
                if (mapTileIds[i] == null) {
                    //System.out.printf("no tile found: %s %d\n",r.name,i);
                }
            }

            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    StaticTiledMapTile tr = (StaticTiledMapTile) ml.getCell(x, map.getWidth() - 1 - y).getTile();
                    Tile tile = mapTileIds[tr.getId()];
                    if (tile == null) {
                        System.out.printf("no tile found: %d %d %d\n", x, y, tr.getId());
                    }
                    tiles[x + (y * map.getWidth())] = tile;
                }
            }
        }

        map.setTiles(tiles);

        MapLayer objectsLayer = tm.getLayers().get("portals");
        if (objectsLayer != null) {
            Iterator<MapObject> iter = objectsLayer.getObjects().iterator();
            while (iter.hasNext()) {
                MapObject obj = iter.next();
                Portal p = map.getPortal(Maps.valueOf(obj.getName()).getId());
                Iterator<String> keys = obj.getProperties().getKeys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = obj.getProperties().get(key).toString();
                    if (key.equals("x")) {
                        p.setX(new Integer(value));
                    } else if (key.equals("y")) {
                        p.setY(new Integer(value));
                    }
                }
            }
        }

        objectsLayer = tm.getLayers().get("moongates");
        if (objectsLayer != null) {
            Iterator<MapObject> iter = objectsLayer.getObjects().iterator();
            while (iter.hasNext()) {
                MapObject obj = iter.next();
                Moongate m = map.getMoongate(obj.getName());
                Iterator<String> keys = obj.getProperties().getKeys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = obj.getProperties().get(key).toString();
                    if (key.equals("x")) {
                        m.setX(new Integer(value));
                    } else if (key.equals("y")) {
                        m.setY(new Integer(value));
                    }
                }
            }
        }

    }

    public static Direction getPath(MapBorderBehavior borderbehavior, int width, int height, int toX, int toY, int validMovesMask, boolean towards, int fromX, int fromY) {
        /* find the directions that lead [to/away from] our target */
        int directionsToObject = towards ? getRelativeDirection(borderbehavior, width, height, toX, toY, fromX, fromY) : ~getRelativeDirection(borderbehavior, width, height, toX, toY, fromX, fromY);

        /* make sure we eliminate impossible options */
        directionsToObject &= validMovesMask;

        /* get the new direction to move */
        if (directionsToObject > 0) {
            return Direction.getRandomValidDirection(directionsToObject);
        } /* there are no valid directions that lead to our target, just move wherever we can! */ else {
            return null;//Direction.getRandomValidDirection(validMovesMask);
        }
    }

    /**
     * Finds the movement distance (not using diagonals) from point a to point b
     * on a map, taking into account map boundaries and such.
     */
    public static int movementDistance(MapBorderBehavior borderbehavior, int width, int height, int fromX, int fromY, int toX, int toY) {
        int dirmask = 0;;
        int dist = 0;

        /* get the direction(s) to the coordinates */
        dirmask = getRelativeDirection(borderbehavior, width, height, toX, toY, fromX, fromY);

        if (borderbehavior == MapBorderBehavior.wrap) {
            if (Math.abs(fromX - toX) > Math.abs(fromX + width - toX)) {
                fromX += width;
            } else if (Math.abs(fromX - toX) > Math.abs(fromX - width - toX)) {
                fromX -= width;
            }

            if (Math.abs(fromY - toY) > Math.abs(fromY + width - toY)) {
                fromY += height;
            } else if (Math.abs(fromY - toY) > Math.abs(fromY - width - toY)) {
                fromY -= height;
            }
        }

        while (fromX != toX || fromY != toY) {

            if (fromX != toX) {
                if (Direction.isDirInMask(Direction.WEST, dirmask)) {
                    fromX--;
                } else {
                    fromX++;
                }
                dist++;
            }
            if (fromY != toY) {
                if (Direction.isDirInMask(Direction.NORTH, dirmask)) {
                    fromY--;
                } else {
                    fromY++;
                }
                dist++;
            }

        }

        return dist;
    }

    /**
     * Returns a mask of directions that indicate where one point is relative to
     * another. For instance, if the object at (x, y) is northeast of (c.x,
     * c.y), then this function returns (MASK_DIR(DIR_NORTH) |
     * MASK_DIR(DIR_EAST)) This function also takes into account map boundaries
     * and adjusts itself accordingly.
     */
    public static int getRelativeDirection(MapBorderBehavior borderbehavior, int width, int height, int toX, int toY, int fromX, int fromY) {
        int dx = 0, dy = 0;
        int dirmask = 0;

        /* adjust our coordinates to find the closest path */
        if (borderbehavior == MapBorderBehavior.wrap) {

            if (Math.abs(fromX - toX) > Math.abs(fromX + width - toX)) {
                fromX += width;
            } else if (Math.abs(fromX - toX) > Math.abs(fromX - width - toX)) {
                fromX -= width;
            }

            if (Math.abs(fromY - toY) > Math.abs(fromY + width - toY)) {
                fromY += height;
            } else if (Math.abs(fromY - toY) > Math.abs(fromY - width - toY)) {
                fromY -= height;
            }

            dx = fromX - toX;
            dy = fromY - toY;
        } else {
            dx = fromX - toX;
            dy = fromY - toY;
        }

        /* add x directions that lead towards to_x to the mask */
        if (dx < 0) {
            dirmask |= Direction.EAST.getMask();
        } else if (dx > 0) {
            dirmask |= Direction.WEST.getMask();
        }

        /* add y directions that lead towards to_y to the mask */
        if (dy < 0) {
            dirmask |= Direction.SOUTH.getMask();
        } else if (dy > 0) {
            dirmask |= Direction.NORTH.getMask();
        }

        /* return the result */
        return dirmask;
    }

    public static AttackVector enemyfireCannon(Context context, List<Drawable> objects, BaseMap combatMap, Direction dir, int startX, int startY, int avatarX, int avatarY) throws PartyDeathException {

        List<AttackVector> path = Utils.getDirectionalActionPath(combatMap, dir.getMask(), startX, startY, 1, 4, true, false, true);

        AttackVector target = null;
        int distance = 1;
        for (AttackVector v : path) {
            AttackResult res = fireAt(context, objects, combatMap, v, false, avatarX, avatarY);
            target = v;
            target.result = res;
            target.distance = distance;
            if (res != AttackResult.NONE) {
                break;
            }
            distance++;
        }

        return target;
    }

    public static AttackVector avatarfireCannon(Context context, List<Drawable> objects, BaseMap combatMap, Direction dir, int startX, int startY) {

        List<AttackVector> path = Utils.getDirectionalActionPath(combatMap, dir.getMask(), startX, startY, 1, 4, true, true, true);
        AttackVector target = null;
        try {
            int distance = 1;
            for (AttackVector v : path) {
                AttackResult res = fireAt(context, objects, combatMap, v, true, 0, 0);
                target = v;
                target.result = res;
                target.distance = distance;
                if (res != AttackResult.NONE) {
                    break;
                }
                distance++;
            }
        } catch (PartyDeathException e) {
            //not happening
        }

        return target;
    }

    public static List<AttackVector> getDirectionalActionPath(BaseMap combatMap, int dirmask, int x, int y, int minDistance, int maxDistance,
            boolean weaponCanAttackThroughObjects, boolean checkForCreatures, boolean isCannonBall) {

        List<AttackVector> path = new ArrayList<>();

        /*
         * try every tile in the given direction, up to the given range.
         * Stop when the the range is exceeded, or the action is blocked.
         */
        int nx = x;
        int ny = y;

        for (int distance = minDistance; distance <= maxDistance; distance++) {

            /* make sure our action isn't taking us off the map */
            if (nx > combatMap.getWidth() - 1 || nx < 0 || ny > combatMap.getHeight() - 1 || ny < 0) {
                break;
            }

            boolean blocked = combatMap.isTileBlockedForRangedAttack(nx, ny, checkForCreatures);

            Tile tile = combatMap.getTile(nx, ny);

            boolean canAttackOverSolid = (tile != null && tile.getRule() != null && weaponCanAttackThroughObjects);

            if (!blocked || canAttackOverSolid || isCannonBall) {
                path.add(new AttackVector(nx, ny));
            } else {
                path.add(new AttackVector(nx, ny));
                break;
            }

            if (Direction.isDirInMask(Direction.NORTH, dirmask)) {
                ny--;
            }
            if (Direction.isDirInMask(Direction.SOUTH, dirmask)) {
                ny++;
            }
            if (Direction.isDirInMask(Direction.EAST, dirmask)) {
                nx++;
            }
            if (Direction.isDirInMask(Direction.WEST, dirmask)) {
                nx--;
            }

        }

        return path;
    }

    public static void animateCannonFire(final BaseScreen screen, final Stage stage, final BaseMap map, final AttackVector av, final int sx, final int sy, final boolean avatarAttack) {

        Sounds.play(Sound.CANNON);

        final ProjectileActor p = new ProjectileActor(screen, Color.WHITE, sx, sy, av.result);

        Vector3 d = screen.getMapPixelCoords(av.x, av.y);

        p.addAction(sequence(moveTo(d.x, d.y, av.distance * .1f), Actions.run(new Runnable() {
            public void run() {
                switch (p.res) {
                    case HIT:
                        p.resultTexture = Exodus.hitTile;
                        map.removeCreature(av.impactedCreature);
                        if (av.impactedDrawable != null && av.impactedDrawable.getShipHull() <= 0) {
                            av.impactedDrawable.remove();
                        }
                        break;
                    case MISS:
                        p.resultTexture = Exodus.missTile;
                        break;
                }

                if (avatarAttack) {
                    Vector3 v = screen.getCurrentMapCoords();
                    screen.finishTurn((int) v.x, (int) v.y);
                }

            }
        }), Actions.fadeOut(.3f), removeActor(p)));

        stage.addActor(p);
    }

    private static AttackResult fireAt(Context context, List<Drawable> objects, BaseMap combatMap, AttackVector target, boolean avatarAttack, int avatarX, int avatarY) throws PartyDeathException {

        AttackResult res = AttackResult.NONE;

        //check for ship
        Drawable ship = null;
        for (Drawable d : objects) {
            if (d.getTile().getName().equals("ship") && d.getCx() == target.x && d.getCy() == target.y) {
                ship = d;
            }
        }

        if (ship != null) {
            ship.damageShip(-1, 10);
            target.impactedDrawable = ship;
            return AttackResult.HIT;
        }

        if (avatarAttack) {

            Creature creature = null;
            for (Creature c : combatMap.getCreatures()) {
                if (c.currentX == target.x && c.currentY == target.y) {
                    creature = c;
                    break;
                }
            }

            if (creature == null) {
                return res;
            }

            if (rand.nextInt(4) == 0) {
                res = AttackResult.HIT;
                target.impactedCreature = creature;
            } else {
                res = AttackResult.MISS;
            }

        } else if (target.x == avatarX && target.y == avatarY) {

            if (context.getTransport() == Transport.SHIP) {
                context.damageShip(-1, 10);
            } else {
                //context.getParty().damageParty(10, 25);
            }

            res = AttackResult.HIT;
        }

        return res;
    }

    public static AttackResult attackHit(Creature attacker, PartyMember defender) {
        int attackValue = rand.nextInt(256) + attacker.getAttackBonus();
        int defenseValue = defender.getDefense();
        return attackValue > defenseValue ? AttackResult.HIT : AttackResult.MISS;
    }

    private static boolean attackHit(PartyMember attacker, Creature defender) {
        int attackValue = rand.nextInt(256) + attacker.getAttackBonus();
        int defenseValue = defender.getDefense();
        return attackValue > defenseValue;
    }

    public static boolean dealDamage(PartyMember attacker, Creature defender, int damage) {
        int xp = defender.getExp();
        if (!damageCreature(defender, damage, true)) {
            attacker.awardXP(xp);
            return false;
        }
        return true;
    }

    public static boolean dealDamage(Creature attacker, PartyMember defender) throws PartyDeathException {
        int damage = attacker.getDamage();
        return defender.applyDamage(damage, true);
    }

    public static boolean damageCreature(Creature cr, int damage, boolean byplayer) {

        if (cr.getTile() != CreatureType.lord_british) {
            cr.setHP(Utils.adjustValueMin(cr.getHP(), -damage, 0));
        }

        switch (cr.getDamageStatus()) {

            case DEAD:
                if (byplayer) {
                    Exodus.hud.add(String.format("%s Killed! Exp. %d", cr.getName(), cr.getExp()));
                } else {
                    Exodus.hud.add(String.format("%s Killed!", cr.getName()));
                }
                return false;
            case FLEEING:
                Exodus.hud.add(String.format("%s Fleeing!", cr.getName()));
                break;

            case CRITICAL:
                Exodus.hud.add(String.format("%s Critical!", cr.getName()));
                break;

            case HEAVILYWOUNDED:
                Exodus.hud.add(String.format("%s Heavily Wounded!", cr.getName()));
                break;

            case LIGHTLYWOUNDED:
                Exodus.hud.add(String.format("%s Lightly Wounded!", cr.getName()));
                break;

            case BARELYWOUNDED:
                Exodus.hud.add(String.format("%s Barely Wounded!", cr.getName()));
                break;
            case FINE:
                break;
            default:
                break;
        }

        return true;
    }

    /**
     * using diagonals computes distance, used with finding nearest party member
     */
    public static int distance(MapBorderBehavior borderbehavior, int width, int height, int fromX, int fromY, int toX, int toY) {
        int dist = movementDistance(borderbehavior, width, height, fromX, fromY, toX, toY);

        if (dist <= 0) {
            return dist;
        }

        /* calculate how many fewer movements there would have been */
        dist -= Math.abs(fromX - toX) < Math.abs(fromY - toY) ? Math.abs(fromX - toX) : Math.abs(fromY - toY);

        return dist;
    }

    public static void animateAttack(Stage stage, final CombatScreen scr, PartyMember attacker, Direction dir, int x, int y, int range) {

        final AttackVector av = Utils.attack(scr.combatMap, attacker, dir, x, y, range);

        boolean magicHit = attacker.getPlayer().weapon.getWeapon().getHittile().equals("magic_flash");

        final ProjectileActor p = new ProjectileActor(scr, magicHit ? Color.CYAN : Color.RED, x, y, av.result);

        Vector3 v = scr.getMapPixelCoords(av.x, av.y);

        final TextureRegion hitTile = (magicHit ? Exodus.magicHitTile : Exodus.hitTile);

        p.addAction(sequence(moveTo(v.x, v.y, av.distance * .1f), new Action() {
            @Override
            public boolean act(float delta) {
                switch (p.res) {
                    case HIT:
                        p.resultTexture = hitTile;
                        break;
                    case MISS:
                        p.resultTexture = Exodus.missTile;
                        break;
                }

                scr.finishPlayerTurn();

                return true;
            }
        }, fadeOut(.2f), removeActor(p)));

        stage.addActor(p);
    }

    private static AttackVector attack(BaseMap combatMap, PartyMember attacker, Direction dir, int x, int y, int range) {

        WeaponType wt = attacker.getPlayer().weapon;
        boolean weaponCanAttackThroughObjects = wt.getWeapon().getAttackthroughobjects();

        List<AttackVector> path = Utils.getDirectionalActionPath(combatMap, dir.getMask(), x, y, 1, range, weaponCanAttackThroughObjects, true, false);

        AttackVector target = null;
        boolean foundTarget = false;
        int distance = 1;
        for (AttackVector v : path) {
            AttackResult res = attackAt(combatMap, v, attacker, range, distance);
            target = v;
            target.result = res;
            target.distance = distance;
            if (res != AttackResult.NONE) {
                foundTarget = true;
                break;
            }
            distance++;
        }

        if (wt.getWeapon().getLose() || (wt.getWeapon().getLosewhenranged() && (!foundTarget || distance > 1))) {
            if (attacker.loseWeapon() == WeaponType.NONE) {
                Exodus.hud.add("Last One!");
            }
        }

        return target;
    }

    public static void animateMagicAttack(Stage stage, final CombatScreen scr, PartyMember attacker, Direction dir, int x, int y, Spell spell, int minDamage, int maxDamage) {

        final AttackVector av = Utils.castSpellAttack(scr.combatMap, attacker, dir, x, y, minDamage, maxDamage, spell);

        Color color = Color.BLUE;
        switch (spell) {
            case FULGAR:
                color = Color.RED;
                break;
            case DECORP:
                color = Color.VIOLET;
                break;
        }

        final ProjectileActor p = new ProjectileActor(scr, color, x, y, av.result);

        Vector3 v = scr.getMapPixelCoords(av.x, av.y);

        p.addAction(sequence(moveTo(v.x, v.y, av.distance * .1f), new Action() {
            public boolean act(float delta) {

                switch (p.res) {
                    case HIT:
                        p.resultTexture = Exodus.magicHitTile;
                        break;
                    case MISS:
                        p.resultTexture = Exodus.missTile;
                        break;
                }

                scr.replaceTile(av.leaveTileName, av.x, av.y);

                return true;
            }
        }, fadeOut(.2f), removeActor(p)));

        stage.addActor(p);
    }
    
    private static AttackVector castSpellAttack(BaseMap combatMap, PartyMember attacker, Direction dir, int x, int y, int minDamage, int maxDamage, Spell spell) {

        List<AttackVector> path = Utils.getDirectionalActionPath(combatMap, dir.getMask(), x, y, 1, 11, true, true, false);

        AttackVector target = null;
        int distance = 1;
        for (AttackVector v : path) {
            AttackResult res = castAt(combatMap, v, attacker, minDamage, maxDamage, spell);
            target = v;
            target.result = res;
            target.distance = distance;
            if (res != AttackResult.NONE) {
                break;
            }
            distance++;
        }

        return target;
    }
    
    private static AttackResult castAt(BaseMap combatMap, AttackVector target, PartyMember attacker, int minDamage, int maxDamage, Spell spell) {

        AttackResult res = AttackResult.NONE;
        Creature creature = null;
        for (Creature c : combatMap.getCreatures()) {
            if (c.currentX == target.x && c.currentY == target.y) {
                creature = c;
                break;
            }
        }

        if (creature == null) {
            return res;
        }
                
        if (spell == Spell.FULGAR) {
            if ("fire".equals(creature.getResists())) {
                Sounds.play(Sound.EVADE);
                Exodus.hud.add("Resisted!\n");
                return AttackResult.MISS;
            }
        } 

        Sounds.play(Sound.NPC_STRUCK);
        
        int attackDamage = ((minDamage >= 0) && (minDamage < maxDamage))
                ? rand.nextInt((maxDamage + 1) - minDamage) + minDamage
                : maxDamage;
        
        dealDamage(attacker, creature, attackDamage);

        return AttackResult.HIT;
    }
    
    private static AttackResult attackAt(BaseMap combatMap, AttackVector target, PartyMember attacker, int range, int distance) {
        AttackResult res = AttackResult.NONE;
        Creature creature = null;
        for (Creature c : combatMap.getCreatures()) {
            if (c.currentX == target.x && c.currentY == target.y) {
                creature = c;
                break;
            }
        }

        WeaponType wt = attacker.getPlayer().weapon;
        boolean wrongRange = (wt.getWeapon().getAbsolute_range() > 0 && (distance != range));

        if (creature == null || wrongRange) {
            return res;
        }

        if (!attackHit(attacker, creature)) {
            Exodus.hud.add("Missed!\n");
            res = AttackResult.MISS;
        } else {
            Sounds.play(Sound.NPC_STRUCK);
            dealDamage(attacker, creature, attacker.getDamage());
            res = AttackResult.HIT;
        }

        return res;
    }

    //was used for TMX type dungeon maps
    public static Texture peerGem(TiledMapTileLayer layer, String[] ids, TextureAtlas atlas, int cx, int cy) throws Exception {
        FileTextureData d = (FileTextureData) (atlas.getRegions().first().getTexture().getTextureData());
        InputStream is = ClassLoader.class.getResourceAsStream("/assets/graphics/" + d.getFileHandle().file().getName());
        BufferedImage sheet = ImageIO.read(is);
        BufferedImage canvas = new BufferedImage(32 * layer.getWidth(), 32 * layer.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < layer.getHeight(); y++) {
            for (int x = 0; x < layer.getWidth(); x++) {
                String val = ids[layer.getCell(x, layer.getHeight() - y - 1).getTile().getId()];
                DungeonTile tile = DungeonTile.getTileByName(val);
                if (tile == null) {
                    val = "brick_floor";
                }
                if (x == cx && y == cy) {
                    val = "avatar";
                }
                TextureAtlas.AtlasRegion ar = (TextureAtlas.AtlasRegion) atlas.findRegion(val);
                BufferedImage sub = sheet.getSubimage(ar.getRegionX(), ar.getRegionY(), 32, 32);
                canvas.getGraphics().drawImage(sub, x * 32, y * 32, 32, 32, null);
            }
        }

        java.awt.Image tmp = canvas.getScaledInstance(20 * 32, 20 * 32, Image.SCALE_AREA_AVERAGING);
        BufferedImage scaledCanvas = new BufferedImage(20 * 32, 20 * 32, BufferedImage.TYPE_INT_ARGB);
        scaledCanvas.getGraphics().drawImage(tmp, 0, 0, null);

        Pixmap p = Utils.createPixmap(scaledCanvas.getWidth(), scaledCanvas.getHeight(), scaledCanvas, 0, 0);

        Texture t = new Texture(p);
        p.dispose();

        return t;
    }

    //used for telescope viewing or in towns
    public static Texture peerGem(Maps map, TextureAtlas atlas) throws Exception {

        Texture t = null;

        if (map.getMap().getType() == MapType.city) {
            FileTextureData d = (FileTextureData) (atlas.getRegions().first().getTexture().getTextureData());
            InputStream is = ClassLoader.class.getResourceAsStream("/assets/graphics/" + d.getFileHandle().file().getName());
            BufferedImage sheet = ImageIO.read(is);
            BufferedImage canvas = new BufferedImage(64 * 32, 64 * 32, BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < 64; y++) {
                for (int x = 0; x < 64; x++) {
                    Tile ct = map.getMap().getTile(x, y);
                    TextureAtlas.AtlasRegion ar = (TextureAtlas.AtlasRegion) atlas.findRegion(ct.getName());
                    BufferedImage sub = sheet.getSubimage(ar.getRegionX(), ar.getRegionY(), 32, 32);
                    canvas.getGraphics().drawImage(sub, x * 32, y * 32, 32, 32, null);

                    Person cr = map.getMap().getPersonAt(x, y);
                    if (cr != null) {
                        canvas.getGraphics().fillRect(x * 32, y * 32, 32, 32);
                    }

                    Drawable obj = map.getMap().getObjectAt(x, y);
                    if (obj != null) {
                        canvas.getGraphics().fillRect(x * 32, y * 32, 32, 32);
                    }
                }
            }

            java.awt.Image tmp = canvas.getScaledInstance(20 * 32, 20 * 32, Image.SCALE_AREA_AVERAGING);
            BufferedImage scaledCanvas = new BufferedImage(20 * 32, 20 * 32, BufferedImage.TYPE_INT_ARGB);
            scaledCanvas.getGraphics().drawImage(tmp, 0, 0, null);

            Pixmap p = createPixmap(
                    Exodus.SCREEN_WIDTH,
                    Exodus.SCREEN_HEIGHT,
                    scaledCanvas,
                    (Exodus.SCREEN_WIDTH - scaledCanvas.getWidth()) / 2,
                    (Exodus.SCREEN_HEIGHT - scaledCanvas.getHeight()) / 2);

            t = new Texture(p);
            p.dispose();

        } else if (map.getMap().getType() == MapType.dungeon) {
            //NO OP not needed since I added the minimap already on the HUD
        }

        return t;

    }

    //used for view gem on the world map only
    public static Texture peerGem(BaseMap worldMap, int avatarX, int avatarY, TextureAtlas atlas) throws Exception {
        FileTextureData d = (FileTextureData) (atlas.getRegions().first().getTexture().getTextureData());
        InputStream is = ClassLoader.class.getResourceAsStream("/assets/graphics/" + d.getFileHandle().file().getName());
        BufferedImage sheet = ImageIO.read(is);
        BufferedImage canvas = new BufferedImage(32 * 64, 32 * 64, BufferedImage.TYPE_INT_ARGB);

        int startX = avatarX - 32;
        int startY = avatarY - 32;
        int endX = avatarX + 32;
        int endY = avatarY + 32;
        int indexX = 0;
        int indexY = 0;
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int cx = x;
                if (x < 0) {
                    cx = 256 + x;
                } else if (x >= 256) {
                    cx = x - 256;
                }
                int cy = y;
                if (y < 0) {
                    cy = 256 + y;
                } else if (y >= 256) {
                    cy = y - 256;
                }
                Tile ct = worldMap.getTile(cx, cy);
                TextureAtlas.AtlasRegion ar = (TextureAtlas.AtlasRegion) atlas.findRegion(ct.getName());
                BufferedImage sub = sheet.getSubimage(ar.getRegionX(), ar.getRegionY(), 32, 32);
                canvas.getGraphics().drawImage(sub, indexX * 32, indexY * 32, 32, 32, null);

                Creature cr = worldMap.getCreatureAt(cx, cy);
                if (cr != null) {
                    canvas.getGraphics().fillRect(indexX * 32, indexY * 32, 32, 32);
                }

                Drawable obj = worldMap.getObjectAt(cx, cy);
                if (obj != null) {
                    canvas.getGraphics().fillRect(indexX * 32, indexY * 32, 32, 32);
                }

                indexX++;
            }
            indexX = 0;
            indexY++;
        }

        //add avatar in the middle
        canvas.getGraphics().fillRect((32 * 64) / 2, (32 * 64) / 2, 32, 32);

        java.awt.Image tmp = canvas.getScaledInstance(20 * 32, 20 * 32, Image.SCALE_AREA_AVERAGING);
        BufferedImage scaledCanvas = new BufferedImage(20 * 32, 20 * 32, BufferedImage.TYPE_INT_ARGB);
        scaledCanvas.getGraphics().drawImage(tmp, 0, 0, null);

        Pixmap p = createPixmap(
                Exodus.SCREEN_WIDTH,
                Exodus.SCREEN_HEIGHT,
                scaledCanvas,
                (Exodus.SCREEN_WIDTH - scaledCanvas.getWidth()) / 2,
                (Exodus.SCREEN_HEIGHT - scaledCanvas.getHeight()) / 2);

        Texture t = new Texture(p);
        p.dispose();
        return t;

    }

    public static Pixmap createPixmap(int width, int height, BufferedImage image, int sx, int sy) {

        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pix.setColor(0f, 0f, 0f, 1f);
        pix.fillRectangle(0, 0, width, height);

        int[] pixels = image.getRGB(0, 0, imgWidth, imgHeight, null, 0, width);

        for (int x = 0; x < imgWidth; x++) {
            for (int y = 0; y < imgHeight; y++) {
                int pixel = pixels[y * width + x];
                pix.drawPixel(sx + x, sy + y, getRGBA(pixel));
            }
        }

        return pix;
    }

    private static int getRGBA(int rgb) {
        int a = rgb >> 24;
        a &= 0x000000ff;
        int rest = rgb & 0x00ffffff;
        rest <<= 8;
        rest |= a;
        return rest;
    }

    public static Texture fillRectangle(int width, int height, Color color, float alpha) {
        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pix.setColor(color.r, color.g, color.b, alpha);
        pix.fillRectangle(0, 0, width, height);
        Texture t = new Texture(pix);
        pix.dispose();
        return t;
    }

}
