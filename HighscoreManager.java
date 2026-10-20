import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Reads and writes UTF-8 text: name, score, hits and shots separated by tabs.
 * File handling is one of the two research topics from the backlog.
 */
public class HighscoreManager {

    private final Path file;
    private int damagedLines;

    public HighscoreManager(Path file) {
        this.file = file.toAbsolutePath();
    }

    /** Loads valid entries, skipping damaged lines; a missing file is an empty list. */
    public Highscore[] load() throws IOException {

        Highscore[] entries = new Highscore[0];

        damagedLines = 0;
        if (!Files.exists(file)) {
            return entries;
        }
        // try-with-resources closes the reader, even when an exception occurs.
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {

            String line = reader.readLine();

            while (line != null) {

                String[] parts = line.split("\t", -1);

                try {
                    if (parts.length != 4) {
                        throw new IllegalArgumentException("Expected four columns.");
                    }

                    Highscore entry = new Highscore(parts[0], Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));

                    entries = append(entries, entry);
                } catch (IllegalArgumentException exception) {
                    damagedLines++;
                }
                line = reader.readLine();
            }
        }
        sortDescending(entries);
        return entries;
    }

    /** Saves an entry, preserving valid older results and reporting IO failures. */
    public void save(Highscore entry) throws IOException {

        Highscore[] entries = append(load(), entry);
        Path parent = file.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path temporaryFile = Files.createTempFile(parent != null ? parent : Path.of("."), "scores-", ".tmp");

        // Write a separate file first, so a failed write cannot truncate the old scores.
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temporaryFile,
                    StandardCharsets.UTF_8)) {
                for (int i = 0; i < entries.length; i++) {
                    writer.write(entries[i].getName() + "\t" + entries[i].getScore()
                            + "\t" + entries[i].getHits() + "\t" + entries[i].getShots());
                    writer.newLine();
                }
            }
            try {
                Files.move(temporaryFile, file, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private Highscore[] append(Highscore[] entries, Highscore entry) {

        Highscore[] expanded = new Highscore[entries.length + 1];

        for (int i = 0; i < entries.length; i++) {
            expanded[i] = entries[i];
        }
        expanded[entries.length] = entry;
        return expanded;
    }

    private void sortDescending(Highscore[] entries) {
        for (int i = 0; i < entries.length - 1; i++) {
            for (int j = i + 1; j < entries.length; j++) {
                if (entries[j].getScore() > entries[i].getScore()) {

                    Highscore temporary = entries[i];

                    entries[i] = entries[j];
                    entries[j] = temporary;
                }
            }
        }
    }

    /** Returns at most five entries, ordered from highest to lowest score. */
    public Highscore[] loadTopFive() throws IOException {

        Highscore[] entries = load();
        Highscore[] top = new Highscore[Math.min(5, entries.length)];

        for (int i = 0; i < top.length; i++) {
            top[i] = entries[i];
        }
        return top;
    }

    /** Returns how many malformed lines were skipped in the last successful load. */
    public int getDamagedLines() {
        return damagedLines;
    }
}