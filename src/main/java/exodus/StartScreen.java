/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exodus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import static exodus.Constants.PARTY_SAV_BASE_FILENAME;
import java.io.IOException;
import java.io.InputStream;
import objects.SaveGame;
import objects.Tile;
import org.apache.commons.io.IOUtils;
import util.UltimaTiledMapLoader;

/**
 *
 * @author Paul
 */
public class StartScreen implements Screen, Constants {

    float time = 0;
    Batch batch;
    OrthogonalTiledMapRenderer splashRenderer;
    OrthographicCamera camera;
    Viewport viewPort;
    Exodus mainGame;
    Stage stage;
    Texture title;
    IntroAnim animator = new IntroAnim();
    TiledMap splashMap;
    
    TextButton manual;
    TextButton manage;
    TextButton journey;

    BitmapFont exodusFont;

    public StartScreen(Exodus main) {
        this.mainGame = main;

        title = new Texture(Gdx.files.classpath("assets/graphics/splash.png"));

        exodusFont = new BitmapFont(Gdx.files.classpath("assets/fonts/exodus.fnt"));

        UltimaTiledMapLoader loader = new UltimaTiledMapLoader(Maps.SOSARIA, Exodus.standardAtlas, 19, 6, tilePixelWidth, tilePixelHeight);
        splashMap = loader.load(intromap, 19, 6, Exodus.baseTileSet, tilePixelWidth);
        splashRenderer = new OrthogonalTiledMapRenderer(splashMap);
        camera = new OrthographicCamera(19 * tilePixelWidth, 8 * tilePixelHeight);
        camera.position.set(tilePixelWidth * 10, tilePixelHeight * 8, 0);
        viewPort = new ScreenViewport(camera);

        batch = new SpriteBatch();
        
        manual = new TextButton("Book", Exodus.skin, "wood");
        manual.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Sounds.play(Sound.TRIGGER);
                mainGame.setScreen(new BookScreen(mainGame, StartScreen.this, Exodus.skin));
            }
        });
        manual.setX(220);
        manual.setY(Exodus.SCREEN_HEIGHT - 410);
        manual.setWidth(150);
        manual.setHeight(25);

        manage = new TextButton("Manage Party", Exodus.skin, "wood");
        manage.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Sounds.play(Sound.TRIGGER);
                mainGame.setScreen(new ManagePartyScreen(mainGame, StartScreen.this, Exodus.skin));
            }
        });
        manage.setX(410);
        manage.setY(Exodus.SCREEN_HEIGHT - 410);
        manage.setWidth(150);
        manage.setHeight(25);

        journey = new TextButton("Journey Onward", Exodus.skin, "wood");
        journey.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Sounds.play(Sound.TRIGGER);
                if (!Gdx.files.internal(PARTY_SAV_BASE_FILENAME).file().exists()) {
                    mainGame.setScreen(new ManagePartyScreen(mainGame, StartScreen.this, Exodus.skin));
                } else {
                    SaveGame saveGame = new SaveGame();
                    try {
                        saveGame.read(PARTY_SAV_BASE_FILENAME);
                    } catch (Exception e) {
                    }
                    SaveGame.CharacterRecord r = saveGame.players[0];
                    if (r == null || r.name.length() < 1) {
                        mainGame.setScreen(new ManagePartyScreen(mainGame, StartScreen.this, Exodus.skin));
                    } else {
                        mainGame.setScreen(new GameScreen(mainGame));
                        stage.clear();
                    }
                }

            }
        });
        journey.setX(600);
        journey.setY(Exodus.SCREEN_HEIGHT - 410);
        journey.setWidth(150);
        journey.setHeight(25);

        stage = new Stage();
        stage.addActor(manual);
        stage.addActor(manage);
        stage.addActor(journey);

        SequenceAction seq1 = Actions.action(SequenceAction.class);
        seq1.addAction(Actions.delay(.2f));
        seq1.addAction(Actions.run(animator));
        stage.addAction(Actions.forever(seq1));

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        if (Exodus.playMusic) {
            if (Exodus.music != null) {
                Exodus.music.stop();
            }
            Sound snd = Sound.SPLASH;
            Exodus.music = Sounds.play(snd, Exodus.musicVolume);
        }

    }

    @Override
    public void hide() {
    }

    @Override
    public void render(float delta) {
        time += Gdx.graphics.getDeltaTime();

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        splashRenderer.setView(camera.combined, 0, 0, 19 * tilePixelWidth, 8 * tilePixelHeight);
        splashRenderer.render();

        batch.begin();
        batch.draw(title, 0, 0);
        exodusFont.draw(batch, "EXODUS", 320, Exodus.SCREEN_HEIGHT - 140);
        Exodus.ultimaFont.draw(batch, "Ultima III", 315, Exodus.SCREEN_HEIGHT - 240);
        Exodus.largeFont.draw(batch, "From the depths of hell...he comes for VENGEANCE!", 300, Exodus.SCREEN_HEIGHT - 342);
        Exodus.largeFont.draw(batch, "LIBGDX Conversion by Paul Antinori", 350, 84);
        Exodus.largeFont.draw(batch, "Copyright 1983 Lord British", 375, 48);
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
    public void dispose() {
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
