package exodus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import objects.SaveGame;

public class HandDialog extends Window implements Constants {

    boolean cancelHide;
    Actor previousKeyboardFocus, previousScrollFocus;
    FocusListener focusListener;
    private final GameScreen screen;
    private final Context context;

    public static int WIDTH = 300;
    public static int HEIGHT = 400;

    private final Table internalTable;

    private final List<String> player1Selection;
    private final List<String> player2Selection;
    private final ScrollPane player1Scroll;
    private final ScrollPane player2Scroll;
    private final Table equipTable;
    private final ScrollPane equipScroll;
    private EquipmentListing selectedEquip;
    private final TextButton hand;
    private final TextButton exit;
    private final Image focusIndicator;

    public HandDialog(GameScreen screen, Context context) {
        super("Hand Equipment", Exodus.skin.get("dialog", Window.WindowStyle.class));
        setSkin(Exodus.skin);
        this.screen = screen;
        this.context = context;
        screen.gameTimer.active = false;
        setModal(true);
        defaults().space(10).pad(5);

        this.internalTable = new Table(Exodus.skin);
        this.internalTable.defaults().pad(5);

        add(this.internalTable).expand().fill();
        row();

        this.focusIndicator = new Image(fillRectangle(200, 27, Color.YELLOW, .45f));
        this.focusIndicator.setWidth(200);
        this.focusIndicator.setHeight(27);

        this.equipTable = new Table(Exodus.skin);
        this.equipTable.defaults().align(Align.left);

        this.player1Selection = new List<>(Exodus.skin);
        this.player2Selection = new List<>(Exodus.skin);

        Array<String> names1 = new Array<>();
        Array<String> names2 = new Array<>();
        for (Party.PartyMember pm : context.getParty().getMembers()) {
            names1.add(pm.getPlayer().name);
            names2.add(pm.getPlayer().name);
        }

        this.player1Selection.setItems(names1);
        this.player2Selection.setItems(names2);

        EventListener li = new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event.toString().equals("touchDown")) {
                    setEquip();
                }
                return false;
            }
        };

        this.player1Selection.addListener(li);
        setEquip();

        this.exit = new TextButton("X", Exodus.skin);
        this.exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                hide();
            }
        });
        getTitleTable().add(this.exit).height(getPadTop()).width(20);

        this.hand = new TextButton(">", Exodus.skin);
        this.hand.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                SaveGame.CharacterRecord rec1 = context.getParty().getMember(player1Selection.getSelectedIndex()).getPlayer();
                SaveGame.CharacterRecord rec2 = context.getParty().getMember(player2Selection.getSelectedIndex()).getPlayer();
                if (rec1.name.equals(rec2.name) || selectedEquip == null) {
                    Sounds.play(Sound.BLOCKED);
                } else {
                    String eq = selectedEquip.name.getText().toString();
                    try {
                        WeaponType wt = WeaponType.valueOf(eq);
                        if (rec1.weapons[wt.ordinal()] > 0) {
                            rec1.weapons[wt.ordinal()]--;
                            selectedEquip.count.setText("(" + rec1.weapons[wt.ordinal()] + ")");
                            rec2.weapons[wt.ordinal()]++;
                        } else {
                            Sounds.play(Sound.BLOCKED);
                        }
                    } catch (IllegalArgumentException e) {
                        try {
                            ArmorType at = ArmorType.valueOf(eq);
                            if (rec1.armors[at.ordinal()] > 0) {
                                rec1.armors[at.ordinal()]--;
                                selectedEquip.count.setText("(" + rec1.armors[at.ordinal()] + ")");
                                rec2.armors[at.ordinal()]++;
                            } else {
                                Sounds.play(Sound.BLOCKED);
                            }
                        } catch (IllegalArgumentException ex) {
                        }
                    }
                }
            }

        }
        );

        this.player1Scroll = new ScrollPane(this.player1Selection, Exodus.skin);

        this.equipScroll = new ScrollPane(this.equipTable, Exodus.skin);

        this.player2Scroll = new ScrollPane(this.player2Selection, Exodus.skin);

        this.equipScroll.addListener(
                new EventListener() {
            @Override
            public boolean handle(Event event
            ) {
                if (event.toString().equals("touchDown")) {
                    if (focusIndicator.getParent() != null) {
                        focusIndicator.getParent().removeActor(focusIndicator);
                    }
                    if (event.getTarget() instanceof EquipmentListing) {
                        selectedEquip = (EquipmentListing) event.getTarget();
                        selectedEquip.addActor(focusIndicator);
                    } else if (event.getTarget().getParent() instanceof EquipmentListing) {
                        selectedEquip = (EquipmentListing) event.getTarget().getParent();
                        selectedEquip.addActor(focusIndicator);
                    }
                }
                return false;
            }
        }
        );

        internalTable.add(player1Scroll)
                .maxWidth(100).width(100);
        internalTable.add(equipScroll)
                .maxWidth(200).width(200);//.maxHeight(400).height(400);
        internalTable.add(hand)
                .maxWidth(50).width(50);
        internalTable.add(player2Scroll)
                .maxWidth(100).width(100);
        internalTable.row();

        focusListener = new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            @Override
            public void scrollFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            private void focusChanged(FocusListener.FocusEvent event) {
                Stage stage = getStage();
                if (isModal() && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == HandDialog.this) {
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(HandDialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus))) {
                        event.cancel();
                    }
                }
            }
        };

    }

    private void setEquip() {
        equipTable.clear();
        selectedEquip = null;
        SaveGame.CharacterRecord rec = context.getParty().getMember(player1Selection.getSelectedIndex()).getPlayer();
        for (int i = 1; i < rec.weapons.length - 1; i++) {
            if (rec.weapons[i] > 0) {
                equipTable.add(new EquipmentListing(WeaponType.get(i).toString(), rec.weapons[i]));
                equipTable.row();
            }
        }
        for (int i = 1; i < rec.armors.length - 1; i++) {
            if (rec.armors[i] > 0) {
                equipTable.add(new EquipmentListing(ArmorType.get(i).toString(), rec.armors[i]));
                equipTable.row();
            }
        }
    }

    public void show(Stage stage) {

        clearActions();

        removeCaptureListener(ignoreTouchDown);

        previousKeyboardFocus = null;
        Actor actor = stage.getKeyboardFocus();
        if (actor != null && !actor.isDescendantOf(this)) {
            previousKeyboardFocus = actor;
        }

        previousScrollFocus = null;
        actor = stage.getScrollFocus();
        if (actor != null && !actor.isDescendantOf(this)) {
            previousScrollFocus = actor;
        }

        pack();
        stage.addActor(this);
        stage.setKeyboardFocus(player1Selection);

        stage.setScrollFocus(this);

        Action action = sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade));
        addAction(action);

        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
    }

    public void hide() {
        Action action = sequence(fadeOut(0.4f, Interpolation.fade), Actions.removeListener(ignoreTouchDown, true), Actions.removeActor());

        Stage stage = getStage();

        if (stage != null) {
            removeListener(focusListener);
        }

        if (action != null) {
            addCaptureListener(ignoreTouchDown);
            addAction(sequence(action, Actions.removeListener(ignoreTouchDown, true), Actions.removeActor()));
        } else {
            remove();
        }

        Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));

        screen.gameTimer.active = true;
    }

    protected InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

    public static Texture fillRectangle(int width, int height, Color color, float alpha) {
        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pix.setColor(color.r, color.g, color.b, alpha);
        pix.fillRectangle(0, 0, width, height);
        Texture t = new Texture(pix);
        pix.dispose();
        return t;

    }

    private class EquipmentListing extends Group {

        final Label name;
        final Label count;

        EquipmentListing(String name, int count) {
            this.name = new Label(name, Exodus.skin);
            this.count = new Label("(" + count + ")", Exodus.skin);

            addActor(this.name);
            addActor(this.count);

            float x = getX();
            this.name.setBounds(x, getY(), 100, 27);
            this.count.setBounds(x += 100, getY(), 100, 27);

            this.setBounds(getX(), getY(), 200, 27);
        }

    }
}
