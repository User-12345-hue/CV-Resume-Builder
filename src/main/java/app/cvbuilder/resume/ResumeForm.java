package app.cvbuilder.resume;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResumeForm {
    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    @Size(max = 120)
    private String fullName;

    @Size(max = 120)
    private String professionalTitle;

    @Email
    @Size(max = 254)
    private String email;

    @Size(max = 60)
    private String phone;

    @Size(max = 200)
    private String location;

    @Size(max = 254)
    private String website;

    @Size(max = 10000)
    private String summary;

    @Size(max = 20000)
    private String experience;

    @Size(max = 20000)
    private String education;

    @Size(max = 10000)
    private String skills;

    @Size(max = 20000)
    private String projects;

    @Size(max = 10000)
    private String certifications;

    @Size(max = 10000)
    private String additional;

    @NotBlank
    private String templateId = "modern";

    public static ResumeForm from(Resume resume) {
        ResumeForm form = new ResumeForm();
        form.title = resume.getTitle();
        form.fullName = resume.getFullName();
        form.professionalTitle = resume.getProfessionalTitle();
        form.email = resume.getEmail();
        form.phone = resume.getPhone();
        form.location = resume.getLocation();
        form.website = resume.getWebsite();
        form.summary = resume.getSummary();
        form.experience = resume.getExperience();
        form.education = resume.getEducation();
        form.skills = resume.getSkills();
        form.projects = resume.getProjects();
        form.certifications = resume.getCertifications();
        form.additional = resume.getAdditional();
        form.templateId = resume.getTemplateId();
        return form;
    }

    public void update(Resume resume) {
        resume.setTitle(title.trim());
        resume.setFullName(fullName.trim());
        resume.setProfessionalTitle(clean(professionalTitle));
        resume.setEmail(clean(email));
        resume.setPhone(clean(phone));
        resume.setLocation(clean(location));
        resume.setWebsite(clean(website));
        resume.setSummary(clean(summary));
        resume.setExperience(clean(experience));
        resume.setEducation(clean(education));
        resume.setSkills(clean(skills));
        resume.setProjects(clean(projects));
        resume.setCertifications(clean(certifications));
        resume.setAdditional(clean(additional));
        resume.setTemplateId(templateId);
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getProfessionalTitle() { return professionalTitle; }
    public void setProfessionalTitle(String professionalTitle) { this.professionalTitle = professionalTitle; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getProjects() { return projects; }
    public void setProjects(String projects) { this.projects = projects; }
    public String getCertifications() { return certifications; }
    public void setCertifications(String certifications) { this.certifications = certifications; }
    public String getAdditional() { return additional; }
    public void setAdditional(String additional) { this.additional = additional; }
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
}
