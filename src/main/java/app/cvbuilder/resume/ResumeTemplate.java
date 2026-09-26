package app.cvbuilder.resume;

import java.util.List;

public enum ResumeTemplate {
    MODERN("modern", "Modern", "A crisp, contemporary layout with a bold accent."),
    CLASSIC("classic", "Classic", "A traditional, clean format for any industry."),
    MINIMAL("minimal", "Minimal", "A quiet, typography-first design.");

    private final String id;
    private final String label;
    private final String description;

    ResumeTemplate(String id, String label, String description) {
        this.id = id;
        this.label = label;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public static List<ResumeTemplate> options() {
        return List.of(values());
    }

    public static boolean contains(String id) {
        for (ResumeTemplate template : values()) {
            if (template.id.equals(id)) {
                return true;
            }
        }
        return false;
    }
}
