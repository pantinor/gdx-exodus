package exodus;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import objects.SaveGame.CharacterRecord;

public class ShrineScreen extends BaseScreen {

    private AvatarActor avatar;
    public Party party;
    private OrthogonalTiledMapRenderer renderer;
    private SpriteBatch batch;
    int playerIndex;
    private Viewport mapViewPort;
    private final Maps map;

    public ShrineScreen(Maps map, BaseScreen returnScreen, Party party, TiledMap tmap, TextureAtlas a1, TextureAtlas a2) {

        scType = ScreenType.SHRINE;
        this.map = map;
        this.returnScreen = returnScreen;
        this.party = party;

        renderer = new OrthogonalTiledMapRenderer(tmap, 1f);

        MapProperties prop = tmap.getProperties();
        mapPixelHeight = prop.get("height", Integer.class) * tilePixelWidth;

        camera = new OrthographicCamera(11 * tilePixelWidth, 11 * tilePixelHeight);

        mapViewPort = new ScreenViewport(camera);

        stage = new Stage();
        stage.setViewport(mapViewPort);

        Vector3 v1 = getMapPixelCoords(5, 10);
        Vector3 v2 = getMapPixelCoords(5, 9);
        Vector3 v3 = getMapPixelCoords(5, 8);
        Vector3 v4 = getMapPixelCoords(5, 7);
        Vector3 v5 = getMapPixelCoords(5, 6);

        avatar = new AvatarActor(a1.findRegion("avatar"));
        avatar.setPos(v1);
        stage.addActor(avatar);

        avatar.addAction(sequence(
                delay(.8f, moveTo(v2.x, v2.y, .1f)),
                delay(.8f, moveTo(v3.x, v3.y, .1f)),
                delay(.8f, moveTo(v4.x, v4.y, .1f)),
                delay(.8f, moveTo(v5.x, v5.y, .1f)), new Action() {
                    public boolean act(float delta) {
                        log("Who will meditate?");
                        return true;
                    }
                }
        ));

        batch = new SpriteBatch();

        newMapPixelCoords = getMapPixelCoords(5, 5);

        log("You enter the ancient shrine and sit before the altar...");

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new PlayerInputAdapter());
    }

    @Override
    public void hide() {
    }

    @Override
    public void render(float delta) {

        time += delta;

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.position.set(newMapPixelCoords.x + 5 * tilePixelWidth, newMapPixelCoords.y, 0);

        camera.update();

        renderer.setView(camera.combined,
                camera.position.x - tilePixelWidth * 10, //this is voodoo
                camera.position.y - tilePixelHeight * 10,
                Exodus.MAP_WIDTH,
                Exodus.MAP_HEIGHT);

        renderer.render();

        batch.begin();
        batch.draw(Exodus.backGround, 0, 0);

        Exodus.hud.render(batch, party);
        batch.end();

        stage.act();
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        mapViewPort.update(width, height, false);
    }

    @Override
    public void partyDeath() {
        //not used here
    }

    @Override
    public void finishTurn(int currentX, int currentY) {
        // TODO Auto-generated method stub

    }

    @Override
    public InputProcessor getPeerGemInputProcessor() {
        return null;
    }

    class AvatarActor extends Actor {

        TextureRegion texture;
        boolean visible = true;

        AvatarActor(TextureRegion texture) {
            this.texture = texture;
        }

        void setPos(Vector3 v) {
            setX(v.x);
            setY(v.y);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            batch.draw(texture, getX(), getY());
        }

    }

    private class PlayerInputAdapter extends InputAdapter {

        @Override
        public boolean keyUp(int keycode) {
            if (keycode >= Keys.NUM_0 && keycode <= Keys.NUM_4) {
                playerIndex = keycode - 7 - 1;
                if (party.getMember(playerIndex) == null) {
                    log("Who else?");
                    return false;
                }
                CharacterRecord c = party.getMember(playerIndex).getPlayer();
                log(String.format("%s: Offering * 100?",c.name));
                Gdx.input.setInputProcessor(new ShrineInputAdapter());
            }
            return false;
        }
    }

    private class ShrineInputAdapter extends InputAdapter {

        @Override
        public boolean keyUp(int keycode) {
            if (keycode >= Keys.NUM_0 && keycode <= Keys.NUM_9) {

                CharacterRecord c = party.getMember(playerIndex).getPlayer();
                int amt = (keycode - 7);
                
                if (c.gold < amt * 100) {
                    log("You cannot cheat the gods!");
                    Exodus.mainGame.setScreen(returnScreen);
                    return false;
                } else if (amt == 0) {
                    log("Begone then!");
                    Exodus.mainGame.setScreen(returnScreen);
                    return false;
                }
                
                log("Shazam!");
                Sounds.play(Sound.DIVINE_INTERVENTION);
                
                c.adjustGold(amt * 100);

                switch (map) {
                    case SHRINE_OF_WISDOM:
                        c.wis += amt;
                        break;
                    case SHRINE_OF_DEXTERITY:
                        c.dex += amt;
                        break;
                    case SHRINE_OF_INTELLIGENCE:
                        c.intell += amt;
                        break;
                    case SHRINE_OF_STRENGTH:
                        c.str += amt;
                        break;
                }

                if (c.str > c.race.getMaxStr()) {
                    c.str = c.race.getMaxStr();
                }
                if (c.dex > c.race.getMaxDex()) {
                    c.dex = c.race.getMaxDex();
                }
                if (c.intell > c.race.getMaxInt()) {
                    c.intell = c.race.getMaxInt();
                }
                if (c.wis > c.race.getMaxWis()) {
                    c.wis = c.race.getMaxWis();
                }

                Exodus.mainGame.setScreen(returnScreen);
            }
            
            return false;
        }
    }

    @Override
    public Vector3 getMapPixelCoords(int x, int y) {
        Vector3 v = new Vector3(x * tilePixelWidth, mapPixelHeight - y * tilePixelHeight - tilePixelHeight, 0);
        return v;
    }

    @Override
    public Vector3 getCurrentMapCoords() {
        return null;
    }

}
