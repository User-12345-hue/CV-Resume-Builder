package app.cvbuilder.resume;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ResumeTextFormatterTest {
    private final ResumeTextFormatter formatter = new ResumeTextFormatter();

    @Test
    void formatsEmphasisAndEscapesUserSuppliedHtml() {
        assertEquals("<strong>Java</strong> &amp; <mark>Spring</mark><br>&lt;script&gt;",
                formatter.format("**Java** & ==Spring==\n<script>"));
    }
}
