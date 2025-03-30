package util;

public class SpreadFOV {

    private final float[][] resistanceMap;
    private final float[][] lightMap;

    public SpreadFOV(float[][] m) {

        int w = m.length;
        int h = m[0].length;

        this.lightMap = new float[w * 3][h * 3];
        this.resistanceMap = new float[w * 3][h * 3];

        //duplications to allow for wrapping          
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                this.resistanceMap[x][y] = m[x][y];
                this.resistanceMap[x][y + h] = m[x][y];
                this.resistanceMap[x][y + h * 2] = m[x][y];

                this.resistanceMap[x + w][y] = m[x][y];
                this.resistanceMap[x + w][y + h] = m[x][y];
                this.resistanceMap[x + w][y + h * 2] = m[x][y];

                this.resistanceMap[x + w * 2][y] = m[x][y];
                this.resistanceMap[x + w * 2][y + h] = m[x][y];
                this.resistanceMap[x + w * 2][y + h * 2] = m[x][y];
            }
        }

    }

    public float[][] lightMap() {
        return this.lightMap;
    }

    public float light(int x, int y) {
        return this.lightMap[x + this.lightMap.length / 3][y + this.lightMap[0].length / 3];
    }

    public void calculateFOV(int sx, int sy, float radius) {

        int startx = sx + lightMap.length / 3;
        int starty = sy + lightMap[0].length / 3;

        for (int y = 0; y < lightMap.length; y++) {
            for (int x = 0; x < lightMap[0].length; x++) {
                lightMap[y][x] = 0;
            }
        }

        lightMap[startx][starty] = 1;//light the starting cell

        for (Direction d : Direction.DIAGONALS) {
            castLight(1, 1.0f, 0.0f, radius, 0, d.deltaX, d.deltaY, 0, startx, starty);
            castLight(1, 1.0f, 0.0f, radius, d.deltaX, 0, 0, d.deltaY, startx, starty);
        }
        
        //light area around start as well
        lightMap[startx][starty + 1] = 1;
        lightMap[startx][starty - 1] = 1;
        lightMap[startx + 1][starty] = 1;
        lightMap[startx + 1][starty + 1] = 1;
        lightMap[startx + 1][starty - 1] = 1;
        lightMap[startx - 1][starty] = 1;
        lightMap[startx - 1][starty + 1] = 1;
        lightMap[startx - 1][starty - 1] = 1;
    }

    private void castLight(int row, float start, float end, float radius, int xx, int xy, int yx, int yy, int startx, int starty) {
        float newStart = 0.0f;
        if (start < end) {
            return;
        }
        boolean blocked = false;
        for (int distance = row; distance <= radius && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                float leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                float rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= 0 && currentY >= 0 && currentX < lightMap.length && currentY < lightMap[0].length) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }

                //check if it's within the lightable area and light if needed
                if (BasicRadiusStrategy.CIRCLE.radius(deltaX, deltaY) <= radius) {
                    float bright = (float) (1 - (BasicRadiusStrategy.CIRCLE.radius(deltaX, deltaY) / radius));
                    lightMap[currentX][currentY] = bright;
                }

                if (blocked) { //previous cell was a blocking one
                    if (resistanceMap[currentX][currentY] >= 1) {//hit a wall
                        newStart = rightSlope;
                        continue;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (resistanceMap[currentX][currentY] >= 1 && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        castLight(distance + 1, start, leftSlope, radius, xx, xy, yx, yy, startx, starty);
                        newStart = rightSlope;
                    }
                }
            }
        }
    }

    private enum BasicRadiusStrategy {

        /**
         * In an unobstructed area the FOV would be a square.
         *
         * This is the shape that would represent movement radius in an 8-way
         * movement scheme with no additional cost for diagonal movement.
         */
        SQUARE,
        /**
         * In an unobstructed area the FOV would be a diamond.
         *
         * This is the shape that would represent movement radius in a 4-way
         * movement scheme.
         */
        DIAMOND,
        /**
         * In an unobstructed area the FOV would be a circle.
         *
         * This is the shape that would represent movement radius in an 8-way
         * movement scheme with all movement cost the same based on distance
         * from the source
         */
        CIRCLE,
        /**
         * In an unobstructed area the FOV would be a cube.
         *
         * This is the shape that would represent movement radius in an 8-way
         * movement scheme with no additional cost for diagonal movement.
         */
        CUBE,
        /**
         * In an unobstructed area the FOV would be a octahedron.
         *
         * This is the shape that would represent movement radius in a 4-way
         * movement scheme.
         */
        OCTAHEDRON,
        /**
         * In an unobstructed area the FOV would be a sphere.
         *
         * This is the shape that would represent movement radius in an 8-way
         * movement scheme with all movement cost the same based on distance
         * from the source
         */
        SPHERE;

        public float radius(int startx, int starty, int startz, int endx, int endy, int endz) {
            return radius((float) startx, (float) starty, (float) startz, (float) endx, (float) endy, (float) endz);
        }

        public float radius(float startx, float starty, float startz, float endx, float endy, float endz) {
            float dx = Math.abs(startx - endx);
            float dy = Math.abs(starty - endy);
            float dz = Math.abs(startz - endz);
            return radius(dx, dy, dz);
        }

        public float radius(int dx, int dy, int dz) {
            return radius((float) dx, (float) dy, (float) dz);
        }

        public float radius(float dx, float dy, float dz) {
            dx = Math.abs(dx);
            dy = Math.abs(dy);
            dz = Math.abs(dz);
            float radius = 0f;
            switch (this) {
                case SQUARE:
                case CUBE:
                    radius = Math.max(dx, Math.max(dy, dz));//radius is longest axial distance
                    break;
                case DIAMOND:
                case OCTAHEDRON:
                    radius = dx + dy + dz;//radius is the manhattan distance
                    break;
                case CIRCLE:
                case SPHERE:
                    radius = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);//standard spherical radius
            }
            return radius;
        }

        public float radius(int startx, int starty, int endx, int endy) {
            return radius((float) startx, (float) starty, (float) endx, (float) endy);
        }

        public float radius(float startx, float starty, float endx, float endy) {
            float dx = Math.abs(startx - endx);
            float dy = Math.abs(starty - endy);
            return radius(dx, dy);
        }

        public float radius(int dx, int dy) {
            return radius((float) dx, (float) dy);
        }

        public float radius(float dx, float dy) {
            return radius(dx, dy, 0f);
        }
    }

    private enum Direction {

        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0), UP_LEFT(-1, -1), UP_RIGHT(1, -1), DOWN_LEFT(-1, 1), DOWN_RIGHT(1, 1), NONE(0, 0);
        /**
         * An array which holds only the four cardinal directions.
         */
        public static final Direction[] CARDINALS = {UP, DOWN, LEFT, RIGHT};
        /**
         * An array which holds only the four cardinal directions in clockwise
         * order.
         */
        public static final Direction[] CARDINALS_CLOCKWISE = {UP, RIGHT, DOWN, LEFT};
        /**
         * An array which holds only the four cardinal directions in
         * counter-clockwise order.
         */
        public static final Direction[] CARDINALS_COUNTERCLOCKWISE = {UP, LEFT, DOWN, RIGHT};
        /**
         * An array which holds only the four diagonal directions.
         */
        public static final Direction[] DIAGONALS = {UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT};
        /**
         * An array which holds all eight OUTWARDS directions.
         */
        public static final Direction[] OUTWARDS = {UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT};
        /**
         * An array which holds all eight OUTWARDS directions in clockwise
         * order.
         */
        public static final Direction[] CLOCKWISE = {UP, UP_RIGHT, RIGHT, DOWN_RIGHT, DOWN, DOWN_LEFT, LEFT, UP_LEFT};
        /**
         * An array which holds all eight OUTWARDS directions in
         * counter-clockwise order.
         */
        public static final Direction[] COUNTERCLOCKWISE = {UP, UP_LEFT, LEFT, DOWN_LEFT, DOWN, DOWN_RIGHT, RIGHT, UP_RIGHT};
        /**
         * The x coordinate difference for this direction.
         */
        public final int deltaX;
        /**
         * The y coordinate difference for this direction.
         */
        public final int deltaY;

        public static Direction getDirection(int x, int y) {
            if ((x | y) == 0) {
                return NONE;
            }
            return CLOCKWISE[(int) (Math.atan2(y, x) * 8f + 2.5f) & 7];
        }

        public static Direction getRoughDirection(int x, int y) {
            x = (x >> 31 | -x >>> 31); // project nayuki signum
            y = (y >> 31 | -y >>> 31); // project nayuki signum
            switch (x) {
                case -1:
                    switch (y) {
                        case 1:
                            return DOWN_LEFT;
                        case -1:
                            return UP_LEFT;
                        default:
                            return LEFT;
                    }
                case 1:
                    switch (y) {
                        case 1:
                            return DOWN_RIGHT;
                        case -1:
                            return UP_RIGHT;
                        default:
                            return RIGHT;
                    }
                default:
                    switch (y) {
                        case 1:
                            return DOWN;
                        case -1:
                            return UP;
                        default:
                            return NONE;
                    }
            }
        }

        public static Direction getCardinalDirection(int x, int y) {
            if ((x | y) == 0) {
                return NONE;
            }
            return CARDINALS_CLOCKWISE[(int) (Math.atan2(y, x) * 4f + 1.5f) & 3];
        }

        public Direction clockwise() {
            switch (this) {
                case UP:
                    return UP_RIGHT;
                case DOWN:
                    return DOWN_LEFT;
                case LEFT:
                    return UP_LEFT;
                case RIGHT:
                    return DOWN_RIGHT;
                case UP_LEFT:
                    return UP;
                case UP_RIGHT:
                    return RIGHT;
                case DOWN_LEFT:
                    return LEFT;
                case DOWN_RIGHT:
                    return DOWN;
                case NONE:
                default:
                    return NONE;
            }
        }

        public Direction counterClockwise() {
            switch (this) {
                case UP:
                    return UP_LEFT;
                case DOWN:
                    return DOWN_RIGHT;
                case LEFT:
                    return DOWN_LEFT;
                case RIGHT:
                    return UP_RIGHT;
                case UP_LEFT:
                    return LEFT;
                case UP_RIGHT:
                    return UP;
                case DOWN_LEFT:
                    return DOWN;
                case DOWN_RIGHT:
                    return RIGHT;
                case NONE:
                default:
                    return NONE;
            }
        }

        public Direction opposite() {
            switch (this) {
                case UP:
                    return DOWN;
                case DOWN:
                    return UP;
                case LEFT:
                    return RIGHT;
                case RIGHT:
                    return LEFT;
                case UP_LEFT:
                    return DOWN_RIGHT;
                case UP_RIGHT:
                    return DOWN_LEFT;
                case DOWN_LEFT:
                    return UP_RIGHT;
                case DOWN_RIGHT:
                    return UP_LEFT;
                case NONE:
                default:
                    return NONE;
            }
        }

        public boolean isDiagonal() {
            return (deltaX & deltaY) != 0;
        }

        public boolean isCardinal() {
            return (deltaX + deltaY & 1) != 0;
        }

        public boolean hasUp() {
            switch (this) {
                case UP:
                case UP_LEFT:
                case UP_RIGHT:
                    return true;
                case DOWN:
                case DOWN_LEFT:
                case DOWN_RIGHT:
                case LEFT:
                case NONE:
                case RIGHT:
                    return false;
            }
            throw new IllegalStateException("Unmatched Direction: " + this);
        }

        public boolean hasDown() {
            switch (this) {
                case DOWN:
                case DOWN_LEFT:
                case DOWN_RIGHT:
                    return true;
                case LEFT:
                case NONE:
                case RIGHT:
                case UP:
                case UP_LEFT:
                case UP_RIGHT:
                    return false;
            }
            throw new IllegalStateException("Unmatched Direction: " + this);
        }

        public boolean hasLeft() {
            switch (this) {
                case DOWN_LEFT:
                case LEFT:
                case UP_LEFT:
                    return true;
                case DOWN:
                case DOWN_RIGHT:
                case NONE:
                case RIGHT:
                case UP:
                case UP_RIGHT:
                    return false;
            }
            throw new IllegalStateException("Unmatched Direction: " + this);
        }

        public boolean hasRight() {
            switch (this) {
                case RIGHT:
                case DOWN_RIGHT:
                case UP_RIGHT:
                    return true;
                case DOWN:
                case NONE:
                case UP:
                case DOWN_LEFT:
                case LEFT:
                case UP_LEFT:
                    return false;
            }
            throw new IllegalStateException("Unmatched Direction: " + this);
        }

        Direction(int x, int y) {
            deltaX = x;
            deltaY = y;
        }
    }

}
