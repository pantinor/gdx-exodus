package exodus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import java.util.HashMap;
import java.util.Map;

public class FinalScreen extends InputAdapter implements Screen {

    private static final float DY_PER_FRAME = 1f;
    private static final float DZ_PER_FRAME = 2f;
    private static final float CRAWL_TILT_DEGREES = 20f;
    private static final float BLOCK_SIZE = 8f;
    private static final float BLOCK_DEPTH = 3f;
    private static final float CHAR_ADVANCE = BLOCK_SIZE * 6f;
    private static final float LINE_ADVANCE = BLOCK_SIZE * 10f;

    private final Vector3 crawlPos = new Vector3(0f, 0f, 0f);
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private Model crawlModel;
    private ModelInstance crawlInstance;

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

    private static final Map<Character, String[]> FONT_5X7 = new HashMap<>();

    static {
        addGlyph('A', "01110", "10001", "10001", "11111", "10001", "10001", "10001");
        addGlyph('B', "11110", "10001", "10001", "11110", "10001", "10001", "11110");
        addGlyph('C', "01111", "10000", "10000", "10000", "10000", "10000", "01111");
        addGlyph('D', "11110", "10001", "10001", "10001", "10001", "10001", "11110");
        addGlyph('E', "11111", "10000", "10000", "11110", "10000", "10000", "11111");
        addGlyph('F', "11111", "10000", "10000", "11110", "10000", "10000", "10000");
        addGlyph('G', "01111", "10000", "10000", "10011", "10001", "10001", "01111");
        addGlyph('H', "10001", "10001", "10001", "11111", "10001", "10001", "10001");
        addGlyph('I', "11111", "00100", "00100", "00100", "00100", "00100", "11111");
        addGlyph('J', "00111", "00010", "00010", "00010", "10010", "10010", "01100");
        addGlyph('K', "10001", "10010", "10100", "11000", "10100", "10010", "10001");
        addGlyph('L', "10000", "10000", "10000", "10000", "10000", "10000", "11111");
        addGlyph('M', "10001", "11011", "10101", "10101", "10001", "10001", "10001");
        addGlyph('N', "10001", "11001", "10101", "10011", "10001", "10001", "10001");
        addGlyph('O', "01110", "10001", "10001", "10001", "10001", "10001", "01110");
        addGlyph('P', "11110", "10001", "10001", "11110", "10000", "10000", "10000");
        addGlyph('Q', "01110", "10001", "10001", "10001", "10101", "10010", "01101");
        addGlyph('R', "11110", "10001", "10001", "11110", "10100", "10010", "10001");
        addGlyph('S', "01111", "10000", "10000", "01110", "00001", "00001", "11110");
        addGlyph('T', "11111", "00100", "00100", "00100", "00100", "00100", "00100");
        addGlyph('U', "10001", "10001", "10001", "10001", "10001", "10001", "01110");
        addGlyph('V', "10001", "10001", "10001", "10001", "01010", "01010", "00100");
        addGlyph('W', "10001", "10001", "10001", "10101", "10101", "10101", "01010");
        addGlyph('X', "10001", "10001", "01010", "00100", "01010", "10001", "10001");
        addGlyph('Y', "10001", "10001", "01010", "00100", "00100", "00100", "00100");
        addGlyph('Z', "11111", "00001", "00010", "00100", "01000", "10000", "11111");
        addGlyph('0', "01110", "10001", "10011", "10101", "11001", "10001", "01110");
        addGlyph('1', "00100", "01100", "00100", "00100", "00100", "00100", "01110");
        addGlyph('2', "01110", "10001", "00001", "00010", "00100", "01000", "11111");
        addGlyph('3', "11110", "00001", "00001", "01110", "00001", "00001", "11110");
        addGlyph('4', "00010", "00110", "01010", "10010", "11111", "00010", "00010");
        addGlyph('5', "11111", "10000", "10000", "11110", "00001", "00001", "11110");
        addGlyph('6', "01110", "10000", "10000", "11110", "10001", "10001", "01110");
        addGlyph('7', "11111", "00001", "00010", "00100", "01000", "01000", "01000");
        addGlyph('8', "01110", "10001", "10001", "01110", "10001", "10001", "01110");
        addGlyph('9', "01110", "10001", "10001", "01111", "00001", "00001", "01110");
        addGlyph(' ', "00000", "00000", "00000", "00000", "00000", "00000", "00000");
        addGlyph(',', "00000", "00000", "00000", "00000", "00100", "00100", "01000");
        addGlyph('.', "00000", "00000", "00000", "00000", "00000", "01100", "01100");
        addGlyph('!', "00100", "00100", "00100", "00100", "00100", "00000", "00100");
        addGlyph('-', "00000", "00000", "00000", "11111", "00000", "00000", "00000");
        addGlyph('?', "01110", "10001", "00001", "00010", "00100", "00000", "00100");
        addGlyph(':', "00000", "00100", "00100", "00000", "00100", "00100", "00000");
    }

    public FinalScreen() {
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, .85f, .78f, .35f, 1f));
        environment.add(new DirectionalLight().set(Color.YELLOW, 0f, -1f, -.35f));

        crawlModel = buildCrawlModel(FINAL_TEXT);
        crawlInstance = new ModelInstance(crawlModel);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void resize(int width, int height) {
        Gdx.gl.glViewport(0, 0, width, height);

        camera = new PerspectiveCamera(67f, width, height);
        camera.position.set(0f, 220f, 520f);
        camera.lookAt(0f, 120f, -900f);
        camera.near = 1f;
        camera.far = 72000f;
        camera.update();

        crawlPos.set(0f, -180f, -120f);

        if (Exodus.music != null) {
            Exodus.music.stop();
        }

        Exodus.music = Sounds.play(Sound.ALIVE, Exodus.musicVolume);
    }

    @Override
    public void render(float dt) {
        crawlPos.y += DY_PER_FRAME;
        crawlPos.z -= DZ_PER_FRAME;

        crawlInstance.transform.idt();
        crawlInstance.transform.translate(crawlPos);
        crawlInstance.transform.rotate(Vector3.X, CRAWL_TILT_DEGREES);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        modelBatch.begin(camera);
        modelBatch.render(crawlInstance, environment);
        modelBatch.end();
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
        if (crawlModel != null) {
            crawlModel.dispose();
        }
        if (modelBatch != null) {
            modelBatch.dispose();
        }
    }

    @Override
    public boolean keyUp(int i) {
        Exodus.mainGame.setScreen(Exodus.startScreen);
        return false;
    }

    private static void addGlyph(char ch, String... rows) {
        FONT_5X7.put(ch, rows);
    }

    private Model buildCrawlModel(String text) {
        ModelBuilder builder = new ModelBuilder();
        builder.begin();

        Material material = new Material(ColorAttribute.createDiffuse(Color.YELLOW));
        MeshPartBuilder part = builder.part(
                "crawl-text",
                GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                material);

        String[] lines = text.split("\\n", -1);
        float y = 0f;
        for (String line : lines) {
            addLine(part, line, y);
            y -= LINE_ADVANCE;
        }

        return builder.end();
    }

    private void addLine(MeshPartBuilder part, String line, float y) {
        float x = -line.length() * CHAR_ADVANCE / 2f;
        for (int i = 0; i < line.length(); i++) {
            addBlockLetter(part, Character.toUpperCase(line.charAt(i)), x, y);
            x += CHAR_ADVANCE;
        }
    }

    private void addBlockLetter(MeshPartBuilder part, char ch, float x, float y) {
        String[] pattern = FONT_5X7.get(ch);
        if (pattern == null) {
            pattern = FONT_5X7.get('?');
        }

        for (int row = 0; row < pattern.length; row++) {
            for (int col = 0; col < pattern[row].length(); col++) {
                if (pattern[row].charAt(col) == '1') {
                    float blockX = x + col * BLOCK_SIZE;
                    float blockY = y + (pattern.length - 1 - row) * BLOCK_SIZE;
                    part.box(blockX, blockY, 0f, BLOCK_SIZE, BLOCK_SIZE, BLOCK_DEPTH);
                }
            }
        }
    }

}
