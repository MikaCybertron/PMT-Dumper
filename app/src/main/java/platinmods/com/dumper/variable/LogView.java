package platinmods.com.dumper.variable;

import android.text.Html;

import androidx.annotation.NonNull;

public class LogView {

    private StringBuilder log;

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

    public void appendSuccess(String text) {
        appendLine("[SUCCESS] " + text);
    }

    public void appendInfo(String text) {
        appendLine("[INFO] " + text);
    }

    public void appendWarning(String text) {
        appendLine("[WARNING] " + text);
    }

    public void appendError(String text) {
        appendLine("[ERROR] " + text);
    }

    @Override
    public String toString() {
        return log.toString();
    }
}
