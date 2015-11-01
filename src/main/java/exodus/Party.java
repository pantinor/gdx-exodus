package exodus;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Random;
import objects.Creature;
import objects.SaveGame;

import objects.SaveGame.CharacterRecord;
import objects.Tile;

import util.PartyDeathException;
import util.Utils;
import util.XORShiftRandom;

public class Party extends Observable implements Constants {

    private SaveGame saveGame;
    private List<PartyMember> members = new ArrayList<>();
    private int activePlayer = 0;
    private Tile transport;
    private int torchduration;
    private final Random rand = new XORShiftRandom();
    private Context context;

    public Party(SaveGame sg) {
        this.saveGame = sg;

        for (CharacterRecord r : saveGame.players) {
            if (r.name == null || r.name.length() < 1) {
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

    public Tile getTransport() {
        return transport;
    }

    /**
     * 0x10-ship facing west 0x11-ship facing north 0x12-ship facing east
     * 0x13-ship facing south 0x14-horse facing west 0x15-horse facing east
     * 0x18-balloon 0x1f-on foot
     *
     * @param transport
     */
    public void setTransport(Tile transport) {
        this.transport = transport;
        saveGame.transport = transport.getIndex();

        if (transport.getRule().has(TileAttrib.horse)) {
            context.setTransportContext(TransportContext.HORSE);
        } else if (transport.getRule().has(TileAttrib.ship)) {
            context.setTransportContext(TransportContext.SHIP);
        } else if (transport.getRule().has(TileAttrib.balloon)) {
            context.setTransportContext(TransportContext.BALLOON);
        } else {
            context.setTransportContext(TransportContext.FOOT);
        }
    }

    public int adjustShipHull(int val) {
        //saveGame.shiphull = Utils.adjustValue(saveGame.shiphull, val, 50, 0);
        return 0;//saveGame.shiphull;
    }

    public void damageParty(int minDamage, int maxDamage) throws PartyDeathException {
        for (int i = 0; i < members.size(); i++) {
            if (rand.nextInt(2) == 0) {
                int damage = minDamage >= 0 && minDamage < maxDamage ? rand.nextInt(maxDamage + 1 - minDamage) + minDamage : maxDamage;
                members.get(i).applyDamage(damage, true);
            }
        }
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
            pm.getPlayer().health = pm.getPlayer().maxHealth;
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
            pm.combatMapExitDirection = null;
        }
        activePlayer = 0;
    }

    public void setTorchduration(int torchduration) {
        this.torchduration = torchduration;
    }

    public void adjustFood(PartyMember member, int v) {
        member.player.food = Utils.adjustValue(member.player.food, v, 999900, 0);
    }

    public void adjustGold(PartyMember member, int v) {
        member.player.gold = Utils.adjustValue(member.player.gold, v, 9999, 0);
    }

    public int getChestGold(PartyMember member) {
        int gold = rand.nextInt(50) + rand.nextInt(8) + 10;
        adjustGold(member, gold);
        return gold;
    }

    public void applyEffect(TileEffect effect) throws PartyDeathException {
        for (int i = 0; i < members.size(); i++) {
            switch (effect) {
                case NONE:
                    break;
                case ELECTRICITY:
                    members.get(i).applyEffect(effect);
                    break;
                case LAVA:
                case FIRE:
                case SLEEP:
                    if (rand.nextInt(2) == 0) {
                        members.get(i).applyEffect(effect);
                    }
                    break;
                case POISONFIELD:
                case POISON:
                    if (rand.nextInt(5) == 0) {
                        members.get(i).applyEffect(effect);
                    }
                    break;
            }
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
            case FIRE:
            case SLEEP:
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
            maxDamage += player.str;
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
                    if (player.status == StatusType.DEAD || player.health == player.maxHealth) {
                        return false;
                    }
                    player.health = player.maxHealth;
                    break;

                case RESURRECT:
                    if (player.status != StatusType.DEAD) {
                        return false;
                    }
                    player.status = StatusType.GOOD;
                    break;

                case HEAL:
                    if (player.status == StatusType.DEAD || player.health == player.maxHealth) {
                        return false;
                    }
                    player.health += 75 + (rand.nextInt(256) % 25);
                    break;

                case CAMPHEAL:
                    if (player.status == StatusType.DEAD || player.health == player.maxHealth) {
                        return false;
                    }
                    player.health += 99 + (rand.nextInt(256) & 119);
                    break;

                case INNHEAL:
                    if (player.status == StatusType.DEAD || player.health == player.maxHealth) {
                        return false;
                    }
                    player.health += 100 + (rand.nextInt(50) * 2);
                    break;

                default:
                    return false;
            }

            if (player.health > player.maxHealth) {
                player.health = player.maxHealth;
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
            if (player.qtyWeapons[weapon] > 0) {
                --player.qtyWeapons[weapon];
                int w = player.qtyWeapons[weapon] + 1;
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
                    player.qtyWeapons[player.weapon.ordinal()]++;
                }
                player.weapon = WeaponType.NONE;
                return true;
            }

            //check if they are already wearing it
            if (player.weapon.ordinal() == i) {
                return true;
            }

            //check if it is in the inventory
            if (player.qtyWeapons[i] <= 0) {
                return false;
            }

            //check if they can wear it
            WeaponType wt = WeaponType.get(i);
//            if (!wt.getWeapon().canUse(player.klass)) {
//                return false;
//            }

            //take off the old and put it in inventory
            if (player.weapon.ordinal() != 0) {
                player.qtyWeapons[player.weapon.ordinal()]++;
            }

            player.weapon = wt;
            player.qtyWeapons[i]--;
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
                    player.qtyArmors[player.armor.ordinal()]++;
                }
                player.armor = ArmorType.NONE;
                return true;
            }

            //check if they are already wearing it
            if (player.armor.ordinal() == i) {
                return true;
            }

            //check if it is in the inventory
            if (player.qtyArmors[i] <= 0) {
                return false;
            }

            //check if they can wear it
            ArmorType at = ArmorType.get(i);
//            if (!at.getArmor().canUse(player.profession)) {
//                return false;
//            }

            //take off the old and put it in inventory
            if (player.armor.ordinal() != 0) {
                player.qtyArmors[player.armor.ordinal()]++;
            }

            player.armor = at;
            player.qtyArmors[i]--;
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

    public boolean isJoinedInParty(String name) {
        for (PartyMember pm : members) {
            if (pm.getPlayer().name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void endTurn(MapType mapType) throws PartyDeathException {

        saveGame.moves++;

        for (int i = 0; i < members.size(); i++) {

            PartyMember member = members.get(i);

            if (mapType != MapType.combat) {

                if (member.player.status != StatusType.DEAD) {
                    adjustFood(member, -1);
                }

                if (member.player.status == StatusType.POISONED) {
                    member.applyDamage(2, false);
                    setChanged();
                    notifyObservers(PartyEvent.POISON_DAMAGE);
                }

            }

            if (!member.isDisabled() && member.player.mana < member.player.getMaxMana()) {
                member.player.mana++;
            }

            if (member.player.food == 0) {
                member.applyDamage(1, false);
                setChanged();
                notifyObservers(PartyEvent.STARVING);
            }
        }

//        if (context.getCurrentMap().getId() == Maps.WORLD.getId() && saveGame.shiphull < 50 && rand.nextInt(4) == 0) {
//            saveGame.shiphull++;
//            if (saveGame.shiphull > 50) {
//                saveGame.shiphull = 50;
//            }
//        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
