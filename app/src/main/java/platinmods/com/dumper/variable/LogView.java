package platinmods.com.dumper.variable;

import androidx.annotation.NonNull;

public class LogView {

    private final StringBuilder log;

    public LogView() {
        this.log = new StringBuilder();
    }

    public void append(String text) {
        log.append(text);
    }

    public void appendLine() {
        append("\n");
    }

    public void appendLine(String text) {
        append(text + "\n");
    }

    public void appendInfo(String text) {
        appendLine("[INFO] " + text);
    }

    public void appendError(String text) {
        appendLine("[ERROR] " + text);
    }

    @NonNull
    @Override
    public String toString() {
        return log.toString();
    }
}
