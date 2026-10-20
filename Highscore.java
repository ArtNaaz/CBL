/** A saved player name and the results of one completed round. */
public class Highscore {

    private final String name;
    private final int score;
    private final int hits;
    private final int shots;

    public Highscore(String name, int score, int hits, int shots) {
        if (!isValidName(name) || score < 0 || hits < 0 || shots < hits) {
            throw new IllegalArgumentException("Invalid highscore.");
        }
        this.name = name.trim();
        this.score = score;
        this.hits = hits;
        this.shots = shots;
    }

    /** Accepts 1-20 letters, digits, spaces, hyphens or underscores after trimming. */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty() || name.trim().length() > 20) {
            return false;
        }

        String trimmed = name.trim();

        for (int i = 0; i < trimmed.length(); i++) {

            char character = trimmed.charAt(i);

            if (!Character.isLetterOrDigit(character) && character != ' '
                    && character != '-' && character != '_') {
                return false;
            }
        }
        return true;
    }

    /** Returns the player's name. */
    public String getName() {
        return name;
    }

    /** Returns the saved score. */
    public int getScore() {
        return score;
    }

    /** Returns the saved hit count. */
    public int getHits() {
        return hits;
    }

    /** Returns the saved shot count. */
    public int getShots() {
        return shots;
    }
}