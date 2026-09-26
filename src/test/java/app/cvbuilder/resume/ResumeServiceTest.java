package app.cvbuilder.resume;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.cvbuilder.account.UserAccount;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {
    @Mock
    private ResumeRepository repository;

    @Mock
    private UserAccount owner;

    @InjectMocks
    private ResumeService service;

    @Test
    void listsResumesOnlyForTheCurrentOwner() {
        List<Resume> expected = List.of(new Resume());
        when(owner.getId()).thenReturn(12L);
        when(repository.findAllByOwnerIdOrderByUpdatedAtDesc(12L)).thenReturn(expected);

        assertSame(expected, service.findAll(owner));
        verify(repository).findAllByOwnerIdOrderByUpdatedAtDesc(12L);
    }

    @Test
    void refusesToReturnAResumeOwnedByAnotherAccount() {
        when(owner.getId()).thenReturn(12L);
        when(repository.findByIdAndOwnerId(44L, 12L)).thenReturn(Optional.empty());

        assertThrows(ResumeNotFoundException.class, () -> service.findOwned(44L, owner));
    }

    @Test
    void createsResumesForTheSignedInOwner() {
        when(owner.getDisplayName()).thenReturn("Avery");
        when(repository.save(any(Resume.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Resume result = service.create(owner);

        assertSame(owner, result.getOwner());
        assertEquals("Untitled resume", result.getTitle());
        assertEquals("Avery", result.getFullName());
        assertEquals("modern", result.getTemplateId());
    }

    @Test
    void doesNotAllowUpdatingAnotherOwnersResume() {
        when(owner.getId()).thenReturn(12L);
        when(repository.findByIdAndOwnerId(44L, 12L)).thenReturn(Optional.empty());

        assertThrows(ResumeNotFoundException.class,
                () -> service.save(owner, 44L, new ResumeForm()));
    }
}
