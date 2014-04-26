package me.pieso.jrrogue.map;

import me.pieso.jrrogue.entity.Floor;
import me.pieso.jrrogue.entity.Wall;
import me.pieso.jrrogue.entity.living.Monster;
import me.pieso.jrrogue.entity.living.Player;
import me.pieso.jrrogue.entity.pickup.GoldPickup;
import me.pieso.jrrogue.entity.pickup.SwordPickup;
import me.pieso.jrrogue.entity.pickup.TorchPickup;
import me.pieso.jrrogue.entity.trap.HealPad;
import me.pieso.jrrogue.entity.trap.Ladders;

public class CellularMap extends MapGenerator {

    public static final double IALIVE = 0.45;
    public static final int ROUNDS = 7;
    private boolean[][] cells;

    public CellularMap(int width, int height, Player player) {
        super(width, height, player);
        this.cells = new boolean[height][width];
    }

    private void set(boolean[][] cc, int x, int y) {
        cc[y][x] = true;
    }

    private void unset(boolean[][] cc, int x, int y) {
        cc[y][x] = false;
    }

    private void init() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (Math.random() > IALIVE) {
                    set(cells, x, y);
                } else {
                    unset(cells, x, y);
                }
            }
        }
    }

    private void realize() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (cells[y][x]) {
                    data[y][x] = new Floor(x, y);
                } else {
                    data[y][x] = new Wall(x, y);
                }
            }
        }
    }

    private int is(int x, int y) {
        if (x < 0 || x > width - 1 || y < 0 || y > height - 1) {
            return 1;
        }
        return (cells[y][x] == true ? 1 : 0);
    }

    private int neighbours(int x, int y) {
        int amo = 0;

        amo += is(x - 1, y - 1);
        amo += is(x, y - 1);
        amo += is(x + 1, y + 1);

        amo += is(x - 1, y);
        amo += is(x + 1, y);

        amo += is(x - 1, y + 1);
        amo += is(x, y + 1);
        amo += is(x + 1, y + 1);

        return amo;
    }

    private void process() {
        boolean[][] ncells = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int nei = neighbours(x, y);
                if (nei < 3) {
                    unset(ncells, x, y);
                } else {
                    if (is(x, y) == 1) {
                        set(ncells, x, y);
                    } else {
                        if (nei > 5) {
                            set(ncells, x, y);
                        }
                    }
                }
            }
        }
        cells = ncells;
    }

    @Override
    public void generate() {
        init();
        for (int i = 0; i < ROUNDS; i++) {
            process();
        }
        realize();
        boxWithWalls();
        putPlayer();
        for (int i = 0; i < (width * height * IALIVE * 0.01); i++) {
            put(new Monster());
        }
        for (int i = 0; i < (width * height * IALIVE * 0.03); i++) {
            put(new GoldPickup());
        }
        for (int i = 0; i < (width * height * IALIVE * 0.005); i++) {
            putTrap(new HealPad(0, 0));
        }
        for (int i = 0; i < (width * height * IALIVE * 0.0005); i++) {
            put(new SwordPickup());
        }
        for (int i = 0; i < (width * height * IALIVE * 0.0005); i++) {
            putTrap(new Ladders(0, 0));
        }
        for (int i = 0; i < (width * height * IALIVE * 0.002); i++) {
            put(new TorchPickup());
        }
    }

}