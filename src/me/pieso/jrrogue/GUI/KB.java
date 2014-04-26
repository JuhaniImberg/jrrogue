package me.pieso.jrrogue.GUI;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import me.pieso.jrrogue.core.Game;
import me.pieso.jrrogue.entity.living.Player;
import me.pieso.jrrogue.item.Inventory;

public class KB implements KeyListener {

    private final Game game;

    public KB(Game game) {
        this.game = game;
    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        Player player = game.getPlayer();
        int x = 0;
        int y = 0;
        if (!player.living()) {
            return;
        }
        out:
        switch (ke.getKeyCode()) {
            case KeyEvent.VK_K:
            case KeyEvent.VK_UP:
                y--;
                break;
            case KeyEvent.VK_J:
            case KeyEvent.VK_DOWN:
                y++;
                break;
            case KeyEvent.VK_H:
            case KeyEvent.VK_LEFT:
                x--;
                break;
            case KeyEvent.VK_L:
            case KeyEvent.VK_RIGHT:
                x++;
                break;
            case KeyEvent.VK_SPACE:
                player.setUse(true);
                break;
            /*case KeyEvent.VK_T:
             game.dropTorch(player.x(), player.y());
             break;*/
            case KeyEvent.VK_PLUS:
                game.zoomup();
                return;
            case KeyEvent.VK_MINUS:
                game.zoomdown();
                return;
            default:
                for (int c = 0; c < Inventory.letters.length; c++) {
                    char cc = Inventory.letters[c];
                    if (ke.getKeyChar() == cc) {
                        player.inventory().remove(c, 1);
                        break out;
                    }
                }
                return;
        }
        game.movePlayer(x, y);
    }

    @Override
    public void keyReleased(KeyEvent ke) {

    }

}