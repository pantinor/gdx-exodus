/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exodus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Align;
import objects.SaveGame;

/**
 *
 * @author Paul
 */
public class FinalScreen extends InputAdapter implements Screen {

    private final SpriteBatch spriteBatch;
    private final BitmapFont bitmapFont;
    private GlyphLayout layout;
    private OrthographicCamera cam2d;
    private PerspectiveCamera cam3d;
    private int WIDTH, HEIGHT;
    private final SaveGame saveGame;
    private final float scrollSpeed = 0.3f; //unit per second

    public static final String FINAL_TEXT = "Congratulations!\n\nThou hast compleated Exodus: Ultima 3 in %d moves.\nReport thy feat!\n\n"
            + "And so it came to pass that on this day EXODUS,\nhell-born incarnate of evil,\nwas vanquished from Sosaria.\n\n"
            + "What now lies ahead in the ULTIMA saga can only be pure speculation!\n\n"
            + "Onward to ULTIMA IV!";

    public FinalScreen(SaveGame sg) {
        spriteBatch = new SpriteBatch();
        this.saveGame = sg;
        
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/gnuolane.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 12;

        bitmapFont = generator.generateFont(parameter);
        bitmapFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        bitmapFont.setUseIntegerPositions(false);
        bitmapFont.getData().setScale(.08f);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void resize(int width, int height) {
        WIDTH = width;
        HEIGHT = height;

        float camWidth = 10.0f;
        float camHeight = camWidth * (float) HEIGHT / (float) WIDTH;
        cam2d = new OrthographicCamera(camWidth, camHeight);
        cam2d.position.set(camWidth / 2.0f, camHeight / 2.0f, 0.0f);
        cam2d.update();

        cam3d = new PerspectiveCamera(90.0f, camWidth, camHeight);
        cam3d.translate(0.0f, -10.0f, 3.0f);
        cam3d.lookAt(0.0f, 0.0f, 0.0f);
        cam3d.update(true);

        layout = new GlyphLayout(bitmapFont, String.format(FINAL_TEXT,saveGame.moves), Color.YELLOW, cam3d.viewportWidth, Align.center, true);
        
        Exodus.music.stop();
        Exodus.music = Sounds.play(Sound.ALIVE, Exodus.musicVolume);
    }

    @Override
    public void render(float dt) {

        cam3d.translate(0.0f, -dt * scrollSpeed, 0.0f);
        cam3d.update(false);

        GL20 gl = Gdx.graphics.getGL20();
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.setProjectionMatrix(cam3d.combined);
        spriteBatch.begin();
        bitmapFont.draw(spriteBatch, layout, -cam3d.viewportWidth / 2f, -cam3d.viewportHeight);
        spriteBatch.end();
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
    public boolean keyUp(int i) {
        Exodus.mainGame.setScreen(Exodus.startScreen);
        return false;
    }

}
