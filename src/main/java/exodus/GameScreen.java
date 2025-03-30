package exodus;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import objects.BaseMap;
import objects.Creature;
import objects.Drawable;
import objects.Moongate;
import objects.Portal;
import objects.SaveGame;
import objects.Tile;

import util.UltimaMapRenderer;
import util.UltimaTiledMapLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import static exodus.Constants.tilePixelHeight;
import static exodus.Constants.tilePixelWidth;
import exodus.Exodus.ExplosionDrawable;
import exodus.Party.PartyMember;
import java.util.Iterator;
import util.PartyDeathException;
import util.Utils;

public class GameScreen extends BaseScreen {

    TextureAtlas moonAtlas;

    public static Animation<TextureRegion> mainAvatar;
    public static Animation<TextureRegion> avatarAnim;
    public static Animation<TextureRegion> corpseAnim;
    public static Animation<TextureRegion> horseAnim;
    public static Animation<TextureRegion> shipAnim;
    public static int avatarDirection = Direction.WEST.getVal();

    TiledMap map;
    UltimaMapRenderer renderer;
    Batch mapBatch, batch;

    public Stage projectilesStage;

    private final Viewport mapViewPort;

    Array<AtlasRegion> moongateTextures = new Array<>();
    public static int phase = 0, trammelphase = 0, trammelSubphase = 0, feluccaphase = 0;

    public SecondaryInputProcessor sip;

    public GameTimer gameTimer = new GameTimer();
    public ExplosionsTimer explosionsTimer = new ExplosionsTimer();

    private static final float FOV_RADIUS = 20f;

    public GameScreen() {

        scType = ScreenType.MAIN;

        initTransportAnimations();
        mainAvatar = avatarAnim;;

        moongateTextures = Exodus.standardAtlas.findRegions("moongate");
        moonAtlas = new TextureAtlas(Gdx.files.classpath("assets/graphics/moon-atlas.txt"));

        batch = new SpriteBatch();

        stage = new Stage(viewport);

        camera = new OrthographicCamera(Exodus.MAP_WIDTH, Exodus.MAP_HEIGHT);

        mapViewPort = new ScreenViewport(camera);

        projectilesStage = new Stage(mapViewPort);

        sip = new SecondaryInputProcessor(this, stage);

        SequenceAction seq1 = Actions.action(SequenceAction.class);
        seq1.addAction(Actions.delay(.25f));
        seq1.addAction(Actions.run(gameTimer));
        stage.addAction(Actions.forever(seq1));

        SequenceAction seq2 = Actions.action(SequenceAction.class);
        seq2.addAction(Actions.delay(2f));
        seq2.addAction(Actions.run(explosionsTimer));
        projectilesStage.addAction(Actions.forever(seq2));

        //add 2 whirlpools manually, they do not spawn automatically
        Creature wp = Exodus.creatures.getInstance(CreatureType.whirlpool, Exodus.standardAtlas);
        wp.currentX = 200;
        wp.currentY = 53;
        Maps.SOSARIA.getMap().addCreature(wp);

        wp = Exodus.creatures.getInstance(CreatureType.whirlpool, Exodus.standardAtlas);
        wp.currentX = 69;
        wp.currentY = 194;
        Maps.SOSARIA.getMap().addCreature(wp);

        addButtons();

    }

    private void initTransportAnimations() {

        Array<AtlasRegion> avatar = Exodus.standardAtlas.findRegions("avatar");
        Array<AtlasRegion> corps = Exodus.standardAtlas.findRegions("corpse");
        Array<AtlasRegion> horse = Exodus.standardAtlas.findRegions("horse");
        Array<AtlasRegion> ship = Exodus.standardAtlas.findRegions("ship");

        avatarAnim = new Animation<>(1f, avatar);
        shipAnim = new Animation<>(1f, ship);

        AtlasRegion[] tmp = new AtlasRegion[4];
        for (int i = 0; i < 4; i++) {
            tmp[i] = corps.get(0);
        }
        corpseAnim = new Animation<>(1f, tmp);

        AtlasRegion[] tmp3 = new AtlasRegion[4];
        AtlasRegion ar = new AtlasRegion(horse.get(0));
        ar.flip(true, false);
        tmp3[0] = horse.get(0);
        tmp3[1] = horse.get(0);
        tmp3[2] = ar;
        tmp3[3] = horse.get(0);
        horseAnim = new Animation<>(1f, tmp3);

    }

    public class GameTimer implements Runnable {

        public boolean active = true;

        @Override
        public void run() {
            if (active) {

                updateMoons(true);

                if (System.currentTimeMillis() - context.getLastCommandTime() > 20 * 1000) {
                    keyUp(Keys.SPACE);
                }
            }
        }
    }

    public class ExplosionsTimer implements Runnable {

        public boolean active = false;

        @Override
        public void run() {
            if (active) {
                Vector3 ac = getCurrentMapCoords();
                int dx = Utils.getRandomBetween((int) ac.x - 9, (int) ac.x + 9);
                int dy = Utils.getRandomBetween((int) ac.y - 9, (int) ac.y + 9);
                if (dx < 12) {
                    return;
                } else if (dx > 52) {
                    return;
                }
                if (dy < 12) {
                    return;
                } else if (dy > 52) {
                    return;
                }
                Actor d = new ExplosionDrawable();
                Vector3 v = getMapPixelCoords(dx, dy);
                d.setX(v.x);
                d.setY(v.y);

                SequenceAction seq = Actions.action(SequenceAction.class);
                seq.addAction(Actions.run(new AddActorAction(projectilesStage, d)));
                if (Math.abs(ac.x - dx) < 2 && Math.abs(ac.y - dy) < 2) {
                    seq.addAction(Actions.run(new PlaySoundAction(Sound.BOOM)));
                    try {
                        context.getParty().applyEffect(TileEffect.FIRE);
                    } catch (PartyDeathException ex) {
                        partyDeath();
                    }
                }
                seq.addAction(Actions.delay(2f));
                seq.addAction(Actions.removeActor(d));

                projectilesStage.addAction(seq);
            }
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
        gameTimer.active = true;

        //load save game if initializing
        if (context == null) {
            context = new Context();
            SaveGame sg = new SaveGame();
            try {
                sg.read(PARTY_SAV_BASE_FILENAME);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Party party = new Party(sg);
            context.setParty(party);

//            for (PartyMember pm : party.getMembers()) {
//                pm.getPlayer().torches = 5;
//                pm.getPlayer().keys = 50;
//                pm.getPlayer().gems = 50;
//                
//                pm.getPlayer().weapons[1] = 10;
//                pm.getPlayer().weapons[2] = 5;
//                pm.getPlayer().weapons[3] = 5;
//
//                pm.getPlayer().armors[3] = 5;
//                pm.getPlayer().armors[4] = 5;
//                pm.getPlayer().armors[5] = 5;
//
//
//                pm.getPlayer().marks[0] = 1;
//                pm.getPlayer().marks[1] = 1;
//                pm.getPlayer().marks[2] = 1;
//                pm.getPlayer().marks[3] = 1;
//
//                pm.getPlayer().cards[0] = 1;
//                pm.getPlayer().cards[1] = 1;
//                pm.getPlayer().cards[2] = 1;
//                pm.getPlayer().cards[3] = 1;
//                pm.getPlayer().weapon = WeaponType.EXOTIC;
//                pm.getPlayer().armor = ArmorType.EXOTIC;
//
//                pm.getPlayer().health = 500;
//                pm.getPlayer().exp = 350;
//                pm.getPlayer().intell = 75;
//                pm.getPlayer().wis = 75;
//                pm.getPlayer().mana = 75;
//                break;
//            }
            //load the surface world first
            loadNextMap(Maps.SOSARIA, sg.partyX, sg.partyY);
            //loadNextMap(Maps.SOSARIA, 40, 212);
            //sg.transport = Transport.SHIP.ordinal();

            //load the dungeon if save game starts in dungeon
            if (Maps.get(sg.location) != Maps.SOSARIA) {
                Portal p = Maps.SOSARIA.getMap().getPortal(sg.location);
                newMapPixelCoords = getMapPixelCoords(p.getX(), p.getY());
                recalcFOV(context.getCurrentMap(), p.getX(), p.getY());
                loadNextMap(Maps.get(sg.location), sg.partyX, sg.partyY, sg.partyX, sg.partyY, sg.dnglevel, Direction.getByValue(sg.orientation), true);
                //loadNextMap(Maps.DOOM, 0, 0, 3, 1, 1, Direction.WEST, true);
                //loadNextMap(Maps.DESTARD, 0, 0, 3, 5, 3, Direction.SOUTH, true);
                //loadNextMap(Maps.DELVE_SORROWS, 0, 0, 3, 19, 1, Direction.EAST, true);
            }

            context.setTransport(Transport.values()[sg.transport]);
            mainAvatar = avatarAnim;
            if (sg.transport == Transport.SHIP.ordinal()) {
                mainAvatar = shipAnim;
            } else if (sg.transport == Transport.HORSE.ordinal()) {
                mainAvatar = horseAnim;
            }

            //load objects to surface stage
            for (int i = 0; i < 24; i++) {
                if (sg.objects_save_tileids[i] != 0 && sg.objects_save_x[i] != 0 && sg.objects_save_y[i] != 0) {
                    Tile t = Exodus.baseTileSet.getTileByIndex(sg.objects_save_tileids[i] & 0xff);
                    Maps.SOSARIA.getMap().addObject(t, sg.objects_save_x[i] & 0xff, sg.objects_save_y[i] & 0xff);
                }
            }
            //load monsters to surface map
            for (int i = 0; i < 8; i++) {
                if (sg.monster_save_tileids[i] != 0 && sg.monster_save_x[i] != 0 && sg.monster_save_y[i] != 0) {
                    Tile t = Exodus.baseTileSet.getTileByIndex(sg.monster_save_tileids[i] & 0xff);
                    Creature cr = Exodus.creatures.getInstance(CreatureType.get(t.getName()), Exodus.standardAtlas);
                    cr.currentX = sg.monster_save_x[i] & 0xff;
                    cr.currentY = sg.monster_save_y[i] & 0xff;
                    cr.currentPos = getMapPixelCoords(cr.currentX, cr.currentY);
                    Maps.SOSARIA.getMap().addCreature(cr);
                }
            }

            //set exodus cards insertion status
            if ((sg.exodusCardsStatus & 0x1) > 0) {
                Maps.EXODUS.getMap().setTile(Exodus.baseTileSet.getTileByName("brick_floor"), 30, 12);
            }
            if ((sg.exodusCardsStatus & 0x2) > 0) {
                Maps.EXODUS.getMap().setTile(Exodus.baseTileSet.getTileByName("brick_floor"), 31, 12);
            }
            if ((sg.exodusCardsStatus & 0x4) > 0) {
                Maps.EXODUS.getMap().setTile(Exodus.baseTileSet.getTileByName("brick_floor"), 32, 12);
            }
            if ((sg.exodusCardsStatus & 0x8) > 0) {
                Maps.EXODUS.getMap().setTile(Exodus.baseTileSet.getTileByName("brick_floor"), 33, 12);
            }
        }

        Maps contextMap = Maps.get(context.getCurrentMap().getId());
        if (contextMap == Maps.EXODUS) {
            explosionsTimer.active = true;
        } else {
            explosionsTimer.active = false;
        }
    }

    @Override
    public void hide() {
        gameTimer.active = false;
        explosionsTimer.active = false;
    }

    public void loadNextMap(Maps m, int x, int y) {
        loadNextMap(m, x, y, 0, 0, 0, null, false);
    }

    public void loadNextMap(Maps m, int x, int y, int dngx, int dngy, int dngLevel, Direction orientation, boolean restoreSG) {

        log("Entering " + m.getLabel() + "!");

        BaseMap baseMap = m.getMap();

        if (baseMap.getType() == MapType.dungeon) {

            DungeonScreen sc = new DungeonScreen(this, context, m);
            if (restoreSG) {
                sc.restoreSaveGameLocation(dngx, dngy, dngLevel, orientation);
            }
            Exodus.mainGame.setScreen(sc);

        } else if (baseMap.getType() == MapType.shrine) {

            map = new UltimaTiledMapLoader(m, Exodus.standardAtlas, baseMap.getWidth(), baseMap.getHeight(), tilePixelWidth, tilePixelHeight).load();
            context.setCurrentTiledMap(map);
            ShrineScreen sc = new ShrineScreen(m, this, context.getParty(), map, Exodus.standardAtlas, Exodus.standardAtlas);
            Exodus.mainGame.setScreen(sc);

        } else {

            context.setCurrentMap(baseMap);

            map = new UltimaTiledMapLoader(m, Exodus.standardAtlas, m.getMap().getWidth(), m.getMap().getHeight(), tilePixelWidth, tilePixelHeight).load();
            context.setCurrentTiledMap(map);

            if (renderer != null) {
                renderer.dispose();
            }
            renderer = new UltimaMapRenderer(context, Exodus.standardAtlas, baseMap, map, 1f);

            mapBatch = renderer.getBatch();

            MapProperties prop = map.getProperties();
            mapPixelHeight = prop.get("height", Integer.class) * tilePixelHeight;

            baseMap.initObjects(this, Exodus.standardAtlas, Exodus.standardAtlas);

            renderer.getFOV().calculateFOV(x, y, FOV_RADIUS);
            newMapPixelCoords = getMapPixelCoords(x, y);

            Maps contextMap = Maps.get(context.getCurrentMap().getId());
            if (contextMap == Maps.EXODUS) {
                explosionsTimer.active = true;
            } else {
                explosionsTimer.active = false;
            }
        }

        if (Exodus.playMusic) {
            if (Exodus.music != null) {
                Exodus.music.stop();
            }
            Sound snd = Sound.valueOf(baseMap.getMusic());
            Exodus.music = Sounds.play(snd, Exodus.musicVolume);
        }

    }

    public void recalcFOV(BaseMap bm, int x, int y) {
        renderer.getFOV().calculateFOV(x, y, FOV_RADIUS);
    }

    public void attackAt(Maps combat, Creature cr) {

        Maps contextMap = Maps.get(context.getCurrentMap().getId());
        BaseMap combatMap = combat.getMap();

        TiledMap tmap = new UltimaTiledMapLoader(combat, Exodus.standardAtlas, combat.getMap().getWidth(), combat.getMap().getHeight(), tilePixelWidth, tilePixelHeight).load();

        CombatScreen sc = new CombatScreen(this, context, contextMap, combatMap, tmap, cr.getTile(), Exodus.creatures, Exodus.standardAtlas);
        Exodus.mainGame.setScreen(sc);

        currentEncounter = cr;
    }

    @Override
    public void endCombat(boolean isWon, BaseMap combatMap, boolean wounded) {

        Exodus.mainGame.setScreen(this);

        if (currentEncounter != null) {

            Tile tile = context.getCurrentMap().getTile(currentEncounter.currentX, currentEncounter.currentY);

            if (isWon) {

                log("Victory!");

                TileRule r = tile.getRule();

                /* add a chest, if the creature leaves one */
                if (!currentEncounter.getNochest() && (r == null || !r.has(TileAttrib.unwalkable))) {
                    Tile ct = Exodus.baseTileSet.getTileByName("chest");
                    context.getCurrentMap().addObject(ct, currentEncounter.currentX, currentEncounter.currentY);
                } else if (currentEncounter.getTile() == CreatureType.pirate_ship) {
                    /* add a ship if you just defeated a pirate ship */
                    Tile st = Exodus.baseTileSet.getTileByName("ship");
                    context.getCurrentMap().addObject(st, currentEncounter.currentX, currentEncounter.currentY);
                }
            } else if (context.getParty().didAnyoneFlee()) {
                log("Battle is lost!");
            } else if (!context.getParty().isAnyoneAlive()) {
                partyDeath();
            }

            context.getCurrentMap().removeCreature(currentEncounter);

            currentEncounter = null;

        }
    }

    @Override
    public void partyDeath() {
        Exodus.mainGame.setScreen(Exodus.startScreen);
    }

    @Override
    public Vector3 getMapPixelCoords(int x, int y) {
        Vector3 v = new Vector3(x * tilePixelWidth, mapPixelHeight - y * tilePixelHeight - tilePixelHeight, 0);
        return v;
    }

    @Override
    public Vector3 getCurrentMapCoords() {
        Vector3 v = camera.unproject(new Vector3(tilePixelWidth * 12, tilePixelHeight * 12, 0), 32, 64, Exodus.MAP_WIDTH, Exodus.MAP_HEIGHT);
        return new Vector3(Math.round(v.x / tilePixelWidth) - 6, (mapPixelHeight - Math.round(v.y) - tilePixelHeight) / tilePixelHeight, 0);
    }

    @Override
    public void render(float delta) {

        time += delta;

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (renderer == null) {
            return;
        }

        camera.position.set(newMapPixelCoords.x + 5 * tilePixelWidth, newMapPixelCoords.y, 0);

        camera.update();

        renderer.setView(camera.combined,
                camera.position.x - tilePixelWidth * 15, //this is voodoo
                camera.position.y - tilePixelHeight * 10,
                Exodus.MAP_WIDTH,
                Exodus.MAP_HEIGHT);

        renderer.render();

        mapBatch.begin();

        if (context.getCurrentMap().getMoongates() != null) {
            for (Moongate g : context.getCurrentMap().getMoongates()) {
                TextureRegion t = g.getCurrentTexture();
                if (t != null) {
                    Vector3 v = getMapPixelCoords(g.getX(), g.getY());
                    mapBatch.draw(t, v.x, v.y);
                }
            }
        }
        mapBatch.end();

        batch.begin();

        batch.draw(Exodus.backGround, 0, 0);

        batch.draw(mainAvatar.getKeyFrames()[avatarDirection], tilePixelWidth * 11, tilePixelHeight * 12);

        Exodus.hud.render(batch, context.getParty());

        Exodus.font.setColor(Color.WHITE);
        if (showZstats > 0) {
            context.getParty().renderZstats(showZstats, Exodus.font, batch, Exodus.SCREEN_HEIGHT);
        }

        if (context.getCurrentMap().getId() == Maps.SOSARIA.getId()) {
            batch.draw(moonAtlas.findRegion("phase_" + trammelphase), 360, Exodus.SCREEN_HEIGHT - 25, 25, 25);
            batch.draw(moonAtlas.findRegion("phase_" + feluccaphase), 380, Exodus.SCREEN_HEIGHT - 25, 25, 25);
            Exodus.font.draw(batch, "Wind " + context.getWindDirection().toString(), 415, Exodus.SCREEN_HEIGHT - 7);
        }

        if (context.getAura().getType() != AuraType.NONE) {
            Exodus.font.draw(batch, context.getAura().getType().toString(), 200, Exodus.SCREEN_HEIGHT - 32);
        }
        batch.end();

        projectilesStage.act();
        projectilesStage.draw();

        stage.act();
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        mapViewPort.update(width, height, false);
    }

    @Override
    public boolean keyUp(int keycode) {

        context.setLastCommandTime(System.currentTimeMillis());

        Vector3 v = getCurrentMapCoords();
        Tile ct = context.getCurrentMap().getTile(v);

        if (keycode == Keys.UP) {
            if (context.getTransport() == Transport.SHIP && avatarDirection + 1 != Direction.NORTH.getVal()) {
                avatarDirection = Direction.NORTH.getVal() - 1;
                finishTurn((int) v.x, (int) v.y);
                return false;
            }
            if (!preMove(v, Direction.NORTH)) {
                return false;
            }
            if (newMapPixelCoords.y + tilePixelHeight >= context.getCurrentMap().getHeight() * tilePixelHeight) {
                newMapPixelCoords.y = 0;
                postMove(Direction.NORTH, (int) v.x, context.getCurrentMap().getHeight() - 1);
            } else {
                newMapPixelCoords.y = newMapPixelCoords.y + tilePixelHeight;
                postMove(Direction.NORTH, (int) v.x, (int) v.y - 1);
            }
            avatarDirection = Direction.NORTH.getVal() - 1;
        } else if (keycode == Keys.RIGHT) {
            if (context.getTransport() == Transport.SHIP && avatarDirection + 1 != Direction.EAST.getVal()) {
                avatarDirection = Direction.EAST.getVal() - 1;
                finishTurn((int) v.x, (int) v.y);
                return false;
            }
            if (!preMove(v, Direction.EAST)) {
                return false;
            }
            if (newMapPixelCoords.x + tilePixelWidth >= context.getCurrentMap().getWidth() * tilePixelWidth) {
                newMapPixelCoords.x = 0;
                postMove(Direction.EAST, 0, (int) v.y);
            } else {
                newMapPixelCoords.x = newMapPixelCoords.x + tilePixelWidth;
                postMove(Direction.EAST, (int) v.x + 1, (int) v.y);
            }
            avatarDirection = Direction.EAST.getVal() - 1;
        } else if (keycode == Keys.LEFT) {
            if (context.getTransport() == Transport.SHIP && avatarDirection + 1 != Direction.WEST.getVal()) {
                avatarDirection = Direction.WEST.getVal() - 1;
                finishTurn((int) v.x, (int) v.y);
                return false;
            }
            if (!preMove(v, Direction.WEST)) {
                return false;
            }
            if (newMapPixelCoords.x - tilePixelWidth < 0) {
                newMapPixelCoords.x = (context.getCurrentMap().getWidth() - 1) * tilePixelWidth;
                postMove(Direction.WEST, context.getCurrentMap().getWidth() - 1, (int) v.y);
            } else {
                newMapPixelCoords.x = newMapPixelCoords.x - tilePixelWidth;
                postMove(Direction.WEST, (int) v.x - 1, (int) v.y);
            }
            avatarDirection = Direction.WEST.getVal() - 1;
        } else if (keycode == Keys.DOWN) {
            if (context.getTransport() == Transport.SHIP && avatarDirection + 1 != Direction.SOUTH.getVal()) {
                avatarDirection = Direction.SOUTH.getVal() - 1;
                finishTurn((int) v.x, (int) v.y);
                return false;
            }
            if (!preMove(v, Direction.SOUTH)) {
                return false;
            }
            if (newMapPixelCoords.y - tilePixelHeight < 0) {
                newMapPixelCoords.y = (context.getCurrentMap().getHeight() - 1) * tilePixelHeight;
                postMove(Direction.SOUTH, (int) v.x, 0);
            } else {
                newMapPixelCoords.y = newMapPixelCoords.y - tilePixelHeight;
                postMove(Direction.SOUTH, (int) v.x, (int) v.y + 1);
            }
            avatarDirection = Direction.SOUTH.getVal() - 1;

        } else if (keycode == Keys.F && context.getTransport() == Transport.SHIP) {

            log("Fire Cannon > ");
            ShipInputAdapter sia = new ShipInputAdapter(v);
            Gdx.input.setInputProcessor(sia);
            return false;

        } else if (keycode == Keys.E) {

            Portal p = context.getCurrentMap().getPortal(v.x, v.y, 0);
            if (p != null) {

                Maps dest = Maps.get(p.getDestmapid());
                if (p.getDestmapid() == Maps.DAWN.getId()) {
                    if (trammelphase == 0 && feluccaphase == 0) {
                        loadNextMap(dest, p.getStartx(), p.getStarty());
                    }
                } else if (p.getDestmapid() != context.getCurrentMap().getId()) {
                    loadNextMap(dest, p.getStartx(), p.getStarty());
                } else {
                    newMapPixelCoords = getMapPixelCoords(p.getStartx(), p.getStarty());
                    recalcFOV(context.getCurrentMap(), p.getStartx(), p.getStarty());
                }
                return false;

            }
        } else if (keycode == Keys.Q) {
            if (context.getCurrentMap().getId() == Maps.SOSARIA.getId()) {
                context.saveGame(v.x, v.y, 0, null, Maps.SOSARIA);
                log("Saved Game.");
            } else {
                log("Cannot save here!");
            }
        } else if (keycode == Keys.Z) {
            showZstats = showZstats + 1;
            if (showZstats >= STATS_PLAYER1 && showZstats <= STATS_PLAYER4) {
                if (showZstats > context.getParty().getMembers().size()) {
                    showZstats = STATS_PLAYER4;
                }
            }
            if (showZstats > STATS_PLAYER4) {
                showZstats = STATS_NONE;
            }
        } else if (keycode == Keys.H) {

            new HandDialog(this, this.context).show(stage);

        } else if (keycode == Keys.B) {

            board((int) v.x, (int) v.y);

        } else if (keycode == Keys.X) {

            if (context.getTransport() == Transport.SHIP) {
                Tile st = Exodus.baseTileSet.getTileByName("ship");
                Drawable ship = context.getCurrentMap().addObject(st, (int) v.x, (int) v.y);
                context.setLastShip(ship);
            } else if (context.getTransport() == Transport.HORSE) {
                Creature cr = Exodus.creatures.getInstance(CreatureType.horse, Exodus.standardAtlas);
                cr.currentX = (int) v.x;
                cr.currentY = (int) v.y;
                context.getCurrentMap().addCreature(cr);
            }

            context.setTransport(Transport.FOOT);
            mainAvatar = avatarAnim;

        } else if (keycode == Keys.M) {
            log("Modify Order:");
            log("exhange #:");
            NewOrderInputAdapter noia = new NewOrderInputAdapter(this);
            Gdx.input.setInputProcessor(noia);
            return false;
        } else if (keycode == Keys.P) {
            peerGem();
        } else if (keycode == Keys.N) {
            negateTime();
        } else if (keycode == Keys.T || keycode == Keys.J || keycode == Keys.S || keycode == Keys.Y || keycode == Keys.U || keycode == Keys.O
                || keycode == Keys.A || keycode == Keys.G || keycode == Keys.R || keycode == Keys.W || keycode == Keys.C || keycode == Keys.L) {

            Gdx.input.setInputProcessor(sip);
            sip.setinitialKeyCode(keycode, context.getCurrentMap(), (int) v.x, (int) v.y);
            return false;

        } else if (keycode == Keys.V) {

            Exodus.playMusic = !Exodus.playMusic;
            if (Exodus.playMusic) {
                Exodus.music.play();
            } else {
                Exodus.music.stop();
            }

        } else if (keycode == Keys.SPACE) {
            log("Pass");
        }

        finishTurn((int) v.x, (int) v.y);

        return false;

    }

    private boolean preMove(Vector3 currentTile, Direction dir) {

        int nx = (int) currentTile.x;
        int ny = (int) currentTile.y;

        if (dir == Direction.NORTH) {
            ny = (int) currentTile.y - 1;
        }
        if (dir == Direction.SOUTH) {
            ny = (int) currentTile.y + 1;
        }
        if (dir == Direction.WEST) {
            nx = (int) currentTile.x - 1;
        }
        if (dir == Direction.EAST) {
            nx = (int) currentTile.x + 1;
        }

        BaseMap bm = context.getCurrentMap();
        if (bm.getBorderbehavior() == MapBorderBehavior.exit) {
            if (nx > bm.getWidth() - 1 || nx < 0 || ny > bm.getHeight() - 1 || ny < 0) {
                Portal p = Maps.SOSARIA.getMap().getPortal(bm.getId());
                loadNextMap(Maps.SOSARIA, p.getX(), p.getY());
                return false;
            }
        }

        if (context.getCurrentMap().getId() == Maps.AMBROSIA.getId()) {
            Portal p = Maps.AMBROSIA.getMap().getPortal(nx, ny, 0);
            if (p != null && p.getName().equals("WHIRLPOOL")) {
                int dx = Utils.getRandomBetween(192, 212);
                int dy = Utils.getRandomBetween(0, 32);
                Sounds.play(Sound.WAVE);
                loadNextMap(Maps.SOSARIA, dx, dy);
                return false;
            }
        }

        int mask = bm.getValidMovesMask(context, (int) currentTile.x, (int) currentTile.y);
        if (!Direction.isDirInMask(dir, mask)) {
            Sounds.play(Sound.BLOCKED);
            finishTurn((int) currentTile.x, (int) currentTile.y);
            return false;
        }

        return true;
    }

    private void postMove(Direction dir, int newx, int newy) {

        if (context.getCurrentMap().getId() == Maps.SOSARIA.getId()) {

            //check for active moongate portal
            for (Moongate g : context.getCurrentMap().getMoongates()) {
                if (g.getCurrentTexture() != null && newx == g.getX() && newy == g.getY()) {
                    Sounds.play(Sound.MOONGATE);
                    Vector3 d = getDestinationForMoongate(g);
                    if (d != null) {
                        newMapPixelCoords = getMapPixelCoords((int) d.x, (int) d.y);
                        renderer.getFOV().calculateFOV((int) d.x, (int) d.y, FOV_RADIUS);
                        return;
                    }
                }
            }

        }

        renderer.getFOV().calculateFOV(newx, newy, FOV_RADIUS);
        log(dir.toString());
    }

    @Override
    public void finishTurn(int currentX, int currentY) {

        try {

            checkHullIntegrity(context.getCurrentMap(), currentX, currentY);

            context.getParty().endTurn(Maps.get(context.getCurrentMap().getId()), context.getCurrentMap().getType());

            context.getAura().passTurn();

            TileEffect effect = context.getCurrentMap().getTile(currentX, currentY).getRule().getEffect();
            context.getParty().applyEffect(effect);
            if (effect == TileEffect.FIRE || effect == TileEffect.LAVA) {
                Sounds.play(Sound.FIREFIELD);
            } else if (effect == TileEffect.POISON || effect == TileEffect.POISONFIELD) {
                Sounds.play(Sound.POISON_EFFECT);
            }

            if (checkRandomCreatures()) {
                spawnCreature(null, currentX, currentY);
            }

            boolean quick = context.getAura().getType() == AuraType.QUICKNESS;
            if (!quick) {
                context.getCurrentMap().moveObjects(this, currentX, currentY);
            }

        } catch (PartyDeathException t) {
            partyDeath();
        }

    }

    public synchronized void replaceTile(String name, int x, int y) {
        if (name == null) {
            return;
        }
        TextureRegion texture = Exodus.standardAtlas.findRegion(name);
        TiledMapTileLayer layer = (TiledMapTileLayer) context.getCurrentTiledMap().getLayers().get("Map Layer");
        Cell cell = layer.getCell(x, context.getCurrentMap().getWidth() - 1 - y);
        TiledMapTile tmt = new StaticTiledMapTile(texture);
        tmt.setId(y * context.getCurrentMap().getWidth() + x);
        if (cell == null) {
            System.err.printf("null cell in %s %d %d %s\n", context.getCurrentMap().getId(), x, y, name);
        }
        try {
            cell.setTile(tmt);
            context.getCurrentMap().setTile(Exodus.baseTileSet.getTileByName(name), x, y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkRandomCreatures() {
        if (context.getCurrentMap().getId() != Maps.SOSARIA.getId()) {
            return false;
        }
        if (context.getCurrentMap().getCreatures().size() >= MAX_CREATURES_ON_MAP) {
            return false;
        }
        return rand.nextInt(32) == 0;
    }

    private boolean spawnCreature(Creature creature, int currentX, int currentY) {

        int dx = 0;
        int dy = 0;
        int tmp = 0;

        boolean ok = false;
        int tries = 0;
        int MAX_TRIES = 10;

        while (!ok && (tries < MAX_TRIES)) {
            dx = 15;
            dy = rand.nextInt(15);

            if (rand.nextInt(100) > 50) {
                dx = -dx;
            }
            if (rand.nextInt(100) > 50) {
                dy = -dy;
            }
            if (rand.nextInt(100) > 50) {
                tmp = dx;
                dx = dy;
                dy = tmp;
            }

            dx = currentX + dx;
            dy = currentY + dy;

            /* make sure we can spawn the creature there */
            if (creature != null) {
                Tile tile = context.getCurrentMap().getTile(dx, dy);
                TileRule rule = tile.getRule();
                if ((creature.getSails() && rule.has(TileAttrib.sailable))
                        || (creature.getSwims() && rule.has(TileAttrib.swimmable))
                        || (creature.getFlies() && !rule.has(TileAttrib.unflyable))) {
                    ok = true;
                } else {
                    tries++;
                }
            } else {
                ok = true;
            }
        }

        if (!ok) {
            return false;
        }

        if (creature != null) {

        } else {
            Tile tile = context.getCurrentMap().getTile(dx, dy);
            creature = getRandomCreatureForTile(tile);
        }

        if (creature != null) {
            creature.currentX = dx;
            creature.currentY = dy;
            context.getCurrentMap().addCreature(creature);
        } else {
            return false;
        }

        return true;
    }

    private Creature getRandomCreatureForTile(Tile tile) {

        int era = 0;
        int randId = 0;

        if (tile == null || tile.getRule() == null) {
            System.err.println("randomForTile: Tile or rule is null");
            return null;
        }

        if (tile.getRule().has(TileAttrib.creatureunwalkable)) {
            return null;
        }

        if (tile.getRule().has(TileAttrib.sailable)) {
            randId = CreatureType.pirate_ship.getValue();
            randId += rand.nextInt(6);
            Creature cr = Exodus.creatures.getInstance(CreatureType.get(randId), Exodus.standardAtlas);
            return cr;
        } else if (tile.getRule().has(TileAttrib.swimmable)) {
            randId = CreatureType.nixie.getValue();
            randId += rand.nextInt(5);//whirlpool will not be spawned
            Creature cr = Exodus.creatures.getInstance(CreatureType.get(randId), Exodus.standardAtlas);
            return cr;
        }

        randId = CreatureType.orc.getValue();
        int avgPtyHlth = this.context.getParty().getAverageMaxHealth();

        if (avgPtyHlth > 700) {
            era = 0x1111; //15
            randId += era & rand.nextInt(16);
        } else if (avgPtyHlth > 500) {
            era = 0x1111; //15
            randId += era & rand.nextInt(16) & rand.nextInt(16);
        } else if (avgPtyHlth > 400) {
            era = 0x0111; //7
            randId += era & rand.nextInt(16);
        } else if (avgPtyHlth > 300) {
            era = 0x0111; //7
            randId += era & rand.nextInt(16) & rand.nextInt(16);
        } else {
            era = 0x0011; //3
            randId += era & rand.nextInt(16);
        }

        Creature cr = Exodus.creatures.getInstance(CreatureType.get(randId), Exodus.standardAtlas);

        return cr;
    }

    private void updateMoons(boolean showmoongates) {

        // world map only
        if (context.getCurrentMap().getId() == 0) {

            if (context.incrementWindCounter() >= MOON_SECONDS_PER_PHASE * 4) {
                if (rand.nextInt(4) == 1) {
                    context.setWindDirection(Direction.getRandomValidDirection(0xff));
                }
                context.setWindCounter(0);
            }

            context.setMoonPhase(context.getMoonPhase() + 1);
            if (context.getMoonPhase() >= MOON_PHASES * MOON_SECONDS_PER_PHASE * 4) {
                context.setMoonPhase(0);
            }

            phase = (context.getMoonPhase() / (4 * MOON_SECONDS_PER_PHASE));
            feluccaphase = phase % 8;
            trammelphase = phase / 3;
            if (trammelphase > 7) {
                trammelphase = 7;
            }
            trammelSubphase = context.getMoonPhase() % (MOON_SECONDS_PER_PHASE * 4 * 3);

            for (Moongate g : context.getCurrentMap().getMoongates()) {
                g.setCurrentTexture(null);
            }

            //for the town of dawn to only show at new moons
            if (trammelphase == 0 && feluccaphase == 0 && trammelSubphase == 1) {
                replaceTile("town", 148, 212);
            } else if (trammelphase == 0 && feluccaphase == 1 && trammelSubphase == 16) {
                replaceTile("grass", 148, 212);
            }

            if (showmoongates) {
                Moongate gate = context.getCurrentMap().getMoongate(trammelphase);
                AtlasRegion texture = null;
                if (trammelSubphase == 0) {
                    texture = moongateTextures.get(0);
                } else if (trammelSubphase == 1) {
                    texture = moongateTextures.get(1);
                } else if (trammelSubphase == 2) {
                    texture = moongateTextures.get(2);
                } else if (trammelSubphase == 3) {
                    texture = moongateTextures.get(3);
                } else if ((trammelSubphase > 3) && (trammelSubphase < (MOON_SECONDS_PER_PHASE * 4 * 3) - 3)) {
                    texture = moongateTextures.get(3);
                } else if (trammelSubphase == (MOON_SECONDS_PER_PHASE * 4 * 3) - 3) {
                    texture = moongateTextures.get(2);
                } else if (trammelSubphase == (MOON_SECONDS_PER_PHASE * 4 * 3) - 2) {
                    texture = moongateTextures.get(1);
                } else if (trammelSubphase == (MOON_SECONDS_PER_PHASE * 4 * 3) - 1) {
                    texture = moongateTextures.get(0);
                }
                gate.setCurrentTexture(texture);
            }

        }
    }

    private Vector3 getDestinationForMoongate(Moongate m) {
        Vector3 dest = new Vector3(m.getX(), m.getY(), 0);
        int destGate = m.getPhase();

        if (feluccaphase == m.getD1()) {
            destGate = m.getD1();
        }
        if (feluccaphase == m.getD2()) {
            destGate = m.getD2();
        }
        if (feluccaphase == m.getD3()) {
            destGate = m.getD3();
        }

        for (Moongate dm : context.getCurrentMap().getMoongates()) {
            if (dm.getPhase() == destGate) {
                dest = new Vector3(dm.getX(), dm.getY(), 0);
            }
        }

        return dest;
    }

    public void board(int x, int y) {

        if (context.getTransport() != Transport.FOOT) {
            log("Board: Can't!");
            return;
        }

        Iterator<Drawable> iter = context.getCurrentMap().getObjects().iterator();
        while (iter.hasNext()) {
            Drawable d = iter.next();
            if ("ship".equals(d.getTile().getName()) && d.getCx() == x && d.getCy() == y) {
                iter.remove();
                log("Board Frigate!");
                if (context.getLastShip() != d) {
                    context.getParty().adjustShipHull(50);
                }
                context.setCurrentShip(d);
                mainAvatar = shipAnim;
                context.setTransport(Transport.SHIP);
                return;
            }
        }

        Creature horse = context.getCurrentMap().getCreatureAt(x, y);
        if (horse != null && (horse.getTile() == CreatureType.horse)) {
            log("Mount Horse!");
            context.getCurrentMap().removeCreature(horse);
            mainAvatar = horseAnim;
            context.setTransport(Transport.HORSE);
            return;
        }

        log("Board What?");

    }

    class ShipInputAdapter extends InputAdapter {

        Vector3 pos;

        ShipInputAdapter(Vector3 pos) {
            this.pos = pos;
        }

        @Override
        public boolean keyUp(int keycode) {
            Direction fireDir = null;

            if (keycode == Keys.LEFT) {
                if (avatarDirection + 1 == Direction.NORTH.getVal()) {
                    fireDir = Direction.WEST;
                }
                if (avatarDirection + 1 == Direction.SOUTH.getVal()) {
                    fireDir = Direction.WEST;
                }
            } else if (keycode == Keys.RIGHT) {
                if (avatarDirection + 1 == Direction.NORTH.getVal()) {
                    fireDir = Direction.EAST;
                }
                if (avatarDirection + 1 == Direction.SOUTH.getVal()) {
                    fireDir = Direction.EAST;
                }
            } else if (keycode == Keys.UP) {
                if (avatarDirection + 1 == Direction.EAST.getVal()) {
                    fireDir = Direction.NORTH;
                }
                if (avatarDirection + 1 == Direction.WEST.getVal()) {
                    fireDir = Direction.NORTH;
                }
            } else if (keycode == Keys.DOWN) {
                if (avatarDirection + 1 == Direction.EAST.getVal()) {
                    fireDir = Direction.SOUTH;
                }
                if (avatarDirection + 1 == Direction.WEST.getVal()) {
                    fireDir = Direction.SOUTH;
                }
            }

            if (fireDir != null) {
                logAppend(fireDir.toString());
                AttackVector av = Utils.avatarfireCannon(context, context.getCurrentMap().getObjects(), context.getCurrentMap(), fireDir, (int) pos.x, (int) pos.y);
                Utils.animateCannonFire(GameScreen.this, projectilesStage, context.getCurrentMap(), av, (int) pos.x, (int) pos.y, true);
            } else {
                log("Broadsides only!");
            }

            Gdx.input.setInputProcessor(new InputMultiplexer(GameScreen.this, stage));
            return false;
        }
    }

    public void getChest(PartyMember pm, int x, int y, boolean disarmed) {

        boolean found = false;

        Drawable chest = null;
        Iterator<Drawable> iter = context.getCurrentMap().getObjects().iterator();
        while (iter.hasNext()) {
            Drawable d = iter.next();
            if ("chest".equals(d.getTile().getName()) && d.getCx() == x && d.getCy() == y) {
                found = true;
                iter.remove();
                chest = d;
                break;
            }
        }

        if (chest == null) {
            //check tile if chest in a city
            Tile tile = context.getCurrentMap().getTile(x, y);
            if (tile.getRule() == TileRule.chest) {
                replaceTile("brick_floor", x, y);
                found = true;
            }
        }

        try {
            if (found) {
                if (!disarmed) {
                    context.getChestTrapHandler(pm);
                }
                log(String.format("The Chest Holds: %d Gold", context.getParty().getChestGold(pm)));
            } else {
                log("Not Here!");
            }
        } catch (PartyDeathException e) {
            partyDeath();
        }

    }

    public void peerGem() {
        //find first party member which has gems
        for (PartyMember pm : context.getParty().getMembers()) {
            if (pm.getPlayer().gems > 0) {
                pm.getPlayer().gems--;
                log("Peer at a Gem!");
                Gdx.input.setInputProcessor(new PeerGemInputAdapter());
                return;
            }
        }
        log("Thou dost have no gems!");
    }

    private void negateTime() {
        //find first party member which has powder
        for (PartyMember pm : context.getParty().getMembers()) {
            if (pm.getPlayer().powder > 0) {
                pm.getPlayer().powder--;
                log("Stop time with a powder!");
                context.getAura().set(AuraType.QUICKNESS, 6);
                return;
            }
        }
        log("None!");
    }

    public void peerTelescope() {
        log("You see a knob on the");
        log("telescope marked A-P.");
        log("You select:");
        Gdx.input.setInputProcessor(new PeerTelescopeInputAdapter());
    }

    @Override
    public InputProcessor getPeerGemInputProcessor() {
        return new GameScreen.PeerGemInputAdapter();
    }

    private class PeerGemInputAdapter extends InputAdapter {

        Image img;

        public PeerGemInputAdapter() {
            try {
                Texture t = null;
                if (context.getCurrentMap().getId() == Maps.SOSARIA.getId()) {
                    Vector3 v = getCurrentMapCoords();
                    t = Utils.peerGem(context.getCurrentMap(), (int) v.x, (int) v.y, Exodus.standardAtlas);
                } else {
                    t = Utils.peerGem(Maps.get(context.getCurrentMap().getId()), Exodus.standardAtlas);
                }
                img = new Image(t);
                img.setX(0);
                img.setY(0);
                img.addAction(sequence(Actions.alpha(0), Actions.fadeIn(1f, Interpolation.fade)));
                stage.addActor(img);
                gameTimer.active = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean keyUp(int keycode) {
            if (img != null) {
                img.remove();
            }
            Gdx.input.setInputProcessor(new InputMultiplexer(GameScreen.this, stage));
            gameTimer.active = true;
            return false;
        }
    }

    private class PeerTelescopeInputAdapter extends InputAdapter {

        Image img;

        @Override
        public boolean keyUp(int keycode) {
            if (keycode >= Keys.A && keycode <= Keys.P) {
                if (img != null) {
                    return false;
                }
                Maps map = Maps.get(keycode - Keys.A + 1);
                log(Keys.toString(keycode).toUpperCase() + " - " + map.getLabel());
                try {
                    Texture t = Utils.peerGem(map, Exodus.standardAtlas);
                    img = new Image(t);
                    img.setX(0);
                    img.setY(0);
                    img.addAction(sequence(Actions.alpha(0), Actions.fadeIn(1f, Interpolation.fade)));
                    stage.addActor(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (keycode == Keys.ENTER || keycode == Keys.SPACE) {
                if (img != null) {
                    img.remove();
                }
                Gdx.input.setInputProcessor(new InputMultiplexer(GameScreen.this, stage));
            }
            return false;
        }
    }

    private void checkHullIntegrity(BaseMap bm, int x, int y) {

        boolean killAll = false;
        if (context.getTransport() == Transport.SHIP && context.getParty().getSaveGame().shiphull <= 0) {
            log("Thy ship sinks!");
            killAll = true;
        } else if (context.getTransport() == Transport.FOOT
                && bm.getTile(x, y).getRule() != null
                && bm.getTile(x, y).getRule().has(TileAttrib.sailable)) {
            //log("Trapped at sea without thy ship, thou dost drown!");
            //killAll = true;
        }

        if (killAll) {
            //context.getParty().killAll();
            //context.getParty().setTransport(Exodus.baseTileSet.getTileByIndex(0x1f));
            mainAvatar = avatarAnim;
            partyDeath();
        }
    }

}
