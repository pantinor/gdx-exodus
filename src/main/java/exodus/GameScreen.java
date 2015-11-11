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
import exodus.Party.PartyMember;
import util.PartyDeathException;

public class GameScreen extends BaseScreen {

    TextureAtlas moonAtlas;

    public static Animation mainAvatar;
    public static Animation avatarAnim;
    public static Animation corpseAnim;
    public static Animation horseAnim;
    public static Animation shipAnim;
    public static int avatarDirection = Direction.WEST.getVal();

    TiledMap map;
    UltimaMapRenderer renderer;
    Batch mapBatch, batch;

    public Stage mapObjectsStage;
    public Stage projectilesStage;

    private final Viewport mapViewPort;

    Array<AtlasRegion> moongateTextures = new Array<>();
    public static int phase = 0, trammelphase = 0, trammelSubphase = 0, feluccaphase = 0;

    //public SecondaryInputProcessor sip;

    public GameTimer gameTimer = new GameTimer();

    public GameScreen(Exodus mainGame) {

        scType = ScreenType.MAIN;

        GameScreen.mainGame = mainGame;

        initTransportAnimations();
        mainAvatar = avatarAnim;;

        moongateTextures = Exodus.standardAtlas.findRegions("moongate");
        moonAtlas = new TextureAtlas(Gdx.files.classpath("assets/graphics/moon-atlas.txt"));

        batch = new SpriteBatch();

        stage = new Stage(viewport);

        camera = new OrthographicCamera(Exodus.MAP_WIDTH, Exodus.MAP_HEIGHT);

        mapViewPort = new ScreenViewport(camera);

        mapObjectsStage = new Stage(mapViewPort);
        Maps.SOSARIA.getMap().setSurfaceMapStage(mapObjectsStage);
        projectilesStage = new Stage(mapViewPort);

        //sip = new SecondaryInputProcessor(this, stage);
        SequenceAction seq1 = Actions.action(SequenceAction.class);
        seq1.addAction(Actions.delay(.25f));
        seq1.addAction(Actions.run(gameTimer));
        stage.addAction(Actions.forever(seq1));
        
        //add 2 whirlpools
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
        //Array<AtlasRegion> corps = Exodus.standardAtlas.findRegions("corpse");
        Array<AtlasRegion> horse = Exodus.standardAtlas.findRegions("horse");
        Array<AtlasRegion> ship = Exodus.standardAtlas.findRegions("ship");

        Array<AtlasRegion> tmp = new Array<>(4);
        for (int i = 0; i < 4; i++) {
            tmp.add(avatar.get(0));
        }
        avatarAnim = new Animation(0.25f, tmp);

//        Array<AtlasRegion> tmp2 = new Array<>(4);
//        for (int i = 0; i < 4; i++) {
//            tmp2.add(corps.get(0));
//        }
//        corpseAnim = new Animation(0.25f, tmp2);

        tmp = new Array<>(4);
        AtlasRegion ar = new AtlasRegion(horse.get(0));
        ar.flip(true, false);
        tmp.add(horse.get(0));
        tmp.add(horse.get(0));
        tmp.add(ar);

        tmp.add(horse.get(0));
        horseAnim = new Animation(0.25f, tmp);

        shipAnim = new Animation(0.25f, ship);

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
            //context.loadJournalEntries();

            //load the surface world first
            //loadNextMap(Maps.SOSARIA, sg.partyX, sg.partyY);
            loadNextMap(Maps.SOSARIA, 224, 26);

            //load the dungeon if save game starts in dungeon
            if (Maps.get(sg.location) != Maps.SOSARIA) {
                //loadNextMap(Maps.get(sg.location), sg.x, sg.y, sg.x, sg.y, sg.dnglevel, Direction.getByValue(sg.orientation + 1), true);
                //loadNextMap(Maps.ABYSS, 0, 0, 5, 5, 0, Direction.SOUTH, true);
                //loadNextMap(Maps.DESTARD, 0, 0, 3, 5, 3, Direction.SOUTH, true);
                //loadNextMap(Maps.DELVE_SORROWS, 0, 0, 3, 19, 1, Direction.EAST, true);
            }

            party.setTransport(Exodus.baseTileSet.getTileByIndex(sg.transport));
            
            mainAvatar = avatarAnim;

            
//            switch (sg.transport) {
//                case 31:
//                    mainAvatar = avatarAnim;
//                    break;
//                case 16:
//                case 17:
//                case 18:
//                case 19:
//                    mainAvatar = shipAnim;
//                    break;
//                case 20:
//                case 21:
//                    mainAvatar = horseAnim;
//                    break;
//            }

//            //load objects to surface stage
//            for (int i=0;i<24;i++) {
//                if (sg.objects_save_tileids[i] != 0 && sg.objects_save_x[i] != 0 && sg.objects_save_y[i] != 0) {
//                    Tile t = Exodus.baseTileSet.getTileByIndex(sg.objects_save_tileids[i] & 0xff);
//                    Drawable d = new Drawable(Maps.SOSARIA.getMap(), sg.objects_save_x[i] & 0xff, sg.objects_save_y[i] & 0xff, t, Exodus.standardAtlas);
//                    Vector3 v = getMapPixelCoords(sg.objects_save_x[i] & 0xff, sg.objects_save_y[i] & 0xff);
//                    d.setX(v.x);
//                    d.setY(v.y);
//                    mapObjectsStage.addActor(d);
//                }
//            }
//            //load monsters to surface map
//            for (int i=0;i<8;i++) {
//                if (sg.monster_save_tileids[i] != 0 && sg.monster_save_x[i] != 0 && sg.monster_save_y[i] != 0) {
//                    Tile t = Exodus.baseTileSet.getTileByIndex(sg.monster_save_tileids[i] & 0xff);
//                    Creature cr = Exodus.creatures.getInstance(CreatureType.get(t.getName()), Exodus.standardAtlas);
//                    cr.currentX = sg.monster_save_x[i] & 0xff;
//                    cr.currentY = sg.monster_save_y[i] & 0xff;
//                    cr.currentPos = getMapPixelCoords(cr.currentX, cr.currentY);
//                    Maps.WORLD.getMap().addCreature(cr);
//                }
//            }
        }

        context.getParty().addObserver(this);
    }

    @Override
    public void hide() {
        gameTimer.active = false;
        context.getParty().deleteObserver(this);
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
                mainGame.setScreen(sc);

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

            renderer.getFOV().calculateFOV(baseMap.getShadownMap(), x, y, 17f);
            newMapPixelCoords = getMapPixelCoords(x, y);
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
        renderer.getFOV().calculateFOV(bm.getShadownMap(), x, y, 17f);
    }

    public void attackAt(Maps combat, Creature cr) {

        Maps contextMap = Maps.get(context.getCurrentMap().getId());
        BaseMap combatMap = combat.getMap();

        TiledMap tmap = new UltimaTiledMapLoader(combat, Exodus.standardAtlas, combat.getMap().getWidth(), combat.getMap().getHeight(), tilePixelWidth, tilePixelHeight).load();

        CombatScreen sc = new CombatScreen(this, context, contextMap, combatMap, tmap, cr.getTile(), Exodus.creatures, Exodus.standardAtlas);
        mainGame.setScreen(sc);

        currentEncounter = cr;
    }

    @Override
    public void endCombat(boolean isWon, BaseMap combatMap, boolean wounded) {

        mainGame.setScreen(this);

        if (currentEncounter != null) {

            Tile tile = context.getCurrentMap().getTile(currentEncounter.currentX, currentEncounter.currentY);

            if (isWon) {

                log("Victory!");

                TileRule r = tile.getRule();

                /* add a chest, if the creature leaves one */
                if (!currentEncounter.getNochest() && (r == null || !r.has(TileAttrib.unwalkable))) {
                    Tile ct = Exodus.baseTileSet.getTileByName("chest");
                    Drawable chest = new Drawable(context.getCurrentMap(), currentEncounter.currentX, currentEncounter.currentY, ct, Exodus.standardAtlas);
                    chest.setX(currentEncounter.currentPos.x);
                    chest.setY(currentEncounter.currentPos.y);
                    mapObjectsStage.addActor(chest);
                } /* add a ship if you just defeated a pirate ship */ else if (currentEncounter.getTile() == CreatureType.pirate_ship) {
                    Tile st = Exodus.baseTileSet.getTileByName("ship");
                    Drawable ship = new Drawable(context.getCurrentMap(), currentEncounter.currentX, currentEncounter.currentY, st, Exodus.standardAtlas);
                    ship.setX(currentEncounter.currentPos.x);
                    ship.setY(currentEncounter.currentPos.y);
                    mapObjectsStage.addActor(ship);
                }
            } else {

                if (context.getParty().didAnyoneFlee()) {
                    log("Battle is lost!");
                } else if (!context.getParty().isAnyoneAlive()) {
                    partyDeath();
                }
            }

            context.getCurrentMap().removeCreature(currentEncounter);

            currentEncounter = null;

        }
    }

    @Override
    public void partyDeath() {
        mainGame.setScreen(new StartScreen(mainGame));
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

        mapObjectsStage.act();
        mapObjectsStage.draw();

        batch.begin();

        batch.draw(Exodus.backGround, 0, 0);

        batch.draw(mainAvatar.getKeyFrames()[avatarDirection], tilePixelWidth * 11, tilePixelHeight * 12);

        //Vector3 v = getCurrentMapCoords();
        //font.draw(batch, String.format("newMapPixelCoords: %d, %d", (int)newMapPixelCoords.x, (int)newMapPixelCoords.y), 10, 500);
        //font.draw(batch, String.format("current map coords: %d, %d", (int)v.x, (int)v.y), 10, 480);
        //font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 10, 460);
        Exodus.hud.render(batch, context.getParty());
//
//        Exodus.font.setColor(Color.WHITE);
//        if (showZstats > 0) {
//            context.getParty().getSaveGame().renderZstats(showZstats, Exodus.font, batch, Exodus.SCREEN_HEIGHT);
//        }
//
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
            if (context.getTransportContext() == TransportContext.SHIP && avatarDirection + 1 != Direction.NORTH.getVal()) {
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
            if (context.getTransportContext() == TransportContext.SHIP && avatarDirection + 1 != Direction.EAST.getVal()) {
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
            if (context.getTransportContext() == TransportContext.SHIP && avatarDirection + 1 != Direction.WEST.getVal()) {
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
            if (context.getTransportContext() == TransportContext.SHIP && avatarDirection + 1 != Direction.SOUTH.getVal()) {
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

        } else if (keycode == Keys.F && context.getTransportContext() == TransportContext.SHIP) {
            log("Fire Cannon > ");
            ShipInputAdapter sia = new ShipInputAdapter(v);
            Gdx.input.setInputProcessor(sia);
            return false;

        } else if (keycode == Keys.K || keycode == Keys.D) {

//            if (context.getCurrentMap().getId() == Maps.WORLD.getId()) {
//                if (keycode == Keys.K && context.getTransportContext() == TransportContext.BALLOON) {
//                    context.getParty().getSaveGame().balloonstate = 1;
//                    log("Klimb altitude");
//                } else if (keycode == Keys.D && context.getTransportContext() == TransportContext.BALLOON) {
//                    if (ct.getRule().has(TileAttrib.canlandballoon)) {
//                        context.getParty().getSaveGame().balloonstate = 0;
//                        renderer.getFOV().calculateFOV(context.getCurrentMap().getShadownMap(), (int) v.x, (int) v.y, 17f);
//                        log("Land balloon");
//                    } else {
//                        log("Not here!");
//                    }
//                }
//            } else {
//                Portal p = context.getCurrentMap().getPortal(v.x, v.y, 0);
//                if (p != null) {
//                    loadNextMap(Maps.get(p.getDestmapid()), p.getStartx(), p.getStarty());
//                    log(p.getMessage());
//                    return false;
//                }
//            }

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

        } else if (keycode == Keys.L) {

        } else if (keycode == Keys.S) {

        } else if (keycode == Keys.M) {

        } else if (keycode == Keys.P) {
            //peerGem();
        } else if (keycode == Keys.U) {

            log("Use Item:");
            log("");
            ItemInputAdapter iia = new ItemInputAdapter(this);
            Gdx.input.setInputProcessor(iia);
            return false;

        } else if (keycode == Keys.T || keycode == Keys.O || keycode == Keys.J || keycode == Keys.L || keycode == Keys.A || keycode == Keys.G || keycode == Keys.R || keycode == Keys.W) {
            //Gdx.input.setInputProcessor(sip);
            //sip.setinitialKeyCode(keycode, context.getCurrentMap(), (int) v.x, (int) v.y);
            return false;

        } else if (keycode == Keys.C) {

            log("Cast Spell: ");
            log("Who casts (1-8): ");
            //Gdx.input.setInputProcessor(new SpellInputProcessor(this, context, stage, (int) v.x, (int) v.y, null));
            return false;

        } else if (keycode == Keys.SPACE) {
            log("Pass");
        }

        finishTurn((int) v.x, (int) v.y);

        return false;

    }

    private boolean preMove(Vector3 currentTile, Direction dir) {

        int nx = (int) currentTile.x;
        int ny = (int) currentTile.y;

//        if (context.getParty().getMember(0).getPlayer().status == StatusType.SLEEPING) {
//            finishTurn(nx, ny);
//            return false;
//        }

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

                //remove any city/town actors (chests) from the map we are leaving
                for (Actor a : mapObjectsStage.getActors()) {
                    if (a instanceof Drawable) {
                        Drawable d = (Drawable) a;
                        if (d.getMapId() != Maps.SOSARIA.getId() && d.getMapId() == bm.getId()) {
                            d.remove();
                        }
                    }
                }

                Portal p = Maps.SOSARIA.getMap().getPortal(bm.getId());
                loadNextMap(Maps.SOSARIA, p.getX(), p.getY());
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
                    }
                }
            }

            //checkSpecialCreatures(dir, newx, newy);
            //checkBridgeTrolls(newx, newy);
        }

        renderer.getFOV().calculateFOV(context.getCurrentMap().getShadownMap(), newx, newy, 17f);

        log(dir.toString());
    }

    @Override
    public void finishTurn(int currentX, int currentY) {

        try {

            checkHullIntegrity(context.getCurrentMap(), currentX, currentY);

            context.getParty().endTurn(context.getCurrentMap().getType());

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

                context.getCurrentMap().moveObjects(this, currentX, currentY);

        } catch (PartyDeathException t) {
            partyDeath();
        }

    }

    public void replaceTile(String name, int x, int y) {
        if (name == null) {
            return;
        }
        TextureRegion texture = Exodus.standardAtlas.findRegion(name);
        TiledMapTileLayer layer = (TiledMapTileLayer) context.getCurrentTiledMap().getLayers().get("Map Layer");
        Cell cell = layer.getCell(x, context.getCurrentMap().getWidth() - 1 - y);
        TiledMapTile tmt = new StaticTiledMapTile(texture);
        tmt.setId(y * context.getCurrentMap().getWidth() + x);
        cell.setTile(tmt);
        context.getCurrentMap().setTile(Exodus.baseTileSet.getTileByName(name), x, y);
    }

    private boolean checkRandomCreatures() {
        if (context.getCurrentMap().getId() != Maps.SOSARIA.getId()
                || context.getCurrentMap().getCreatures().size() >= MAX_CREATURES_ON_MAP) {
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
            randId += rand.nextInt(5);
            Creature cr = Exodus.creatures.getInstance(CreatureType.get(randId), Exodus.standardAtlas);
            return cr;
        }

        if (context.getParty().getSaveGame().moves > 30000) {
            era = 15;
        } else if (context.getParty().getSaveGame().moves > 20000) {
            era = 7;
        } else {
            era = 3;
        }

        randId = CreatureType.orc.getValue();
        randId += era & rand.nextInt(16) & rand.nextInt(16);
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
                replaceTile("town",148,212);
            } else if (trammelphase == 0 && feluccaphase == 1 && trammelSubphase == 16) {
                replaceTile("grass",148,212);
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

        if (context.getTransportContext() != TransportContext.FOOT) {
            log("Board: Can't!");
            return;
        }

        Tile tile = null;

        //check for ship
        Drawable ship = null;
        for (Actor a : mapObjectsStage.getActors()) {
            if (a instanceof Drawable) {
                Drawable d = (Drawable) a;
                if (d.getTile().getName().equals("ship") && d.getCx() == x && d.getCy() == y) {
                    ship = d;
                    tile = d.getTile();
                }
            }
        }

        //check for horse
        Creature horse = context.getCurrentMap().getCreatureAt(x, y);
        if (horse != null && (horse.getTile() == CreatureType.horse)) {
            tile = Exodus.baseTileSet.getTileByName("horse");
        }

        if (tile == null) {
            log("Board What?");
            return;
        }

        if (tile.getRule().has(TileAttrib.ship)) {
            log("Board Frigate!");
            if (context.getLastShip() != ship) {
                context.getParty().adjustShipHull(50);
            }
            context.setCurrentShip(ship);
            ship.remove();
            mainAvatar = shipAnim;

        } else if (tile.getRule().has(TileAttrib.horse)) {
            log("Mount Horse!");
            context.getCurrentMap().removeCreature(horse);
            mainAvatar = horseAnim;

        } else {
            log("Board What?");
            return;
        }

        context.getParty().setTransport(tile);

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
                //AttackVector av = Utils.avatarfireCannon(context, mapObjectsStage, context.getCurrentMap(), fireDir, (int) pos.x, (int) pos.y);
                //Utils.animateCannonFire(GameScreen.this, projectilesStage, context.getCurrentMap(), av, (int) pos.x, (int) pos.y, true);
            } else {
                log("Broadsides only!");
            }

            Gdx.input.setInputProcessor(new InputMultiplexer(GameScreen.this, stage));
            return false;
        }
    }

    public void getChest(int index, int x, int y) {

        boolean found = false;

        Drawable chest = null;
        Array<Actor> as = mapObjectsStage.getActors();
        for (Actor a : as) {
            if (a instanceof Drawable) {
                Drawable d = (Drawable) a;
                if ("chest".equals(d.getTile().getName()) && d.getCx() == x && d.getCy() == y) {
                    chest = (Drawable) a;
                    found = true;
                    chest.remove();
                    break;
                }
            }
        }

        if (chest == null) {
            //check tile too, ie in cities
            Tile tile = context.getCurrentMap().getTile(x, y);
            if (tile.getRule() == TileRule.chest) {
                replaceTile("brick_floor", x, y);
                found = true;
            }
        }

        try {
            if (found) {
                PartyMember pm = context.getParty().getMember(index);
                if (pm == null) {
                    System.err.println("member is null " + index);
                }
                if (pm.getPlayer() == null) {
                    System.err.println("player is null " + index);
                }
                context.getChestTrapHandler(pm);
                log(String.format("The Chest Holds: %d Gold", context.getParty().getChestGold(pm)));
            } else {
                log("Not Here!");
            }
        } catch (PartyDeathException e) {
            partyDeath();
        }

    }

    public void peerGem(PartyMember pm) {
        if (pm.getPlayer().gems > 0) {
            pm.getPlayer().gems--;
            log("Peer at a Gem!");
            Gdx.input.setInputProcessor(new PeerGemInputAdapter());
        } else {
            log("Thou dost have no gems!");
        }
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
                    //t = Utils.peerGem(context.getCurrentMap(), (int) v.x, (int) v.y, Exodus.standardAtlas);
                } else {
                    //t = Utils.peerGem(Maps.get(context.getCurrentMap().getId()), Exodus.standardAtlas);
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

    public class PeerTelescopeInputAdapter extends InputAdapter {

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
                    Texture t = null;//Utils.peerGem(map, Exodus.standardAtlas);
                    img = new Image(t);
                    img.setX(0);
                    img.setY(0);
                    img.addAction(sequence(Actions.alpha(0), Actions.fadeIn(1f, Interpolation.fade)));
                    stage.addActor(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else { //if (keycode == Keys.ENTER || keycode == Keys.SPACE) {
                if (img != null) {
                    img.remove();
                }
                Gdx.input.setInputProcessor(new InputMultiplexer(GameScreen.this, stage));
            }
            return false;
        }
    }

    class ItemInputAdapter extends InputAdapter {

        GameScreen screen;
        StringBuilder buffer = new StringBuilder();

        public ItemInputAdapter(GameScreen screen) {
            this.screen = screen;
        }

        @Override
        public boolean keyUp(int keycode) {
            if (keycode == Keys.ENTER) {
                if (buffer.length() < 1) {
                    return false;
                }
                String text = buffer.toString().toUpperCase();
                try {
                    Item item = Item.valueOf(Item.class, text);

                    switch (item) {
                        case BOOK:
                        case BELL:
                        case CANDLE:
                            //screen.useBBC(item);
                            break;
                        case WHEEL:
                            //screen.useWheel();
                            break;
                        case SKULL:
                            //screen.useSkull();
                            break;
                        case HORN:
                            //screen.useHorn();
                            break;
                        default:
                            screen.log("What?");
                            break;
                    }

                    Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));

                    Vector3 v = getCurrentMapCoords();
                    screen.finishTurn((int) v.x, (int) v.y);

                } catch (IllegalArgumentException e) {
                    screen.log("What?");
                    Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
                }

            } else if (keycode == Keys.BACKSPACE) {
                if (buffer.length() > 0) {
                    buffer.deleteCharAt(buffer.length() - 1);
                    screen.logDeleteLastChar();
                }
            } else if (keycode >= 29 && keycode <= 54) {
                buffer.append(Keys.toString(keycode).toUpperCase());
                screen.logAppend(Keys.toString(keycode).toUpperCase());
            }
            return false;
        }
    }


    private void checkHullIntegrity(BaseMap bm, int x, int y) {

        boolean killAll = false;
        if (context.getTransportContext() == TransportContext.SHIP) {// && context.getParty().getSaveGame().shiphull <= 0) {
            log("Thy ship sinks!");
            killAll = true;

        } else if (context.getTransportContext() == TransportContext.FOOT
                && bm.getTile(x, y).getRule() != null
                && bm.getTile(x, y).getRule().has(TileAttrib.sailable)) {
            log("Trapped at sea without thy ship, thou dost drown!");
            killAll = true;
        }

        if (killAll) {
            //context.getParty().killAll();
            //context.getParty().setTransport(Exodus.baseTileSet.getTileByIndex(0x1f));
            mainAvatar = avatarAnim;
            partyDeath();
        }
    }

}
