package vendor;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import exodus.Constants.ArmorType;

public class ArmorAdapter extends XmlAdapter<String, ArmorType> {

    public String marshal(ArmorType t) {
        return t.toString();
    }

    public ArmorType unmarshal(String val) {
        return ArmorType.valueOf(val);
    }
}
