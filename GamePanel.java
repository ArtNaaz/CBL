import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class GamePanel extends JPanel implements Runnable {

    private static final long serialVersionUID = 1L;

    final int originalTileSize = 16;
    final int scale = 4;                       // 4 = 1024x768, 5 = 1280x960
    final int TileSize = originalTileSize * scale;

    final int maxScreenCol = 16;
    final int maxScreenRow = 12;

    final int screenWidth = TileSize * maxScreenCol;
    final int screenHeight = TileSize * maxScreenRow;

    /** The states a round can be in. */
    private enum State { READY, PLAYING, PAUSED, FINISHED }

    private static final int FPS = 60;
    private static final int ROUND_SECONDS = 60;
    private static final int ROUND_FRAMES = FPS * ROUND_SECONDS;

    private Thread gameThread;
    private final List<animal> animals = new ArrayList<>();

    private volatile State state = State.READY;
    private int framesLeft = ROUND_FRAMES;

    private int score = 0;
    private int shots = 0;
    private int ducksHit = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(new Color(120, 190, 245));
        setDoubleBuffered(true);
        setFocusable(true);
        
         
        ImageIcon icon = new ImageIcon("CrossImage.png");
        
        Point hotSpot = new Point(0, 0); 
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Cursor customCursor = toolkit.createCustomCursor(icon.getImage(), hotSpot, "CustomCursorName");

       
        this.setCursor(customCursor);

        addMouseListener(new MouseHandler(this));

        
        getInputMap(WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "pause");
        getActionMap().put("pause", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });

        spawnTwoDucks();
    }

    private void spawnTwoDucks() {
        animals.clear();

        Duck first = new Duck(screenWidth, screenHeight);
        Duck second = new Duck(screenWidth, screenHeight);

        // Prevent the two ducks from starting almost on top of each other.
        while (Math.abs(first.getX() - second.getX()) < 150 &&
               Math.abs(first.getY() - second.getY()) < 100) {
            second = new Duck(screenWidth, screenHeight);
        }

        animals.add(first);
        animals.add(second);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        final double drawInterval = 1_000_000_000.0 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            update();
            repaint();

            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime /= 1_000_000.0;

                if (remainingTime < 0) {
                    remainingTime = 0;
                }

                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // ------------------------------------------------------------------
    // Round control and getters (used by ScorePanel and DuckSeason)
    // ------------------------------------------------------------------

    /** Starts a fresh round. Ignored while a round is running or paused. */
    public synchronized void startRound() {
        if (isPlaying()) {
            return;
        }
        score = 0;
        shots = 0;
        ducksHit = 0;
        framesLeft = ROUND_FRAMES;
        spawnTwoDucks();
        state = State.PLAYING;
    }

    /** Pauses a running round or resumes a paused one. */
    public synchronized void togglePause() {
        if (state == State.PLAYING) {
            state = State.PAUSED;
        } else if (state == State.PAUSED) {
            state = State.PLAYING;
        }
    }

    public int getScore() {
        return score;
    }

    public int getHits() {
        return ducksHit;
    }

    public int getShots() {
        return shots;
    }

    /** Hit percentage, 0 when nothing has been shot yet. */
    public double getAccuracy() {
        return shots == 0 ? 0.0 : 100.0 * ducksHit / shots;
    }

    public synchronized int getSecondsLeft() {
        return (framesLeft + FPS - 1) / FPS;
    }

    /** True while a round is running, including when it is paused. */
    public boolean isPlaying() {
        return state == State.PLAYING || state == State.PAUSED;
    }

    public boolean isPaused() {
        return state == State.PAUSED;
    }

    public boolean isFinished() {
        return state == State.FINISHED;
    }

    // ------------------------------------------------------------------
    // Game logic
    // ------------------------------------------------------------------

    public synchronized void update() {
        if (state != State.PLAYING) {
            return;
        }

        framesLeft--;
        if (framesLeft <= 0) {
            framesLeft = 0;
            state = State.FINISHED;
            return;
        }

        Iterator<animal> iterator = animals.iterator();

        while (iterator.hasNext()) {
            animal current = iterator.next();
            current.update(screenWidth, screenHeight);

            // A duck that has flown away is removed.
            if (current instanceof Duck &&
                !current.isDying() &&
                ((Duck) current).isOutsideScreen(screenWidth, screenHeight)) {
                iterator.remove();
            }
        }

        // Death animation is allowed to finish before removing the duck.
        iterator = animals.iterator();
        while (iterator.hasNext()) {
            animal current = iterator.next();

            if (current.isFinished()) {
                iterator.remove();
            }
        }

        // Once both ducks are gone, start another pair.
        if (animals.isEmpty()) {
            spawnTwoDucks();
        }
    }

    /**
     * Called by MouseHandler after a mouse click.
     */
    public synchronized void shoot(int mouseX, int mouseY) {
        if (state != State.PLAYING) {
            return;
        }

        shots++;

        // Iterate backwards so the first hit is enough.
        for (int i = animals.size() - 1; i >= 0; i--) {
            animal current = animals.get(i);

            if (current.isAlive() && current.getBounds().contains(mouseX, mouseY)) {
                current.shoot();
                ducksHit++;
                score += 100;
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    // Drawing
    // ------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        // The game thread changes the list in update(), so draw under the same lock.
        synchronized (this) {
            drawBackground(g2);

            for (animal current : animals) {
                current.draw(g2);
            }

            drawHUD(g2);
            drawOverlay(g2);
        }

        g2.dispose();
    }

    private void drawBackground(Graphics2D g2) {
        // Sky
        g2.setColor(new Color(120, 190, 245));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Clouds
        g2.setColor(Color.WHITE);
        g2.fillOval(90, 70, 100, 45);
        g2.fillOval(145, 55, 90, 60);
        g2.fillOval(205, 75, 110, 40);

        g2.fillOval(650, 100, 100, 45);
        g2.fillOval(700, 80, 90, 55);

        // Ground
        g2.setColor(new Color(70, 170, 70));
        g2.fillRect(0, screenHeight - 90, screenWidth, 90);

        // Grass lines
        g2.setColor(new Color(45, 130, 50));
        for (int x = 0; x < screenWidth; x += 35) {
            g2.drawLine(x, screenHeight - 90, x + 8, screenHeight - 105);
        }
    }

    private void drawHUD(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 20));

        g2.drawString("DUCK SEASON", 20, 30);
        g2.drawString("Score: " + score, 20, 55);
        g2.drawString("Shots: " + shots, 180, 55);
        g2.drawString("Hits: " + ducksHit, 300, 55);
        g2.drawString("Time: " + getSecondsLeft() + " s", 420, 55);

        g2.setFont(new Font("Arial", Font.PLAIN, 15));
        g2.drawString("Click on the ducks to shoot!", 20, screenHeight - 25);
    }

    /** Dims the playfield and shows a message when no round is running. */
    private void drawOverlay(Graphics2D g2) {
        String title;
        String subtitle;

        switch (state) {
            case READY:
                title = "DUCK SEASON";
                subtitle = "Press 'Start Game' to begin";
                break;
            case PAUSED:
                title = "PAUSED";
                subtitle = "Press P or 'Resume' to continue";
                break;
            case FINISHED:
                title = "TIME'S UP!";
                subtitle = "Score: " + score + "  -  enter your name to save it";
                break;
            default:
                return;
        }

        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.WHITE);

        g2.setFont(new Font("Arial", Font.BOLD, 56));
        FontMetrics titleMetrics = g2.getFontMetrics();
        g2.drawString(title,
                (screenWidth - titleMetrics.stringWidth(title)) / 2,
                screenHeight / 2 - 10);

        g2.setFont(new Font("Arial", Font.PLAIN, 22));
        FontMetrics subtitleMetrics = g2.getFontMetrics();
        g2.drawString(subtitle,
                (screenWidth - subtitleMetrics.stringWidth(subtitle)) / 2,
                screenHeight / 2 + 35);
    }
}
