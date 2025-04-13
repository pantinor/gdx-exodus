package objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

@XmlRootElement(name = "moongate")
public class Moongate {

    private String name;
    private int phase;
    private int x;
    private int y;

    private int d1;
    private int d2;
    private int d3;

    private int mapTileId;

    private AtlasRegion currentTexture;

    @XmlAttribute
    public int getPhase() {
        return phase;
    }

    @XmlAttribute
    public int getX() {
        return x;
    }

    @XmlAttribute
    public int getY() {
        return y;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {

        String template = "<object name=\"%s\" type=\"portal\" x=\"%s\" y=\"%s\" width=\"24\" height=\"24\">\n"

                + "</object>\n";

        return String.format(template, name, x * 24, y * 24, x, y, phase);
    }

    public AtlasRegion getCurrentTexture() {
        return currentTexture;
    }

    public void setCurrentTexture(AtlasRegion currentTexture) {
        this.currentTexture = currentTexture;
    }

    public int getMapTileId() {
        return mapTileId;
    }

    public void setMapTileId(int mapTileId) {
        this.mapTileId = mapTileId;
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute
    public int getD1() {
        return d1;
    }

    @XmlAttribute
    public int getD2() {
        return d2;
    }

    @XmlAttribute
    public int getD3() {
        return d3;
    }

    public void setD1(int d1) {
        this.d1 = d1;
    }

    public void setD2(int d2) {
        this.d2 = d2;
    }

    public void setD3(int d3) {
        this.d3 = d3;
    }

}
