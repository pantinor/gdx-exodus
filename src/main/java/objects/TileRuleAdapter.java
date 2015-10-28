package objects;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import exodus.Constants.TileRule;

public class TileRuleAdapter extends XmlAdapter<String, TileRule> {

    public String marshal(TileRule t) {
        return t.toString();
    }

    public TileRule unmarshal(String val) {
        return TileRule.valueOf(val);
    }
}
