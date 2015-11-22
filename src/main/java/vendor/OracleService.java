/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vendor;

import exodus.Context;
import exodus.Party;

/**
 *
 * @author Paul
 */
public class OracleService extends BaseVendor {

    public OracleService(Vendor vendor, Context context, Party.PartyMember member) {
        super(vendor, context, member);
    }

    @Override
    public void init() {
        welcomeMessage = "Welcome unto %s\n\n%s says: Peace and Joy be with you friend.";
        state = BaseVendor.ConvState.WAIT_NEED_HELP;
    }

    @Override
    public boolean nextDialog() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setResponse(String input) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
