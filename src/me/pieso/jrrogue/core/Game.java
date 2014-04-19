package me.pieso.jrrogue.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import me.pieso.jrrogue.entity.Entity;
import me.pieso.jrrogue.entity.Floor;
import me.pieso.jrrogue.entity.Living;
import me.pieso.jrrogue.entity.Monster;
import me.pieso.jrrogue.entity.Player;
import me.pieso.jrrogue.entity.Torch;
import me.pieso.jrrogue.entity.Wall;

public final class Game implements ActionListener {

    private final int width;
    private final int height;
    private Floor[][] data;
    private Player player;
    private final List<Runnable> hooks;
    private final List<Living> live;

    public Game() {
        this.width = 30;
        this.height = 30;
        this.data = new Floor[height][width];
        this.hooks = new ArrayList<>();
        this.live = new ArrayList<>();
        makeDumb();
        spawnPlayer();
        calculateVision();
    }

    public void addHook(Runnable r) {
        this.hooks.add(r);
    }

    public void runHooks() {
        for (Runnable r : hooks) {
            r.run();
        }
    }

    public void makeDumb() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    data[y][x] = new Wall(x, y);
                } else if (x > 1 && x < width - 2
                        && y > 1 && y < height - 2) {
                    if (new Random().nextDouble() > 0.75) {
                        data[y][x] = new Wall(x, y);
                    } else {
                        data[y][x] = new Floor(x, y);
                        if (new Random().nextDouble() > 0.95) {
                            Monster m = new Monster();
                            live.add(m);
                            data[y][x].set(m);
                        }
                    }
                } else {
                    data[y][x] = new Floor(x, y);
                }
            }
        }
    }

    public void spawnPlayer() {
        player = new Player();
        data[1][1].set(player);
    }

    public Player getPlayer() {
        return player;
    }

    public boolean moveRandomly(Entity e) {
        int x = e.x();
        int y = e.y();
        switch (new Random().nextInt(4)) {
            case 0:
                x++;
                break;
            case 1:
                x--;
                break;
            case 2:
                y++;
                break;
            case 3:
                y--;
                break;
            default:
                System.out.println("crap");
        }
        return move(e, e.x(), e.y(), x, y);
    }

    public void calculateVision() {
        calculateVisionEntity(player);
        for (Living l : live) {
            if (l instanceof Torch) {
                calculateVisionEntity(l);
            }
        }
    }

    public void calculateVisionEntity(Entity e) {
        int mm = 4;
        boolean over = false;
        boolean torch = false;
        if (e instanceof Torch) {
            torch = true;
            if (((Torch) e).ttl() == 1) {
                over = true;
            }
        }
        for (int y = e.y() - mm; y < e.y() + mm; y++) {
            for (int x = e.x() - mm; x < e.x() + mm; x++) {
                if (x >= 0 && y >= 0 && x < width && y < height) {
                    int dis = Math.abs(x - e.x()) + Math.abs(y - e.y());
                    if (dis < mm - 1 && !over) {
                        data[y][x].setFoggy(false);
                        data[y][x].setSeen(true);
                    } else {
                        if (torch && over) {
                            data[y][x].setFoggy(true);
                        } else if (!torch) {
                            data[y][x].setFoggy(true);
                        }
                    }
                }
            }
        }
    }

    public List<Floor> getNeighbours(Entity e) {
        return getNeighbours(e.x(), e.y());
    }

    public List<Floor> getNeighbours(int rx, int ry) {
        List<Floor> res = new ArrayList<>();
        for (int y = (ry - 1); y < (ry + 2); y++) {
            for (int x = (rx - 1); x < (rx + 2); x++) {
                if (x >= 0 && y >= 0 && x < width && y < height) {
                    if (rx == x && ry == y) {

                    } else {
                        res.add(data[y][x]);
                    }
                }
            }
        }
        return res;
    }

    public boolean movePlayer(int dx, int dy) {
        boolean b = move(player, player.x(), player.y(), player.x() + dx, player.y() + dy);
        calculateVision();
        tick();
        return b;
    }

    public void dropTorch(int x, int y) {
        List<Floor> possible = getNeighbours(x, y);
        Torch torch = new Torch();
        while (true) {
            int len = possible.size();
            System.out.println(len);
            if (len == 0) {
                return;
            }
            int pos = new Random().nextInt(len);
            if (!possible.get(pos).set(torch)) {
                possible.remove(pos);
            } else {
                break;
            }
        }
        live.add(torch);
    }

    public boolean move(Entity e, int x1, int y1, int x2, int y2) {
        if (x1 >= 0 && x1 < width && y1 >= 0 && y1 < height
                && x2 >= 0 && x2 < width && y2 >= 0 && y2 < height) {
            Entity en = data[y2][x2].get();
            if (en == null) {
                if (data[y1][x1].remove(e)) {
                    if (data[y2][x2].set(e)) {
                        return true;
                    } else {
                        data[y1][x1].set(e);
                    }
                }
            } else {
                e.bumped(en);
            }
        }
        return false;
    }

    public void tick() {
        ArrayList<Living> deletion = new ArrayList<>();
        player.tick(this);
        if (!player.living()) {
            player.addStatus("You are dead.");
        }
        for (Living l : live) {
            l.tick(this);
            if (!l.living()) {
                deletion.add(l);
            }
        }
        for (Living l : deletion) {
            data[l.y()][l.x()].remove(l);
            live.remove(l);
        }
        runHooks();
    }

    public Entity[][] getData() {
        return data;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {

    }
}
