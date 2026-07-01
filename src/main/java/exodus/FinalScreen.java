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

public class FinalScreen extends InputAdapter implements Screen {

    private final SpriteBatch spriteBatch;
    private final BitmapFont bitmapFont;
    private GlyphLayout layout;
    private OrthographicCamera cam2d;
    private PerspectiveCamera cam3d;
    private int WIDTH, HEIGHT;
    private final float scrollSpeed = 0.3f; //unit per second
    private static final int FONT_SIZE = 48;
    private static final float FONT_SCALE = 0.02f;

    public static final String FINAL_TEXT
            = "And so it came to pass\n\n"
            + "that on this day\n\n"
            + "EXODUS,\n\n"
            + "hell-born incarnate of evil,\n\n"
            + "was vanquished from Sosaria.\n\n"
            + "What now lies ahead\n\n"
            + "in the ULTIMA saga\n\n"
            + "can only be pure speculation!\n\n"
            + "Onward to ULTIMA IV!";

    public FinalScreen() {
        spriteBatch = new SpriteBatch();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/gnuolane.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = FONT_SIZE;
        parameter.genMipMaps = true;
        parameter.hinting = FreeTypeFontGenerator.Hinting.Full;

        bitmapFont = generator.generateFont(parameter);
        generator.dispose();
        bitmapFont.getRegion().getTexture().setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear);
        bitmapFont.setUseIntegerPositions(false);
        bitmapFont.getData().setScale(FONT_SCALE);
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

        layout = new GlyphLayout(bitmapFont, FINAL_TEXT, Color.YELLOW, cam3d.viewportWidth, Align.center, true);

        if (Exodus.music != null) {
            Exodus.music.stop();
        }
        
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
