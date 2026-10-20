import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Displays statistics and controls separately from the clickable playfield. */
public class ScorePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Color TEXT_COLOR = new Color(234, 242, 232);

    private final JLabel scoreLabel = new JLabel();
    private final JLabel statistics = new JLabel();
    private final JButton startButton = new JButton("Start Game");
    private final JButton pauseButton = new JButton("Pause (P)");
    private final JButton highscoreButton = new JButton("Highscores");
    private final JTextField nameField = new JTextField();
    private final JButton saveButton = new JButton("Save Score");
    private final JLabel messageLabel = new JLabel(" ");

    public ScorePanel(ActionListener listener) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(300, 768));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 14, 20));
        setBackground(new Color(22, 48, 56));

        JLabel heading = new JLabel("ROUND STATISTICS");
        JPanel buttons = new JPanel(new GridLayout(3, 1, 0, 6));
        JLabel nameLabel = new JLabel("Player name (1-20 characters)");
        JLabel help = new JLabel("<html>Each duck you hit: 100 points<br>"
                + "Click on a duck to shoot it.<br>"
                + "A round lasts 60 seconds.<br>"
                + "Press P to pause.</html>");

        heading.setForeground(new Color(255, 221, 130));
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        scoreLabel.setForeground(TEXT_COLOR);
        scoreLabel.setPreferredSize(new Dimension(260, 48));
        scoreLabel.setMaximumSize(new Dimension(260, 48));
        statistics.setForeground(TEXT_COLOR);
        statistics.setFont(new Font("SansSerif", Font.PLAIN, 15));
        statistics.setPreferredSize(new Dimension(260, 70));
        statistics.setMaximumSize(new Dimension(260, 70));
        configureButton(startButton, "start", listener);
        configureButton(pauseButton, "pause", listener);
        configureButton(highscoreButton, "highscores", listener);
        buttons.add(startButton);
        buttons.add(pauseButton);
        buttons.add(highscoreButton);
        buttons.setOpaque(false);
        buttons.setMaximumSize(new Dimension(260, 100));
        nameLabel.setForeground(TEXT_COLOR);
        nameField.setMaximumSize(new Dimension(260, 27));
        configureButton(saveButton, "save", listener);
        saveButton.setMaximumSize(new Dimension(260, 28));
        messageLabel.setForeground(new Color(255, 221, 130));
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        help.setForeground(TEXT_COLOR);
        help.setFont(new Font("SansSerif", Font.PLAIN, 12));
        add(heading);
        add(scoreLabel);
        add(statistics);
        add(Box.createVerticalStrut(10));
        add(buttons);
        add(Box.createVerticalStrut(14));
        add(nameLabel);
        add(nameField);
        add(Box.createVerticalStrut(5));
        add(saveButton);
        add(Box.createVerticalStrut(5));
        add(messageLabel);
        add(Box.createVerticalGlue());
        add(help);
        for (int i = 0; i < getComponentCount(); i++) {

            Component component = getComponent(i);

            if (component instanceof JComponent) {
                ((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);
            }
        }
    }

    private void configureButton(JButton button, String command, ActionListener listener) {
        button.setActionCommand(command);
        button.addActionListener(listener);
        button.setFocusable(false);
    }

    /** Refreshes labels and enables only actions valid for the current round. */
    public void refresh(GamePanel game, boolean saved) {

        boolean playing = game.isPlaying();
        boolean finished = game.isFinished();

        scoreLabel.setText(game.getScore() + " points");
        statistics.setText("<html>Hits / shots: " + game.getHits() + " / " + game.getShots() + "<br>"
                + "Accuracy: " + String.format("%.1f%%", game.getAccuracy()) + "<br>"
                + "Time left: " + game.getSecondsLeft() + " s</html>");
        startButton.setEnabled(!playing);
        startButton.setText(finished ? "Play Again" : "Start Game");
        pauseButton.setEnabled(playing);
        pauseButton.setText(game.isPaused() ? "Resume (P)" : "Pause (P)");
        nameField.setEnabled(finished && !saved);
        saveButton.setEnabled(finished && !saved);
    }

    /** Returns the player name typed after a round. */
    public String getPlayerName() {
        return nameField.getText();
    }

    /** Shows a short status message, without interpreting it as HTML. */
    public void showMessage(String message) {
        messageLabel.setText(message);
    }
}