package objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import exodus.Constants.InventoryType;
import vendor.InventoryTypeAdapter;

@XmlRootElement(name = "personrole")
public class PersonRole {

    private String role;
    private InventoryType inventoryType;

    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(InventoryTypeAdapter.class)
    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public void setInventoryType(InventoryType inventoryType) {
        this.inventoryType = inventoryType;
    }

    @XmlAttribute(name = "role")
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
