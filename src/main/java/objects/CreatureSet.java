package objects;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import exodus.Constants.CreatureType;
import util.Utils;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.utils.Array;
import java.util.Collections;

@XmlRootElement(name = "creatures")
public class CreatureSet {

    private List<Creature> creatures;

    @XmlElement(name = "creature")
    public List<Creature> getCreatures() {
        return creatures;
    }

    public void setCreatures(List<Creature> creatures) {
        this.creatures = creatures;
    }

    public void init() {

        for (Creature cr : creatures) {
            CreatureType ct = CreatureType.get(cr.getId());
            if (ct != null) {
                ct.setCreature(cr);
            } else {
                System.err.printf("CreatureSet.init: Could not find creature type with id %d\n", cr.getId());
            }
        }
        
        Collections.sort(creatures);
    }

    public Creature getInstance(CreatureType type, TextureAtlas atlas1) {
        for (Creature cr : creatures) {
            //System.err.printf("%s %s %s\n", type, cr.getTile(), cr.getName());
            if (cr.getTile() == type || cr.getName().toLowerCase().equals(type.toString())) {

                Creature newCr = new Creature(cr);

                Array<AtlasRegion> tr = atlas1.findRegions(cr.getTile().toString());
                int frameRate = Utils.getRandomBetween(1, 3);
                newCr.setAnim(new Animation(frameRate, tr));
                
                int fr = Utils.getRandomBetween(0, tr.size);
                TextureRegion reg = tr.get(fr);
                Decal d = Decal.newDecal(reg, true);
                d.setScale(.018f);
                newCr.setDecal(d);
                
                if (type == CreatureType.twister) {
                    newCr.setAnim(new Animation(.2f, tr));
                }
                
                if (type == CreatureType.whirlpool) {
                    newCr.setAnim(new Animation(.3f, tr));
                }

                return newCr;
            }
        }

        System.err.println(type + " not found.");

        return null;
    }
}
