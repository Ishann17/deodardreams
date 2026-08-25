package com.deodardreams.dto;

/**
 * Validates the public enquiry request DTO against its Jakarta Bean Validation rules.
 * This test intentionally does not load Spring or access the database.
 */

import com.deodardreams.dto.request.CreateEnquiryUserRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateEnquiryUserRequestDtoValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldAcceptValidEnquiryRequest() {

        // Arrange
        CreateEnquiryUserRequestDto request = createValidRequest();

        // Act
        Set<ConstraintViolation<CreateEnquiryUserRequestDto>> violations =
                validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectInvalidFirstName() {

        CreateEnquiryUserRequestDto request = createValidRequest();
        request.setFirstName("Somesh123");

        Set<ConstraintViolation<CreateEnquiryUserRequestDto>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
        assertHasViolationForField(violations, "firstName");
    }

    @Test
    void shouldRejectInvalidMobileNumber() {

        CreateEnquiryUserRequestDto request = createValidRequest();
        request.setMobile("12345");

        Set<ConstraintViolation<CreateEnquiryUserRequestDto>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
        assertHasViolationForField(violations, "mobile");
    }

    @Test
    void shouldRejectInvalidEmail() {

        CreateEnquiryUserRequestDto request = createValidRequest();
        request.setEmail("invalid-email");

        Set<ConstraintViolation<CreateEnquiryUserRequestDto>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
        assertHasViolationForField(violations, "email");
    }

    @Test
    void shouldRejectZeroRooms() {

        CreateEnquiryUserRequestDto request = createValidRequest();
        request.setNumberOfRooms(0);

        Set<ConstraintViolation<CreateEnquiryUserRequestDto>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
        assertHasViolationForField(violations, "numberOfRooms");
    }

    @Test
    void shouldRejectZeroAdults() {

        CreateEnquiryUserRequestDto request = createValidRequest();
        request.setNumberOfAdults(0);

        Set<ConstraintViolation<CreateEnquiryUserRequestDto>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
        assertHasViolationForField(violations, "numberOfAdults");
    }

    @Test
    void shouldRejectNegativeChildren() {

        CreateEnquiryUserRequestDto request = createValidRequest();
        request.setChildrenBelow12(-1);

        Set<ConstraintViolation<CreateEnquiryUserRequestDto>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
        assertHasViolationForField(violations, "childrenBelow12");
    }

    @Test
    void shouldRejectPastCheckInDate() {

        CreateEnquiryUserRequestDto request = createValidRequest();
        request.setCheckIn(LocalDate.now().minusDays(1));

        Set<ConstraintViolation<CreateEnquiryUserRequestDto>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
        assertHasViolationForField(violations, "checkIn");
    }

    @Test
    void shouldRejectTodayAsCheckOutDate() {

        CreateEnquiryUserRequestDto request = createValidRequest();
        request.setCheckOut(LocalDate.now());

        Set<ConstraintViolation<CreateEnquiryUserRequestDto>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
        assertHasViolationForField(violations, "checkOut");
    }

    private CreateEnquiryUserRequestDto createValidRequest() {

        CreateEnquiryUserRequestDto request =
                new CreateEnquiryUserRequestDto();

        request.setFirstName("Somesh");
        request.setLastName("Mehta");
        request.setMobile("7778889990");
        request.setEmail("somesh@example.com");
        request.setNumberOfRooms(2);
        request.setNumberOfAdults(4);
        request.setChildrenBelow12(1);
        request.setCheckIn(LocalDate.now().plusDays(5));
        request.setCheckOut(LocalDate.now().plusDays(8));
        request.setEnquiryMessage("Looking for a family stay.");

        return request;
    }

    private void assertHasViolationForField(
            Set<ConstraintViolation<CreateEnquiryUserRequestDto>> violations,
            String fieldName) {

        assertTrue(
                violations.stream()
                        .anyMatch(violation ->
                                violation.getPropertyPath()
                                        .toString()
                                        .equals(fieldName)
                        )
        );
    }
}