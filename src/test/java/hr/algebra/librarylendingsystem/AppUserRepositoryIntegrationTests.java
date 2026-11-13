package hr.algebra.librarylendingsystem;

import hr.algebra.librarylendingsystem.domain.AppUser;
import hr.algebra.librarylendingsystem.domain.Role;
import hr.algebra.librarylendingsystem.repository.AppUserRepository;
import hr.algebra.librarylendingsystem.repository.RoleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AppUserRepositoryIntegrationTests {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndRetrieveAppUser() {
        AppUser user = createUser();

        user = appUserRepository.saveAndFlush(user);

        Long userId = user.getId();

        entityManager.clear();

        AppUser savedUser = appUserRepository
                .findById(userId)
                .orElseThrow();

        assertEquals(userId, savedUser.getId());
        assertEquals("Test", savedUser.getFirstName());
        assertEquals("Member", savedUser.getLastName());
        assertEquals("test-password-hash", savedUser.getPasswordHash());

        assertTrue(savedUser.isEnabled());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    void shouldFindUserByEmailIgnoringCase() {
        AppUser user = appUserRepository.saveAndFlush(
                createUser()
        );

        String uppercaseEmail = user.getEmail().toUpperCase();

        entityManager.clear();

        AppUser foundUser = appUserRepository
                .findByEmailIgnoreCase(uppercaseEmail)
                .orElseThrow();

        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getEmail(), foundUser.getEmail());
    }

    @Test
    void shouldCheckEmailExistenceIgnoringCase() {
        AppUser user = appUserRepository.saveAndFlush(
                createUser()
        );

        assertTrue(
                appUserRepository.existsByEmailIgnoreCase(
                        user.getEmail().toUpperCase()
                )
        );

        assertFalse(
                appUserRepository.existsByEmailIgnoreCase(
                        "missing-" + UUID.randomUUID() + "@example.com"
                )
        );
    }

    @Test
    void shouldRejectDuplicateEmailIgnoringCase() {
        String email = uniqueEmail();

        AppUser firstUser = new AppUser(
                email,
                "first-password-hash",
                "First",
                "Member"
        );

        appUserRepository.saveAndFlush(firstUser);

        AppUser secondUser = new AppUser(
                email.toUpperCase(),
                "second-password-hash",
                "Second",
                "Member"
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> appUserRepository.saveAndFlush(secondUser)
        );
    }

    @Test
    void shouldFindSeededRoles() {
        Role member = roleRepository
                .findByName("MEMBER")
                .orElseThrow();

        Role librarian = roleRepository
                .findByName("LIBRARIAN")
                .orElseThrow();

        Role admin = roleRepository
                .findByName("ADMIN")
                .orElseThrow();

        assertNotNull(member.getId());
        assertNotNull(librarian.getId());
        assertNotNull(admin.getId());

        assertEquals("MEMBER", member.getName());
        assertEquals("LIBRARIAN", librarian.getName());
        assertEquals("ADMIN", admin.getName());
    }

    @Test
    void shouldPersistUserRoleAssociations() {
        Role memberRole = roleRepository
                .findByName("MEMBER")
                .orElseThrow();

        Role librarianRole = roleRepository
                .findByName("LIBRARIAN")
                .orElseThrow();

        AppUser user = createUser();

        user.addRole(memberRole);
        user.addRole(librarianRole);

        user = appUserRepository.saveAndFlush(user);

        Long userId = user.getId();

        entityManager.clear();

        AppUser savedUser = appUserRepository
                .findById(userId)
                .orElseThrow();

        assertEquals(2, savedUser.getRoles().size());

        assertTrue(
                savedUser.getRoles().stream()
                        .anyMatch(role ->
                                "MEMBER".equals(role.getName())
                        )
        );

        assertTrue(
                savedUser.getRoles().stream()
                        .anyMatch(role ->
                                "LIBRARIAN".equals(role.getName())
                        )
        );
    }

    @Test
    void shouldRemoveRoleAssociationWithoutDeletingRole() {
        Role memberRole = roleRepository
                .findByName("MEMBER")
                .orElseThrow();

        AppUser user = createUser();

        user.addRole(memberRole);

        user = appUserRepository.saveAndFlush(user);

        Long userId = user.getId();
        Long roleId = memberRole.getId();

        entityManager.clear();

        AppUser savedUser = appUserRepository
                .findById(userId)
                .orElseThrow();

        assertEquals(1, savedUser.getRoles().size());

        Role assignedRole = savedUser.getRoles()
                .iterator()
                .next();

        savedUser.removeRole(assignedRole);

        appUserRepository.saveAndFlush(savedUser);

        entityManager.clear();

        AppUser updatedUser = appUserRepository
                .findById(userId)
                .orElseThrow();

        assertTrue(updatedUser.getRoles().isEmpty());

        assertTrue(roleRepository.existsById(roleId));
    }

    @Test
    void shouldFetchUserTogetherWithRoles() {
        Role memberRole = roleRepository
                .findByName("MEMBER")
                .orElseThrow();

        AppUser user = createUser();

        user.addRole(memberRole);

        user = appUserRepository.saveAndFlush(user);

        String email = user.getEmail();

        entityManager.clear();

        AppUser foundUser = appUserRepository
                .findWithRolesByEmailIgnoreCase(
                        email.toUpperCase()
                )
                .orElseThrow();

        assertTrue(
                entityManager.getEntityManagerFactory()
                        .getPersistenceUnitUtil()
                        .isLoaded(foundUser, "roles")
        );

        assertEquals(1, foundUser.getRoles().size());

        assertEquals(
                "MEMBER",
                foundUser.getRoles().iterator().next().getName()
        );
    }

    private AppUser createUser() {
        return new AppUser(
                uniqueEmail(),
                "test-password-hash",
                "Test",
                "Member"
        );
    }

    private String uniqueEmail() {
        return "test-" + UUID.randomUUID() + "@example.com";
    }
}