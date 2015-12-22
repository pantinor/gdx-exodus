package exodus;

import objects.BaseMap;
import objects.Creature;
import objects.Drawable;
import objects.Tile;
import util.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import exodus.Party.PartyMember;

public class SpellUtil implements Constants {

    public static boolean spellCast(final BaseScreen screen, final Context context, final Spell spell,
            final PartyMember caster, final PartyMember subject, final Direction dir) {

        if (caster == null || spell == null || screen == null) {
            return false;
        }

        switch (spell) {
            case SANCTU:
            case ALCORT:
            case SANCTU_MANI:
            case SURMANDUM:
            case ANJU_SERMANI:
                if (subject == null) {
                    Exodus.hud.add("Thou must indicate a target to cast the spell!");
                    return false;
                }
                break;

            case MITTAR:
            case FULGAR:
            case MENTAR:
            case DECORP:
            case APPAR_UNEM:
            case EXCUUN:
                if (dir == null) {
                    Exodus.hud.add("Thou must indicate a direction to cast the spell!");
                    return false;
                }
                break;

            default:
                break;

        }

        if (caster.getPlayer().mana < spell.getCost()) {
            Exodus.hud.add("Thou dost not have enough magic points!");
            return false;
        }

        Party party = context.getParty();

        if (context.getAura().getType() == AuraType.NEGATE) {
            Exodus.hud.add("Spell is negated!");
            return false;
        }

        caster.adjustMagic(spell.getCost());

        SequenceAction seq = Actions.action(SequenceAction.class);
        seq.addAction(Actions.run(new PlaySoundAction(spell.getSound())));
        seq.addAction(Actions.delay(0.5f));
        seq.addAction(Actions.run(new Runnable() {
            @Override
            public void run() {
                switch (spell) {
                    case REPOND:
                        spellRepel(screen, caster);
                        break;
                    case MITTAR:
                        spellMagicAttack((CombatScreen) screen, caster, Spell.MITTAR, dir, 64, 16);
                        break;
                    case LORUM:
                        break;
                    case DOR_ACRON:
                        spellZdown(screen, caster);
                        break;
                    case SUR_ACRON:
                        spellYup(screen, caster);
                        break;
                    case FULGAR:
                        spellMagicAttack((CombatScreen) screen, caster, Spell.FULGAR, dir, 24, 128);
                        break;
                    case DAG_ACRON:
                        spellBlink(screen, Direction.EAST);
                        break;
                    case MENTAR:
                        spellMagicAttack((CombatScreen) screen, caster, Spell.MENTAR, dir, 64, 20);
                        break;
                    case DAG_LORUM:
                        break;
                    case FAL_DIVI:
                        break;
                    case NOXUM:
                        spellTremor(screen, caster);
                        break;
                    case DECORP:
                        spellMagicAttack((CombatScreen) screen, caster, Spell.DECORP, dir, -1, 232);
                        break;
                    case ALTAIR:
                        context.getAura().set(AuraType.NEGATE, 10);
                        break;
                    case DAG_MENTAR:
                        break;
                    case NECORP:
                        break;
                    case NOTHING:
                        useRageOfGod(screen, caster);
                        break;
                    case PONTORI:
                        spellUndead(screen, caster);
                        break;
                    case APPAR_UNEM:
                        break;
                    case SANCTU:
                        subject.heal(HealType.HEAL);
                        break;
                    case LUMINAE:
                        break;
                    case REC_SU:
                        spellYup(screen, caster);
                        break;
                    case REC_DU:
                        spellZdown(screen, caster);
                        break;
                    case LIB_REC:
                        break;
                    case ALCORT:
                        subject.heal(HealType.CURE);
                        break;
                    case SEQUITU:
                        spellXit(screen, caster);
                        break;
                    case SOMINAE:
                        break;
                    case SANCTU_MANI:
                        subject.heal(HealType.FULLHEAL);
                        break;
                    case VIEDA:
                        spellView(screen, caster);
                        break;
                    case EXCUUN:
                        break;
                    case SURMANDUM:
                        subject.heal(HealType.RESURRECT);
                        break;
                    case ZXKUQYB:
                        useMaskOfMinax(screen, caster);
                        break;
                    case ANJU_SERMANI:
                        subject.heal(HealType.RECALL);
                        break;
                    default:
                        break;

                }
            }
        }));

        screen.getStage().addAction(seq);

        return true;
    }

    private static void spellMagicAttack(CombatScreen screen, PartyMember caster, Spell spell, Direction dir, int minDamage, int maxDamage) {

        int x = caster.combatCr.currentX;
        int y = caster.combatCr.currentY;
        if (dir == Direction.NORTH) {
            y--;
        }
        if (dir == Direction.SOUTH) {
            y++;
        }
        if (dir == Direction.EAST) {
            x++;
        }
        if (dir == Direction.WEST) {
            x--;
        }

        Utils.animateMagicAttack(screen.getStage(), screen, caster, dir, x, y, spell, minDamage, maxDamage);

    }

    public static void spellBlink(BaseScreen screen, Direction dir) {

        if (screen.scType == ScreenType.MAIN) {

            GameScreen gameScreen = (GameScreen) screen;
            BaseMap bm = screen.context.getCurrentMap();

            Vector3 v = gameScreen.getCurrentMapCoords();
            int x = (int) v.x;
            int y = (int) v.y;

            if (bm.getId() != Maps.SOSARIA.getId()) {
                return;
            }

            int distance = 0;
            int diff = 0;
            Direction reverseDir = Direction.reverse(dir);

            int var = (dir.getMask() & (Direction.WEST.getMask() | Direction.EAST.getMask())) > 0 ? x : y;

            /* find the distance we are going to move */
            distance = (var) % 0x10;
            if (dir == Direction.EAST || dir == Direction.SOUTH) {
                distance = 0x10 - distance;
            }

            /* see if we move another 16 spaces over */
            diff = 0x10 - distance;
            if ((diff > 0) && (Utils.rand.nextInt(diff * diff) > distance)) {
                distance += 0x10;
            }

            /* test our distance, and see if it works */
            for (int i = 0; i < distance; i++) {
                if (dir == Direction.NORTH) {
                    y--;
                }
                if (dir == Direction.SOUTH) {
                    y++;
                }
                if (dir == Direction.WEST) {
                    x--;
                }
                if (dir == Direction.EAST) {
                    x++;
                }
            }

            int i = distance;
            /* begin walking backward until you find a valid spot */
            while ((i-- > 0) && bm.getTile(x, y) != null && bm.getTile(x, y).getRule().has(TileAttrib.unwalkable)) {
                if (reverseDir == Direction.NORTH) {
                    y--;
                }
                if (reverseDir == Direction.SOUTH) {
                    y++;
                }
                if (reverseDir == Direction.WEST) {
                    x--;
                }
                if (reverseDir == Direction.EAST) {
                    x++;
                }
            }

            if (bm.getTile(x, y) != null && !bm.getTile(x, y).getRule().has(TileAttrib.unwalkable)) {

                /* we didn't move! */
                if (x == (int) v.x && y == (int) v.y) {
                    screen.log("Failed to teleport!");
                }

                gameScreen.newMapPixelCoords = gameScreen.getMapPixelCoords(x, y);
                gameScreen.recalcFOV(bm, x, y);

            } else {
                screen.log("Failed to teleport!");
            }
        } else {
            screen.log("Outdoors only!");
        }

    }

    public static void spellTremor(BaseScreen screen, PartyMember caster) {

        final CombatScreen combatScreen = (CombatScreen) screen;

        SequenceAction seq = Actions.action(SequenceAction.class);

        for (Creature cr : combatScreen.combatMap.getCreatures()) {

            if (cr.getHP() > 192) {
                seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
                continue;
            } else {

                if (Utils.rand.nextInt(2) == 0) {
                    /* Deal maximum damage to creature */
                    Utils.dealDamage(caster, cr, 0xFF);

                    Tile tile = Exodus.baseTileSet.getTileByName("hit_flash");
                    Drawable d = new Drawable(combatScreen.combatMap, cr.currentX, cr.currentY, tile, Exodus.standardAtlas);
                    d.setX(cr.currentPos.x);
                    d.setY(cr.currentPos.y);
                    d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));

                    seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
                    seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));

                } else if (Utils.rand.nextInt(2) == 0) {
                    /* Deal enough damage to creature to make it flee */
                    if (cr.getHP() > 23) {
                        Utils.dealDamage(caster, cr, cr.getHP() - 23);

                        Tile tile = Exodus.baseTileSet.getTileByName("hit_flash");
                        Drawable d = new Drawable(combatScreen.combatMap, cr.currentX, cr.currentY, tile, Exodus.standardAtlas);
                        d.setX(cr.currentPos.x);
                        d.setY(cr.currentPos.y);
                        d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));

                        seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
                        seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));

                    }
                } else {
                    seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
                }
            }

            if (cr.getDamageStatus() == CreatureStatus.DEAD) {
                seq.addAction(Actions.run(combatScreen.new RemoveCreatureAction(cr)));
            }

        }

        seq.addAction(Actions.run(new Runnable() {
            @Override
            public void run() {
                combatScreen.finishPlayerTurn();
            }
        }));

        combatScreen.getStage().addAction(seq);

    }

    public static void spellUndead(BaseScreen screen, PartyMember caster) {

        SequenceAction seq = Actions.action(SequenceAction.class);

        final CombatScreen combatScreen = (CombatScreen) screen;

        int level = caster.getPlayer().getLevel();
        boolean turn = Utils.rand.nextInt(100) >= 50;

        if (level > 5) {
            turn = Utils.rand.nextInt(100) >= 35;
        }
        if (level > 10) {
            turn = Utils.rand.nextInt(100) >= 20;
        }
        if (level > 15) {
            turn = true;
        }

        for (Creature cr : combatScreen.combatMap.getCreatures()) {
            if (cr.getUndead() && turn) {

                Tile tile = Exodus.baseTileSet.getTileByName("hit_flash");
                Drawable d = new Drawable(combatScreen.combatMap, cr.currentX, cr.currentY, tile, Exodus.standardAtlas);
                d.setX(cr.currentPos.x);
                d.setY(cr.currentPos.y);

                d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));

                seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));

                Utils.dealDamage(caster, cr, 23);
            } else {
                seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
            }

            if (cr.getDamageStatus() == CreatureStatus.DEAD) {
                seq.addAction(Actions.run(combatScreen.new RemoveCreatureAction(cr)));
            }
        }

        seq.addAction(Actions.run(new Runnable() {
            @Override
            public void run() {
                combatScreen.finishPlayerTurn();
            }
        }));

        combatScreen.getStage().addAction(seq);

    }

    public static void spellRepel(BaseScreen screen, PartyMember caster) {

        SequenceAction seq = Actions.action(SequenceAction.class);

        final CombatScreen combatScreen = (CombatScreen) screen;

        int level = caster.getPlayer().getLevel();
        boolean turn = Utils.rand.nextInt(100) >= 50;

        if (level > 5) {
            turn = Utils.rand.nextInt(100) >= 35;
        }
        if (level > 10) {
            turn = Utils.rand.nextInt(100) >= 20;
        }
        if (level > 15) {
            turn = true;
        }

        for (Creature cr : combatScreen.combatMap.getCreatures()) {
            if (turn && (cr.getTile() == CreatureType.troll || cr.getTile() == CreatureType.orc || cr.getTile() == CreatureType.gremlin)) {

                Tile tile = Exodus.baseTileSet.getTileByName("hit_flash");
                Drawable d = new Drawable(combatScreen.combatMap, cr.currentX, cr.currentY, tile, Exodus.standardAtlas);
                d.setX(cr.currentPos.x);
                d.setY(cr.currentPos.y);

                d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));

                seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));

                Utils.dealDamage(caster, cr, 23);
            } else {
                seq.addAction(Actions.run(new PlaySoundAction(Sound.EVADE)));
            }

            if (cr.getDamageStatus() == CreatureStatus.DEAD) {
                seq.addAction(Actions.run(combatScreen.new RemoveCreatureAction(cr)));
            }
        }

        seq.addAction(Actions.run(new Runnable() {
            @Override
            public void run() {
                combatScreen.finishPlayerTurn();
            }
        }));

        combatScreen.getStage().addAction(seq);

    }

    public static void spellView(BaseScreen screen, PartyMember caster) {
        InputProcessor ip = screen.getPeerGemInputProcessor();
        if (ip != null) {
            Gdx.input.setInputProcessor(ip);
        }
    }

    public static void spellXit(BaseScreen screen, PartyMember caster) {
        if (screen.scType == ScreenType.DUNGEON) {
            DungeonScreen dngScreen = (DungeonScreen) screen;
            screen.log("Leaving " + dngScreen.dngMap.getLabel());
            if (DungeonScreen.mainGame != null) {
                DungeonScreen.mainGame.setScreen(dngScreen.gameScreen);
            }
        }
    }

    public static void spellYup(BaseScreen screen, PartyMember caster) {
        if (screen.scType == ScreenType.DUNGEON) {
            DungeonScreen dngScreen = (DungeonScreen) screen;

            dngScreen.currentLevel--;

            if (dngScreen.currentLevel < 0) {
                dngScreen.currentLevel = 0;
                if (dngScreen.mainGame != null) {
                    dngScreen.mainGame.setScreen(dngScreen.gameScreen);
                }
            } else {

                for (int i = 0; i < 32; i++) {
                    int x = Utils.rand.nextInt(8);
                    int y = Utils.rand.nextInt(8);
                    if (dngScreen.validTeleportLocation(x, y, dngScreen.currentLevel)) {
                        dngScreen.currentPos = new Vector3(x + .5f, .5f, y + .5f);
                        dngScreen.camera.position.set(dngScreen.currentPos);
                        if (dngScreen.currentDir == Direction.EAST) {
                            dngScreen.camera.lookAt(dngScreen.currentPos.x + 1, dngScreen.currentPos.y, dngScreen.currentPos.z);
                        } else if (dngScreen.currentDir == Direction.WEST) {
                            dngScreen.camera.lookAt(dngScreen.currentPos.x - 1, dngScreen.currentPos.y, dngScreen.currentPos.z);
                        } else if (dngScreen.currentDir == Direction.NORTH) {
                            dngScreen.camera.lookAt(dngScreen.currentPos.x, dngScreen.currentPos.y, dngScreen.currentPos.z - 1);
                        } else if (dngScreen.currentDir == Direction.SOUTH) {
                            dngScreen.camera.lookAt(dngScreen.currentPos.x, dngScreen.currentPos.y, dngScreen.currentPos.z + 1);
                        }
                        dngScreen.moveMiniMapIcon();
                        break;
                    }
                }

                dngScreen.createMiniMap();
            }

        }
    }

    public static void spellZdown(BaseScreen screen, PartyMember caster) {
        if (screen.scType == ScreenType.DUNGEON) {
            DungeonScreen dngScreen = (DungeonScreen) screen;

            dngScreen.currentLevel++;

            if (dngScreen.currentLevel > DungeonScreen.DUNGEON_LVLS) {

                dngScreen.currentLevel = DungeonScreen.DUNGEON_LVLS;

            } else {

                for (int i = 0; i < 32; i++) {
                    int x = Utils.rand.nextInt(8);
                    int y = Utils.rand.nextInt(8);
                    if (dngScreen.validTeleportLocation(x, y, dngScreen.currentLevel)) {
                        dngScreen.currentPos = new Vector3(x + .5f, .5f, y + .5f);
                        dngScreen.camera.position.set(dngScreen.currentPos);
                        if (dngScreen.currentDir == Direction.EAST) {
                            dngScreen.camera.lookAt(dngScreen.currentPos.x + 1, dngScreen.currentPos.y, dngScreen.currentPos.z);
                        } else if (dngScreen.currentDir == Direction.WEST) {
                            dngScreen.camera.lookAt(dngScreen.currentPos.x - 1, dngScreen.currentPos.y, dngScreen.currentPos.z);
                        } else if (dngScreen.currentDir == Direction.NORTH) {
                            dngScreen.camera.lookAt(dngScreen.currentPos.x, dngScreen.currentPos.y, dngScreen.currentPos.z - 1);
                        } else if (dngScreen.currentDir == Direction.SOUTH) {
                            dngScreen.camera.lookAt(dngScreen.currentPos.x, dngScreen.currentPos.y, dngScreen.currentPos.z + 1);
                        }
                        dngScreen.moveMiniMapIcon();
                        break;
                    }
                }

                dngScreen.createMiniMap();
            }

        }
    }

    public static void destoryAllCreatures(BaseScreen screen, PartyMember caster) {

        if (screen.scType == ScreenType.MAIN) {

            final GameScreen gameScreen = (GameScreen) screen;

            SequenceAction seq = Actions.action(SequenceAction.class);

            for (final Creature cr : screen.context.getCurrentMap().getCreatures()) {

                /* Deal maximum damage to creature */
                Utils.dealDamage(caster, cr, 0xFF);

                Tile tile = Exodus.baseTileSet.getTileByName("hit_flash");
                Drawable d = new Drawable(screen.context.getCurrentMap(), cr.currentX, cr.currentY, tile, Exodus.standardAtlas);
                d.setX(cr.currentPos.x);
                d.setY(cr.currentPos.y);
                d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));

                seq.addAction(Actions.run(new AddActorAction(gameScreen.getStage(), d)));
                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));
                seq.addAction(Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        screen.context.getCurrentMap().getCreatures().remove(cr);
                    }
                }));

            }

            gameScreen.getStage().addAction(seq);

        }

    }

    public static void useMaskOfMinax(BaseScreen screen, PartyMember caster) {

        if (screen.scType == ScreenType.COMBAT) {

            final CombatScreen combatScreen = (CombatScreen) screen;

            final SequenceAction seq = Actions.action(SequenceAction.class);

            for (Creature cr : combatScreen.combatMap.getCreatures()) {

                if (Utils.rand.nextInt(3) == 0) {
                    /* Deal maximum damage to creature */
                    Utils.dealDamage(caster, cr, 0xFF);
                } else {
                    if (cr.getHP() > 23) {
                        Utils.dealDamage(caster, cr, cr.getHP() * (3 / 4));
                    } else {
                        Utils.dealDamage(caster, cr, 15);
                    }
                }

                Actor d = new CloudDrawable();
                d.setX(cr.currentPos.x - 16);
                d.setY(cr.currentPos.y - 16);
                d.addAction(Actions.sequence(Actions.delay(2f), Actions.removeActor()));

                seq.addAction(Actions.run(new PlaySoundAction(Sound.SPIRITS)));
                seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));

                if (cr.getDamageStatus() == CreatureStatus.DEAD) {
                    seq.addAction(Actions.run(combatScreen.new RemoveCreatureAction(cr)));
                }

            }

            seq.addAction(Actions.run(new Runnable() {
                @Override
                public void run() {
                    combatScreen.finishPlayerTurn();
                }
            }));

            combatScreen.getStage().addAction(seq);

        } else {
            Sounds.play(Sound.ERROR);
        }

    }

    public static void useRageOfGod(BaseScreen screen, PartyMember caster) {

        if (screen.scType == ScreenType.COMBAT) {

            final CombatScreen combatScreen = (CombatScreen) screen;

            final SequenceAction seq = Actions.action(SequenceAction.class);

            for (Creature cr : combatScreen.combatMap.getCreatures()) {

                if (Utils.rand.nextInt(2) == 0) {
                    /* Deal maximum damage to creature */
                    Utils.dealDamage(caster, cr, 0xFF);
                } else if (Utils.rand.nextInt(2) == 0) {
                    /* Deal enough damage to creature to make it flee */
                    if (cr.getHP() > 23) {
                        Utils.dealDamage(caster, cr, cr.getHP() - 23);
                    }
                } else {
                    //deal damage of half its hit points
                    Utils.dealDamage(caster, cr, cr.getHP() / 2);
                }

                Actor d = new ExplosionLargeDrawable();
                d.setX(cr.currentPos.x - 32 * 3 + 16);
                d.setY(cr.currentPos.y - 32 * 3 + 16);
                d.addAction(Actions.sequence(Actions.delay(2f), Actions.removeActor()));

                seq.addAction(Actions.run(new PlaySoundAction(Sound.RAGE)));
                seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));

                if (cr.getDamageStatus() == CreatureStatus.DEAD) {
                    seq.addAction(Actions.run(combatScreen.new RemoveCreatureAction(cr)));
                }

            }

            seq.addAction(Actions.run(new Runnable() {
                @Override
                public void run() {
                    combatScreen.finishPlayerTurn();
                }
            }));

            combatScreen.getStage().addAction(seq);

        } else {
            Sounds.play(Sound.ERROR);
        }

    }

    private static class CloudDrawable extends Actor {

        float stateTime;

        @Override
        public void draw(Batch batch, float parentAlpha) {
            stateTime += Gdx.graphics.getDeltaTime();
            batch.draw(Exodus.cloud.getKeyFrame(stateTime, false), getX(), getY(), 64, 64);
        }
    }

    private static class ExplosionDrawable extends Actor {

        float stateTime;

        @Override
        public void draw(Batch batch, float parentAlpha) {
            stateTime += Gdx.graphics.getDeltaTime();
            batch.draw(Exodus.explosion.getKeyFrame(stateTime, false), getX(), getY(), 64, 64);
        }
    }

    private static class ExplosionLargeDrawable extends Actor {

        float stateTime;

        @Override
        public void draw(Batch batch, float parentAlpha) {
            stateTime += Gdx.graphics.getDeltaTime();
            batch.draw(Exodus.explosionLarge.getKeyFrame(stateTime, false), getX(), getY(), 192, 192);
        }
    }

}
