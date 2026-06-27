
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import util.Utils;

public class DrawConflictMaps {

    private static final int MAP_W = 11;
    private static final int MAP_H = 11;
    private static final int TILE_SIZE = 16;

    private static final String[] CONFLICT_FILES = {
        "CNFLCT_A.ULT",
        "CNFLCT_B.ULT",
        "CNFLCT_C.ULT",
        "CNFLCT_F.ULT",
        "CNFLCT_G.ULT",
        "CNFLCT_M.ULT",
        "CNFLCT_Q.ULT",
        "CNFLCT_R.ULT",
        "CNFLCT_S.ULT"
    };

    private static final Map<Integer, String> TILE_NAMES = createTileNames();

    public static void main(String[] args) throws Exception {
        Path outputDir = Paths.get("target/conflict-output");
        Files.createDirectories(outputDir);

        BufferedImage atlasImage = readImageResource("/assets/graphics/ultima-ega.png");
        Map<String, AtlasRegion> atlas = readAtlasResource("/assets/graphics/latest-atlas.txt");

        for (String fname : CONFLICT_FILES) {
            byte[] bytes = readBytesResource("/assets/data/" + fname);

            if (bytes.length < 0x79) {
                throw new IllegalArgumentException(fname + " is too small: " + bytes.length + " bytes");
            }

            BufferedImage out = new BufferedImage(
                    MAP_W * TILE_SIZE,
                    MAP_H * TILE_SIZE,
                    BufferedImage.TYPE_INT_ARGB
            );

            Graphics2D g = out.createGraphics();

            int pos = 0x00;

            for (int y = 0; y < MAP_H; y++) {
                for (int x = 0; x < MAP_W; x++) {
                    int tileIndex = bytes[pos] & 0xff;
                    pos++;

                    String tileName = TILE_NAMES.get(tileIndex);

                    if (tileName == null) {
                        System.err.println(fname + ": unknown tile index " + tileIndex + " at x=" + x + ", y=" + y);
                        tileName = "blank";
                    }

                    //System.err.println(fname + ": processing tile '" + tileName + "' index=" + tileIndex + " at x=" + x + ", y=" + y);

                    AtlasRegion region = atlas.get(tileName);

                    if (region == null) {
                        System.err.println(fname + ": atlas region not found for tile '" + tileName + "' index=" + tileIndex + " at x=" + x + ", y=" + y);
                        region = atlas.get("blank");
                    }

                    if (region == null) {
                        continue;
                    }

                    BufferedImage tile = cropLibGdxAtlasRegion(atlasImage, region);

                    g.drawImage(tile, x * TILE_SIZE, y * TILE_SIZE, null);
                }
            }

            g.dispose();

            String outName = fname.replace(".ULT", ".png");
            Path outPath = outputDir.resolve(outName);
            ImageIO.write(out, "png", outPath.toFile());

            System.out.println("Wrote " + outPath.toAbsolutePath());
        }
    }

    private static BufferedImage cropLibGdxAtlasRegion(BufferedImage atlasImage, AtlasRegion r) {
        //int javaY = atlasImage.getHeight() - r.y - r.h;
        return atlasImage.getSubimage(r.x, r.y, r.w, r.h);
    }

    private static BufferedImage readImageResource(String path) throws IOException {
        try (InputStream is = Utils.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new FileNotFoundException("Resource not found: " + path);
            }
            return ImageIO.read(is);
        }
    }

    private static byte[] readBytesResource(String path) throws IOException {
        try (InputStream is = Utils.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new FileNotFoundException("Resource not found: " + path);
            }
            return is.readAllBytes();
        }
    }

    private static Map<String, AtlasRegion> readAtlasResource(String path) throws IOException {
        String text;

        try (InputStream is = Utils.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new FileNotFoundException("Resource not found: " + path);
            }
            text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        return parseLibGdxAtlas(text);
    }

    private static Map<String, AtlasRegion> parseLibGdxAtlas(String text) {
        Map<String, AtlasRegion> regions = new LinkedHashMap<>();

        String currentName = null;
        Integer x = null;
        Integer y = null;
        Integer w = null;
        Integer h = null;

        String[] lines = text.split("\\R");

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.isEmpty()) {
                continue;
            }

            // Header lines from libGDX atlas.
            if (line.equals("format: RGBA8888")
                    || line.startsWith("filter:")
                    || line.startsWith("repeat:")
                    || line.endsWith(".png")) {
                continue;
            }

            boolean isProperty = line.contains(":");

            if (!isProperty) {
                if (currentName != null && x != null && y != null && w != null && h != null) {
                    /*
                     * Keep the first frame for animated tiles.
                     * Example: water, orc, skeleton, lava, etc. may have multiple atlas entries.
                     */
                    regions.putIfAbsent(currentName, new AtlasRegion(currentName, x, y, w, h));
                }

                currentName = line;
                x = y = w = h = null;
                continue;
            }

            if (line.startsWith("xy:")) {
                int[] pair = parsePair(line.substring(3));
                x = pair[0];
                y = pair[1];
            } else if (line.startsWith("size:")) {
                w = 16;
                h = 16;
            }
        }

        if (currentName != null && x != null && y != null && w != null && h != null) {
            regions.putIfAbsent(currentName, new AtlasRegion(currentName, x, y, w, h));
        }

        return regions;
    }

    private static int[] parsePair(String s) {
        String[] parts = s.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Bad atlas pair: " + s);
        }

        return new int[]{
            Integer.parseInt(parts[0].trim()),
            Integer.parseInt(parts[1].trim())
        };
    }

    private static Map<Integer, String> createTileNames() {
        Map<Integer, String> m = new HashMap<>();

        m.put(0, "water");
        m.put(1, "grass");
        m.put(2, "brush");
        m.put(3, "forest");
        m.put(4, "mountains");
        m.put(5, "dungeon");
        m.put(6, "towne");
        m.put(7, "castle");
        m.put(8, "floor");
        m.put(9, "chest");
        m.put(10, "horse");
        m.put(11, "frigate");
        m.put(12, "whirlpool");
        m.put(13, "serpent");
        m.put(14, "squid");
        m.put(15, "pirate");
        m.put(16, "merchant");
        m.put(17, "jester");
        m.put(18, "guard");
        m.put(19, "lord_british");
        m.put(20, "fighter");
        m.put(21, "cleric");
        m.put(22, "wizard");
        m.put(23, "thief");
        m.put(24, "orc");
        m.put(25, "skeleton");
        m.put(26, "giant");
        m.put(27, "daemon");
        m.put(28, "pincher");
        m.put(29, "dragon");
        m.put(30, "balron");
        m.put(31, "exodus");
        m.put(32, "energy_field");
        m.put(33, "lava");
        m.put(34, "moongate");
        m.put(35, "wall");
        m.put(36, "blank");
        m.put(37, "space");
        m.put(38, "A");
        m.put(39, "B");
        m.put(40, "C");
        m.put(41, "D");
        m.put(42, "E");
        m.put(43, "F");
        m.put(44, "G");
        m.put(45, "H");
        m.put(46, "I");
        m.put(47, "V");
        m.put(48, "Y");
        m.put(49, "L");
        m.put(50, "M");
        m.put(51, "N");
        m.put(52, "O");
        m.put(53, "P");
        m.put(54, "W");
        m.put(55, "R");
        m.put(56, "S");
        m.put(57, "T");
        m.put(58, "snake1");
        m.put(59, "snake2");
        m.put(60, "magic");
        m.put(61, "fire");
        m.put(62, "shrine");
        m.put(63, "ranger");

        return m;
    }

    private record AtlasRegion(String name, int x, int y, int w, int h) {

    }
}
