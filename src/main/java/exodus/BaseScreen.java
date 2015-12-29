package exodus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import java.util.Random;

import objects.BaseMap;
import objects.Creature;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import util.XORShiftRandom;

public abstract class BaseScreen implements Screen, InputProcessor, Constants {

    public ScreenType scType;

    protected BaseScreen returnScreen;
    public Context context;
    protected Stage stage;

    protected float time = 0;
    protected Random rand = new XORShiftRandom();

    protected int mapPixelHeight;
    public Vector3 newMapPixelCoords;

    protected Viewport viewport = new ScreenViewport();

    protected Camera camera;

    protected int showZstats = 0;

    protected Vector2 currentMousePos;

    protected Creature currentEncounter;

    /**
     * translate map tile coords to world pixel coords
     */
    public abstract Vector3 getMapPixelCoords(int x, int y);

    /**
     * get the map coords at the camera center
     */
    public abstract Vector3 getCurrentMapCoords();

    @Override
    public void dispose() {
    }

    public void log(String s) {
        Exodus.hud.add(s);
    }

    public void logAppend(String s) {
        Exodus.hud.append(s);
    }

    public void logDeleteLastChar() {
        Exodus.hud.logDeleteLastChar();
    }

    public Stage getStage() {
        return stage;
    }

    public abstract void finishTurn(int currentX, int currentY);

    public void endCombat(boolean isWon, BaseMap combatMap, boolean wounded) {
    }

    public final void addButtons() {

        TextButton bookButt = new TextButton("Book", Exodus.skin, "wood");
        bookButt.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Exodus.mainGame.setScreen(new BookScreen(BaseScreen.this, Exodus.skin));
            }
        });
        bookButt.setX(625);
        bookButt.setY(15);

        stage.addActor(bookButt);

    }

    public abstract InputProcessor getPeerGemInputProcessor();

    public abstract void partyDeath();

    @Override
    public void hide() {
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        currentMousePos = new Vector2(screenX, screenY);
        return false;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
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
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    public class NewOrderInputAdapter extends InputAdapter {

        int p1 = -1;
        int p2 = -1;
        BaseScreen screen;

        public NewOrderInputAdapter(BaseScreen screen) {
            this.screen = screen;
        }

        @Override
        public boolean keyUp(int keycode) {
            if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_4) {
                if (p1 == -1) {
                    p1 = keycode - 7 - 1;
                    logAppend(" " + (p1 + 1));
                    log("with #:");
                    return false;
                } else if (p2 == -1) {
                    p2 = keycode - 7 - 1;
                    logAppend(" " + (p2 + 1));
                    context.getParty().swapPlayers(p1, p2);
                }
            } else {
                log("What?");
            }

            if (this.screen instanceof GameScreen) {
                Vector3 v = getCurrentMapCoords();
                finishTurn((int) v.x, (int) v.y);
            }

            Gdx.input.setInputProcessor(new InputMultiplexer(this.screen, stage));
            return false;
        }
    }

}
