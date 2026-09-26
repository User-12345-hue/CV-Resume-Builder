package app.cvbuilder.resume;

import app.cvbuilder.account.UserAccount;
import jakarta.validation.Valid;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ResumeController {
    private static final long MAX_PHOTO_BYTES = 5 * 1024 * 1024;
    private static final long MAX_PHOTO_PIXELS = 20_000_000;
    private static final int PHOTO_MAX_EDGE = 1200;

    private final ResumeService resumes;
    private final ResumeTextFormatter formatter;

    public ResumeController(ResumeService resumes, ResumeTextFormatter formatter) {
        this.resumes = resumes;
        this.formatter = formatter;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserAccount user, Model model) {
        model.addAttribute("resumes", resumes.findAll(user));
        model.addAttribute("displayName", user.getDisplayName());
        return "dashboard";
    }

    @GetMapping("/resumes/new")
    public String newResume(Model model) {
        model.addAttribute("form", new ResumeForm());
        model.addAttribute("resumeId", null);
        model.addAttribute("postUrl", "/resumes");
        model.addAttribute("templates", ResumeTemplate.options());
        return "resume-editor";
    }

    @PostMapping("/resumes")
    public String createResume(
            @AuthenticationPrincipal UserAccount user,
            @Valid @ModelAttribute("form") ResumeForm form,
            BindingResult bindingResult,
            Model model) {
        if (!ResumeTemplate.contains(form.getTemplateId())) {
            bindingResult.rejectValue("templateId", "template.invalid", "Choose one of the available templates.");
        }
        if (bindingResult.hasErrors()) {
            prepareEditor(model, null, user);
            return "resume-editor";
        }
        Resume resume = resumes.save(user, null, form);
        return "redirect:/resumes/" + resume.getId() + "/edit?saved";
    }

    @GetMapping("/resumes/{id}/edit")
    public String editResume(
            @AuthenticationPrincipal UserAccount user,
            @PathVariable Long id,
            Model model) {
        Resume resume = findOwnedOr404(id, user);
        model.addAttribute("form", ResumeForm.from(resume));
        model.addAttribute("resumeId", id);
        model.addAttribute("postUrl", "/resumes/" + id);
        model.addAttribute("resume", resume);
        model.addAttribute("templates", ResumeTemplate.options());
        return "resume-editor";
    }

    @PostMapping("/resumes/{id}")
    public String updateResume(
            @AuthenticationPrincipal UserAccount user,
            @PathVariable Long id,
            @Valid @ModelAttribute("form") ResumeForm form,
            BindingResult bindingResult,
            Model model) {
        if (!ResumeTemplate.contains(form.getTemplateId())) {
            bindingResult.rejectValue("templateId", "template.invalid", "Choose one of the available templates.");
        }
        if (bindingResult.hasErrors()) {
            prepareEditor(model, id, user);
            return "resume-editor";
        }
        resumes.save(user, id, form);
        return "redirect:/resumes/" + id + "/edit?saved";
    }

    @GetMapping("/resumes/{id}/preview")
    public String preview(
            @AuthenticationPrincipal UserAccount user,
            @PathVariable Long id,
            Model model) {
        Resume resume = findOwnedOr404(id, user);
        model.addAttribute("resume", resume);
        model.addAttribute("formattedSummary", formatter.format(resume.getSummary()));
        model.addAttribute("formattedExperience", formatter.format(resume.getExperience()));
        model.addAttribute("formattedEducation", formatter.format(resume.getEducation()));
        model.addAttribute("formattedSkills", formatter.format(resume.getSkills()));
        model.addAttribute("formattedProjects", formatter.format(resume.getProjects()));
        model.addAttribute("formattedCertifications", formatter.format(resume.getCertifications()));
        model.addAttribute("formattedAdditional", formatter.format(resume.getAdditional()));
        return "resume-preview";
    }

    @PostMapping("/resumes/{id}/delete")
    public String deleteResume(@AuthenticationPrincipal UserAccount user, @PathVariable Long id) {
        resumes.delete(user, id);
        return "redirect:/dashboard?deleted";
    }

    @PostMapping("/resumes/{id}/photo")
    public String uploadPhoto(
            @AuthenticationPrincipal UserAccount user,
            @PathVariable Long id,
            @RequestParam("photo") MultipartFile photo,
            RedirectAttributes redirectAttributes) {
        try {
            if (photo.isEmpty() || photo.getSize() > MAX_PHOTO_BYTES) {
                throw new IllegalArgumentException("Choose an image smaller than 5 MB.");
            }
            resumes.updatePhoto(user, id, normalizePhoto(photo.getBytes()));
            redirectAttributes.addFlashAttribute("photoMessage", "Photo updated.");
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The image could not be read.", exception);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("photoError", exception.getMessage());
        }
        return "redirect:/resumes/" + id + "/edit";
    }

    @GetMapping("/resumes/{id}/photo")
    public ResponseEntity<byte[]> photo(
            @AuthenticationPrincipal UserAccount user,
            @PathVariable Long id) {
        byte[] bytes = findOwnedOr404(id, user).getProfilePhoto();
        if (bytes == null || bytes.length == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.noCache().cachePrivate())
                .header("X-Content-Type-Options", "nosniff")
                .body(bytes);
    }

    private Resume findOwnedOr404(Long id, UserAccount user) {
        return resumes.findOwned(id, user);
    }

    private void prepareEditor(Model model, Long id, UserAccount user) {
        model.addAttribute("resumeId", id);
        model.addAttribute("postUrl", id == null ? "/resumes" : "/resumes/" + id);
        model.addAttribute("templates", ResumeTemplate.options());
        if (id != null) {
            model.addAttribute("resume", findOwnedOr404(id, user));
        }
    }

    private static byte[] normalizePhoto(byte[] source) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(source))) {
            if (input == null) {
                throw new IllegalArgumentException("Upload a PNG, JPEG, or GIF image.");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Upload a PNG, JPEG, or GIF image.");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!Set.of("png", "jpeg", "gif").contains(format)) {
                    throw new IllegalArgumentException("Upload a PNG, JPEG, or GIF image.");
                }
                int originalWidth = reader.getWidth(0);
                int originalHeight = reader.getHeight(0);
                long pixels = (long) originalWidth * originalHeight;
                if (originalWidth <= 0 || originalHeight <= 0 || pixels > MAX_PHOTO_PIXELS) {
                    throw new IllegalArgumentException("The image dimensions are too large.");
                }
                BufferedImage original = reader.read(0);
                if (original == null) {
                    throw new IllegalArgumentException("The image could not be decoded.");
                }
                return resizeToPng(original);
            } finally {
                reader.dispose();
            }
        }
    }

    private static byte[] resizeToPng(BufferedImage original) throws IOException {
        double scale = Math.min(1.0, (double) PHOTO_MAX_EDGE
                / Math.max(original.getWidth(), original.getHeight()));
        int width = Math.max(1, (int) Math.round(original.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(original.getHeight() * scale));
        BufferedImage normalized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = normalized.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(original, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (!ImageIO.write(normalized, "png", output)) {
                throw new IOException("No PNG image encoder is available.");
            }
            return output.toByteArray();
        }
    }
}
