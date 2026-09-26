package app.cvbuilder.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private UserAccountRepository users;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountService accounts;

    @Test
    void normalizesEmailAndStoresOnlyEncodedPassword() {
        when(users.existsByEmail("person@example.com")).thenReturn(false);
        when(passwordEncoder.encode("a long passphrase")).thenReturn("encoded-password");
        when(users.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserAccount result = accounts.register(" Person@Example.com ", "  Person  ", "a long passphrase");

        assertEquals("person@example.com", result.getEmail());
        assertEquals("Person", result.getDisplayName());
        assertEquals("encoded-password", result.getPassword());
        verify(passwordEncoder).encode("a long passphrase");
    }

    @Test
    void rejectsAnEmailThatIsAlreadyRegistered() {
        when(users.existsByEmail("person@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyRegisteredException.class,
                () -> accounts.register("person@example.com", "Person", "a long passphrase"));
    }
}
