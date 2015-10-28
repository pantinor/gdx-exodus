/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exodus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.io.IOException;
import java.io.InputStream;
import objects.Tile;
import org.apache.commons.io.IOUtils;
import util.UltimaTiledMapLoader;

/**
 *
 * @author Paul
 */
public class StartScreen implements Screen, InputProcessor, Constants {

    float time = 0;
    Batch batch;
    OrthogonalTiledMapRenderer splashRenderer;
    OrthographicCamera camera;
    Viewport viewPort;
    Exodus mainGame;
    Stage stage;
    Texture title;
    BitmapFont font;
    IntroAnim animator = new IntroAnim();
    TiledMap splashMap;

    public StartScreen(Exodus main) {
        this.mainGame = main;

        title = new Texture(Gdx.files.classpath("assets/graphics/splash.png"));

        font = new BitmapFont(Gdx.files.classpath("assets/fonts/Calisto_24.fnt"));
        font.setColor(Color.WHITE);

        UltimaTiledMapLoader loader = new UltimaTiledMapLoader(Maps.SOSARIA, Exodus.standardAtlas, 19, 6, tilePixelWidth, tilePixelHeight);
        splashMap = loader.load(intromap, 19, 6, Exodus.baseTileSet, tilePixelWidth);
        splashRenderer = new OrthogonalTiledMapRenderer(splashMap);
        camera = new OrthographicCamera(19 * tilePixelWidth, 8 * tilePixelHeight);
        camera.position.set(tilePixelWidth * 10, tilePixelHeight * 8, 0);
        viewPort = new ScreenViewport(camera);

        batch = new SpriteBatch();

        stage = new Stage();
        //stage.addActor(init);
        //stage.addActor(journey);

        SequenceAction seq1 = Actions.action(SequenceAction.class);
        seq1.addAction(Actions.delay(.2f));
        seq1.addAction(Actions.run(animator));
        stage.addAction(Actions.forever(seq1));

        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));

        Exodus.music = Sounds.play(Sound.SPLASH, Exodus.musicVolume);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        time += Gdx.graphics.getDeltaTime();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float x = Exodus.SCREEN_WIDTH / 2 - 320;
        float y = 100;
        float width = 640;
        float height = 50;

        camera.update();

        splashRenderer.setView(camera.combined, 0, 0, 19 * tilePixelWidth, 8 * tilePixelHeight);
        splashRenderer.render();

        batch.begin();
        batch.draw(title, 0, 0);
        font.draw(batch, "From the detphs of hell...", 320, Exodus.SCREEN_HEIGHT - 364);
        font.draw(batch, "...he comes for VENGEANCE!", 320, Exodus.SCREEN_HEIGHT - 396);
        font.draw(batch, "LIBGDX Conversion by Paul Antinori", 300, 84);
        font.draw(batch, "Copyright 1983 Lord British", 350, 48);
        batch.end();

        stage.act();
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        viewPort.update(width, height, false);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    private class IntroAnim implements Runnable {

        int moveind = 0;

        @Override
        public void run() {

            if (moveind >= movesCommands.length) {
                moveind = 0;
                return;
            }

            if (movesCommands[moveind] == -1) {
                moveind++;
                return;
            }

            int idx = movesData[moveind] >> 1;

            Tile tile = Exodus.baseTileSet.getTileByIndex(idx);

            int y = 0;
            int x = movesCommands[moveind];
            while (x > 18) {
                y++;
                x -= 19;
            }

            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            Array<TextureAtlas.AtlasRegion> tileRegions = Exodus.standardAtlas.findRegions(tile.getName());
            Array<StaticTiledMapTile> ar = new Array<>();
            for (TextureAtlas.AtlasRegion r : tileRegions) {
                ar.add(new StaticTiledMapTile(r));
            }

            TiledMapTile tmt = null;
            if (tileRegions.size > 1 && tile.getIndex() != 11 && tile.getIndex() != 15) {//dont animate the ships
                tmt = new AnimatedTiledMapTile(.7f, ar);
            } else {
                tmt = ar.first();
            }

            tmt.setId(y * 19 + x);
            cell.setTile(tmt);
            ((TiledMapTileLayer) splashMap.getLayers().get(0)).setCell(x, 6 - 1 - y, cell);

            moveind++;

        }
    }

    public static int[] intromap = {
        0x10, 0x10, 0x10, 0x0C, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x00, 0x00, 0x04, 0x08, 0x08, 0x10, 0x10, 0x10,
        0x10, 0x10, 0x0C, 0x04, 0x04, 0x1C, 0x00, 0x00, 0x04, 0x04, 0x00, 0x00, 0x00, 0x00, 0x04, 0x08, 0x08, 0x14, 0x10,
        0x10, 0x0C, 0x04, 0x04, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, 0x04, 0x08, 0x08, 0x10,
        0x10, 0x0C, 0x04, 0x04, 0x04, 0x2C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, 0x04, 0x04, 0x08, 0x08, 0x08,
        0x0C, 0x0C, 0x0C, 0x04, 0x04, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, 0x04, 0x04, 0x08, 0x10, 0x84, 0x10,
        0x88, 0x10, 0x10, 0x0C, 0x04, 0x04, 0x04, 0x00, 0x00, 0x00, 0x00, 0x04, 0x04, 0x04, 0x08, 0x10, 0x10, 0x10, 0x10
    };

    public static final byte[] movesCommands = new byte[512];
    public static final byte[] movesData = new byte[512];

    static {
        InputStream is1 = ClassLoader.class.getResourceAsStream("/assets/data/moves.ult");
        try {
            byte[] tmp = IOUtils.toByteArray(is1);
            System.arraycopy(tmp, 0, movesCommands, 0, 512);
            System.arraycopy(tmp, 512, movesData, 0, 512);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
