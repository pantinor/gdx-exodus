package exodus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import exodus.Party.PartyMember;
import java.util.Map;

public class SpellInputProcessor extends InputAdapter implements Constants {

    private final BaseScreen screen;
    private final Stage stage;
    private Spell spell;
    private final PartyMember caster;
    private final Context context;
    private final Map<String, Spell> spellSelection;

    public SpellInputProcessor(BaseScreen screen, Context context, Stage stage, Map<String, Spell> spellSelection, PartyMember pm) {
        this.screen = screen;
        this.stage = stage;
        this.caster = pm;
        this.context = context;
        this.spellSelection = spellSelection;
        
        screen.log("Which Spell? ");
        for (String key : this.spellSelection.keySet()) {
            screen.log(String.format("%s - %s (%d)", key, this.spellSelection.get(key), this.spellSelection.get(key).getCost()));
        }

    }

    @Override
    public boolean keyUp(int keycode) {

        if (keycode >= Keys.A && keycode <= this.spellSelection.size() + Keys.A) {

            spell = this.spellSelection.get(Keys.toString(keycode));
            screen.log("" + spell.getDesc() + "!");

            switch (spell) {
                case SANCTU:
                case ALCORT:
                case SANCTU_MANI:
                case SURMANDUM:
                case ANJU_SERMANI:
                    screen.log("on who (1-4)? ");
                    Gdx.input.setInputProcessor(new SubjectInputAdapter());
                    break;

                case MITTAR:
                case FULGAR:
                case MENTAR:
                case DECORP:
                case APPAR_UNEM:
                case EXCUUN:
                    screen.log("Direction? ");
                    Gdx.input.setInputProcessor(new DirectionInputAdapter());
                    break;

                default:
                    SpellUtil.spellCast(screen, context, spell, caster, null, null);
                    Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
                    break;

            }

        } else {
            screen.log("Not a spell!");
            Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
        }

        return false;
    }

    class SubjectInputAdapter extends InputAdapter {

        @Override
        public boolean keyUp(int keycode) {
            if (keycode >= Keys.NUM_1 && keycode <= Keys.NUM_4) {
                screen.logAppend("" + (keycode - 7));
                PartyMember subject = context.getParty().getMember(keycode - 7 - 1);
                SpellUtil.spellCast(screen, context, spell, caster, subject, null);
            } else {
                screen.log("Not a player!");
            }
            Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
            return false;
        }
    }

    class DirectionInputAdapter extends InputAdapter {

        @Override
        public boolean keyUp(int keycode) {
            Direction dir = Direction.NORTH;
            if (keycode == Keys.UP) {
                dir = Direction.NORTH;
            } else if (keycode == Keys.DOWN) {
                dir = Direction.SOUTH;
            } else if (keycode == Keys.LEFT) {
                dir = Direction.WEST;
            } else if (keycode == Keys.RIGHT) {
                dir = Direction.EAST;
            } else {
                screen.log("what?");
                Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
                return false;
            }

            if (!(screen instanceof DungeonScreen)) {
                screen.logAppend(dir.toString());
            }

            SpellUtil.spellCast(screen, context, spell, caster, null, dir);
            Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
            return false;
        }
    }

}
