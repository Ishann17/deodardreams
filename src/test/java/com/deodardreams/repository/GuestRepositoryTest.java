package com.deodardreams.repository;

import com.deodardreams.model.Guest;
import com.deodardreams.testconfig.MySqlTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Starts a focused JPA test context containing repositories and embedded database configuration.
@Import(MySqlTestContainerConfig.class) // Adds our MySQL Test container configuration to the Spring test context.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Prevents @DataJpaTest from replacing our Testcontainers MySQL database with an embedded database.
public class GuestRepositoryTest {

    @Autowired
    private GuestRepository guestRepository;

    @Test
    void shouldFindGuestByPhoneNumber() {
        //Arrange
        Guest testGuest = new Guest();
        testGuest.setFirstName("Somesh");
        testGuest.setLastName("Mehta");
        testGuest.setEmail("somesh.test@example.com");
        testGuest.setPhoneNumber("7778889990");

        guestRepository.save(testGuest);


        //Act
        Optional<Guest> guest = guestRepository.findByPhoneNumber(testGuest.getPhoneNumber());

        //Assert
        assertTrue(guest.isPresent());

        // Verify that the repository returned the guest we saved.
        assertEquals(testGuest.getPhoneNumber(), guest.get().getPhoneNumber());
    }

    @Test
    void shouldReturnEmptyWhenPhoneNumberDoesNotExist() {
        // Arrange
        String phoneNumber = "9990001111";

        // Act
        Optional<Guest> result =
                guestRepository.findByPhoneNumber(phoneNumber);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectDuplicatePhoneNumber() {

        // Arrange
        Guest firstGuest = new Guest();
        firstGuest.setFirstName("Somesh");
        firstGuest.setLastName("Mehta");
        firstGuest.setEmail("somesh.test1@example.com");
        firstGuest.setPhoneNumber("7778889990");

        Guest secondGuest = new Guest();
        secondGuest.setFirstName("Vishal");
        secondGuest.setLastName("Sharma");
        secondGuest.setEmail("vishal.test2@example.com");
        secondGuest.setPhoneNumber("7778889990");

        // Flushes the pending INSERT to MySQL immediately so the database unique constraint is checked during this test.
        guestRepository.saveAndFlush(firstGuest);

        // Act & Assert
        assertThrows(
                DataIntegrityViolationException.class,
                () -> guestRepository.saveAndFlush(secondGuest)
        );
    }
}
