package objects;

import exodus.Constants;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import vendor.Vendor;

public class Person implements Constants {

    private int id;
    private int start_x;
    private int start_y;
    private int x;
    private int y;
    private ObjectMovementBehavior movement;
    private Tile tile;
    private int tileMapId;
    private int dialogId;
    private int tileIndex;

    private TextureRegion textureRegion;
    private Animation anim;
    private Vector3 currentPos;
    private String conversation;
    private boolean isTalking = false;
    private Vendor vendor;
    private Creature emulatingCreature;
    private boolean removedFromMap;

    public int getId() {
        return id;
    }

    public int getStart_x() {
        return start_x;
    }

    public int getStart_y() {
        return start_y;
    }

    public ObjectMovementBehavior getMovement() {
        return movement;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStart_x(int start_x) {
        this.start_x = start_x;
    }

    public void setStart_y(int start_y) {
        this.start_y = start_y;
    }

    public void setMovement(ObjectMovementBehavior movement) {
        this.movement = movement;
    }

    public Tile getTile() {
        return tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public int getTileMapId() {
        return tileMapId;
    }

    public void setTileMapId(int tileMapId) {
        this.tileMapId = tileMapId;
    }

    public int getDialogId() {
        return dialogId;
    }

    public void setDialogId(int dialogId) {
        this.dialogId = dialogId;
    }

    public String toTMXString() {

        String template = "<object name=\"%s\" type=\"%s\" x=\"%s\" y=\"%s\" width=\"32\" height=\"32\">\n"
                + "<properties>\n"
                + "<property name=\"id\" value=\"%s\"/>\n"
                + "<property name=\"tileType\" value=\"%s\"/>\n"
                + "<property name=\"movement\" value=\"%s\"/>\n"
                + "<property name=\"startX\" value=\"%s\"/>\n"
                + "<property name=\"startY\" value=\"%s\"/>\n"
                + "<property name=\"dialogId\" value=\"%s\"/>\n"
                + "<property name=\"text\" value=\"%s\"/>\n"
                + "</properties>\n"
                + "</object>\n";

        return String.format(template,
                tile.getName(), tile.getName(), start_x * 32, start_y * 32, id, tile.getName(), movement, start_x, start_y, dialogId, 
                conversation == null ? "Good Day!" : conversation);
    }

    @Override
    public String toString() {
        return String.format("Person [id=%s, start_x=%s, start_y=%s, dialogId=%s, tileIndex=%s conv: %s]", id, start_x, start_y, dialogId, tileIndex, 
                conversation == null ? "Good Day!" : conversation);
    }

    public int getTileIndex() {
        return tileIndex;
    }

    public void setTileIndex(int tileIndex) {
        this.tileIndex = tileIndex;
    }

    public Vector3 getCurrentPos() {
        return currentPos;
    }

    public String getConversation() {
        return conversation;
    }

    public void setCurrentPos(Vector3 currentPos) {
        this.currentPos = currentPos;
    }

    public void setConversation(String conversation) {
        this.conversation = conversation;
    }

    public Animation getAnim() {
        return anim;
    }

    public void setAnim(Animation anim) {
        this.anim = anim;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isTalking() {
        return isTalking;
    }

    public void setTalking(boolean isTalking) {
        this.isTalking = isTalking;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor v) {
        this.vendor = v;
    }

    public Creature getEmulatingCreature() {
        return emulatingCreature;
    }

    public void setEmulatingCreature(Creature emulatingCreature) {
        this.emulatingCreature = emulatingCreature;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public void setTextureRegion(TextureRegion textureRegion) {
        this.textureRegion = textureRegion;
    }

    public boolean isRemovedFromMap() {
        return removedFromMap;
    }

    public void setRemovedFromMap(boolean removedFromMap) {
        this.removedFromMap = removedFromMap;
    }

}
