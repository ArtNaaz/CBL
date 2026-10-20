import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Base class for all animals in the game.
 * Duck inherits movement speed, size, animation and death behaviour from here.
 */
public abstract class animal {
    protected int x;
    protected int y;
    protected int size;
    protected int speed;

    protected boolean alive = true;
    protected boolean dying = false;

    // Death animation
    protected int deathFrame = 0;
    protected final int maxDeathFrames = 18;

    protected animal(int x, int y, int size, int speed) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.speed = speed;
    }

    public abstract void update(int panelWidth, int panelHeight);

    public abstract void draw(Graphics2D g2);

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isDying() {
        return dying;
    }

    public boolean isFinished() {
        return dying && deathFrame >= maxDeathFrames;
    }

    public void shoot() {
        if (alive) {
            alive = false;
            dying = true;
            deathFrame = 0;
        }
    }

    protected void updateDeathAnimation() {
        if (dying) {
            deathFrame++;
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
