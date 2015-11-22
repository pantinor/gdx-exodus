package vendor;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import exodus.Constants.InventoryType;
import exodus.Constants.Maps;
import exodus.Context;
import exodus.Party.PartyMember;
import objects.Person;

@XmlRootElement(name = "vendorSet")
public class VendorClassSet {

    private List<VendorClass> vendorClasses;

    @XmlElement(name = "vendorClass")
    public List<VendorClass> getVendorClasses() {
        return vendorClasses;
    }

    public void setVendorClasses(List<VendorClass> vendorClasses) {
        this.vendorClasses = vendorClasses;
    }

    public Vendor getVendor(InventoryType type, Maps id) {
        for (VendorClass vc : vendorClasses) {
            if (vc.getType() == type) {
                for (Vendor v : vc.getVendors()) {
                    if (v.getMapId() == id) {
                        return v;
                    }
                }
            }
        }
        return null;
    }

    public void init() {
        for (VendorClass vc : vendorClasses) {
            for (Vendor v : vc.getVendors()) {
                v.setVendorClass(vc);
                Maps map = v.getMapId();
                for (Person p : map.getMap().getPeople()) {
                    if (p.getStart_x() == v.getStartX() && p.getStart_y() == v.getStartY()) {
                        p.setVendor(v);
                    }
                }
            }
        }
    }

    public BaseVendor getVendorImpl(InventoryType type, Maps map, Context context, PartyMember member) {

        BaseVendor v = null;

        switch (type) {
            case ARMOR:
                v = new ArmorVendor(getVendor(type, map), context, member);
                break;
            case FOOD:
                v = new FoodVendor(getVendor(type, map), context, member);
                break;
            case GUILDITEM:
                v = new GuildVendor(getVendor(type, map), context, member);
                break;
            case HEALER:
                v = new HealerService(getVendor(type, map), context, member);
                break;
            case HORSE:
                v = new HorseService(getVendor(type, map), context, member);
                break;
            case TAVERNINFO:
            case TAVERN:
                v = new TavernService(getVendor(type, map), context, member);
                break;
            case WEAPON:
                v = new WeaponVendor(getVendor(type, map), context, member);
                break;
            case ORACLEINFO:
                v = new OracleService(getVendor(type, map), context, member);
                break;
            default:
                break;

        }

        return v;

    }

}
