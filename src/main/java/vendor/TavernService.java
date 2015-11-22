package vendor;

import exodus.Constants.InventoryType;
import exodus.Context;
import exodus.Party;
import vendor.BaseVendor.ConvState;

public class TavernService extends BaseVendor {

    int youPay;
    int tip;
    Item specFood;
    Item ale;

    public TavernService(Vendor vendor, Context context, Party.PartyMember member) {
        super(vendor, context, member);

        for (Item i : vendor.getInventoryItems()) {
            if (i.getType() == InventoryType.FOOD) {
                if (i.getName().equals(vendor.getGenericField1())) {
                    specFood = i;
                } else {
                    ale = i;
                }
            }
        }
    }

    @Override
    public void init() {
        welcomeMessage = "%s says: Welcome to %s";
        state = ConvState.ASK_FOOD_ALE;
    }

    @Override
    public boolean nextDialog() {

        int haveCount = 0;

        switch (state) {
            case ASK_FOOD_ALE:
                displayToScreen(String.format(welcomeMessage, vendor.getOwner(), vendor.getName()));
                displayToScreen("What'll it be, Food er Ale? ");
                break;

            case WAIT_BUY_HOW_MANY:
                displayToScreen("Our specialty is " + vendor.getGenericField1() + " which costs " + specFood.getPrice() + "gp.");
                displayToScreen("How many plates would you like?");
                break;

            case BUY_FOOD:
                haveCount = member.getPlayer().food;
                member.getPlayer().food = haveCount + currentCount;
                member.getPlayer().adjustGold(-specFood.getPrice() * currentCount);
                displayToScreen("Here ye arr.");
                displayToScreen("Somethin' else?");
                state = ConvState.ANYTHING_ELSE;
                break;

            case WAIT_YOU_PAY:
                displayToScreen("Here's a mug of our best. That'll be " + ale.getPrice() + "gp.");
                displayToScreen("You pay?");
                break;

            case BUY_ALE:
                member.getPlayer().adjustGold(-youPay);
                displayToScreen("Here ye arr.");
                displayToScreen("Somethin' else?");
                state = ConvState.ANYTHING_ELSE;
                break;

            case BUY_ALE_WITH_TIP:
                member.getPlayer().adjustGold(-youPay);
                displayToScreen("Here ye arr.");
                Item item = vendor.getTavernInfo(tip);
                if (item != null) {
                    displayToScreen(String.format(item.getDescription(),vendor.getOwner()));
                } else {
                    displayToScreen("Thank you kindly.");
                }    
                displayToScreen("Somethin' else?");
                state = ConvState.ANYTHING_ELSE;
                break;

            case ANYTHING_ELSE:
                displayToScreen("Somethin' else?");
                break;
            case DECLINE_BUY:
                displayToScreen("Won't pay, eh. Ya scum, be gone fore ey call the guards!");
                state = ConvState.FAREWELL;
                break;

            case FAREWELL:
                displayToScreen("See ya mate!");
                return false;
            default:
                displayToScreen("I cannot help ya with that.");
                state = ConvState.ANYTHING_ELSE;
                break;

        }
        return true;
    }

    @Override
    public void setResponse(String input) {
        if (state == ConvState.ASK_FOOD_ALE || state == ConvState.ANYTHING_ELSE) {
            if (input.startsWith("f")) {
                state = ConvState.WAIT_BUY_HOW_MANY;
            } else if (input.startsWith("a")) {
                state = ConvState.WAIT_YOU_PAY;
            } else if (input.startsWith("y")) {
                state = ConvState.ASK_FOOD_ALE;
            } else {
                state = ConvState.FAREWELL;
            }
        } else if (state == ConvState.WAIT_BUY_HOW_MANY) {

            int count = 1;
            try {
                count = Integer.parseInt(input);
            } catch (Exception e) {
                count = 1;
            }
            currentCount = count;
            if (currentCount == 0) {
                state = ConvState.FAREWELL;
            } else {
                state = ConvState.BUY_FOOD;
            }

        } else if (state == ConvState.BUY_FOOD) {

            if (checkCanPayFood()) {
                state = ConvState.ANYTHING_ELSE;
            } else {
                state = ConvState.FAREWELL;
            }

        } else if (state == ConvState.WAIT_YOU_PAY) {
            int amnt = ale.getPrice();
            try {
                amnt = Integer.parseInt(input);
            } catch (Exception e) {
            }

            youPay = amnt;
            tip = amnt - ale.getPrice();

            if (!checkCanPayAle()) {
                state = ConvState.FAREWELL;
                return;
            }

            if (youPay < ale.getPrice()) {
                state = ConvState.DECLINE_BUY;
            } else if (youPay == 1) {
                state = ConvState.FAREWELL;
            } else if (youPay == ale.getPrice()) {
                state = ConvState.BUY_ALE;
            } else if (youPay > ale.getPrice()) {
                state = ConvState.BUY_ALE_WITH_TIP;
            }

        } else if (state == ConvState.BUY_ALE) {

            state = ConvState.ANYTHING_ELSE;

        } else if (state == ConvState.FAREWELL || input == null || input.length() < 1 || input.equalsIgnoreCase("bye")) {
            state = ConvState.FAREWELL;
        }

    }

    public boolean checkCanPayFood() {

        boolean ret = false;

        if (specFood.getPrice() * currentCount > member.getPlayer().gold) {
            displayToScreen("Ya can only afford " + Math.round(member.getPlayer().gold / specFood.getPrice()) + " plates.");
        } else {
            ret = true;
        }

        return ret;
    }

    public boolean checkCanPayAle() {

        boolean ret = false;

        if (ale.getPrice() + tip > member.getPlayer().gold) {
            displayToScreen("It seems that you have not the gold. Good Day!");
        } else {
            ret = true;
        }

        return ret;
    }

}
