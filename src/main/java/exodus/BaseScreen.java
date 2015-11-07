package exodus;

import java.util.Observable;
import java.util.Observer;
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

public abstract class BaseScreen implements Screen, InputProcessor, Constants, Observer {

    public ScreenType scType;

    public static Exodus mainGame;

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
    public abstract Vector3 getMapPixelCoords(int x, int y) ;

    /**
     * get the map coords at the camera center
     */
    public abstract Vector3 getCurrentMapCoords() ;

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
        TextButton jourButt = new TextButton("Journal", Exodus.skin, "wood");
        jourButt.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                //mainGame.setScreen(new JournalScreen(mainGame, BaseScreen.this, Exodus.skin, context.getJournal()));
            }
        });
        jourButt.setX(530);
        jourButt.setY(15);
        stage.addActor(jourButt);

        TextButton bookButt = new TextButton("Book", Exodus.skin, "wood");
        bookButt.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(new BookScreen(mainGame, BaseScreen.this, Exodus.skin));
            }
        });
        bookButt.setX(625);
        bookButt.setY(15);
        
        stage.addActor(bookButt);
        
    }
    
    public abstract InputProcessor getPeerGemInputProcessor() ;
    
    @Override
    public void update(Observable obs, Object obj) {
//        if (obj instanceof PartyEvent) {
//            PartyEvent ev = (PartyEvent) obj;
//            switch (ev) {
//                case ACTIVE_PLAYER_CHANGED:
//                    break;
//                case ADVANCED_LEVEL:
//                    log("Thou art now Level");
//                    Sounds.play(Sound.MAGIC);
//                    break;
//                case POSITIVE_KARMA:
//                    Sounds.play(Sound.POSITIVE_EFFECT);
//                    break;
//                case NEGATIVE_KARMA:
//                    Sounds.play(Sound.NEGATIVE_EFFECT);
//                    break;
//                case INVENTORY_ADDED:
//                    break;
//                case LOST_EIGHTH:
//                    log("Thou hast lost an eighth of avatarhood!");
//                    Sounds.play(Sound.STEAL_ESSENCE);
//                    break;
//                case MEMBER_JOINED:
//                    Sounds.play(Sound.POSITIVE_EFFECT);
//                    break;
//                case PARTY_REVIVED:
//                case PARTY_DEATH:
//                    break;
//                case POISON_DAMAGE:
//                    Sounds.play(Sound.POISON_DAMAGE);
//                    break;
//                case STARVING:
//                    log("Starving!!!");
//                    Sounds.play(Sound.NEGATIVE_EFFECT);
//                    break;
//                case TRANSPORT_CHANGED:
//                    break;
//                default:
//                    break;
//
//            }
//        }
    }

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

}
