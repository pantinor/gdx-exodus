package exodus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import static exodus.Constants.PARTY_SAV_BASE_FILENAME;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import objects.SaveGame;
import objects.SaveGame.CharacterRecord;

public class ManagePartyScreen implements Screen, Constants {

    Exodus mainGame;
    Screen returnScreen;
    Stage stage;
    Batch batch;
    BitmapFont font;
    Texture bkgnd;

    List<RosterIndex> registry;
    List<PartyIndex> partyFormation;

    private final Table buttonTable;

    TextButton apply;
    TextButton clear;
    TextButton add;
    TextButton remove;
    TextButton save;
    TextButton cancel;

    TextField nameField;
    Slider strEdit;
    Slider intEdit;
    Slider dexEdit;
    Slider wisEdit;
    SelectBox<Profession> profSelect;
    SelectBox<ClassType> raceSelect;
    SelectBox<SexType> sexSelect;

    private static final String EMPTY = "<empty>";

    SaveGame saveGame = new SaveGame();

    public ManagePartyScreen(Exodus main, Screen rs, Skin skin) {
        this.mainGame = main;
        this.stage = new Stage();
        this.batch = new SpriteBatch();
        this.returnScreen = rs;

        font = Exodus.font;//new BitmapFont(Gdx.files.classpath("assets/fonts/BellMT_16.fnt"));
        font.setColor(Color.WHITE);

        bkgnd = new Texture(Gdx.files.classpath("assets/graphics/roster.png"));

        try {
            saveGame.read(PARTY_SAV_BASE_FILENAME);
        } catch (Exception e) {
            //none
        }

        PartyIndex[] mbrs = new PartyIndex[4];
        for (int i = 0; i < mbrs.length; i++) {
            CharacterRecord r = saveGame.players[i];
            if (r == null || r.name.length() < 1) {
                r = new CharacterRecord();
                r.name = EMPTY;
            }
            mbrs[i] = new PartyIndex(r, i + 1);
        }

        partyFormation = new List<>(skin);
        partyFormation.setItems(mbrs);

        RosterIndex[] recs = new RosterIndex[20];
        for (int i = 0; i < recs.length; i++) {
            recs[i] = new RosterIndex(new CharacterRecord(), i + 1);
            recs[i].character.name = EMPTY;
        }

        InputStream is;
        LittleEndianDataInputStream dis = null;
        try {
            is = new FileInputStream(Gdx.files.internal("roster.sav").file());
            dis = new LittleEndianDataInputStream(is);
            for (RosterIndex ri : recs) {
                try {
                    ri.character.read(dis);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }

        for (RosterIndex ri : recs) {
            if (ri.character.name.trim().length() < 1) {
                ri.character.name = EMPTY;
            }
        }

        registry = new List<>(skin);
        registry.setItems(recs);

        this.buttonTable = new Table(skin);
        this.buttonTable.defaults().pad(10);

        apply = new TextButton("Apply to Slot", skin, "wood");
        clear = new TextButton("Clear Slot", skin, "wood");
        add = new TextButton("Add to Party", skin, "wood");
        remove = new TextButton("Remove from Party", skin, "wood");
        cancel = new TextButton("Cancel", skin, "wood");
        save = new TextButton("Save", skin, "wood");

        apply.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                int st = (int) strEdit.getValue();
                int dx = (int) dexEdit.getValue();
                int in = (int) intEdit.getValue();
                int wi = (int) wisEdit.getValue();

                int total = st + dx + in + wi;
                if (total > 50 || nameField.getText().length() < 1) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }

                CharacterRecord sel = registry.getSelected().character;
                sel.name = nameField.getText();
                sel.sex = sexSelect.getSelected();
                sel.race = raceSelect.getSelected();
                sel.profession = profSelect.getSelected();
                sel.str = (int) strEdit.getValue();
                sel.dex = (int) dexEdit.getValue();
                sel.intell = (int) intEdit.getValue();
                sel.wis = (int) wisEdit.getValue();
                sel.health = 150;
                sel.food = 150;
                sel.gold = 150;
            }
        });

        clear.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                registry.getSelected().character = new CharacterRecord();
                registry.getSelected().character.name = EMPTY;
            }
        });

        add.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                CharacterRecord rsel = registry.getSelected().character;
                CharacterRecord psel = partyFormation.getSelected().character;
                if (!psel.name.equals(EMPTY)) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                    return;
                }
                for (PartyIndex pi : partyFormation.getItems()) {
                    if (pi.character.name.equals(rsel.name)) {
                        Sounds.play(Sound.NEGATIVE_EFFECT);
                        return;
                    }
                }
                partyFormation.getSelected().character = rsel;
            }
        });

        remove.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                CharacterRecord psel = partyFormation.getSelected().character;
                if (psel.name.equals(EMPTY)) {
                    return;
                }
                RosterIndex found = null;
                for (RosterIndex ri : registry.getItems()) {
                    if (ri.character.name.equals(psel.name)) {
                        found = ri;
                        found.character = psel;
                        break;
                    }
                }
                if (found == null) {
                    for (RosterIndex ri : registry.getItems()) {
                        if (ri.character.name.equals(EMPTY)) {
                            found = ri;
                            found.character = psel;
                            break;
                        }
                    }
                }
                if (found == null) {
                    Sounds.play(Sound.NEGATIVE_EFFECT);
                } else {
                    partyFormation.getSelected().character = new CharacterRecord();
                    partyFormation.getSelected().character.name = EMPTY;
                }
            }
        });

        save.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                try {
                    FileOutputStream fos = new FileOutputStream("roster.sav");
                    LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(fos);
                    for (RosterIndex ri : registry.getItems()) {
                        if (ri.character.name.equals(EMPTY)) {
                            ri.character.name = null;
                        }
                        ri.character.write(dos);
                    }

                    saveGame.players[0] = partyFormation.getItems().get(0).character;
                    saveGame.players[1] = partyFormation.getItems().get(1).character;
                    saveGame.players[2] = partyFormation.getItems().get(2).character;
                    saveGame.players[3] = partyFormation.getItems().get(3).character;
                    for (CharacterRecord r : saveGame.players) {
                        if (r.name.equals(EMPTY)) {
                            r.name = null;
                        }
                    }
                    saveGame.write(PARTY_SAV_BASE_FILENAME);

                } catch (Exception e) {

                }

                mainGame.setScreen(returnScreen);
            }
        });

        cancel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(returnScreen);
            }
        });

        buttonTable.add(apply).width(150);
        buttonTable.add(clear).width(150);
        buttonTable.add(add).width(150);
        buttonTable.add(remove).width(150);
        buttonTable.add(cancel).width(150);
        buttonTable.add(save).width(150);

        buttonTable.setX(Exodus.SCREEN_WIDTH / 2);
        buttonTable.setY(Exodus.SCREEN_HEIGHT - 24);

        ScrollPane sp1 = new ScrollPane(partyFormation, skin);
        sp1.setX(304);
        sp1.setY(320);
        sp1.setWidth(224);
        sp1.setHeight(96);

        ScrollPane sp2 = new ScrollPane(registry, skin);
        sp2.setX(64);
        sp2.setY(Exodus.SCREEN_HEIGHT - 512);
        sp2.setWidth(160);
        sp2.setHeight(448);

        nameField = new TextField("", skin);
        strEdit = new Slider(5, 25, 1, false, skin);
        dexEdit = new Slider(5, 25, 1, false, skin);
        intEdit = new Slider(5, 25, 1, false, skin);
        wisEdit = new Slider(5, 25, 1, false, skin);
        profSelect = new SelectBox<>(skin);
        profSelect.setItems(Profession.values());
        profSelect.setSelected(Profession.FIGHTER);
        raceSelect = new SelectBox<>(skin);
        raceSelect.setItems(ClassType.values());
        raceSelect.setSelected(ClassType.HUMAN);
        sexSelect = new SelectBox<>(skin);
        sexSelect.setItems(SexType.values());
        sexSelect.setSelected(SexType.MALE);

        int x = 404;
        nameField.setX(x);
        strEdit.setX(x);
        dexEdit.setX(x);
        intEdit.setX(x);
        wisEdit.setX(x);
        profSelect.setX(x);
        raceSelect.setX(x);
        sexSelect.setX(x);

        int y = Exodus.SCREEN_HEIGHT - 84;
        nameField.setY(y);
        sexSelect.setY(y -= 28);
        raceSelect.setY(y -= 28);
        profSelect.setY(y -= 28);
        strEdit.setY(y -= 28);
        dexEdit.setY(y -= 28);
        intEdit.setY(y -= 28);
        wisEdit.setY(y -= 28);

        nameField.setMaxLength(16);
        profSelect.setWidth(100);
        raceSelect.setWidth(100);
        sexSelect.setWidth(100);

        stage.addActor(nameField);
        stage.addActor(strEdit);
        stage.addActor(intEdit);
        stage.addActor(dexEdit);
        stage.addActor(wisEdit);
        stage.addActor(profSelect);
        stage.addActor(raceSelect);
        stage.addActor(sexSelect);

        stage.addActor(buttonTable);
        stage.addActor(sp1);
        stage.addActor(sp2);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        CharacterRecord sel = this.registry.getSelected().character;

        batch.begin();
        batch.draw(bkgnd, 0, 0);

        int viewY = Exodus.SCREEN_HEIGHT - 72;
        int x = 304;

        font.draw(batch, "Name: ", x, viewY);
        font.draw(batch, "Sex: ", x, viewY -= 28);
        font.draw(batch, "Race: ", x, viewY -= 28);
        font.draw(batch, "Type: ", x, viewY -= 28);
        font.draw(batch, "Strength: ", x, viewY -= 28);
        font.draw(batch, "Dexterity: ", x, viewY -= 28);
        font.draw(batch, "Intelligence: ", x, viewY -= 28);
        font.draw(batch, "Wisdom: ", x, viewY -= 28);

        int st = (int) strEdit.getValue();
        int dx = (int) dexEdit.getValue();
        int in = (int) intEdit.getValue();
        int wi = (int) wisEdit.getValue();

        int total = st + dx + in + wi;
        if (total > 50) {
            font.setColor(Color.RED);
        }

        font.draw(batch, st + "", x + 250, viewY += 28 * 3);
        font.draw(batch, dx + "", x + 250, viewY -= 28);
        font.draw(batch, in + "", x + 250, viewY -= 28);
        font.draw(batch, wi + "", x + 250, viewY -= 28);

        font.setColor(Color.WHITE);

        viewY = Exodus.SCREEN_HEIGHT - 560;
        x = 64;

        font.draw(batch, "Name: " + sel.name, x, viewY);
        font.draw(batch, "Sex: " + sel.sex.toString(), x, viewY -= 18);
        font.draw(batch, "Race: " + sel.race.toString(), x, viewY -= 18);
        font.draw(batch, "Type: " + sel.profession.toString(), x, viewY -= 18);
        font.draw(batch, "Status: " + sel.status.toString(), x, viewY -= 18);
        font.draw(batch, "Weapon: " + sel.weapon.toString(), x, viewY -= 24);
        font.draw(batch, "Armour: " + sel.armor.toString(), x, viewY -= 18);

        viewY = Exodus.SCREEN_HEIGHT - 560;
        x = 64 + 200;

        font.draw(batch, "Strength: " + sel.str, x, viewY);
        font.draw(batch, "Dexterity: " + sel.dex, x, viewY -= 18);
        font.draw(batch, "Intelligence: " + sel.intell, x, viewY -= 18);
        font.draw(batch, "Wisdom: " + sel.wis, x, viewY -= 18);
        font.draw(batch, "Hit Points: " + (sel.health & 0xff), x, viewY -= 24);
        font.draw(batch, "Experience: " + (sel.exp & 0xff), x, viewY -= 18);
        font.draw(batch, "Food: " + (sel.food & 0xff), x, viewY -= 18);
        font.draw(batch, "Gold: " + (sel.gold & 0xff), x, viewY -= 18);

        batch.end();

        stage.act();
        stage.draw();
    }

    private class RosterIndex {

        CharacterRecord character;
        int index;

        RosterIndex(CharacterRecord sp, int idx) {
            this.character = sp;
            this.index = idx;
        }

        @Override
        public String toString() {
            return " " + index + " - " + character.name;
        }
    }

    private class PartyIndex {

        CharacterRecord character;
        int index;

        PartyIndex(CharacterRecord sp, int idx) {
            this.character = sp;
            this.index = idx;
        }

        @Override
        public String toString() {
            return " Party Slot #" + index + " : " + character.name;
        }
    }

    @Override
    public void resize(int width, int height) {
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

}
