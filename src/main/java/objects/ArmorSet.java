package objects;

import exodus.Constants.ArmorType;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "armors")
public class ArmorSet {

    private List<Armor> armors;

    @XmlElement(name = "armor")
    public List<Armor> getArmors() {
        return armors;
    }

    public void setArmors(List<Armor> armors) {
        this.armors = armors;
    }

    public void init() {
        for (Armor a : this.armors) {
            ArmorType t = a.getType();
            t.setArmor(a);
        }
    }

}
