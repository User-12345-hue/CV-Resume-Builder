package app.cvbuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.cvbuilder.account.UserAccountRepository;
import app.cvbuilder.resume.Resume;
import app.cvbuilder.resume.ResumeRepository;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:cvbuilder;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "spring.thymeleaf.cache=false"
})
class WebApplicationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ResumeRepository resumes;

    @Autowired
    private UserAccountRepository users;

    @BeforeEach
    void clearDatabase() {
        resumes.deleteAll();
        users.deleteAll();
    }

    @Test
    void userCanRegisterCreateResumeAndSeeItInDashboardAndPreview() throws Exception {
        MockHttpSession session = registerAndLogin("avery@example.com", "Avery Example");

        mvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Good to see you, Avery Example")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Your first resume")));

        mvc.perform(post("/resumes")
                        .session(session)
                        .with(csrf())
                        .param("title", "Product designer")
                        .param("fullName", "Avery Example")
                        .param("professionalTitle", "Product designer")
                        .param("email", "avery@example.com")
                        .param("summary", "I **design** thoughtful, ==accessible== products. <script>")
                        .param("experience", "Product designer · Acme · 2022–2025")
                        .param("templateId", "classic"))
                .andExpect(status().is3xxRedirection());

        Resume saved = resumes.findAll().get(0);
        mvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Product designer")));
        mvc.perform(get("/resumes/{id}/preview", saved.getId()).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "I <strong>design</strong> thoughtful, <mark>accessible</mark> products. &lt;script&gt;")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Print / Save PDF")));

        ByteArrayOutputStream image = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", image);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/resumes/{id}/photo", saved.getId())
                        .file(new MockMultipartFile("photo", "portrait.png", "image/png", image.toByteArray()))
                        .session(session)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/resumes/{id}/photo", saved.getId()).session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));
    }

    @Test
    void accountCannotReadAnotherUsersResume() throws Exception {
        MockHttpSession ownerSession = registerAndLogin("owner@example.com", "Resume Owner");
        mvc.perform(post("/resumes")
                        .session(ownerSession)
                        .with(csrf())
                        .param("title", "Private resume")
                        .param("fullName", "Resume Owner")
                        .param("templateId", "modern"))
                .andExpect(status().is3xxRedirection());
        Long resumeId = resumes.findAll().get(0).getId();

        MockHttpSession otherSession = registerAndLogin("other@example.com", "Different User");
        mvc.perform(get("/resumes/{id}/preview", resumeId).session(otherSession))
                .andExpect(status().isNotFound());
    }

    private MockHttpSession registerAndLogin(String email, String displayName) throws Exception {
        mvc.perform(post("/register")
                        .with(csrf())
                        .param("displayName", displayName)
                        .param("email", email)
                        .param("password", "a secure test passphrase"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        MvcResult login = mvc.perform(post("/login")
                        .with(csrf())
                        .param("username", email)
                        .param("password", "a secure test passphrase"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andReturn();
        return (MockHttpSession) login.getRequest().getSession(false);
    }
}
