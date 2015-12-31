package exodus;

import objects.BaseMap;
import objects.Creature;
import objects.Drawable;
import objects.Tile;
import util.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import exodus.Exodus.CloudDrawable;
import exodus.Exodus.ExplosionLargeDrawable;
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

        switch (spell) {
            case PONTORI:
            case REPOND:
                if (caster.usedFreeSpellInCombat) {
                    Exodus.hud.add("Can't!");
                    return false;
                } else {
                    caster.usedFreeSpellInCombat = true;
                }
        }

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
                        spellRepond(screen, caster);
                        break;
                    case MITTAR:
                        spellMagicAttack((CombatScreen) screen, caster, Spell.MITTAR, dir, 16, 24);
                        break;
                    case LORUM:
                        ((DungeonScreen) screen).isTorchOn = true;
                        break;
                    case DOR_ACRON:
                        spellZdown(screen, caster);
                        break;
                    case SUR_ACRON:
                        spellYup(screen, caster);
                        break;
                    case FULGAR:
                        spellMagicAttack((CombatScreen) screen, caster, Spell.FULGAR, dir, 32, 64);
                        break;
                    case DAG_ACRON:
                        spellBlink(screen, Direction.getRandomValidDirection(0xff));
                        break;
                    case MENTAR:
                        spellMagicAttack((CombatScreen) screen, caster, Spell.MENTAR, dir, 32, caster.getPlayer().intell);
                        break;
                    case PABULUM:
                        for (PartyMember pm : context.getParty().getMembers()) {
                            pm.getPlayer().food += Utils.getRandomBetween(5, 10);
                        }
                        break;
                    case FAL_DIVI:
                        //REPLACE ME
                        break;
                    case NOXUM:
                        spellGroupDamage(screen, caster, 24, caster.getPlayer().intell);
                        break;
                    case DECORP:
                        spellMagicAttack((CombatScreen) screen, caster, Spell.DECORP, dir, 128, 255);
                        break;
                    case ALTAIR:
                        context.getAura().set(AuraType.QUICKNESS, 5);
                        break;
                    case DAG_MENTAR:
                        spellGroupDamage(screen, caster, 32, caster.getPlayer().intell);
                        break;
                    case NECORP:
                        spellGroupDamage(screen, caster, 64, 128);
                        break;
                    case UNSPEAKABLE:
                        useRageOfGod(screen, caster);
                        break;
                    case PONTORI:
                        spellUndead(screen, caster);
                        break;
                    case APPAR_UNEM:
                        if (screen instanceof GameScreen) {
                            Vector3 v = ((GameScreen) screen).getCurrentMapCoords();
                            ((GameScreen) screen).getChest(caster, (int) v.x, (int) v.y, true);
                        } else if (screen instanceof DungeonScreen) {
                            int x = (Math.round(((DungeonScreen) screen).currentPos.x) - 1);
                            int y = (Math.round(((DungeonScreen) screen).currentPos.z) - 1);
                            ((DungeonScreen) screen).getChest(caster, x, y, true);
                        }
                        break;
                    case SANCTU:
                        subject.heal(HealType.HEAL);
                        if (screen instanceof CombatScreen) {
                            seq.addAction(Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    ((CombatScreen) screen).finishPlayerTurn();
                                }
                            }));
                        }
                        break;
                    case LUMINAE:
                        ((DungeonScreen) screen).isTorchOn = true;
                        break;
                    case REC_SU:
                        spellYup(screen, caster);
                        break;
                    case REC_DU:
                        spellZdown(screen, caster);
                        break;
                    case LIB_REC:
                        spellLibRec(screen, caster);
                        break;
                    case ALCORT:
                        subject.heal(HealType.CURE);
                        if (screen instanceof CombatScreen) {
                            seq.addAction(Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    ((CombatScreen) screen).finishPlayerTurn();
                                }
                            }));
                        }
                        break;
                    case SEQUITU:
                        spellXit(screen, caster);
                        break;
                    case SOMINAE:
                        //REPLACE ME
                        break;
                    case SANCTU_MANI:
                        subject.heal(HealType.FULLHEAL);
                        if (screen instanceof CombatScreen) {
                            seq.addAction(Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    ((CombatScreen) screen).finishPlayerTurn();
                                }
                            }));
                        }
                        break;
                    case VIEDA:
                        spellView(screen, caster);
                        break;
                    case EXCUUN:
                        spellMagicAttack((CombatScreen) screen, caster, Spell.EXCUUN, dir, 64, 255);
                        break;
                    case SURMANDUM:
                        subject.heal(HealType.RESURRECT);
                        if (screen instanceof CombatScreen) {
                            seq.addAction(Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    ((CombatScreen) screen).finishPlayerTurn();
                                }
                            }));
                        }
                        break;
                    case ZXKUQYB:
                        useMaskOfMinax(screen, caster);
                        break;
                    case ANJU_SERMANI:
                        caster.getPlayer().wis -= 5;
                        Exodus.hud.add("Lost 5 points of wisdom!");
                        subject.heal(HealType.RECALL);
                        if (screen instanceof CombatScreen) {
                            seq.addAction(Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    ((CombatScreen) screen).finishPlayerTurn();
                                }
                            }));
                        }
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

    public static void spellGroupDamage(BaseScreen screen, PartyMember caster, int minDamage, int maxDamage) {

        final CombatScreen combatScreen = (CombatScreen) screen;

        SequenceAction seq = Actions.action(SequenceAction.class);

        for (Creature cr : combatScreen.combatMap.getCreatures()) {

            Utils.dealDamage(caster, cr, Utils.getRandomBetween(minDamage, maxDamage));
            Tile tile = Exodus.baseTileSet.getTileByName("hit_flash");
            Drawable d = new Drawable(combatScreen.combatMap, cr.currentX, cr.currentY, tile, Exodus.standardAtlas);
            d.setX(cr.currentPos.x);
            d.setY(cr.currentPos.y);
            d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));

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

    }

    public static void spellUndead(BaseScreen screen, PartyMember caster) {

        SequenceAction seq = Actions.action(SequenceAction.class);

        final CombatScreen combatScreen = (CombatScreen) screen;

        int level = caster.getPlayer().getLevel();

        for (Creature cr : combatScreen.combatMap.getCreatures()) {

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

            if (cr.getUndead() && turn) {
                Tile tile = Exodus.baseTileSet.getTileByName("hit_flash");
                Drawable d = new Drawable(combatScreen.combatMap, cr.currentX, cr.currentY, tile, Exodus.standardAtlas);
                d.setX(cr.currentPos.x);
                d.setY(cr.currentPos.y);

                d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));

                seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));

                Utils.dealDamage(caster, cr, cr.getBasehp());
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

    public static void spellRepond(BaseScreen screen, PartyMember caster) {

        SequenceAction seq = Actions.action(SequenceAction.class);

        final CombatScreen combatScreen = (CombatScreen) screen;

        int level = caster.getPlayer().getLevel();

        for (Creature cr : combatScreen.combatMap.getCreatures()) {

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

            if (turn && (cr.getTile() == CreatureType.troll || cr.getTile() == CreatureType.orc || cr.getTile() == CreatureType.gremlin)) {

                Tile tile = Exodus.baseTileSet.getTileByName("hit_flash");
                Drawable d = new Drawable(combatScreen.combatMap, cr.currentX, cr.currentY, tile, Exodus.standardAtlas);
                d.setX(cr.currentPos.x);
                d.setY(cr.currentPos.y);

                d.addAction(Actions.sequence(Actions.delay(.4f), Actions.fadeOut(.2f), Actions.removeActor()));

                seq.addAction(Actions.run(new AddActorAction(combatScreen.getStage(), d)));
                seq.addAction(Actions.run(new PlaySoundAction(Sound.NPC_STRUCK)));

                Utils.dealDamage(caster, cr, cr.getBasehp());
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
            if (Exodus.mainGame != null) {
                Exodus.mainGame.setScreen(dngScreen.gameScreen);
            }
        }
    }

    public static void spellYup(BaseScreen screen, PartyMember caster) {
        DungeonScreen dngScreen = (DungeonScreen) screen;

        dngScreen.currentLevel--;

        if (dngScreen.currentLevel < 0) {
            dngScreen.currentLevel = 0;
            Exodus.mainGame.setScreen(dngScreen.gameScreen);
        } else {

            for (int i = 0; i < 32; i++) {
                int x = Utils.rand.nextInt(16);
                int y = Utils.rand.nextInt(16);
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

    public static void spellZdown(BaseScreen screen, PartyMember caster) {

        DungeonScreen dngScreen = (DungeonScreen) screen;

        dngScreen.currentLevel++;

        if (dngScreen.currentLevel > DungeonScreen.DUNGEON_LVLS) {

            dngScreen.currentLevel = DungeonScreen.DUNGEON_LVLS;

        } else {

            for (int i = 0; i < 32; i++) {
                int x = Utils.rand.nextInt(16);
                int y = Utils.rand.nextInt(16);
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

    public static void spellLibRec(BaseScreen screen, PartyMember caster) {

        DungeonScreen dngScreen = (DungeonScreen) screen;

        for (int i = 0; i < 32; i++) {
            int x = Utils.rand.nextInt(16);
            int y = Utils.rand.nextInt(16);
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

    public static void destoryAllCreatures(BaseScreen screen, PartyMember caster) {

        if (screen.scType == ScreenType.MAIN) {

            final GameScreen gameScreen = (GameScreen) screen;

            SequenceAction seq = Actions.action(SequenceAction.class);

            for (final Creature cr : screen.context.getCurrentMap().getCreatures()) {

                Utils.dealDamage(caster, cr, 255);

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

        final CombatScreen combatScreen = (CombatScreen) screen;

        final SequenceAction seq = Actions.action(SequenceAction.class);

        for (Creature cr : combatScreen.combatMap.getCreatures()) {

            if (Utils.rand.nextInt(3) == 0) {
                Utils.dealDamage(caster, cr, 255);
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

    }

    public static void useRageOfGod(BaseScreen screen, PartyMember caster) {

        final CombatScreen combatScreen = (CombatScreen) screen;

        final SequenceAction seq = Actions.action(SequenceAction.class);

        for (Creature cr : combatScreen.combatMap.getCreatures()) {

            if (Utils.rand.nextInt(2) == 0) {
                Utils.dealDamage(caster, cr, 255);
            } else if (Utils.rand.nextInt(2) == 0) {
                if (cr.getHP() > 23) {
                    Utils.dealDamage(caster, cr, cr.getHP() - 23);
                }
            } else {
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

    }

}
