package exodus;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import objects.BaseMap;
import objects.Creature;
import objects.Portal;

import org.apache.commons.io.IOUtils;

import util.DungeonTileModelInstance;
import util.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.UBJsonReader;
import util.PartyDeathException;
import util.UltimaTiledMapLoader;

public class DungeonScreen extends BaseScreen {

    public Maps dngMap;
    private final String dungeonFileName;
    public GameScreen gameScreen;

    public Environment environment;
    public ModelBatch modelBatch;
    private SpriteBatch batch;
    private DecalBatch decalBatch;

    public CameraInputController inputController;
    public AssetManager assets;

    //3d models
    public Model fountainModel;
    public Model ladderModel;
    public Model chestModel;
    public Model orbModel;
    public Model avatarModel;
    public Model blocksModel;

    boolean showMiniMap = true;

    boolean isTorchOn = true;
    private Vector3 vdll = new Vector3(.04f, .04f, .04f);
    private Vector3 nll2 = new Vector3(1f, 0.8f, 0.6f);
    private Vector3 nll = new Vector3(.96f, .58f, 0.08f);

    Model lightModel;
    Renderable pLight;
    PointLight fixedLight;
    float lightFactor;

    public static final int DUNGEON_DIM = 16;
    public static final int DUNGEON_LVLS = 8;
    public DungeonTile[][][] dungeonTiles = new DungeonTile[DUNGEON_LVLS][DUNGEON_DIM][DUNGEON_DIM];

    public List<DungeonTileModelInstance> modelInstances = new ArrayList<>();
    public List<ModelInstance> floor = new ArrayList<>();
    public List<ModelInstance> ceiling = new ArrayList<>();
    public String[] texts = new String[DUNGEON_LVLS];

    public static Texture MINI_MAP_TEXTURE;
    public static final int DIM = 7;
    public static final int OFST = DIM;
    public static final int MM_BKGRND_DIM = DIM * DUNGEON_DIM + OFST * 2;
    public static final int xalignMM = 32;
    public static final int yalignMM = 64;

    public int currentLevel = 0;
    public Vector3 currentPos;
    public Direction currentDir = Direction.EAST;

    public SecondaryInputProcessor sip;
    private Texture miniMap;
    private MiniMapIcon miniMapIcon;

    public DungeonScreen(GameScreen gameScreen, Context context, Maps map) {

        scType = ScreenType.DUNGEON;
        this.dngMap = map;
        this.context = context;
        this.dungeonFileName = map.getMap().getFname();
        this.gameScreen = gameScreen;
        this.stage = new Stage();
        sip = new SecondaryInputProcessor(this, stage);

        addButtons();

        init();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
    }

    @Override
    public void hide() {
    }

    public void init() {

        FileHandleResolver resolver = new Constants.ClasspathResolver();

        assets = new AssetManager(resolver);
        assets.load("assets/graphics/dirt.png", Texture.class);
        assets.load("assets/graphics/map.png", Texture.class);
        assets.load("assets/graphics/Stone_Masonry.jpg", Texture.class);
        assets.load("assets/graphics/door.png", Texture.class);
        assets.load("assets/graphics/mortar.png", Texture.class);
        assets.load("assets/graphics/rock.png", Texture.class);

        assets.update(2000);

        //convert the collada dae format to the g3db format (do not use the obj format)
        //export from sketchup to collada dae format, then open dae in blender and export to the fbx format, then convert fbx to the g3db like below
        //C:\Users\Paul\Desktop\blender>fbx-conv-win32.exe -o G3DB ./Chess/pawn.fbx ./pawn.g3db
        ModelLoader<?> gloader = new G3dModelLoader(new UBJsonReader());
        fountainModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/fountain2.g3db"));
        ladderModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/ladder.g3db"));
        chestModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/chest.g3db"));
        orbModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/orb.g3db"));
        avatarModel = gloader.loadModel(Gdx.files.classpath("assets/graphics/wizard.g3db"));
        //blocksModel = gloader.loadModel(Gdx.files.internal("assets/graphics/box.g3db"));

        Pixmap pixmap = new Pixmap(MM_BKGRND_DIM, MM_BKGRND_DIM, Format.RGBA8888);
        pixmap.setColor(0.8f, 0.7f, 0.5f, .8f);
        pixmap.fillRectangle(0, 0, MM_BKGRND_DIM, MM_BKGRND_DIM);
        MINI_MAP_TEXTURE = new Texture(pixmap);
        pixmap.dispose();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.05f, 0.05f, 0.05f, 1f));
        //environment.set(new ColorAttribute(ColorAttribute.Fog, 0.13f, 0.13f, 0.13f, 1f));

        fixedLight = new PointLight().set(1f, 0.8f, 0.6f, 4f, 4f, 4f, 5f);
        environment.add(fixedLight);

        modelBatch = new ModelBatch();
        batch = new SpriteBatch();

        camera = new PerspectiveCamera(67, Exodus.MAP_WIDTH, Exodus.MAP_HEIGHT);

        camera.near = 0.1f;
        camera.far = 1000f;

        decalBatch = new DecalBatch(new CameraGroupStrategy(camera));
        
//        inputController = new CameraInputController(camera);
//        inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
//        inputController.translateUnits = 30f;

        ModelBuilder builder = new ModelBuilder();
        lightModel = builder.createSphere(.1f, .1f, .1f, 10, 10, new Material(ColorAttribute.createDiffuse(1, 1, 1, 1)), Usage.Position);
        lightModel.nodes.get(0).parts.get(0).setRenderable(pLight = new Renderable());

        for (int x = 0; x < DUNGEON_DIM + 4; x++) {
            for (int y = 0; y < DUNGEON_DIM + 4; y++) {
                Model sf = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/rock.png", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
                floor.add(new ModelInstance(sf, new Vector3(x - 1.5f, -.5f, y - 1.5f)));
            }
        }
        for (int x = 0; x < DUNGEON_DIM + 4; x++) {
            for (int y = 0; y < DUNGEON_DIM + 4; y++) {
                Model sf = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/dirt.png", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
                ceiling.add(new ModelInstance(sf, new Vector3(x - 1.5f, 1.5f, y - 1.5f)));
            }
        }

        try {
            InputStream is = ClassLoader.class.getResourceAsStream("/assets/data/" + dungeonFileName.toLowerCase());
            byte[] bytes = IOUtils.toByteArray(is);

            int pos = 0;
            for (int i = 0; i < DUNGEON_LVLS; i++) {
                for (int y = 0; y < DUNGEON_DIM; y++) {
                    for (int x = 0; x < DUNGEON_DIM; x++) {
                        byte index = bytes[pos];
                        pos++;
                        DungeonTile tile = DungeonTile.getTileByValue(index);
                        
                        for (objects.Label l : this.dngMap.getMap().getLabels()) {
                            if (l.getX() == x && l.getY() == y && l.getZ() == i) {
                                tile = DungeonTile.valueOf(l.getName());
                                break;
                            }
                        }
                        
                        dungeonTiles[i][x][y] = tile;
                        addBlock(i, tile, x + .5f, .5f, y + .5f);
                    }
                }
            }

            pos = 0x800;
            int[] textOffsets = new int[DUNGEON_LVLS];
            for (int x = 0; x < DUNGEON_LVLS; x++) {
                textOffsets[x] = (bytes[pos] & 0xff) + 0x800;
                pos += 2;
            }

            for (int x = 0; x < DUNGEON_LVLS; x++) {
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
                texts[x] = new String(b, "UTF-8").trim();
                texts[x] = texts[x].replaceAll("[<>]", "");
                texts[x] = texts[x].replaceAll("[\n\r]", " ");
            }

            miniMapIcon = new MiniMapIcon();
            miniMapIcon.setOrigin(5, 5);

            stage.addActor(miniMapIcon);

            setStartPosition();

            camera.position.set(currentPos);
            camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);

            //duplicate some of the outer edge tiles around the outside 
            //so that the wrapping is not so black hole on the sides
            //i went 2 layers duplicated on each edges + the corners
            for (int i = 0; i < DUNGEON_LVLS; i++) {
                {
                    int y = 0;
                    for (int x = 0; x < DUNGEON_DIM; x++) {//bottom across the top
                        DungeonTile tile = dungeonTiles[i][x][y + DUNGEON_DIM - 1];
                        if (tile == DungeonTile.CHEST) {
                            tile = DungeonTile.FLOOR;
                        }
                        addBlock(i, tile, x + .5f, .5f, y - .5f);

                        tile = dungeonTiles[i][x][y + DUNGEON_DIM - 2];
                        if (tile == DungeonTile.CHEST) {
                            tile = DungeonTile.FLOOR;
                        }
                        addBlock(i, tile, x + .5f, .5f, y - 1.5f);
                    }
                    for (int x = 0; x < DUNGEON_DIM; x++) {//top across the bottom
                        DungeonTile tile = dungeonTiles[i][x][y];
                        if (tile == DungeonTile.CHEST) {
                            tile = DungeonTile.FLOOR;
                        }
                        addBlock(i, tile, x + .5f, .5f, y + .5f + DUNGEON_DIM);

                        tile = dungeonTiles[i][x][y + 1];
                        if (tile == DungeonTile.CHEST) {
                            tile = DungeonTile.FLOOR;
                        }
                        addBlock(i, tile, x + .5f, .5f, y + .5f + DUNGEON_DIM + 1);
                    }

                }
                {
                    int x = 0;
                    for (int y = 0; y < DUNGEON_DIM; y++) {
                        DungeonTile tile = dungeonTiles[i][x][y];
                        if (tile == DungeonTile.CHEST) {
                            tile = DungeonTile.FLOOR;
                        }
                        addBlock(i, tile, x + .5f + DUNGEON_DIM, .5f, y + .5f);

                        tile = dungeonTiles[i][x + 1][y];
                        if (tile == DungeonTile.CHEST) {
                            tile = DungeonTile.FLOOR;
                        }
                        addBlock(i, tile, x + .5f + DUNGEON_DIM + 1, .5f, y + .5f);
                    }
                    for (int y = 0; y < DUNGEON_DIM; y++) {
                        DungeonTile tile = dungeonTiles[i][x + DUNGEON_DIM - 1][y];
                        if (tile == DungeonTile.CHEST) {
                            tile = DungeonTile.FLOOR;
                        }
                        addBlock(i, tile, x - .5f, .5f, y + .5f);

                        tile = dungeonTiles[i][x + DUNGEON_DIM - 2][y];
                        if (tile == DungeonTile.CHEST) {
                            tile = DungeonTile.FLOOR;
                        }
                        addBlock(i, tile, x - 1.5f, .5f, y + .5f);
                    }
                }

                {//copy bottom right corner to the top left corner
                    DungeonTile tile = dungeonTiles[i][DUNGEON_DIM - 1][DUNGEON_DIM - 1];
                    addBlock(i, tile, -.5f, .5f, -.5f);
                }

                {//copy bottom left corner to the top right corner
                    DungeonTile tile = dungeonTiles[i][0][DUNGEON_DIM - 1];
                    addBlock(i, tile, DUNGEON_DIM + .5f, .5f, -.5f);
                }

                {//copy top right corner to the bottom left corner
                    DungeonTile tile = dungeonTiles[i][DUNGEON_DIM - 1][0];
                    addBlock(i, tile, -.5f, .5f, DUNGEON_DIM + .5f);
                }

                {//copy top left corner to the bottom right corner
                    DungeonTile tile = dungeonTiles[i][0][0];
                    addBlock(i, tile, DUNGEON_DIM + .5f, .5f, DUNGEON_DIM + .5f);
                }

            }

            createMiniMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * finds the up ladder on the first level and puts you there
     */
    private void setStartPosition() {
        for (int y = 0; y < DUNGEON_DIM; y++) {
            for (int x = 0; x < DUNGEON_DIM; x++) {
                DungeonTile tile = dungeonTiles[currentLevel][x][y];
                if (tile == DungeonTile.NOTHING && currentPos == null) {
                    currentPos = new Vector3(x + .5f, .5f, y + .5f);
                }
                if (tile == DungeonTile.LADDER_UP) {
                    currentPos = new Vector3(x + .5f, .5f, y + .5f);
                }
            }
        }

        createMiniMap();
        moveMiniMapIcon();

    }

    public void restoreSaveGameLocation(int x, int y, int z, Direction orientation) {

        currentPos = new Vector3(x + .5f, .5f, y + .5f);
        camera.position.set(currentPos);
        currentDir = orientation;
        currentLevel = z;

        if (currentDir == Direction.EAST) {
            camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
        } else if (currentDir == Direction.WEST) {
            camera.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
        } else if (currentDir == Direction.NORTH) {
            camera.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
        } else if (currentDir == Direction.SOUTH) {
            camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
        }

        createMiniMap();
        moveMiniMapIcon();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        batch.dispose();
        decalBatch.dispose();
        MINI_MAP_TEXTURE.dispose();
        miniMapIcon.dispose();
        stage.dispose();
        for (ModelInstance mi : floor) {
            mi.model.dispose();
        }
        for (ModelInstance mi : ceiling) {
            mi.model.dispose();
        }
    }

    @Override
    public Vector3 getMapPixelCoords(int x, int y) {
        return null;
    }

    @Override
    public Vector3 getCurrentMapCoords() {
        return null;
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        lightFactor += Gdx.graphics.getDeltaTime();
        float lightSize = 4.75f + 0.25f * (float) Math.sin(lightFactor) + .2f * MathUtils.random();

        Vector3 ll = isTorchOn ? nll : vdll;
        ll = isTorchOn ? nll2 : vdll;
        fixedLight.set(ll.x, ll.y, ll.z, currentPos.x, currentPos.y + .35f, currentPos.z, lightSize);
        ((ColorAttribute) pLight.material.get(ColorAttribute.Diffuse)).color.set(fixedLight.color);
        pLight.worldTransform.setTranslation(fixedLight.position);

        Gdx.gl.glViewport(32, 64, Exodus.MAP_WIDTH, Exodus.MAP_HEIGHT);

        camera.update();

        modelBatch.begin(camera);

        modelBatch.render(pLight);

        for (ModelInstance i : floor) {
            modelBatch.render(i, environment);
        }
        for (ModelInstance i : ceiling) {
            modelBatch.render(i, environment);
        }

        for (DungeonTileModelInstance i : modelInstances) {
            if (i.getLevel() == currentLevel) {
                modelBatch.render(i.getInstance(), environment);
            }
        }

        modelBatch.end();

        for (Creature cr : dngMap.getMap().getCreatures()) {
            decalBatch.add(cr.getDecal());
        }
        decalBatch.flush();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.begin();
        batch.draw(Exodus.backGround, 0, 0);
        if (showMiniMap) {
            batch.draw(MINI_MAP_TEXTURE, xalignMM, yalignMM);
            batch.draw(miniMap, xalignMM, yalignMM);
        }
        Exodus.hud.render(batch, context.getParty());
        Exodus.font.draw(batch, this.dngMap.getLabel(), 315, Exodus.SCREEN_HEIGHT - 7);
        Exodus.font.draw(batch, "Level " + (currentLevel + 1), 515, Exodus.SCREEN_HEIGHT - 7);
        if (showZstats > 0) {
            //context.getParty().getSaveGame().renderZstats(showZstats, Exodus.font, batch, Exodus.SCREEN_HEIGHT);
        }
        batch.end();

        stage.act();
        stage.draw();

    }

    public void addBlock(int level, DungeonTile tile, float tx, float ty, float tz) {
        ModelBuilder builder = new ModelBuilder();
        if (tile == DungeonTile.WALL) {
            Model model = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/mortar.png", Texture.class))), Usage.Position | Usage.Normal | Usage.TextureCoordinates);
            ModelInstance instance = new ModelInstance(model, tx, ty, tz);
            //rotate so the texture is aligned right
            instance.transform.setFromEulerAngles(0, 0, 90).trn(tx, ty, tz);
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);
        } else if (tile.toString().startsWith("FOUNTAIN")) {
            ModelInstance instance = new ModelInstance(fountainModel, tx - .15f, 0, tz + .2f);
            instance.nodes.get(0).scale.set(.010f, .010f, .010f);
            instance.calculateTransforms();
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);
        } else if (tile.toString().startsWith("LADDER")) {
            ModelInstance instance = new ModelInstance(ladderModel, tx, 0, tz);
            instance.nodes.get(0).scale.set(.060f, .060f, .060f);
            instance.calculateTransforms();
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);

            Model manhole = builder.createCylinder(.75f, .02f, .75f, 32, new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)), Usage.Position | Usage.Normal);
            if (tile == DungeonTile.LADDER_DOWN) {
                instance = new ModelInstance(manhole, tx, 0, tz);
                modelInstances.add(new DungeonTileModelInstance(instance, tile, level));
            } else if (tile == DungeonTile.LADDER_UP) {
                instance = new ModelInstance(manhole, tx, 1, tz);
                modelInstances.add(new DungeonTileModelInstance(instance, tile, level));
            } else if (tile == DungeonTile.LADDER_UP_DOWN) {
                instance = new ModelInstance(manhole, tx, 0, tz);
                modelInstances.add(new DungeonTileModelInstance(instance, tile, level));
                instance = new ModelInstance(manhole, tx, 1, tz);
                modelInstances.add(new DungeonTileModelInstance(instance, tile, level));
            }

        } else if (tile == DungeonTile.CHEST) {
            ModelInstance instance = new ModelInstance(chestModel, tx, 0, tz);
            instance.nodes.get(0).scale.set(.010f, .010f, .010f);
            instance.calculateTransforms();
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);
            in.x = (int) tx;
            in.y = (int) tz;
        } else if (tile.toString().startsWith("MARK")) {
            ModelInstance instance = new ModelInstance(orbModel, tx, .5f, tz);
            instance.nodes.get(0).scale.set(.0025f, .0025f, .0025f);
            instance.calculateTransforms();
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            in.x = (int) tx;
            in.y = (int) tz;
            modelInstances.add(in);
        } else if (tile == DungeonTile.TIME_LORD) {
            ModelInstance instance = new ModelInstance(avatarModel, tx, 0, tz);
            instance.transform.scale(.010f, .010f, .010f);
            instance.transform.rotate(0, 1, 0, 55);//rotation 75
            instance.calculateTransforms();
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            in.x = (int) tx;
            in.y = (int) tz;
            modelInstances.add(in);
        } else if (tile.toString().startsWith("FIELD")) {
            Color c = Color.GREEN;
            if (tile == DungeonTile.FIELD_ENERGY) {
                c = Color.BLUE;
            }
            if (tile == DungeonTile.FIELD_FIRE) {
                c = Color.RED;
            }
            if (tile == DungeonTile.FIELD_SLEEP) {
                c = Color.PURPLE;
            }
            Model model = builder.createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(c), ColorAttribute.createSpecular(c), new BlendingAttribute(0.7f)), Usage.Position | Usage.Normal);
            ModelInstance instance = new ModelInstance(model, tx, .5f, tz);
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);
            in.x = (int) tx;
            in.y = (int) tz;
        } else if (tile == DungeonTile.DOOR || tile == DungeonTile.SECRET_DOOR) {
            Model model = builder.createBox(1, 1, 1, new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/mortar.png", Texture.class))), Usage.Position | Usage.TextureCoordinates | Usage.Normal);
            ModelInstance instance = new ModelInstance(model, tx, ty, tz);
            instance.transform.setFromEulerAngles(0, 0, 90).trn(tx, ty, tz);
            DungeonTileModelInstance in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);

            Material matDoor = null;
            if (tile == DungeonTile.DOOR) {
                matDoor = new Material(TextureAttribute.createDiffuse(assets.get("assets/graphics/door.png", Texture.class)));
            } else {
                matDoor = new Material(new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY), ColorAttribute.createSpecular(Color.DARK_GRAY), new BlendingAttribute(0.3f)));
            }

            model = builder.createBox(1.04f, .85f, .6f, matDoor, Usage.Position | Usage.TextureCoordinates | Usage.Normal);
            instance = new ModelInstance(model, tx, .4f, tz);
            in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);

            model = builder.createBox(.6f, .85f, 1.04f, matDoor, Usage.Position | Usage.TextureCoordinates | Usage.Normal);
            instance = new ModelInstance(model, tx, .4f, tz);
            in = new DungeonTileModelInstance(instance, tile, level);
            modelInstances.add(in);
        }
    }

    public void createMiniMap() {

        if (miniMap != null) {
            miniMap.dispose();
        }

        Pixmap pixmap = new Pixmap(MM_BKGRND_DIM, MM_BKGRND_DIM, Format.RGBA8888);
        for (int y = 0; y < DUNGEON_DIM; y++) {
            for (int x = 0; x < DUNGEON_DIM; x++) {
                DungeonTile tile = dungeonTiles[currentLevel][x][y];
                if (tile == DungeonTile.WALL || tile == DungeonTile.SECRET_DOOR) {
                    pixmap.setColor(0.3f, 0.3f, 0.3f, 0.7f);
                    pixmap.fillRectangle(OFST + (x * DIM), OFST + (y * DIM), DIM, DIM);
                } else if (tile == DungeonTile.DOOR) {
                    pixmap.setColor(0.6f, 0.6f, 0.6f, 0.7f);
                    pixmap.fillRectangle(OFST + (x * DIM), OFST + (y * DIM), DIM, DIM);
                } else if (tile.toString().startsWith("FIELD")) {
                    Color c = Color.GREEN;
                    if (tile == DungeonTile.FIELD_ENERGY) {
                        c = Color.BLUE;
                    }
                    if (tile == DungeonTile.FIELD_FIRE) {
                        c = Color.RED;
                    }
                    if (tile == DungeonTile.FIELD_SLEEP) {
                        c = Color.PURPLE;
                    }
                    pixmap.setColor(c);
                    pixmap.fillRectangle(OFST + (x * DIM), OFST + (y * DIM), DIM, DIM);
                } else if (tile.toString().startsWith("LADDER")) {
                    drawLadderTriangle(tile, pixmap, x, y);
                }
            }
        }

        miniMap = new Texture(pixmap);
        pixmap.dispose();
    }

    private void drawLadderTriangle(DungeonTile tile, Pixmap pixmap, int x, int y) {
        int cx = OFST + (x * DIM);
        int cy = OFST + (y * DIM);
        pixmap.setColor(Color.YELLOW);
        if (tile == DungeonTile.LADDER_DOWN) {
            pixmap.fillTriangle(cx + 0, cy + 0, cx + 4, cy + 7, cx + 7, cy + 0);
        } else if (tile == DungeonTile.LADDER_UP) {
            pixmap.fillTriangle(cx + 0, cy + 7, cx + 4, cy + 0, cx + 7, cy + 7);
        } else if (tile == DungeonTile.LADDER_UP_DOWN) {
            pixmap.fillTriangle(cx + 0, cy + 4, cx + 4, cy + 0, cx + 7, cy + 4);
            pixmap.fillTriangle(cx + 0, cy + 4, cx + 4, cy + 7, cx + 7, cy + 4);
        }
    }

    private Texture createMiniMapIcon(Direction dir) {
        Pixmap pixmap = new Pixmap(DIM, DIM, Format.RGBA8888);
        pixmap.setColor(1f, 0f, 0f, 1f);
        if (dir == Direction.EAST) {
            pixmap.fillTriangle(0, 0, 0, 7, 7, 4);
        } else if (dir == Direction.NORTH) {
            pixmap.fillTriangle(0, 7, 4, 0, 7, 7);
        } else if (dir == Direction.WEST) {
            pixmap.fillTriangle(7, 0, 0, 4, 7, 7);
        } else if (dir == Direction.SOUTH) {
            pixmap.fillTriangle(0, 0, 4, 7, 7, 0);
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public InputProcessor getPeerGemInputProcessor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class MiniMapIcon extends Actor {

        Texture north;
        Texture south;
        Texture east;
        Texture west;

        public MiniMapIcon() {
            super();
            //could not get rotateBy to work so needed to do it this way
            this.north = createMiniMapIcon(Direction.NORTH);
            this.east = createMiniMapIcon(Direction.EAST);
            this.west = createMiniMapIcon(Direction.WEST);
            this.south = createMiniMapIcon(Direction.SOUTH);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (!showMiniMap) {
                return;
            }
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            Texture t = north;
            if (currentDir == Direction.EAST) {
                t = east;
            }
            if (currentDir == Direction.WEST) {
                t = west;
            }
            if (currentDir == Direction.SOUTH) {
                t = south;
            }
            batch.draw(t, getX(), getY());
        }

        public void dispose() {
            north.dispose();
            east.dispose();
            west.dispose();
            south.dispose();
        }
    }

    public void moveMiniMapIcon() {
        miniMapIcon.setX(xalignMM + OFST + (Math.round(currentPos.x) - 1) * DIM);
        miniMapIcon.setY(yalignMM + MM_BKGRND_DIM - OFST - (Math.round(currentPos.z)) * DIM);
    }

    public void battleWandering(Creature cr, int x, int y) {
        if (cr == null) {
            return;
        }
        Maps contextMap = Maps.get(dngMap.getId());
        DungeonTile tile = dungeonTiles[currentLevel][x][y];
        TiledMap tmap = new UltimaTiledMapLoader(tile.getCombatMap(), Exodus.standardAtlas, 11, 11, tilePixelWidth, tilePixelHeight).load();
        context.setCurrentTiledMap(tmap);
        CombatScreen sc = new CombatScreen(this, context, contextMap, tile.getCombatMap().getMap(), tmap, cr.getTile(), Exodus.creatures, Exodus.standardAtlas);
        mainGame.setScreen(sc);
        currentEncounter = cr;
    }

    @Override
    public void partyDeath() {
        mainGame.setScreen(new StartScreen(mainGame));
    }

    @Override
    public void endCombat(boolean isWon, BaseMap combatMap, boolean wounded) {

        mainGame.setScreen(this);

        if (isWon) {

            if (currentEncounter != null) {
                log("Victory!");
                int x = (Math.round(currentPos.x) - 1);
                int y = (Math.round(currentPos.z) - 1);
                /* add a chest, if the creature leaves one */
                if (!currentEncounter.getNochest() && dungeonTiles[currentLevel][x][y] == DungeonTile.NOTHING) {
                    ModelInstance instance = new ModelInstance(chestModel, x + .5f, 0, y + .5f);
                    instance.nodes.get(0).scale.set(.010f, .010f, .010f);
                    instance.calculateTransforms();
                    DungeonTileModelInstance in = new DungeonTileModelInstance(instance, DungeonTile.CHEST, currentLevel);
                    in.x = x;
                    in.y = y;
                    modelInstances.add(in);
                    dungeonTiles[currentLevel][x][y] = DungeonTile.CHEST;
                }
            }

        } else {
            if (combatMap.getType() == MapType.combat && context.getParty().didAnyoneFlee()) {
                log("Battle is lost!");
                //no flee penalty in dungeons
            } else if (!context.getParty().isAnyoneAlive()) {
                partyDeath();
            }
        }

        if (currentEncounter != null) {
            dngMap.getMap().removeCreature(currentEncounter);
            currentEncounter = null;
        }

        //if exiting dungeon rooms, move out of the room with orientation to next coordinate
        if (combatMap.getType() == MapType.dungeon) {
            Direction exitDirection = context.getParty().getActivePartyMember().combatMapExitDirection;
            if (exitDirection != null) {
                currentDir = exitDirection;

                int x = (Math.round(currentPos.x) - 1);
                int y = (Math.round(currentPos.z) - 1);

                //check for portal to another dungeon
                for (Portal p : combatMap.getPortals()) {
                    if (p.getX() == x && p.getY() == y && p.getExitDirection() == exitDirection) {
                        Maps m = Maps.get(p.getDestmapid());
                        if (m == dngMap) {
                            break;
                        }
                        log("Entering " + m.getLabel() + "!");
                        DungeonScreen sc = new DungeonScreen(this.gameScreen, this.context, m);
                        sc.restoreSaveGameLocation(p.getStartx(), p.getStarty(), p.getStartlevel(), currentDir);
                        mainGame.setScreen(sc);
                        this.gameScreen.newMapPixelCoords = this.gameScreen.getMapPixelCoords(p.getRetroActiveDest().getX(), p.getRetroActiveDest().getY());
                        return;
                    }
                }

                if (exitDirection == Direction.EAST) {
                    x = x + 1;
                    if (x > 7) {
                        x = 0;
                    }
                } else if (exitDirection == Direction.WEST) {
                    x = x - 1;
                    if (x < 0) {
                        x = 7;
                    }
                } else if (exitDirection == Direction.NORTH) {
                    y = y - 1;
                    if (y < 0) {
                        y = 7;
                    }
                } else if (exitDirection == Direction.SOUTH) {
                    y = y + 1;
                    if (y > 7) {
                        y = 0;
                    }
                }

                DungeonTile tile = dungeonTiles[currentLevel][x][y];
                try {
                    if (tile != DungeonTile.WALL) {
                        currentPos = new Vector3(x + .5f, .5f, y + .5f);
                        camera.position.set(currentPos);
                        if (currentDir == Direction.EAST) {
                            camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
                        } else if (currentDir == Direction.WEST) {
                            camera.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
                        } else if (currentDir == Direction.NORTH) {
                            camera.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
                        } else if (currentDir == Direction.SOUTH) {
                            camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
                        }
                        checkTileAffects(tile, x, y);
                        moveMiniMapIcon();
                    }
                } catch (PartyDeathException e) {
                    partyDeath();
                }

            }
        }

    }

    @Override
    public boolean keyUp(int keycode) {

        int x = (Math.round(currentPos.x) - 1);
        int y = (Math.round(currentPos.z) - 1);
        DungeonTile tile = dungeonTiles[currentLevel][x][y];

        if (keycode == Keys.LEFT) {

            if (currentDir == Direction.EAST) {
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
                currentDir = Direction.NORTH;
            } else if (currentDir == Direction.WEST) {
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
                currentDir = Direction.SOUTH;
            } else if (currentDir == Direction.NORTH) {
                camera.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
                currentDir = Direction.WEST;
            } else if (currentDir == Direction.SOUTH) {
                camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
                currentDir = Direction.EAST;
            }
            setCreatureRotations();
            return false;

        } else if (keycode == Keys.RIGHT) {

            if (currentDir == Direction.EAST) {
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
                currentDir = Direction.SOUTH;
            } else if (currentDir == Direction.WEST) {
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
                currentDir = Direction.NORTH;
            } else if (currentDir == Direction.NORTH) {
                camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
                currentDir = Direction.EAST;
            } else if (currentDir == Direction.SOUTH) {
                camera.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
                currentDir = Direction.WEST;
            }
            setCreatureRotations();
            return false;

        } else if (keycode == Keys.UP) {

            //forward
            if (currentDir == Direction.EAST) {
                x = x + 1;
                if (x > DUNGEON_DIM - 1) {
                    x = 0;
                }
            } else if (currentDir == Direction.WEST) {
                x = x - 1;
                if (x < 0) {
                    x = DUNGEON_DIM - 1;
                }
            } else if (currentDir == Direction.NORTH) {
                y = y - 1;
                if (y < 0) {
                    y = DUNGEON_DIM - 1;
                }
            } else if (currentDir == Direction.SOUTH) {
                y = y + 1;
                if (y > DUNGEON_DIM - 1) {
                    y = 0;
                }
            }

            try {
                move(dungeonTiles[currentLevel][x][y], x, y);
            } catch (PartyDeathException e) {
                partyDeath();
            }
            return false;

        } else if (keycode == Keys.DOWN) {

            //backwards
            if (currentDir == Direction.EAST) {
                x = x - 1;
                if (x < 0) {
                    x = DUNGEON_DIM - 1;
                }
            } else if (currentDir == Direction.WEST) {
                x = x + 1;
                if (x > DUNGEON_DIM - 1) {
                    x = 0;
                }
            } else if (currentDir == Direction.NORTH) {
                y = y + 1;
                if (y > DUNGEON_DIM - 1) {
                    y = 0;
                }
            } else if (currentDir == Direction.SOUTH) {
                y = y - 1;
                if (y < 0) {
                    y = DUNGEON_DIM - 1;
                }
            }

            try {
                move(dungeonTiles[currentLevel][x][y], x, y);
            } catch (PartyDeathException e) {
                partyDeath();
            }
            return false;

        } else if (keycode == Keys.K) {
            if (tile == DungeonTile.LADDER_UP || tile == DungeonTile.LADDER_UP_DOWN) {
                currentLevel--;
                if (currentLevel < 0) {
                    currentLevel = 0;
                    if (mainGame != null) {
                        mainGame.setScreen(gameScreen);
                        dispose();
                    }
                } else {
                    createMiniMap();
                }
            }
            return false;

        } else if (keycode == Keys.D) {
            if (tile == DungeonTile.LADDER_DOWN || tile == DungeonTile.LADDER_UP_DOWN) {
                currentLevel++;
                if (currentLevel > DUNGEON_LVLS) {
                    currentLevel = DUNGEON_LVLS;
                } else {
                    createMiniMap();
                }
            }
            return false;

        } else if (keycode == Keys.Q) {
            context.saveGame(x, y, currentLevel, currentDir, dngMap);
            log("Saved Game.");
            return false;

        } else if (keycode == Keys.C) {
            log("Cast Spell: ");
            log("Who casts (1-8): ");
            //Gdx.input.setInputProcessor(new SpellInputProcessor(this, context, stage, x, y, null));

        } else if (keycode == Keys.I) {

            isTorchOn = !isTorchOn;

        } else if (keycode == Keys.G || keycode == Keys.R || keycode == Keys.W) {
            log("Which party member?");
            Gdx.input.setInputProcessor(sip);
            sip.setinitialKeyCode(keycode, tile, x, y);

        } else if (keycode == Keys.H) {
            //CombatScreen.holeUp(this.dngMap, x, y, this, context, Exodus.creatures, Exodus.standardAtlas, false);
            return false;

        } else if (keycode == Keys.V) {
            showMiniMap = !showMiniMap;
        } else if (keycode == Keys.M) {

            //mainGame.setScreen(new MixtureScreen(mainGame, this, Exodus.skin, context.getParty()));
        } else if (keycode == Keys.S) {
            if (tile == DungeonTile.ALTAR) {

            } else {

                if (tile.getValue() >= 144 && tile.getValue() <= 148) {
                    log("You find a Fountain. Who drinks?");
                } else if (tile == DungeonTile.ORB) {
                    log("You find a Magical Orb...Who touches?");
                } else {
                    log("Who searches?");
                }

                Gdx.input.setInputProcessor(sip);
                sip.setinitialKeyCode(keycode, tile, x, y);
            }

        } else if (keycode == Keys.U) {
//            if (dngMap == Maps.ABYSS && tile == DungeonTile.ALTAR) {
//                log("Use which item?");
//                log("");
//                AbyssInputAdapter aia = new AbyssInputAdapter(x, y);
//                Gdx.input.setInputProcessor(aia);
//                return false;
//            }

        } else if (keycode == Keys.Z) {
//            showZstats = showZstats + 1;
//            if (showZstats >= STATS_PLAYER1 && showZstats <= STATS_PLAYER8) {
//                if (showZstats > context.getParty().getMembers().size()) {
//                    showZstats = STATS_WEAPONS;
//                }
//            }
//            if (showZstats > STATS_SPELLS) {
//                showZstats = STATS_NONE;
//            }
            return false;

        } else {
            log("Pass");
        }

        finishTurn(x, y);

        return false;
    }

    private void move(DungeonTile tile, int x, int y) throws PartyDeathException {

        if (tile != DungeonTile.WALL && tile != DungeonTile.FIELD_ENERGY) {
            currentPos = new Vector3(x + .5f, .5f, y + .5f);
            camera.position.set(currentPos);
            if (currentDir == Direction.EAST) {
                camera.lookAt(currentPos.x + 1, currentPos.y, currentPos.z);
            } else if (currentDir == Direction.WEST) {
                camera.lookAt(currentPos.x - 1, currentPos.y, currentPos.z);
            } else if (currentDir == Direction.NORTH) {
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z - 1);
            } else if (currentDir == Direction.SOUTH) {
                camera.lookAt(currentPos.x, currentPos.y, currentPos.z + 1);
            }
            moveMiniMapIcon();
            checkTileAffects(tile, x, y);
        }

        if (tile == DungeonTile.MISTY_WRITINGS) {

        }

        finishTurn(x, y);
    }

    private void checkTileAffects(DungeonTile tile, int x, int y) throws PartyDeathException {
        switch (tile) {
            case WIND_TRAP:
                log("Wind extinguished your torch!");
                Sounds.play(Sound.WIND);
                isTorchOn = false;
                break;
            case PIT_TRAP:
                log("Pit!");
                context.getParty().applyEffect(TileEffect.LAVA);
                Sounds.play(Sound.BOOM);
                dungeonTiles[currentLevel][x][y] = DungeonTile.NOTHING;
                break;
            case FIELD_POISON:
                context.getParty().applyEffect(TileEffect.POISONFIELD);
                Sounds.play(Sound.POISON_DAMAGE);
                break;
            case FIELD_SLEEP:
                context.getParty().applyEffect(TileEffect.SLEEP);
                Sounds.play(Sound.SLEEP);
                break;
            case FIELD_FIRE:
                context.getParty().applyEffect(TileEffect.LAVA);
                Sounds.play(Sound.FIREFIELD);
                break;
            case MISTY_WRITINGS:
                Label label = new Label(texts[currentLevel], Exodus.skin, "ultima", Color.WHITE);
                label.setPosition(32*6, 32*12);
                stage.addActor(label);
                label.addAction(sequence(Actions.moveTo(32*6, 32*8, 2f), Actions.fadeOut(2f), Actions.removeActor(label)));
                break;
            case TIME_LORD:
                Label greet = new Label("Greetings!\nI am the Time Lord.\nTo seal EXODUS,\nremember this..\nThere is only one way.\nLove, Sol.... Moon, Death.", 
                        Exodus.skin, "small-ultima", Color.BLUE);
                greet.setPosition(32*6, 32*10);
                stage.addActor(greet);
                greet.addAction(sequence(Actions.moveTo(32*6, 32*6, 4f), Actions.fadeOut(1f), Actions.removeActor(greet)));
                break;
                
        }
    }

    @Override
    public void finishTurn(int currentX, int currentY) {
        context.getAura().passTurn();

        creatureCleanup(currentX, currentY);

        if (checkRandomDungeonCreatures()) {
            spawnDungeonCreature(null, currentX, currentY);
        }

        moveDungeonCreatures(this, currentX, currentY);
    }

    public void getMark(DungeonTile type, int index) {
        if (index >= context.getParty().getMembers().size()) {
            return;
        }
        Party.PartyMember pm = context.getParty().getMember(index);
        int x = (Math.round(currentPos.x) - 1);
        int y = (Math.round(currentPos.z) - 1);
        
        log("You touch the " + type.getType() + "!");
        
        switch(type) {
            case MARK_KINGS:
                pm.getPlayer().marks[0] = 1;
                break;
            case MARK_FIRE:
                pm.getPlayer().marks[1] = 1;
                break;
            case MARK_SNAKE:
                pm.getPlayer().marks[2] = 1;
                break;
            case MARK_FORCE:
                pm.getPlayer().marks[3] = 1;
                break;
        }

        Sounds.play(Sound.LIGHTNING);

        try {
            pm.applyDamage(50, false);
        } catch (PartyDeathException pde) {
            partyDeath();
        }

//        //remove model instance
//        DungeonTileModelInstance mark = null;
//        for (DungeonTileModelInstance dmi : modelInstances) {
//            if (dmi.getTile() == DungeonTile.MARK) {
//                if (dmi.x == x && dmi.y == y && dmi.getLevel() == currentLevel) {
//                    mark = dmi;
//                    break;
//                }
//            }
//        }
//        modelInstances.remove(mark);
//        dungeonTiles[currentLevel][x][y] = DungeonTile.NOTHING;

    }

    public void dungeonDrinkFountain(DungeonTile type, int index) {
        try {
            if (index >= context.getParty().getMembers().size()) {
                return;
            }
            Party.PartyMember pm = context.getParty().getMember(index);
            switch (type) {
                case FOUNTAIN_PLAIN:
                    log("Hmmm--No Effect!");
                    break;
                case FOUNTAIN_HEAL:
                    if (pm.heal(HealType.FULLHEAL)) {
                        Sounds.play(Sound.HEALING);
                        log("Ahh-Refreshing!");
                    } else {
                        log("Hmmm--No Effect!");
                    }
                    break;
                case FOUNTAIN_ACID:
                    pm.applyDamage(25, false);
                    Sounds.play(Sound.DAMAGE_EFFECT);
                    log("Bleck--Nasty!");
                    break;
                case FOUNTAIN_CURE:
                    if (pm.heal(HealType.CURE)) {
                        Sounds.play(Sound.HEALING);
                        log("Hmmm--Delicious!");
                    } else {
                        log("Hmmm--No Effect!");
                    }
                    break;
                case FOUNTAIN_POISON:
                    if (pm.getPlayer().status != StatusType.POISONED) {
                        Sounds.play(Sound.DAMAGE_EFFECT);
                        pm.applyEffect(TileEffect.POISON);
                        pm.applyDamage(25, false);
                        log("Argh-Choke-Gasp!");
                    } else {
                        log("Hmm--No Effect!");
                    }
                    break;
            }
        } catch (PartyDeathException pde) {
            partyDeath();
        }
    }

    public boolean validTeleportLocation(int x, int y, int z) {
        return dungeonTiles[z][x][y] == DungeonTile.NOTHING;
    }

    public void getChest(int index, int x, int y) {
        try {
            DungeonTileModelInstance chest = null;
            for (DungeonTileModelInstance dmi : modelInstances) {
                if (dmi.getTile() == DungeonTile.CHEST) {
                    if (dmi.x == x && dmi.y == y && dmi.getLevel() == currentLevel) {
                        chest = dmi;
                        break;
                    }
                }
            }

            if (chest != null) {

                Party.PartyMember pm = context.getParty().getMember(index);
                if (pm == null) {
                    System.err.println("member is null " + index);
                }
                if (pm.getPlayer() == null) {
                    System.err.println("player is null " + index);
                }
                context.getChestTrapHandler(pm);
                log(String.format("The Chest Holds: %d Gold", context.getParty().getChestGold(pm)));

                //remove chest model instance
                modelInstances.remove(chest);
                dungeonTiles[currentLevel][x][y] = DungeonTile.NOTHING;

            } else {
                log("Not Here!");
            }
        } catch (PartyDeathException e) {
            partyDeath();
        }
    }

    private void creatureCleanup(int currentX, int currentY) {
        Iterator<Creature> i = dngMap.getMap().getCreatures().iterator();
        while (i.hasNext()) {
            Creature cr = i.next();
            if (cr.currentLevel != this.currentLevel) {
                i.remove();
            }
        }
    }

    private boolean checkRandomDungeonCreatures() {
        int spawnValue = 32;// - (currentLevel << 2);
        if (dngMap.getMap().getCreatures().size() >= MAX_WANDERING_CREATURES_IN_DUNGEON || rand.nextInt(spawnValue) != 0) {
            return false;
        }
        return true;
    }

    /**
     * spawn a dungeon creature in a random walkable place in the level.
     * monsters can walk thru rooms and such but not walls.
     */
    private boolean spawnDungeonCreature(Creature creature, int currentX, int currentY) {

        int dx = 0;
        int dy = 0;
        int tmp = 0;

        boolean ok = false;
        int tries = 0;
        int MAX_TRIES = 10;

        while (!ok && (tries < MAX_TRIES)) {
            dx = 7;
            dy = rand.nextInt(7);

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

            if (dx < 0) {
                dx = DUNGEON_DIM + dx;
            } else if (dx > DUNGEON_DIM - 1) {
                dx = dx - DUNGEON_DIM;
            }
            if (dy < 0) {
                dy = DUNGEON_DIM + dy;
            } else if (dy > DUNGEON_DIM - 1) {
                dy = dy - DUNGEON_DIM;
            }

            /* make sure we can spawn the creature there */
            if (creature != null) {
                DungeonTile tile = dungeonTiles[currentLevel][dx][dy];
                if (tile.getCreatureWalkable()) {
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

            //Make a Weighted Random Choice with level as a factor
            int total = 0;
            for (CreatureType ct : CreatureType.values()) {
                total += ct.getSpawnLevel() <= currentLevel ? ct.getSpawnWeight() : 0;
            }

            int thresh = rand.nextInt(total);
            CreatureType monster = null;

            for (CreatureType ct : CreatureType.values()) {
                thresh -= ct.getSpawnLevel() <= currentLevel ? ct.getSpawnWeight() : 0;
                if (thresh < 0) {
                    monster = ct;
                    break;
                }
            }

            creature = Exodus.creatures.getInstance(monster, Exodus.standardAtlas);
        }

        if (creature != null) {
            creature.currentX = dx;
            creature.currentY = dy;
            creature.currentLevel = currentLevel;
            dngMap.getMap().addCreature(creature);

            System.out.println("spawned in dungeon: " + creature.getTile());
            setCreatureRotations();
        } else {
            return false;
        }

        return true;
    }

    private void moveDungeonCreatures(BaseScreen screen, int avatarX, int avatarY) {
        for (Creature cr : dngMap.getMap().getCreatures()) {

            int mask = getValidMovesMask(cr.currentX, cr.currentY, cr, avatarX, avatarY);
            //dont use wrap border behavior with the dungeon maps
            Direction dir = Utils.getPath(MapBorderBehavior.wrap, DUNGEON_DIM, DUNGEON_DIM, avatarX, avatarY, mask, true, cr.currentX, cr.currentY);
            if (dir == null) {
                continue;
            }

            if (dir == Direction.NORTH) {
                cr.currentY = cr.currentY - 1 < 0 ? DUNGEON_DIM - 1 : cr.currentY - 1;
            }
            if (dir == Direction.SOUTH) {
                cr.currentY = cr.currentY + 1 >= DUNGEON_DIM ? 0 : cr.currentY + 1;
            }
            if (dir == Direction.EAST) {
                cr.currentX = cr.currentX + 1 >= DUNGEON_DIM ? 0 : cr.currentX + 1;
            }
            if (dir == Direction.WEST) {
                cr.currentX = cr.currentX - 1 < 0 ? DUNGEON_DIM - 1 : cr.currentX - 1;
            }

            cr.getDecal().setPosition(cr.currentX + .5f, .3f, cr.currentY + .5f);

            //if touches avatar then invoke battle!
            if (Utils.movementDistance(MapBorderBehavior.wrap, DUNGEON_DIM, DUNGEON_DIM, avatarX, avatarY, cr.currentX, cr.currentY) == 0) {
                battleWandering(cr, avatarX, avatarY);
            }

        }
    }

    //rotates the 2d sprite decal so that it faces the avatar in 3d space
    private void setCreatureRotations() {
        for (Creature cr : dngMap.getMap().getCreatures()) {
            if (currentDir == Direction.NORTH) {
                cr.getDecal().setRotationY(0);
            }
            if (currentDir == Direction.SOUTH) {
                cr.getDecal().setRotationY(0);
            }
            if (currentDir == Direction.EAST) {
                cr.getDecal().setRotationY(90);
            }
            if (currentDir == Direction.WEST) {
                cr.getDecal().setRotationY(90);
            }
        }
    }

    private int getValidMovesMask(int x, int y, Creature cr, int avatarX, int avatarY) {

        int mask = 0;

        DungeonTile north = dungeonTiles[currentLevel][x][y - 1 < 0 ? DUNGEON_DIM - 1 : y - 1];
        DungeonTile south = dungeonTiles[currentLevel][x][y + 1 >= DUNGEON_DIM ? 0 : y + 1];
        DungeonTile east = dungeonTiles[currentLevel][x + 1 >= DUNGEON_DIM ? 0 : x + 1][y];
        DungeonTile west = dungeonTiles[currentLevel][x - 1 < 0 ? DUNGEON_DIM - 1 : x - 1][y];

        mask = addToMask(Direction.NORTH, mask, north, x, y - 1 < 0 ? DUNGEON_DIM - 1 : y - 1, cr, avatarX, avatarY);
        mask = addToMask(Direction.SOUTH, mask, south, x, y + 1 >= DUNGEON_DIM ? 0 : y + 1, cr, avatarX, avatarY);
        mask = addToMask(Direction.EAST, mask, east, x + 1 >= DUNGEON_DIM - 1 ? 0 : x + 1, y, cr, avatarX, avatarY);
        mask = addToMask(Direction.WEST, mask, west, x - 1 < 0 ? DUNGEON_DIM - 1 : x - 1, y, cr, avatarX, avatarY);

        return mask;

    }

    private int addToMask(Direction dir, int mask, DungeonTile tile, int x, int y, Creature cr, int avatarX, int avatarY) {
        if (tile != null) {
            boolean canmove = false;
            if (tile.getCreatureWalkable()) {
                canmove = true;
            }
            for (Creature cre : dngMap.getMap().getCreatures()) {
                if (cre.currentX == x && cre.currentY == y && cre.currentLevel == cr.currentLevel) {
                    canmove = false;
                    break;
                }
            }
            if (canmove) {
                mask = Direction.addToMask(dir, mask);
            }
        }
        return mask;
    }

}
