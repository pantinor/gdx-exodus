package vendor;

import com.badlogic.gdx.math.Vector3;

import objects.Creature;
import exodus.Constants.CreatureType;
import exodus.Context;
import exodus.Exodus;
import exodus.GameScreen;
import exodus.Party;

public class HorseService extends BaseVendor {

    public HorseService(Vendor vendor, Context context, Party.PartyMember member) {
        super(vendor, context, member);
    }

    @Override
    public void init() {
        welcomeMessage = "Welcome friend!";
        state = ConvState.WAIT_BUY_INPUT;
    }

    @Override
    public boolean nextDialog() {
        switch (state) {
            case WAIT_BUY_INPUT:
                displayToScreen(String.format(welcomeMessage, vendor.getName(), vendor.getOwner()));
                displayToScreen("Can I interest thee in horses?");
                break;

            case WAIT_BUY_ONE:
                displayToScreen("For only 200g.p thou can have the best. Wilt thou buy?");
                break;
            case BUY_ITEM:
                member.getPlayer().adjustGold(-currentSelectedItem.getPrice());
                displayToScreen("Here, a better breed thou shalt not find ever!");
                if (screen != null) {
                    Creature cr = Exodus.creatures.getInstance(CreatureType.horse, Exodus.standardAtlas);
                    Vector3 v = screen.getCurrentMapCoords();
                    cr.currentX = (int) v.x;
                    cr.currentY = (int) v.y;
                    context.getCurrentMap().addCreature(cr);
                    ((GameScreen) screen).board((int) v.x, (int) v.y);
                }
                return false;
            case DECLINE_BUY:
                displayToScreen("A shame, thou looks like thou could use a good horse!");
                return false;
            case FAREWELL:
                displayToScreen("Fare thee well!");
                return false;
            default:
                displayToScreen("I cannot help thee with that.");
                state = ConvState.WAIT_BUY_INPUT;
                break;

        }
        return true;
    }

    @Override
    public void setResponse(String input) {
        if (state == ConvState.WAIT_BUY_INPUT) {
            if (input.startsWith("y")) {
                state = ConvState.WAIT_BUY_ONE;
            } else {
                state = ConvState.DECLINE_BUY;
            }
        } else if (state == ConvState.WAIT_BUY_ONE) {
            Item item = vendor.getItem("y");
            currentSelectedItem = item;
            if (input.startsWith("y") && checkCanPay(item)) {
                currentSelectedItem = item;
                state = ConvState.BUY_ITEM;
            } else {
                state = ConvState.FAREWELL;
            }

        } else if (state == ConvState.BUY_ITEM) {
            state = ConvState.FAREWELL;

        } else if (state == ConvState.FAREWELL || input == null || input.length() < 1 || input.equalsIgnoreCase("bye")) {
            state = ConvState.FAREWELL;
        }

    }

    @Override
    public boolean checkCanPay(Item item) {
        if (item == null) {
            displayToScreen("I cannot help thee with that.");
            return false;
        }

        boolean ret = false;

        if (item.getPrice() > member.getPlayer().gold) {
            displayToScreen("It seems thou hast not gold enough to pay!");
        } else {
            ret = true;
        }

        return ret;
    }

}
