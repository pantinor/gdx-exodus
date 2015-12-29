
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Align;

public class StarWarsText implements ApplicationListener {

    private SpriteBatch spriteBatch;
    private BitmapFont bitmapFont;
    private OrthographicCamera cam2d;
    private PerspectiveCamera cam3d;
    private int WIDTH, HEIGHT;

    private final float scrollSpeed = 0.3f; //unit per second

    public static final String FINAL_TEXT = "Congratulations!\n\nThou hast compleated Exodus: Ultima 3 in %d moves.\nReport thy feat!\n\n"
            + "And so it came to pass that on this day EXODUS,\nhell-born incarnate of evil,\nwas vanquished from Sosaria.\n\n"
            + "What now lies ahead in the ULTIMA saga can only be pure speculation!\n\n"
            + "Onward to ULTIMA IV!";

    GlyphLayout layout;

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "test";
        cfg.width = 1024;
        cfg.height = 768;
        new LwjglApplication(new StarWarsText(), cfg);
    }

    @Override
    public void create() {

        spriteBatch = new SpriteBatch();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/gnuolane.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 12;
        bitmapFont = generator.generateFont(parameter);
        //bitmapFont = new BitmapFont();
        bitmapFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
        bitmapFont.setUseIntegerPositions(false);
        bitmapFont.getData().setScale(.08f);
        //Gdx.input.setInputProcessor(this);
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

    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

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
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

}
