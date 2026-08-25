package com.deodardreams.service;

/**
 * Unit tests for EnquiryUserServiceImpl.
 * Verifies service orchestration without loading Spring or connecting
 * to MySQL. Mapper and repository are mocked because their behavior
 * is tested separately.
 */

import com.deodardreams.dto.request.CreateEnquiryUserRequestDto;
import com.deodardreams.dto.response.EnquiryUserResponseDto;
import com.deodardreams.mapper.EnquiryUserMapper;
import com.deodardreams.model.EnquiryUser;
import com.deodardreams.repository.EnquiryUserRepository;
import com.deodardreams.service.enquiry.EnquiryUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnquiryUserServiceTest {

    @Mock
    private EnquiryUserMapper enquiryUserMapper;

    @Mock
    private EnquiryUserRepository enquiryUserRepository;

    @InjectMocks
    private EnquiryUserServiceImpl enquiryUserService;

    private CreateEnquiryUserRequestDto request;
    private EnquiryUser enquiryUser;
    private EnquiryUser savedEnquiryUser;
    private EnquiryUserResponseDto response;

    @BeforeEach
    void setUp() {
        request = new CreateEnquiryUserRequestDto();

        enquiryUser = new EnquiryUser();
        savedEnquiryUser = new EnquiryUser();

        //response = new EnquiryUserResponseDto();
    }

    @Test
    void shouldCreateEnquirySuccessfully() {

        // Arrange
        when(enquiryUserMapper.toEnquiryUserEntity(request))
                .thenReturn(enquiryUser);

        when(enquiryUserRepository.save(enquiryUser))
                .thenReturn(savedEnquiryUser);

        /*when(enquiryUserMapper.toEnquiryUserResponse(savedEnquiryUser))
                .thenReturn(response);*/

        // Act
        EnquiryUserResponseDto result =
                enquiryUserService.createEnquiry(request);

        // Assert
        assertNotNull(result);
        assertEquals(response, result);

        verify(enquiryUserMapper).toEnquiryUserEntity(request);
        verify(enquiryUserRepository).save(enquiryUser);
        //verify(enquiryUserMapper).toEnquiryUserResponse(savedEnquiryUser);

        verifyNoMoreInteractions(
                enquiryUserMapper,
                enquiryUserRepository
        );
    }

    @Test
    void shouldPropagateExceptionWhenSavingEnquiryFails() {

        // Arrange
        when(enquiryUserMapper.toEnquiryUserEntity(request))
                .thenReturn(enquiryUser);

        RuntimeException exception =
                new RuntimeException("Database unavailable");

        when(enquiryUserRepository.save(enquiryUser))
                .thenThrow(exception);

        // Act & Assert
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> enquiryUserService.createEnquiry(request)
        );

        assertEquals("Database unavailable", thrown.getMessage());

        verify(enquiryUserMapper).toEnquiryUserEntity(request);
        verify(enquiryUserRepository).save(enquiryUser);

        /*verify(enquiryUserMapper, never())
                .toEnquiryUserResponse(any());*/

        verifyNoMoreInteractions(
                enquiryUserMapper,
                enquiryUserRepository
        );
    }
}