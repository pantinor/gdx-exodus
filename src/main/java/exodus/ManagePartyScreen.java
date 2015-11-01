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
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
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
    Texture bkgnd2;

    List<RosterIndex> registry;
    List<PartyIndex> partyFormation;

    ImageButton apply;
    ImageButton clear;
    ImageButton add;
    ImageButton remove;
    
    ImageButton iconLeft, partyIconLeft;
    ImageButton iconRight, partyIconRight;

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
    
    int pidx = 11+2*16;

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
        bkgnd2 = new Texture(Gdx.files.classpath("assets/graphics/manage.png"));

        try {
            saveGame.read(PARTY_SAV_BASE_FILENAME);
        } catch (Exception e) {
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

        Skin imgBtnSkin = new Skin(Gdx.files.classpath("assets/skin/imgBtn.json"));

        apply = new ImageButton(imgBtnSkin, "left");
        clear = new ImageButton(imgBtnSkin, "clear");
        add = new ImageButton(imgBtnSkin, "right");
        remove = new ImageButton(imgBtnSkin, "left");
        cancel = new TextButton("Cancel", skin, "wood");
        save = new TextButton("Save", skin, "wood");
        iconLeft = new ImageButton(imgBtnSkin, "sm-arr-left");
        iconRight = new ImageButton(imgBtnSkin, "sm-arr-right");
        partyIconLeft = new ImageButton(imgBtnSkin, "sm-arr-left");
        partyIconRight = new ImageButton(imgBtnSkin, "sm-arr-right");
        
        apply.setX(348);
        apply.setY(Exodus.SCREEN_HEIGHT - 150);

        clear.setX(348);
        clear.setY(Exodus.SCREEN_HEIGHT - 200);

        add.setX(348);
        add.setY(Exodus.SCREEN_HEIGHT - 450);

        remove.setX(348);
        remove.setY(Exodus.SCREEN_HEIGHT - 500);

        save.setX(512);
        save.setY(Exodus.SCREEN_HEIGHT - 42);

        cancel.setX(712);
        cancel.setY(Exodus.SCREEN_HEIGHT - 42);
        
        iconLeft.setX(750);
        iconLeft.setY(Exodus.SCREEN_HEIGHT - 175);
        iconRight.setX(825);
        iconRight.setY(Exodus.SCREEN_HEIGHT - 175);
        
        partyIconLeft.setX(770);
        partyIconLeft.setY(Exodus.SCREEN_HEIGHT - 684);
        partyIconRight.setX(770+75);
        partyIconRight.setY(Exodus.SCREEN_HEIGHT - 684);

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
                sel.portaitIndex = pidx;

                Sounds.play(Sound.TRIGGER);
            }
        });

        clear.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                registry.getSelected().character = new CharacterRecord();
                registry.getSelected().character.name = EMPTY;
                Sounds.play(Sound.TRIGGER);
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
                Sounds.play(Sound.TRIGGER);
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
                    Sounds.play(Sound.TRIGGER);
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
                    int numberInParty = 0;
                    for (CharacterRecord r : saveGame.players) {
                        if (r.name.equals(EMPTY)) {
                            r.name = null;
                        } else {
                            numberInParty ++;
                        }
                    }
                    saveGame.numberInParty = numberInParty;
                    saveGame.write(PARTY_SAV_BASE_FILENAME);
                } catch (Exception e) {
                }
                Sounds.play(Sound.TRIGGER);
                mainGame.setScreen(returnScreen);
            }
        });

        cancel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                mainGame.setScreen(returnScreen);
            }
        });
        
        iconLeft.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                pidx--;
                if (pidx < 0) {
                    pidx = 0;
                }
            }
        });
        
        iconRight.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                pidx++;
                if (pidx > 13*16-3) {
                    pidx = 13*16-3;
                }
            }
        });
        
        partyIconLeft.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                partyFormation.getSelected().character.portaitIndex --;
                if (partyFormation.getSelected().character.portaitIndex < 0) {
                    partyFormation.getSelected().character.portaitIndex = 0;
                }
            }
        });
        
        partyIconRight.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                partyFormation.getSelected().character.portaitIndex ++;
                if (partyFormation.getSelected().character.portaitIndex > 13*16-3) {
                    partyFormation.getSelected().character.portaitIndex = 13*16-3;
                }
            }
        });

        ScrollPane sp1 = new ScrollPane(partyFormation, skin);
        sp1.setX(496);
        sp1.setY(264);
        sp1.setWidth(224);
        sp1.setHeight(96);

        ScrollPane sp2 = new ScrollPane(registry, skin);
        sp2.setX(80);
        sp2.setY(Exodus.SCREEN_HEIGHT - 528 - 16);
        sp2.setWidth(160);
        sp2.setHeight(464);

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

        int x = 604;
        nameField.setX(x);
        strEdit.setX(x);
        dexEdit.setX(x);
        intEdit.setX(x);
        wisEdit.setX(x);
        profSelect.setX(x);
        raceSelect.setX(x);
        sexSelect.setX(x);

        int y = Exodus.SCREEN_HEIGHT - 112;
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

        stage.addActor(apply);
        stage.addActor(remove);
        stage.addActor(clear);
        stage.addActor(save);
        stage.addActor(cancel);
        stage.addActor(add);
        stage.addActor(iconLeft);
        stage.addActor(iconRight);
        stage.addActor(partyIconLeft);
        stage.addActor(partyIconRight);
        stage.addActor(sp1);
        stage.addActor(sp2);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        if (Exodus.playMusic) {
            if (Exodus.music != null) {
                Exodus.music.stop();
            }
            Sound snd = Sound.M2;
            Exodus.music = Sounds.play(snd, Exodus.musicVolume);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(bkgnd2, 0, 0);
        batch.draw(bkgnd, 0, 0);

        int viewY = Exodus.SCREEN_HEIGHT - 96;
        int x = 504;

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
        
        batch.draw(Exodus.faceTiles[pidx],x+260,Exodus.SCREEN_HEIGHT - 190);

        CharacterRecord sel = this.registry.getSelected().character;

        viewY = Exodus.SCREEN_HEIGHT - 590;
        x = 90;

        font.draw(batch, "Name: " + sel.name, x, viewY);
        font.draw(batch, "Sex: " + sel.sex.toString(), x, viewY -= 18);
        font.draw(batch, "Race: " + sel.race.toString(), x, viewY -= 18);
        font.draw(batch, "Type: " + sel.profession.toString(), x, viewY -= 18);
        font.draw(batch, "Status: " + sel.status.toString(), x, viewY -= 18);
        font.draw(batch, "Weapon: " + sel.weapon.toString(), x, viewY -= 24);
        font.draw(batch, "Armour: " + sel.armor.toString(), x, viewY -= 18);

        viewY = Exodus.SCREEN_HEIGHT - 590;
        x = 90 + 135;

        font.draw(batch, "Strength: " + sel.str, x, viewY);
        font.draw(batch, "Dexterity: " + sel.dex, x, viewY -= 18);
        font.draw(batch, "Intelligence: " + sel.intell, x, viewY -= 18);
        font.draw(batch, "Wisdom: " + sel.wis, x, viewY -= 18);
        font.draw(batch, "Hit Points: " + sel.health, x, viewY -= 42);
        font.draw(batch, "Experience: " + sel.exp, x, viewY -= 18);

        viewY = Exodus.SCREEN_HEIGHT - 590;
        x = 90 + 250;

        font.draw(batch, "Food: " + sel.food, x, viewY);
        font.draw(batch, "Gold: " + sel.gold, x, viewY -= 18);
        
        batch.draw(Exodus.faceTiles[sel.portaitIndex],x+30,Exodus.SCREEN_HEIGHT - 704);

        sel = this.partyFormation.getSelected().character;

        viewY = Exodus.SCREEN_HEIGHT - 590;
        x = 504;

        font.draw(batch, "Name: " + sel.name, x, viewY);
        font.draw(batch, "Sex: " + sel.sex.toString(), x, viewY -= 18);
        font.draw(batch, "Race: " + sel.race.toString(), x, viewY -= 18);
        font.draw(batch, "Type: " + sel.profession.toString(), x, viewY -= 18);
        font.draw(batch, "Status: " + sel.status.toString(), x, viewY -= 18);
        font.draw(batch, "Weapon: " + sel.weapon.toString(), x, viewY -= 24);
        font.draw(batch, "Armour: " + sel.armor.toString(), x, viewY -= 18);

        viewY = Exodus.SCREEN_HEIGHT - 590;
        x = 504 + 135;

        font.draw(batch, "Strength: " + sel.str, x, viewY);
        font.draw(batch, "Dexterity: " + sel.dex, x, viewY -= 18);
        font.draw(batch, "Intelligence: " + sel.intell, x, viewY -= 18);
        font.draw(batch, "Wisdom: " + sel.wis, x, viewY -= 18);
        font.draw(batch, "Hit Points: " + sel.health, x, viewY -= 42);
        font.draw(batch, "Experience: " + sel.exp, x, viewY -= 18);

        viewY = Exodus.SCREEN_HEIGHT - 590;
        x = 504 + 250;

        font.draw(batch, "Food: " + sel.food, x, viewY);
        font.draw(batch, "Gold: " + sel.gold, x, viewY -= 18);
        batch.draw(Exodus.faceTiles[sel.portaitIndex],x+30,Exodus.SCREEN_HEIGHT - 704);

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
