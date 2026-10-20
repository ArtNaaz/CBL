import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Starts the game and connects the playfield, the score panel and the highscore file. */
public class DuckSeason implements ActionListener {

    private final GamePanel gamePanel = new GamePanel();
    private final ScorePanel scorePanel = new ScorePanel(this);
    private final HighscoreManager manager = new HighscoreManager(Paths.get("highscores.txt"));

    private boolean saved = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DuckSeason().show());
    }

    private void show() {
        JFrame frame = new JFrame("Duck Season");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        // Make the side panel exactly as tall as the playfield.
        scorePanel.setPreferredSize(new Dimension(300, gamePanel.getPreferredSize().height));

        frame.add(gamePanel, BorderLayout.CENTER);
        frame.add(scorePanel, BorderLayout.EAST);
        frame.pack();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // javax.swing.Timer runs on the Swing thread, so it can safely update the labels.
        scorePanel.refresh(gamePanel, saved);
        new Timer(100, e -> scorePanel.refresh(gamePanel, saved)).start();

        gamePanel.startGameThread();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "start":
                saved = false;
                scorePanel.showMessage(" ");
                gamePanel.startRound();
                break;
            case "pause":
                gamePanel.togglePause();
                break;
            case "save":
                saveScore();
                break;
            case "highscores":
                showHighscores();
                break;
            default:
                break;
        }
        scorePanel.refresh(gamePanel, saved);
    }

    private void saveScore() {
        if (!gamePanel.isFinished() || saved) {
            return;
        }

        String name = scorePanel.getPlayerName();

        if (!Highscore.isValidName(name)) {
            scorePanel.showMessage("Use 1-20 letters, digits, space, - or _");
            return;
        }

        try {
            manager.save(new Highscore(name, gamePanel.getScore(),
                    gamePanel.getHits(), gamePanel.getShots()));
            saved = true;
            scorePanel.showMessage("Score saved!");
        } catch (IOException exception) {
            scorePanel.showMessage("Could not save the score.");
        }
    }

    private void showHighscores() {
        try {
            Highscore[] top = manager.loadTopFive();
            StringBuilder text = new StringBuilder();

            for (int i = 0; i < top.length; i++) {
                text.append(i + 1).append(". ").append(top[i].getName())
                        .append(" - ").append(top[i].getScore())
                        .append(" points (").append(top[i].getHits())
                        .append("/").append(top[i].getShots()).append(" hits)\n");
            }

            if (top.length == 0) {
                text.append("No scores yet.");
            }

            if (manager.getDamagedLines() > 0) {
                text.append("\n").append(manager.getDamagedLines())
                        .append(" damaged line(s) skipped.");
            }

            JOptionPane.showMessageDialog(scorePanel, text.toString(),
                    "Highscores", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException exception) {
            scorePanel.showMessage("Could not read the scores.");
        }
    }
}