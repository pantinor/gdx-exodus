/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exodus;

import com.badlogic.gdx.Files;

import util.LogDisplay;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Array;
import objects.ArmorSet;
import objects.CreatureSet;
import objects.MapSet;
import objects.TileSet;
import objects.WeaponSet;
import util.Utils;
//import vendor.VendorClassSet;

public class Exodus extends Game {

    public static int SCREEN_WIDTH = 1024;
    public static int SCREEN_HEIGHT = 768;

    public static int MAP_WIDTH = 672;
    public static int MAP_HEIGHT = 672;

    public static LogDisplay hud;
    public static Texture backGround;
    public static BitmapFont font;
    public static BitmapFont smallFont;

    public static StartScreen startScreen;
    public static Skin skin;

    public static boolean playMusic = true;
    public static float musicVolume = 0.1f;
    public static Music music;

    public static MapSet maps;
    public static TileSet baseTileSet;
    public static WeaponSet weapons;
    public static ArmorSet armors;
    public static CreatureSet creatures;
    //public static VendorClassSet vendorClassSet;
    public static TextureAtlas standardAtlas;

    public static TextureRegion magicHitTile;
    public static TextureRegion hitTile;
    public static TextureRegion missTile;
    public static TextureRegion corpse;

    public static Animation explosionLarge;
    public static Animation explosion;
    public static Animation cloud;

    public static TextureRegion[] faceTiles = new TextureRegion[13 * 16];

    public static void main(String[] args) {

        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Exodus";
        cfg.width = SCREEN_WIDTH;
        cfg.height = SCREEN_HEIGHT;
        cfg.addIcon("assets/graphics/exodus.png", Files.FileType.Classpath);
        new LwjglApplication(new Exodus(), cfg);

    }

    @Override
    public void create() {

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/lindberg.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 16;
        font = generator.generateFont(parameter);

        parameter.size = 10;
        smallFont = generator.generateFont(parameter);

        parameter.size = 24;
        BitmapFont fontLarger = generator.generateFont(parameter);

        generator.dispose();

        skin = new Skin(Gdx.files.classpath("assets/skin/uiskin.json"));
        skin.remove("default-font", BitmapFont.class);
        skin.add("default-font", font, BitmapFont.class);
        skin.add("journal", font, BitmapFont.class);
        skin.add("death-screen", fontLarger, BitmapFont.class);

        Label.LabelStyle ls = skin.get("default", Label.LabelStyle.class);
        ls.font = font;
        TextButton.TextButtonStyle tbs = skin.get("default", TextButton.TextButtonStyle.class);
        tbs.font = font;
        TextButton.TextButtonStyle tbswood = skin.get("wood", TextButton.TextButtonStyle.class);
        tbswood.font = font;
        SelectBox.SelectBoxStyle sbs = skin.get("default", SelectBox.SelectBoxStyle.class);
        sbs.font = font;
        sbs.listStyle.font = font;
        CheckBox.CheckBoxStyle cbs = skin.get("default", CheckBox.CheckBoxStyle.class);
        cbs.font = font;
        List.ListStyle lis = skin.get("default", List.ListStyle.class);
        lis.font = font;
        TextField.TextFieldStyle tfs = skin.get("default", TextField.TextFieldStyle.class);
        tfs.font = font;

        hud = new LogDisplay(font);

        try {

            backGround = new Texture(Gdx.files.classpath("assets/graphics/frame.png"));

            TextureRegion[][] trs = TextureRegion.split(new Texture(Gdx.files.classpath("assets/graphics/Portraits.gif")), 56, 64);
            for (int row = 0; row < 13; row++) {
                for (int col = 0; col < 16; col++) {
                    faceTiles[row * 16 + col] = trs[row][col];
                }
            }

            standardAtlas = new TextureAtlas(Gdx.files.classpath("assets/graphics/latest-atlas.txt"));

            hitTile = standardAtlas.findRegion("hit_flash");
            magicHitTile = standardAtlas.findRegion("magic_flash");
            missTile = standardAtlas.findRegion("miss_flash");
            corpse = standardAtlas.findRegion("corpse");

            TextureAtlas tmp = new TextureAtlas(Gdx.files.classpath("assets/graphics/explosion-atlas.txt"));
            Array<TextureAtlas.AtlasRegion> ar = tmp.findRegions("expl");
            explosion = new Animation(.2f, ar);

            tmp = new TextureAtlas(Gdx.files.classpath("assets/graphics/Exp_type_B.atlas"));
            ar = tmp.findRegions("im");
            explosionLarge = new Animation(.1f, ar);

            tmp = new TextureAtlas(Gdx.files.classpath("assets/graphics/cloud-atlas.txt"));
            ar = tmp.findRegions("cloud");
            cloud = new Animation(.2f, ar);

            baseTileSet = (TileSet) Utils.loadXml("assets/xml/tileset-base.xml", TileSet.class);
            baseTileSet.setMaps();

            maps = (MapSet) Utils.loadXml("assets/xml/maps.xml", MapSet.class);
            maps.init(baseTileSet);

            //vendorClassSet = (VendorClassSet) Utils.loadXml("vendor.xml", VendorClassSet.class);
            //vendorClassSet.init();
            weapons = (WeaponSet) Utils.loadXml("assets/xml/weapons.xml", WeaponSet.class);
            armors = (ArmorSet) Utils.loadXml("assets/xml/armors.xml", ArmorSet.class);
            creatures = (CreatureSet) Utils.loadXml("assets/xml/creatures.xml", CreatureSet.class);
            creatures.init();
            weapons.init();
            armors.init();

        } catch (Exception e) {
            e.printStackTrace();
        }

        startScreen = new StartScreen(this);

        //setScreen(new GameScreen(this));
        setScreen(startScreen);
    }

    private static Texture fillRectangle(int width, int height, Color color) {
        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pix.setColor(color);
        pix.fillRectangle(0, 0, width, height);
        Texture t = new Texture(pix);
        pix.dispose();
        return t;
    }

}
