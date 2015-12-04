/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exodus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.List;
import objects.BaseMap;
import objects.Tile;

/**
 *
 * @author Paul
 */
public class Sosaria extends Game {

    protected boolean perspective;
    protected Camera camera;
    private Environment environment;
    private SpriteBatch batch;
    public ModelBatch modelBatch;
    public CameraInputController inputController;
    public AssetManager assets;

    public List<ModelInstance> modelInstances = new ArrayList<>();

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "test";
        cfg.width = 1024;
        cfg.height = 768;
        new LwjglApplication(new Sosaria(), cfg);
    }

    @Override
    public void create() {

        Exodus ult = new Exodus();
        ult.create();

        batch = new SpriteBatch();
        modelBatch = new ModelBatch();

        FileHandleResolver resolver = new Constants.ClasspathResolver();

        assets = new AssetManager(resolver);
        assets.load("assets/graphics/dirt.png", Texture.class);
        assets.load("assets/graphics/Stone_Masonry.jpg", Texture.class);
        assets.load("assets/graphics/door.png", Texture.class);
        assets.load("assets/graphics/mortar.png", Texture.class);
        assets.load("assets/graphics/rock.png", Texture.class);
        assets.load("assets/graphics/grass.png", Texture.class);

        assets.update(2000);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.5f, 1f));

        camera = new PerspectiveCamera(67, Exodus.MAP_WIDTH, Exodus.MAP_HEIGHT);
        camera.near = 0.1f;
        camera.far = 1000f;

        Vector3 camPos = new Vector3(32, 6, 32);
        camera.position.set(camPos);
        camera.lookAt(camPos.x + 1, camPos.y, camPos.z);

        inputController = new CameraInputController(camera);
        inputController.rotateLeftKey = inputController.rotateRightKey = inputController.forwardKey = inputController.backwardKey = 0;
        inputController.translateUnits = 30f;

        Gdx.input.setInputProcessor(inputController);

        try {

            BaseMap map = Constants.Maps.AMBROSIA.getMap();

            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    Tile tile = map.getTile(x, y);
                    ModelInstance instance = null;
                    if (tile.getName().equals("mountains")) {
                        instance = createPolygonBox(Color.DARK_GRAY, 1, 2, 1, 0, x, 0, y);
                    } else if (tile.getName().equals("hills")) {
                        instance = createPolygonBox(Color.GRAY, 1, 1, 1, 0, x, 0, y);
                    } else if (tile.getName().equals("grass")) {
                        instance = createPolygonBox(Color.GREEN, 1, 1, 1, 0, x, 0, y);
                    } else if (tile.getName().equals("water")) {
                        instance = createPolygonBox(Color.BLUE, 1, 1, 1, 0, x, 0, y);
                    } else if (tile.getName().equals("sea")) {
                        instance = createPolygonBox(Color.NAVY, 1, 1, 1, 0, x, 0, y);
                    } else if (tile.getName().equals("shallows")) {
                        instance = createPolygonBox(Color.SKY, 1, 1, 1, 0, x, 0, y);
                    } else {
                        instance = createPolygonBox(Color.YELLOW, 1, 1, 1, 0, x, 0, y);
                    }

                    this.modelInstances.add(instance);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

        Gdx.gl.glViewport(32, 64, Exodus.MAP_WIDTH, Exodus.MAP_HEIGHT);

        camera.update();

        modelBatch.begin(camera);

        for (ModelInstance i : modelInstances) {
            modelBatch.render(i, environment);
        }

        modelBatch.end();

    }

    public ModelInstance createPolygonBox(Color color, float width, float height, float length, float rotation, float x, float y, float z) {

        Vector3 corner000 = new Vector3(-width / 2, -height / 2, -length / 2);
        Vector3 corner010 = new Vector3(width / 2, -height / 2, -length / 2);
        Vector3 corner100 = new Vector3(-width / 2, -height / 2, length / 2);
        Vector3 corner110 = new Vector3(width / 2, -height / 2, length / 2);

        Vector3 corner001 = new Vector3(-width / 2, height / 2, -length / 2);
        Vector3 corner011 = new Vector3(width / 2, height / 2, -length / 2);
        Vector3 corner101 = new Vector3(-width / 2, height / 2, length / 2);
        Vector3 corner111 = new Vector3(width / 2, height / 2, length / 2);

        Material material = new Material(ColorAttribute.createDiffuse(color));
        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        mb.part("box", GL30.GL_TRIANGLES, Usage.Position | Usage.Normal, material).box(corner000, corner010, corner100, corner110, corner001, corner011, corner101, corner111);
        Model model = mb.end();

        ModelInstance modelInstance = new ModelInstance(model);

        modelInstance.transform.rotate(new Vector3(0, 1, 0), rotation);
        modelInstance.transform.setTranslation(x, y, z);

        return modelInstance;
    }

}
