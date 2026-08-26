package com.deodardreams.service.enquiry;

import com.deodardreams.dto.request.CreateEnquiryUserRequestDto;
import com.deodardreams.dto.response.EnquiryAlertResponseDto;
import com.deodardreams.dto.response.EnquiryUserResponseDto;
import com.deodardreams.mapper.EnquiryUserMapper;
import com.deodardreams.model.EnquiryUser;
import com.deodardreams.repository.EnquiryUserRepository;
import com.deodardreams.service.mail.EmailService;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EnquiryUserServiceImpl implements EnquiryUserService{

    private final EnquiryUserMapper enquiryUserMapper;
    private final EnquiryUserRepository enquiryUserRepository;
    private final EmailService emailService;

    public EnquiryUserServiceImpl(EnquiryUserMapper enquiryUserMapper, EnquiryUserRepository enquiryUserRepository, EmailService emailService) {
        this.enquiryUserMapper = enquiryUserMapper;
        this.enquiryUserRepository = enquiryUserRepository;
        this.emailService = emailService;
    }

    @Timed("enquiry.create") // Measures execution time of the enquiry creation operation for production monitoring.
    @Override
    public EnquiryUserResponseDto createEnquiry(CreateEnquiryUserRequestDto request) {
        log.info("Creating enquiry");

        EnquiryUser enquiryUserEntity = enquiryUserRepository.save(enquiryUserMapper.toEnquiryUserEntity(request));
        log.info("Enquiry saved successfully with id={}", enquiryUserEntity.getId());
        createEnquiryAlertToAdmin(enquiryUserEntity);
        emailService.sendEnquiryAcknowledgement(enquiryUserEntity.getEmail(),enquiryUserEntity.getFirstName());

        return new EnquiryUserResponseDto(
                "Thank you, %s. We have received your enquiry and will get back to you shortly."
                        .formatted(enquiryUserEntity.getFirstName())
        );
    }

    @Override
    public void createEnquiryAlertToAdmin(EnquiryUser enquiryUser) {
        EnquiryAlertResponseDto enquiryAlertResponseDto = enquiryUserMapper.toEnquiryAlertResponseDto(enquiryUser);
        emailService.sendEnquiryAlertToAdmin(enquiryAlertResponseDto);
    }
}
