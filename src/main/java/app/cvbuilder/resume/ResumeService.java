package app.cvbuilder.resume;

import app.cvbuilder.account.UserAccount;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResumeService {
    private final ResumeRepository resumes;

    public ResumeService(ResumeRepository resumes) {
        this.resumes = resumes;
    }

    @Transactional(readOnly = true)
    public List<Resume> findAll(UserAccount owner) {
        return resumes.findAllByOwnerIdOrderByUpdatedAtDesc(owner.getId());
    }

    @Transactional(readOnly = true)
    public Resume findOwned(Long id, UserAccount owner) {
        return resumes.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(ResumeNotFoundException::new);
    }

    @Transactional
    public Resume create(UserAccount owner) {
        Resume resume = new Resume();
        resume.setOwner(owner);
        resume.setTitle("Untitled resume");
        resume.setFullName(owner.getDisplayName());
        resume.setTemplateId("modern");
        return resumes.save(resume);
    }

    @Transactional
    public Resume save(UserAccount owner, Long id, ResumeForm form) {
        Resume resume = id == null ? new Resume() : findOwned(id, owner);
        if (id == null) {
            resume.setOwner(owner);
        }
        form.update(resume);
        return resumes.save(resume);
    }

    @Transactional
    public void updatePhoto(UserAccount owner, Long id, byte[] photo) {
        Resume resume = findOwned(id, owner);
        resume.setProfilePhoto(photo.clone());
        resumes.save(resume);
    }

    @Transactional
    public void delete(UserAccount owner, Long id) {
        resumes.delete(findOwned(id, owner));
    }
}
