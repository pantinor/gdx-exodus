package exodus;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import objects.Person;
import vendor.BaseVendor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import exodus.Party.PartyMember;
import util.LogScrollPane;

public class ConversationDialog extends Window implements Constants {

    boolean cancelHide;
    Actor previousKeyboardFocus, previousScrollFocus;
    FocusListener focusListener;
    GameScreen screen;
    Person person;
    PartyMember member;
    BaseVendor vendor;
    Stage stage;

    public static int width = 300;
    public static int height = 400;

    Table internalTable;
    TextField input;
    LogScrollPane scrollPane;

    public ConversationDialog(PartyMember member, Person p, GameScreen screen, Stage stage) {
        super("", Exodus.skin.get("dialog", WindowStyle.class));
        setSkin(Exodus.skin);
        this.stage = stage;
        this.screen = screen;
        this.person = p;
        this.member = member;
        initialize();
    }

    private void initialize() {

        screen.gameTimer.active = false;

        setModal(true);

        defaults().space(10);
        add(internalTable = new Table(Exodus.skin)).expand().fill();
        row();

        internalTable.defaults().pad(1);

        scrollPane = new LogScrollPane(Exodus.skin, width);
        scrollPane.setHeight(height);

        input = new TextField("", Exodus.skin);
        input.setTextFieldListener(new TextFieldListener() {
            @Override
            public void keyTyped(TextField tf, char key) {

                if (key == '\r') {

                    if (tf.getText().length() == 0) {
                        if (!cancelHide) {
                            hide();
                        }
                        cancelHide = false;
                    }

                    if (vendor != null) {

                        String input = tf.getText();
                        vendor.setResponse(input);
                        vendor.nextDialog();

                    } else {

                        scrollPane.add(person.getConversation());

                    }

                    tf.setText("");
                }
            }
        });

        defaults().pad(5);

        internalTable.add(scrollPane).maxWidth(width).width(width);
        internalTable.row();
        internalTable.add(input).maxWidth(width).width(width);

        focusListener = new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            @Override
            public void scrollFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            private void focusChanged(FocusEvent event) {
                Stage stage = getStage();
                if (isModal() && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == ConversationDialog.this) {
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(ConversationDialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus))) {
                        event.cancel();
                    }
                }
            }
        };

        person.setTalking(true);

//        if (person.getConversation() != null) {
//            if (person.getVendor() != null && person.getVendor().getRole().equals("lordbritish")) {
//
//                LordBritishConversation conv = (LordBritishConversation) person.getConversation();
//                scrollPane.add(conv.intro(screen.context));
//
//                SequenceAction seq = Actions.action(SequenceAction.class);
//                Party party = screen.context.getParty();
//                if (party.getMember(0).getPlayer().status == StatusType.DEAD) {
//                    party.getMember(0).heal(HealType.RESURRECT);
//                    party.getMember(0).heal(HealType.FULLHEAL);
//                    seq.addAction(Actions.run(new LBAction(Sound.HEALING, "I resurrect thee.")));
//                    seq.addAction(Actions.delay(3f));
//                }
//
//                for (int i = 0; i < party.getMembers().size(); i++) {
//                    PartyMember pm = party.getMember(i);
//                    if (pm.getPlayer().advanceLevel()) {
//                        seq.addAction(Actions.run(new LBAction(Sound.MAGIC, pm.getPlayer().name + " thou art now level " + pm.getPlayer().getLevel())));
//                        seq.addAction(Actions.delay(3f));
//                    }
//                }
//
//                stage.addAction(seq);
//
//            } else {
//                scrollPane.add(person.getConversation());
//            }
        if (person.getVendor() != null) {

            vendor = Exodus.vendorClassSet.getVendorImpl(person.getVendor().getVendorType(), Maps.get(screen.context.getCurrentMap().getId()), screen.context, member);
            vendor.setScreen(screen);
            vendor.setScrollPane(scrollPane);
            vendor.nextDialog();
        }

    }

    class LBAction implements Runnable {

        private Sound sound;
        private String message;

        public LBAction(Sound sound, String message) {
            this.sound = sound;
            this.message = message;
        }

        @Override
        public void run() {
            Sounds.play(sound);
            scrollPane.add(message);
        }
    }

    protected void workspace(Stage stage) {
        if (stage == null) {
            addListener(focusListener);
        } else {
            removeListener(focusListener);
        }
        super.setStage(stage);
    }

    public ConversationDialog show(Stage stage, Action action) {
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
        stage.setKeyboardFocus(input);
        stage.setScrollFocus(this);

        if (action != null) {
            addAction(action);
        }

        return this;
    }

    public ConversationDialog show(Stage stage) {
        show(stage, sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade)));
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
        return this;
    }

    public void hide(Action action) {

        Stage stage = getStage();
        if (stage != null) {
            removeListener(focusListener);
            if (previousKeyboardFocus != null && previousKeyboardFocus.getStage() == null) {
                previousKeyboardFocus = null;
            }
            Actor actor = stage.getKeyboardFocus();
            if (actor == null || actor.isDescendantOf(this)) {
                stage.setKeyboardFocus(previousKeyboardFocus);
            }

            if (previousScrollFocus != null && previousScrollFocus.getStage() == null) {
                previousScrollFocus = null;
            }
            actor = stage.getScrollFocus();
            if (actor == null || actor.isDescendantOf(this)) {
                stage.setScrollFocus(previousScrollFocus);
            }
        }
        if (action != null) {
            addCaptureListener(ignoreTouchDown);
            addAction(sequence(action, Actions.removeListener(ignoreTouchDown, true), Actions.removeActor()));
        } else {
            remove();
        }

        Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
        screen.gameTimer.active = true;

        screen.context.getCurrentMap().resetTalkingFlags();

    }

    public void hide() {
        hide(sequence(fadeOut(0.4f, Interpolation.fade), Actions.removeListener(ignoreTouchDown, true), Actions.removeActor()));
    }

    protected InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };
}
