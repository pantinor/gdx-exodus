package util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
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
import objects.Person;
import objects.ProjectileActor;
import objects.Tile;
import objects.TileSet;
import org.apache.commons.io.IOUtils;

public class Utils implements Constants {

    public static Random rand = new XORShiftRandom();

    public static String properCase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

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

        InputStream is = Utils.class.getResourceAsStream("/assets/data/" + fname);
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
                        tile = ts.getTileByIndex(36);
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

            int pos = 0x00;
            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    int index = bytes[pos] & 0xff;
                    pos++;
                    Tile tile = ts.getTileByIndex(index);
                    if (tile == null) {
                        tile = ts.getTileByIndex(36);
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
                        tile = ts.getTileByIndex(36);
                    }
                    if (tile.getIndex() == 63) { //avatar position
                        tile = ts.getTileByIndex(4);
                    }
                    tiles[x + y * map.getWidth()] = tile;
                }
            }
        }

        map.setTiles(tiles);

    }

    public static void setPeople(BaseMap map, Tile[] tiles, byte[] bytes, TileSet ts) {
        List<Person> people = new ArrayList<>();

        int pos = 0x1180;
        for (int x = 0; x < 32; x++) {
            int index = (bytes[pos] & 0xff) / 4;
            pos++;
            Tile tile = ts.getTileByIndex(index);
            if (tile == null) {
                tile = ts.getTileByIndex(36);
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
                tile = ts.getTileByIndex(36);
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

        Iterator<Person> iter = people.iterator();
        while (iter.hasNext()) {
            Person p = iter.next();
            if (p.getId() == 0 && p.getX() == 0 && p.getY() == 0 && p.getTile().getName().equals("water")) {
                iter.remove();//remove null person objects
            }
        }

        map.setPeople(people);
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
                        if (av.impactedCreature != null && !"whirlpool".equals(av.impactedCreature.getTile())) {
                            map.removeCreature(av.impactedCreature);
                        }
                        if (av.impactedDrawable != null && av.impactedDrawable.getShipHull() <= 0) {
                            map.removeObject(av.impactedDrawable);
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

        Drawable emptyShip = null;
        for (Drawable d : objects) {
            if (d.getTile().getName().equals("frigate") && d.getCx() == target.x && d.getCy() == target.y) {
                emptyShip = d;
            }
        }

        if (emptyShip != null) {
            emptyShip.damageShip(10);
            target.impactedDrawable = emptyShip;
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

            if (creature == null || "whirlpool".equals(creature.getTile())) {
                return res;
            }

            if (rand.nextInt(3) == 0) {
                res = AttackResult.HIT;
                target.impactedCreature = creature;
            } else {
                res = AttackResult.MISS;
            }

        } else if (target.x == avatarX && target.y == avatarY) {

            if (context.getTransport() == Transport.SHIP) {
                if (rand.nextInt(3) == 0) {
                    context.damageShip(10);
                    res = AttackResult.HIT;
                } else {
                    res = AttackResult.MISS;
                }
            } else {
                if (rand.nextInt(3) == 0) {
                    context.getParty().damageParty(10, 25);
                    res = AttackResult.HIT;
                } else {
                    res = AttackResult.MISS;
                }
            }
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

        if (!"lord_british".equals(cr.getTile())) {
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

        boolean magicHit = attacker.getPlayer().weapon.getWeapon().getHittile().equals("magic");

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

                //scr.replaceTile(av.leaveTileName, av.x, av.y);
                scr.finishPlayerTurn();

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

    //used for telescope viewing or in towns
    public static Texture peerGem(Maps map, TextureAtlas atlas) throws Exception {

        Texture t = null;

        if (map.getMap().getType() == MapType.city) {
            FileTextureData d = (FileTextureData) (atlas.getRegions().first().getTexture().getTextureData());
            InputStream is = Utils.class.getResourceAsStream("/assets/graphics/" + d.getFileHandle().file().getName());
            byte[] bytes = IOUtils.toByteArray(is);
            Pixmap sheet = new Pixmap(bytes, 0, bytes.length);

            int peerGemTiles = 64; // 64x64 tiles in peer view
            int dstTileSize = 10;  // each tile drawn as 10x10
            int peerGemSize = peerGemTiles * dstTileSize; // 640x640 canvas

            int offsetX = (Exodus.SCREEN_WIDTH - peerGemSize) / 2;
            int offsetY = (Exodus.SCREEN_HEIGHT - peerGemSize) / 2;

            Pixmap p = new Pixmap(Exodus.SCREEN_WIDTH, Exodus.SCREEN_HEIGHT, Pixmap.Format.RGBA8888);
            p.setFilter(Pixmap.Filter.BiLinear);
            p.setColor(Color.BLACK);
            p.fill();

            for (int y = 0; y < peerGemTiles; y++) {
                for (int x = 0; x < peerGemTiles; x++) {
                    Tile ct = map.getMap().getTile(x, y);
                    TextureAtlas.AtlasRegion ar = (TextureAtlas.AtlasRegion) atlas.findRegion(ct.getName());

                    p.drawPixmap(
                            sheet,
                            ar.getRegionX(),
                            ar.getRegionY(),
                            TILE_DIM,
                            TILE_DIM,
                            offsetX + x * dstTileSize,
                            offsetY + y * dstTileSize,
                            dstTileSize,
                            dstTileSize);

                    Person cr = map.getMap().getPersonAt(x, y);
                    if (cr != null) {
                        p.setColor(Color.WHITE);
                        p.fillRectangle(
                                offsetX + x * dstTileSize,
                                offsetY + y * dstTileSize,
                                dstTileSize,
                                dstTileSize);
                    }

                    Drawable obj = map.getMap().getObjectAt(x, y);
                    if (obj != null) {
                        p.setColor(Color.WHITE);
                        p.fillRectangle(
                                offsetX + x * dstTileSize,
                                offsetY + y * dstTileSize,
                                dstTileSize,
                                dstTileSize);
                    }
                }
            }

            t = new Texture(p);
            p.dispose();
            sheet.dispose();

        } else if (map.getMap().getType() == MapType.dungeon) {
            //NO OP not needed since I added the minimap already on the HUD
        }

        return t;
    }

    //used for view gem on the world map only
    public static Texture peerGem(BaseMap worldMap, int avatarX, int avatarY, TextureAtlas atlas) throws Exception {
        FileTextureData d = (FileTextureData) (atlas.getRegions().first().getTexture().getTextureData());
        InputStream is = Utils.class.getResourceAsStream("/assets/graphics/" + d.getFileHandle().file().getName());
        byte[] bytes = IOUtils.toByteArray(is);
        Pixmap sheet = new Pixmap(bytes, 0, bytes.length);

        int peerGemTiles = 32; //32x32 tiles in peer view
        int peerGemSize = 512; //image canvas will be 512x512 pixels wide
        int dstTileSize = peerGemSize / peerGemTiles;

        int mapWidth = worldMap.getWidth();
        int mapHeight = worldMap.getHeight();

        int offsetX = (Exodus.SCREEN_WIDTH - peerGemSize) / 2;
        int offsetY = (Exodus.SCREEN_HEIGHT - peerGemSize) / 2;

        Pixmap p = new Pixmap(Exodus.SCREEN_WIDTH, Exodus.SCREEN_HEIGHT, Pixmap.Format.RGBA8888);
        p.setFilter(Pixmap.Filter.BiLinear);
        p.setColor(Color.BLACK);
        p.fill();

        int startX = avatarX - TILE_DIM;
        int startY = avatarY - TILE_DIM;
        int endX = avatarX + TILE_DIM;
        int endY = avatarY + TILE_DIM;

        int indexX = 0;
        int indexY = 0;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int cx = ((x % mapWidth) + mapWidth) % mapWidth;
                int cy = ((y % mapHeight) + mapHeight) % mapHeight;

                Tile ct = worldMap.getTile(cx, cy);
                TextureAtlas.AtlasRegion ar = (TextureAtlas.AtlasRegion) atlas.findRegion(ct.getName());

                p.drawPixmap(
                        sheet,
                        ar.getRegionX(),
                        ar.getRegionY(),
                        TILE_DIM,
                        TILE_DIM,
                        offsetX + indexX * dstTileSize,
                        offsetY + indexY * dstTileSize,
                        dstTileSize,
                        dstTileSize);

                Creature cr = worldMap.getCreatureAt(cx, cy);
                if (cr != null) {
                    p.setColor(Color.WHITE);
                    p.fillRectangle(
                            offsetX + indexX * dstTileSize,
                            offsetY + indexY * dstTileSize,
                            dstTileSize,
                            dstTileSize);
                }

                Drawable obj = worldMap.getObjectAt(cx, cy);
                if (obj != null) {
                    p.setColor(Color.WHITE);
                    p.fillRectangle(
                            offsetX + indexX * dstTileSize,
                            offsetY + indexY * dstTileSize,
                            dstTileSize,
                            dstTileSize);
                }

                indexX++;
            }

            indexX = 0;
            indexY++;
        }

        //add avatar in the middle
        p.setColor(Color.WHITE);
        p.fillRectangle(
                offsetX + (peerGemTiles / 2) * dstTileSize,
                offsetY + (peerGemTiles / 2) * dstTileSize,
                dstTileSize,
                dstTileSize);

        Texture t = new Texture(p);
        p.dispose();
        sheet.dispose();

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
