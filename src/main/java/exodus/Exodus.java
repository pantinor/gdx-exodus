package exodus;

import com.badlogic.gdx.Files;

import util.LogDisplay;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Array;
import exodus.Constants.MapType;
import objects.ArmorSet;
import objects.CreatureSet;
import objects.MapSet;
import objects.TileSet;
import objects.WeaponSet;
import util.Utils;
import vendor.VendorClassSet;

public class Exodus extends Game {

    public static int SCREEN_WIDTH = 1024;
    public static int SCREEN_HEIGHT = 768;

    public static int MAP_WIDTH = 672;
    public static int MAP_HEIGHT = 672;

    public static LogDisplay hud;
    public static Texture backGround;
    
    public static BitmapFont font;
    public static BitmapFont smallFont;
    public static BitmapFont largeFont;
    public static BitmapFont ultimaFont;
    
    public static Exodus mainGame;
    public static StartScreen startScreen;
    
    public static Skin skin;

    public static boolean playMusic = true;
    public static float musicVolume = 0.1f;
    public static Music music;

    public static MapSet maps;
    public static TileSet baseTileSet;
    public static TileSet u4TileSet;
    public static WeaponSet weapons;
    public static ArmorSet armors;
    public static CreatureSet creatures;
    public static VendorClassSet vendorClassSet;
    public static TextureAtlas standardAtlas;

    public static TextureRegion magicHitTile;
    public static TextureRegion hitTile;
    public static TextureRegion missTile;
    public static TextureRegion corpse;

    public static Animation<TextureRegion> explosionLarge;
    public static Animation<TextureRegion> explosion;
    public static Animation<TextureRegion> cloud;

    public static TextureRegion[] faceTiles = new TextureRegion[13 * 16];

    public static void main(String[] args) {

        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Ultima 3 - Exodus";
        cfg.width = SCREEN_WIDTH;
        cfg.height = SCREEN_HEIGHT;
        cfg.addIcon("assets/graphics/exodus.png", Files.FileType.Classpath);
        new LwjglApplication(new Exodus(), cfg);

    }

    @Override
    public void create() {

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/gnuolane.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 18;
        font = generator.generateFont(parameter);

        parameter.size = 16;
        smallFont = generator.generateFont(parameter);

        parameter.size = 24;
        largeFont = generator.generateFont(parameter);

        generator.dispose();
        
        generator = new FreeTypeFontGenerator(Gdx.files.classpath("assets/fonts/ultima.ttf"));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 96;
        ultimaFont = generator.generateFont(parameter);
        
        parameter.size = 48;
        BitmapFont smallUltimaFont = generator.generateFont(parameter);
        
        generator.dispose();


        skin = new Skin(Gdx.files.classpath("assets/skin/uiskin.json"));
        skin.remove("default-font", BitmapFont.class);
        skin.add("default-font", font, BitmapFont.class);
        skin.add("journal", font, BitmapFont.class);
        skin.add("death-screen", largeFont, BitmapFont.class);
        skin.add("ultima", ultimaFont, BitmapFont.class);
        skin.add("small-ultima", smallUltimaFont, BitmapFont.class);

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

        hud = new LogDisplay();

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
            
            u4TileSet = (TileSet) Utils.loadXml("assets/xml/u4-tileset-base.xml", TileSet.class);
            u4TileSet.setMaps();

            maps = (MapSet) Utils.loadXml("assets/xml/maps.xml", MapSet.class);
            maps.init(baseTileSet);
            maps.init(MapType.combat, u4TileSet);//set combat maps with the u4 tile set
            maps.init(MapType.shrine, u4TileSet);//set combat maps with the u4 tile set
            
            vendorClassSet = (VendorClassSet) Utils.loadXml("assets/xml/vendor.xml", VendorClassSet.class);
            vendorClassSet.init();
            
            weapons = (WeaponSet) Utils.loadXml("assets/xml/weapons.xml", WeaponSet.class);
            armors = (ArmorSet) Utils.loadXml("assets/xml/armors.xml", ArmorSet.class);
            creatures = (CreatureSet) Utils.loadXml("assets/xml/creatures.xml", CreatureSet.class);
            creatures.init();
            weapons.init();
            armors.init();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        mainGame = this;
        startScreen = new StartScreen();

        setScreen(startScreen);
    }
    
    public static class CloudDrawable extends Actor {

        float stateTime;

        @Override
        public void draw(Batch batch, float parentAlpha) {
            stateTime += Gdx.graphics.getDeltaTime();
            batch.draw(Exodus.cloud.getKeyFrame(stateTime, false), getX(), getY(), 64, 64);
        }
    }

    public static class ExplosionDrawable extends Actor {

        float stateTime;

        @Override
        public void draw(Batch batch, float parentAlpha) {
            stateTime += Gdx.graphics.getDeltaTime();
            batch.draw(Exodus.explosion.getKeyFrame(stateTime, false), getX(), getY(), 64, 64);
        }
    }

    public static class ExplosionLargeDrawable extends Actor {

        float stateTime;

        @Override
        public void draw(Batch batch, float parentAlpha) {
            stateTime += Gdx.graphics.getDeltaTime();
            batch.draw(Exodus.explosionLarge.getKeyFrame(stateTime, false), getX(), getY(), 192, 192);
        }
    }

}
