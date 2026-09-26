package app.cvbuilder.resume;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ResumeFormTest {
    @Test
    void trimsRequiredValuesAndClearsBlankOptionalValues() {
        ResumeForm form = new ResumeForm();
        form.setTitle("  Designer CV  ");
        form.setFullName("  Avery Example ");
        form.setProfessionalTitle("  ");
        form.setEmail("  avery@example.com ");
        form.setTemplateId("classic");
        Resume resume = new Resume();

        form.update(resume);

        assertEquals("Designer CV", resume.getTitle());
        assertEquals("Avery Example", resume.getFullName());
        assertNull(resume.getProfessionalTitle());
        assertEquals("avery@example.com", resume.getEmail());
        assertEquals("classic", resume.getTemplateId());
    }
}
