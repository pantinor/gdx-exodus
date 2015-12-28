package exodus;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import static exodus.Constants.STATS_PLAYER1;
import static exodus.Constants.STATS_PLAYER4;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Random;
import objects.Creature;
import objects.SaveGame;

import objects.SaveGame.CharacterRecord;

import util.PartyDeathException;
import util.Utils;
import util.XORShiftRandom;

public class Party extends Observable implements Constants {

    private SaveGame saveGame;
    private List<PartyMember> members = new ArrayList<>();
    private int activePlayer = 0;
    private int torchduration;
    private final Random rand = new XORShiftRandom();
    private Context context;

    public Party(SaveGame sg) {
        this.saveGame = sg;

        for (CharacterRecord r : saveGame.players) {
            if (r == null || r.name == null || r.name.length() < 1) {
                //none
            } else {
                members.add(new PartyMember(this, r));
            }
        }

    }

    public void addMember(SaveGame.CharacterRecord rec) throws Exception {
        if (rec == null) {
            throw new Exception("Cannot add null record to party members!");
        }
        members.add(new PartyMember(this, rec));
    }

    public List<PartyMember> getMembers() {
        return members;
    }

    public PartyMember getMember(int index) {
        if (index >= members.size()) {
            return null;
        }
        return members.get(index);
    }

    public SaveGame getSaveGame() {
        return saveGame;
    }

    public void setSaveGame(SaveGame saveGame) {
        this.saveGame = saveGame;
    }

    public int getActivePlayer() {
        return activePlayer;
    }

    public int adjustShipHull(int val) {
        saveGame.shiphull = Utils.adjustValue(saveGame.shiphull, val, 50, 0);
        return saveGame.shiphull;
    }

    public void damageParty(int minDamage, int maxDamage) throws PartyDeathException {
        for (int i = 0; i < members.size(); i++) {
            if (rand.nextInt(2) == 0) {
                int damage = minDamage >= 0 && minDamage < maxDamage ? rand.nextInt(maxDamage + 1 - minDamage) + minDamage : maxDamage;
                members.get(i).applyDamage(damage, true);
            }
        }
    }
    
    public void poolGold(int index) {
        if (index >= members.size()) {
            return;
        }
        int total = 0;
        for (PartyMember pm : members) {
            total+=pm.getPlayer().gold;
            pm.getPlayer().gold = 0;
        }
        members.get(index).getPlayer().gold = total;
    }

    public int getTorchduration() {
        return torchduration;
    }

    public void setMembers(List<PartyMember> members) {
        this.members = members;
    }

    public void swapPlayers(int p1, int p2) {

        int size = members.size();
        if (p1 >= size || p2 >= size) {
            return;
        }

        CharacterRecord tmp = saveGame.players[p1];
        saveGame.players[p1] = saveGame.players[p2];
        saveGame.players[p2] = tmp;

        PartyMember tmp1 = members.get(p1);
        members.set(p1, members.get(p2));
        members.set(p2, tmp1);

        if (p1 == activePlayer) {
            activePlayer = p2;
        } else if (p2 == activePlayer) {
            activePlayer = p1;
        }

    }

    public PartyMember getActivePartyMember() {
        return members.get(activePlayer);
    }

    /**
     * Gets the next active index without changing the active index
     */
    public int getNextActiveIndex() {
        int tmp = activePlayer;
        boolean flag = true;
        while (flag) {
            tmp++;
            if (tmp >= members.size()) {
                tmp = 0;
            }
            if (!members.get(tmp).isDisabled()) {
                flag = false;
            }
        }
        return tmp;
    }

    public boolean isRoundDone() {
        int tmp = activePlayer;
        tmp++;
        if (tmp >= members.size()) {
            return true;
        }
        boolean noMoreAble = true;;
        for (int i = tmp; i < members.size(); i++) {
            if (!members.get(i).isDisabled()) {
                noMoreAble = false;
            }
        }
        return noMoreAble;
    }

    public PartyMember getAndSetNextActivePlayer() {
        boolean allbad = true;
        for (int i = 0; i < members.size(); i++) {
            if (!members.get(i).isDisabled()) {
                allbad = false;
            }
        }
        if (allbad) {
            activePlayer = members.size() - 1;
            return null;
        }

        PartyMember p = null;
        boolean flag = true;
        while (flag) {
            this.activePlayer++;
            if (activePlayer >= members.size()) {
                activePlayer = 0;
            }
            if (!members.get(activePlayer).isDisabled()) {
                p = members.get(activePlayer);
                flag = false;
            }
        }
        return p;
    }

    public boolean isOKtoExitDirection(Direction dir) {
        for (PartyMember pm : members) {
            if (pm.combatMapExitDirection != null && pm.combatMapExitDirection != dir) {
                return false;
            }
        }
        return true;
    }

    public boolean didAnyoneFlee() {
        boolean anyonefled = false;
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).fled) {
                anyonefled = true;
            }
        }
        return anyonefled;
    }

    public int getAbleCombatPlayers() {
        int n = 0;
        for (int i = 0; i < members.size(); i++) {
            if (!members.get(i).isDisabled()) {
                n++;
            }
        }
        return n;
    }

    public boolean isAnyoneAlive() {
        boolean anyonealive = false;
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getPlayer().status != StatusType.DEAD) {
                anyonealive = true;
            }
        }
        return anyonealive;
    }

    public void reviveAll() {
        activePlayer = 0;
        for (PartyMember pm : members) {
            pm.fled = false;
            pm.getPlayer().status = StatusType.GOOD;
            pm.getPlayer().health = pm.getPlayer().getMaxHealth();
        }
    }

    public void killAll() {
        activePlayer = 0;
        for (PartyMember pm : members) {
            pm.fled = false;
            pm.getPlayer().status = StatusType.DEAD;
            pm.getPlayer().health = 0;
        }
    }

    public void reset() {
        for (PartyMember pm : members) {
            pm.fled = false;
            pm.usedFreeSpellInCombat = false;
            pm.combatMapExitDirection = null;
        }
        activePlayer = 0;
    }

    public void setTorchduration(int torchduration) {
        this.torchduration = torchduration;
    }

    public int getChestGold(PartyMember member) {
        int gold = rand.nextInt(50) + rand.nextInt(8) + 10;
        member.player.adjustGold(gold);
        return gold;
    }

    public void applyEffect(TileEffect effect) throws PartyDeathException {
        for (int i = 0; i < members.size(); i++) {
            members.get(i).applyEffect(effect);
        }
    }

    public void applyEffect(PartyMember pm, TileEffect effect) throws PartyDeathException {
        switch (effect) {
            case NONE:
                break;
            case ELECTRICITY:
                pm.applyEffect(effect);
                break;
            case LAVA:
            case GREMLINS:
            case FIRE:
                if (rand.nextInt(2) == 0) {
                    pm.applyEffect(effect);
                }
                break;
            case POISONFIELD:
            case POISON:
                if (rand.nextInt(5) == 0) {
                    pm.applyEffect(effect);
                }
                break;
        }
    }

    public class PartyMember {

        private CharacterRecord player;
        private final Party party;

        public boolean usedFreeSpellInCombat;
        public boolean fled;
        public Direction combatMapExitDirection;
        public Creature combatCr;

        public PartyMember(Party py, CharacterRecord p) {
            this.party = py;
            this.player = p;
        }

        public CreatureStatus getDamagedState() {
            if (player.health <= 0) {
                return CreatureStatus.DEAD;
            } else if (player.health < 24) {
                return CreatureStatus.FLEEING;
            } else {
                return CreatureStatus.BARELYWOUNDED;
            }
        }

        public int getDamage() {
            int maxDamage = player.weapon.getWeapon().getDmax();
            maxDamage += player.str * 1.5;
            if (maxDamage > 255) {
                maxDamage = 255;
            }
            return Utils.getRandomBetween(player.weapon.getWeapon().getDmin(), maxDamage);
        }

        public void applyEffect(TileEffect effect) throws PartyDeathException {
            if (player.status == StatusType.DEAD) {
                return;
            }

            switch (effect) {
                case NONE:
                    break;
                case GREMLINS:
                    player.food = Utils.adjustValue(player.food, -2, 999900, 0);
                    break;
                case LAVA:
                case FIRE:
                    applyDamage(16 + (rand.nextInt(32)), false);
                    break;
                case POISONFIELD:
                case POISON:
                    if (player.status != StatusType.POISONED) {
                        player.status = StatusType.POISONED;
                    }
                    break;
                case ELECTRICITY:
                    break;
                default:
            }

        }

        public void awardXP(int value) {
            int exp = Utils.adjustValueMax(player.exp, value, 9999);
            player.exp = exp;
        }

        public void adjustMagic(int value) {
            player.mana = Utils.adjustValueMin(player.mana, -value, 0);
        }

        public boolean heal(HealType type) {
            switch (type) {

                case NONE:
                    return true;

                case CURE:
                    if (player.status != StatusType.POISONED) {
                        return false;
                    }
                    player.status = StatusType.GOOD;
                    break;

                case FULLHEAL:
                    if (player.status == StatusType.DEAD || player.health == player.getMaxHealth()) {
                        return false;
                    }
                    player.health = player.getMaxHealth();
                    break;

                case RESURRECT:
                    if (player.status != StatusType.DEAD) {
                        return false;
                    }
                    player.health = 1;
                    player.status = StatusType.GOOD;
                    break;

                case HEAL:
                    if (player.status == StatusType.DEAD || player.health == player.getMaxHealth()) {
                        return false;
                    }
                    player.health += 75 + (rand.nextInt(256) % 25);
                    break;

                case RECALL:
                    if (player.status != StatusType.ASH) {
                        return false;
                    }
                    player.health = 1;
                    player.status = StatusType.GOOD;
                    break;

                default:
                    return false;
            }

            if (player.health > player.getMaxHealth()) {
                player.health = player.getMaxHealth();
            }

            return true;
        }

        public Party getParty() {
            return party;
        }

        public CharacterRecord getPlayer() {
            return player;
        }

        public Creature nearestOpponent(int dist, boolean ranged) {
            return null;
        }

        public boolean isDead() {
            return player.status == StatusType.DEAD;
        }

        public boolean isDisabled() {
            return ((player.status == StatusType.GOOD || player.status == StatusType.POISONED) && !fled) ? false : true;
        }

        /**
         * Lose the equipped weapon for the player (flaming oil, ranged daggers,
         * etc.) Returns the number of weapons left of that type, including the
         * one in the players hand
         */
        public WeaponType loseWeapon() {
            int weapon = player.weapon.ordinal();
            if (player.weapons[weapon] > 0) {
                --player.weapons[weapon];
                int w = player.weapons[weapon] + 1;
                return WeaponType.get(w);
            } else {
                player.weapon = WeaponType.NONE;
                return WeaponType.NONE;
            }
        }

        public boolean readyWeapon(int i) {

            if (i >= 16) {
                return false;
            }

            //check if they are going bare hands
            if (i == 0) {
                //take off the old and put it in inventory
                if (player.weapon.ordinal() != 0) {
                    player.weapons[player.weapon.ordinal()]++;
                }
                player.weapon = WeaponType.NONE;
                return true;
            }

            //check if they are already wearing it
            if (player.weapon.ordinal() == i) {
                return true;
            }

            //check if it is in the inventory
            if (player.weapons[i] <= 0) {
                return false;
            }

            //check if they can wear it
            WeaponType wt = WeaponType.get(i);
            if (!wt.getWeapon().canUse(player.profession)) {
                return false;
            }

            //take off the old and put it in inventory
            if (player.weapon.ordinal() != 0) {
                player.weapons[player.weapon.ordinal()]++;
            }

            player.weapon = wt;
            player.weapons[i]--;
            return true;
        }

        public boolean wearArmor(int i) {

            if (i >= 8) {
                return false;
            }

            //check if they are going naked
            if (i == 0) {
                //take off the old and put it in inventory
                if (player.armor.ordinal() != 0) {
                    player.armors[player.armor.ordinal()]++;
                }
                player.armor = ArmorType.NONE;
                return true;
            }

            //check if they are already wearing it
            if (player.armor.ordinal() == i) {
                return true;
            }

            //check if it is in the inventory
            if (player.armors[i] <= 0) {
                return false;
            }

            //check if they can wear it
            ArmorType at = ArmorType.get(i);
            if (!at.getArmor().canUse(player.profession)) {
                return false;
            }

            //take off the old and put it in inventory
            if (player.armor.ordinal() != 0) {
                player.armors[player.armor.ordinal()]++;
            }

            player.armor = at;
            player.armors[i]--;
            return true;
        }

        public boolean applyDamage(int damage, boolean combatRelatedDamage) throws PartyDeathException {
            int newHp = player.health;

            if (isDead()) {
                return false;
            }

            newHp -= damage;

            if (newHp < 0) {
                player.status = StatusType.DEAD;
                newHp = 0;
            }

            player.health = newHp;

            if (!combatRelatedDamage && isDead() && !this.party.isAnyoneAlive()) {
                throw new PartyDeathException();
            }

            return true;
        }

        public int getAttackBonus() {
            if (player.dex >= 40) {
                return 255;
            }
            return player.dex;
        }

        public int getDefense() {
            return player.armor.getArmor().getDefense();
        }

    }

    public void endTurn(Maps map, MapType mapType) throws PartyDeathException {

        saveGame.moves++;

        for (int i = 0; i < members.size(); i++) {

            PartyMember member = members.get(i);

            if (mapType != MapType.combat) {
                
                int decr_interval = (map == Maps.SOSARIA || map == Maps.EXODUS ? 40 : 10);

                if (member.player.status != StatusType.DEAD) {
                    member.player.submorsels -= decr_interval;
                    if (member.player.submorsels < 0) {

                        member.player.submorsels = 400;
                        member.player.food = Utils.adjustValue(member.player.food, -1, 999900, 0);

                        if (member.player.status == StatusType.POISONED) {
                            member.applyDamage(1, false);
                        } else {
                            member.player.health = Utils.adjustValue(member.player.health, 1, member.player.getMaxHealth(), 0);
                        }

                        if (!member.isDisabled() && member.player.mana < member.player.getMaxMana()) {
                            member.player.mana++;
                            if (member.player.profession == Profession.DRUID) {
                                member.player.mana++;
                            }
                        }

                        if (member.player.food <= 0) {
                            member.applyDamage(rand.nextInt(2)+1, false);
                        }
                    }
                }
            }

        }

        if (context.getCurrentMap().getId() == Maps.SOSARIA.getId() && saveGame.shiphull < 50 && rand.nextInt(4) == 0) {
            saveGame.shiphull++;
            if (saveGame.shiphull > 50) {
                saveGame.shiphull = 50;
            }
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    //to proper case
    public static String pc(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public String[] getZstats() {

        StringBuilder sb1 = new StringBuilder();
        for (PartyMember pm : members) {
            CharacterRecord p = pm.getPlayer();
            sb1.append(pc(p.name)).append("  ").append(pc(p.race.toString())).
                    append("|").append(pc(p.profession.toString())).
                    append("|").append(pc(p.sex.getDesc())).append(" STATUS: ").append(p.status).
                    append("| |MANA: ").append(p.mana).append("  LV: ").append(p.getLevel()).
                    append("|" + "STR: ").append(p.str).append("  HEALTH: ").append(p.health).
                    append("|" + "DEX: ").append(p.dex).append("  MAXHEALTH: ").append(p.getMaxHealth()).
                    append("|" + "INT: ").append(p.intell).append("  EXP: ").append(p.exp).
                    append("|" + "WIS: ").append(p.wis).
                    append("| |READIED: ").append(p.weapon.getWeapon().getName()).
                    append("|WORN: ").append(p.armor.getArmor().getName()).
                    append("| |" + "TORCHES: ").append(p.torches).append(" GEMS: ").append(p.gems).
                    append("|" + "KEYS: ").append(p.keys).append(" POWDERS: ").append(p.powder).
                    append("|" + "GOLD: ").append(p.gold).append(" FOOD: ").append(p.food);

            sb1.append("| ");

            for (int i = 1; i < p.armors.length -1; i++) {
                sb1.append("|" + ArmorType.values()[i].getArmor().getName() + ": ").append(p.armors[i]);
                i++;
                if (i < p.armors.length-1) {
                    sb1.append(" " + ArmorType.values()[i].getArmor().getName() + ": ").append(p.armors[i]);
                }
            }
            
            if (p.armors[ArmorType.EXOTIC.ordinal()] > 0) {
                sb1.append("|" + ArmorType.values()[ArmorType.EXOTIC.ordinal()].getArmor().getName() + ": ").append(p.armors[ArmorType.EXOTIC.ordinal()]);
            }

            sb1.append("| ");

            for (int i = 1; i < p.weapons.length-1; i++) {
                sb1.append("|" + WeaponType.values()[i].getWeapon().getName() + ": ").append(p.weapons[i]);
                i++;
                if (i < p.weapons.length-1) {
                    sb1.append(" " + WeaponType.values()[i].getWeapon().getName() + ": ").append(p.weapons[i]);
                }
            }
            
            if (p.weapons[WeaponType.EXOTIC.ordinal()] > 0) {
                sb1.append("|" + WeaponType.values()[WeaponType.EXOTIC.ordinal()].getWeapon().getName() + ": ").append(p.weapons[WeaponType.EXOTIC.ordinal()]);
            }

            sb1.append("| ");
            
            String mk = (p.marks[0] > 0 ? "|MARK KINGS: " + p.marks[0] + " ": "|");
            String mf = (p.marks[1] > 0 ? "MARK FIRE: " + p.marks[1]: "");
            String ms = (p.marks[2] > 0 ? "|MARK SNAKE: " + p.marks[2] + " ": "|");
            String mfo = (p.marks[3] > 0 ? "MARK FORCE: " + p.marks[3]: "");
            String cd = (p.cards[0] > 0 ? "|CARD DEATH: " + p.cards[0] + " ": "|");
            String cs = (p.cards[1] > 0 ? "CARD SOL: " + p.cards[1]: "");
            String cm = (p.cards[2] > 0 ? "|CARD MOONS: " + p.cards[2] + " ": "|");
            String cl = (p.cards[3] > 0 ? "CARD LOVE: " + p.cards[3]: "");

            sb1.append(mk).append(mf);
            sb1.append(ms).append(mfo);
            sb1.append(cd).append(cs);
            sb1.append(cm).append(cl);

            sb1.append("~");
        }

        String[] ret = new String[1];
        ret[0] = sb1.toString();

        return ret;
    }

    public Texture zstatsBox;

    public void renderZstats(int showZstats, BitmapFont font, Batch batch, int SCREEN_HEIGHT) {

        if (zstatsBox == null) {
            Pixmap pixmap = new Pixmap(195, 675, Pixmap.Format.RGBA8888);
            pixmap.setColor(0f, 0f, 0f, 0.65f);
            pixmap.fillRectangle(0, 0, 195, 675);
            zstatsBox = new Texture(pixmap);
            pixmap.dispose();
        }

        batch.draw(zstatsBox, 10, SCREEN_HEIGHT - 15 - 675);

        int rx = 15;
        int ry = SCREEN_HEIGHT - 25;

        String[] pages = getZstats();
        if (showZstats >= STATS_PLAYER1 && showZstats <= STATS_PLAYER4) {
            String[] players = pages[0].split("\\~");
            for (int i = 0; i < players.length; i++) {
                String[] lines = players[i].split("\\|");
                if (i != showZstats - 1) {
                    continue;
                }
                ry = SCREEN_HEIGHT - 25;
                font.draw(batch, "Player " + (i + 1), rx, ry);
                ry = ry - 18;
                for (int j = 0; j < lines.length; j++) {
                    if (lines[j] == null || lines[j].length() < 1) {
                        continue;
                    }
                    font.draw(batch, lines[j], rx, ry);
                    ry = ry - 18;
                }
            }
        }

    }

}
