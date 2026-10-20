import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Duck is a concrete animal.
 * It receives speed, size and death-animation behaviour from animal.
 */
public class Duck extends animal {
    private static final Random random = new Random();

    private int dx;
    private int dy;
    private int wingFrame = 0;
    private boolean facingRight;

    public Duck(int panelWidth, int panelHeight) {
        super(
            random.nextBoolean() ? -80 : panelWidth + 20,
            60 + random.nextInt(Math.max(1, panelHeight - 220)),
            70 + random.nextInt(21),
            3 + random.nextInt(3)
        );

        facingRight = x < 0;
        dx = facingRight ? speed : -speed;
        dy = random.nextBoolean() ? 1 : -1;
    }

    @Override
    public void update(int panelWidth, int panelHeight) {
        if (dying) {
            updateDeathAnimation();
            return;
        }

        x += dx;
        y += dy;

        // Small random vertical changes make the flight less straight.
        if (random.nextInt(30) == 0) {
            dy = random.nextInt(5) - 2;
        }

        int minY = 45;
        int maxY = panelHeight - size - 80;

        if (y <= minY) {
            y = minY;
            dy = Math.abs(dy);
        }

        if (y >= maxY) {
            y = maxY;
            dy = -Math.abs(dy);
        }

        wingFrame = (wingFrame + 1) % 20;
    }

    public boolean isOutsideScreen(int panelWidth, int panelHeight) {
        return x < -size - 30 || x > panelWidth + 30;
    }

    @Override
    public void draw(Graphics2D g2) {
        if (dying) {
            drawDeathAnimation(g2);
        } else {
            drawFlyingDuck(g2);
        }
    }

    private void drawFlyingDuck(Graphics2D g2) {
        int bodyX = x + 12;
        int bodyY = y + 25;

        // Body
        g2.setColor(new Color(90, 55, 25));
        g2.fillOval(bodyX, bodyY, size - 20, size - 32);

        // Head
        int headX = facingRight ? x + size - 35 : x + 12;
        g2.setColor(new Color(105, 65, 30));
        g2.fillOval(headX, y + 5, 32, 32);

        // Beak
        g2.setColor(new Color(235, 150, 30));
        if (facingRight) {
            g2.fillPolygon(
                new int[]{headX + 28, headX + 47, headX + 28},
                new int[]{y + 16, y + 23, y + 27},
                3
            );
        } else {
            g2.fillPolygon(
                new int[]{headX + 4, headX - 15, headX + 4},
                new int[]{y + 16, y + 23, y + 27},
                3
            );
        }

        // Eye
        g2.setColor(Color.BLACK);
        int eyeX = facingRight ? headX + 20 : headX + 7;
        g2.fillOval(eyeX, y + 14, 6, 6);

        // Wings
        g2.setColor(new Color(70, 45, 25));
        int wingOffset = wingFrame < 10 ? 0 : 10;

        g2.fillOval(x + 18, y + 20 - wingOffset, 35, 55);

        // Tail
        g2.setColor(new Color(75, 45, 20));
        if (facingRight) {
            g2.fillPolygon(
                new int[]{x + 15, x - 10, x + 18},
                new int[]{y + 35, y + 20, y + 50},
                3
            );
        } else {
            g2.fillPolygon(
                new int[]{x + size - 15, x + size + 10, x + size - 18},
                new int[]{y + 35, y + 20, y + 50},
                3
            );
        }
    }

    private void drawDeathAnimation(Graphics2D g2) {
        int centerX = x + size / 2;
        int centerY = y + size / 2;

        int fallDistance = deathFrame * 4;
        int currentY = centerY + fallDistance;

        // Body becomes smaller while falling.
        int currentSize = Math.max(8, size - deathFrame * 3);

        g2.setColor(new Color(90, 55, 25));
        g2.fillOval(
            centerX - currentSize / 2,
            currentY - currentSize / 2,
            currentSize,
            currentSize
        );

        // X marks the hit/death state.
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(4));
        int r = Math.max(8, 18 - deathFrame / 2);
        g2.drawLine(centerX - r, currentY - r, centerX + r, currentY + r);
        g2.drawLine(centerX + r, currentY - r, centerX - r, currentY + r);
    }
}
