package app.cvbuilder.resume;

import org.springframework.stereotype.Component;

@Component("resumeFormatter")
public class ResumeTextFormatter {
    public String format(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return escapeHtml(text)
                .replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>")
                .replaceAll("==(.+?)==", "<mark>$1</mark>")
                .replaceAll("\\r\\n|\\r|\\n", "<br>");
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
