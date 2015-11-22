package vendor;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import exodus.Sound;

public class SoundAdapter extends XmlAdapter<String, Sound> {

    public String marshal(Sound t) {
        return t.toString();
    }

    public Sound unmarshal(String val) {
        return Sound.valueOf(val);
    }
}
