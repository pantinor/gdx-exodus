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
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import exodus.BaseScreen;
import exodus.Constants;
import exodus.Context;
import exodus.Exodus;
import exodus.Sound;
import exodus.Sounds;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import objects.BaseMap;
import objects.Creature;
import objects.Drawable;
import objects.Moongate;
import objects.Person;
import objects.PersonRole;
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
                        System.out.printf("no tile found: %d %d %d\n",x,y,tr.getId());
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

    public static AttackVector enemyfireCannon(Context context, Stage stage, BaseMap combatMap, Direction dir, int startX, int startY, int avatarX, int avatarY) throws PartyDeathException {

        List<AttackVector> path = Utils.getDirectionalActionPath(combatMap, dir.getMask(), startX, startY, 1, 4, true, false, true);

        AttackVector target = null;
        int distance = 1;
        for (AttackVector v : path) {
            AttackResult res = fireAt(context, stage, combatMap, v, false, avatarX, avatarY);
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

            boolean canAttackOverSolid = (tile != null && tile.getRule() != null
                    && tile.getRule().has(TileAttrib.halberdattackover) && weaponCanAttackThroughObjects);

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

    private static AttackResult fireAt(Context context, Stage stage, BaseMap combatMap, AttackVector target, boolean avatarAttack, int avatarX, int avatarY) throws PartyDeathException {

        AttackResult res = AttackResult.NONE;

        //check for ship
        Drawable ship = null;
        for (Actor a : stage.getActors()) {
            if (a instanceof Drawable) {
                Drawable d = (Drawable) a;
                if (d.getTile().getName().equals("ship") && d.getCx() == target.x && d.getCy() == target.y) {
                    ship = d;
                }
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

            if (context.getTransportContext() == TransportContext.SHIP) {
                context.damageShip(-1, 10);
            } else {
                //context.getParty().damageParty(10, 25);
            }

            res = AttackResult.HIT;
        }

        return res;
    }
}
