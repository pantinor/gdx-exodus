/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vendor;

import exodus.Constants;
import exodus.Context;
import exodus.Party;

/**
 *
 * @author Paul
 */
public class OracleService extends BaseVendor {

    int youPay;

    public OracleService(Vendor vendor, Context context, Party.PartyMember member) {
        super(vendor, context, member);
    }

    @Override
    public void init() {
        welcomeMessage = "%s:\nProphet of Life!";
        state = BaseVendor.ConvState.WAIT_BUY_INPUT;
    }

    @Override
    public boolean nextDialog() {
        switch (state) {
            case WAIT_BUY_INPUT:
                displayToScreen(String.format(welcomeMessage, vendor.getOwner()));
                displayToScreen("How may 100 gp is your offering?");
                break;
            case ANYTHING_ELSE:
                displayToScreen("More offering?");
                break;
            case BUY_ITEM:
                member.getPlayer().adjustGold(-currentCount);
                Item item = getOracleInfo(currentCount);
                if (item != null) {
                    displayToScreen(String.format(item.getDescription(), vendor.getOwner()));
                }
                displayToScreen("More offering?");
                state = ConvState.ANYTHING_ELSE;
                return false;
            default:
                displayToScreen("Fare thee well and good luck!");
                break;

        }
        return true;
    }

    @Override
    public void setResponse(String input) {
        if (state == ConvState.WAIT_BUY_INPUT) {
            int offering = 1;
            try {
                offering = Integer.parseInt(input);
            } catch (Exception e) {
                offering = 1;
            }

            currentCount = offering * 100;

            if (!checkCanPay(currentCount)) {
                state = ConvState.ANYTHING_ELSE;
                return;
            }

            state = ConvState.BUY_ITEM;

        } else if (state == ConvState.ANYTHING_ELSE) {
            if (input.startsWith("y")) {
                state = ConvState.WAIT_BUY_INPUT;
            } else {
                state = ConvState.FAREWELL;
            }

        } else if (state == ConvState.FAREWELL || input == null || input.length() < 1 || input.equalsIgnoreCase("bye")) {
            state = ConvState.FAREWELL;
        }
    }

    private Item getOracleInfo(int amt) {
        for (Item i : vendor.getInventoryItems()) {
            if (i.getType() == Constants.InventoryType.ORACLEINFO) {
                if (amt == i.getPrice()) {
                    return i;
                }
            }
        }
        return null;
    }

    private boolean checkCanPay(int amt) {

        if (amt == 0) {
            displayToScreen("And so the sage said unto thee, if thou can solve my rhyme..");
            return false;
        }

        boolean ret = false;

        if (amt > member.getPlayer().gold) {
            displayToScreen("It seems thou hast not gold enough to pay!");
        } else {
            ret = true;
        }

        return ret;
    }

}
