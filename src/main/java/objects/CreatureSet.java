package objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.utils.Array;

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
    }

    public Creature getInstance(int id, TextureAtlas atlas) {
        for (Creature cr : creatures) {
            if (id == cr.getId()) {
                return getInstance(cr.getTile(), atlas);
            }
        }
        throw new IllegalArgumentException("Creature ID not found " + id);
    }

    public Creature getRandomDungeonInstance(int dungeonLevel, TextureAtlas atlas, Random rand) {
        List<Creature> eligible = new ArrayList<>();
        for (Creature cr : creatures) {
            if (cr.getLevel() > 0 && cr.getLevel() <= dungeonLevel && !cr.getSails() && !cr.getSwims() && !cr.getGood() && !cr.getWontattack()) {
                eligible.add(cr);
            }
        }

        if (eligible.isEmpty()) {
            return null;
        }

        Creature cr = eligible.get(rand.nextInt(eligible.size()));
        return getInstance(cr.getTile(), atlas);
    }

    public Creature getInstance(String type, TextureAtlas atlas) {

        for (Creature cr : creatures) {
            if (type.equals(cr.getTile())) {
                Creature newCr = new Creature(cr);

                Array<AtlasRegion> regions = atlas.findRegions(type);

                if (regions == null || regions.size == 0) {
                    System.err.println("No atlas regions found for " + cr.getTile());
                    return null;
                }

                float frameDuration = "whirlpool".equals(type) ? 0.3f : 0.2f;
                newCr.setAnim(new Animation<TextureRegion>(frameDuration, regions));

                TextureRegion region = regions.get(0);
                Decal decal = Decal.newDecal(region, true);
                decal.setScale(0.018f);

                newCr.setDecal(decal);

                return newCr;
            }
        }
        throw new IllegalArgumentException("Creature Type not found " + type);
    }
}
