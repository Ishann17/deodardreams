package com.deodardreams.mapper;

/**
 * Maps enquiry request/response DTOs to and from the EnquiryUser entity.
 *
 * MapStruct generates the implementation at compile time, keeping mapping
 * logic out of the service layer.
 */

import com.deodardreams.dto.request.CreateEnquiryUserRequestDto;
import com.deodardreams.dto.response.EnquiryAlertResponseDto;
import com.deodardreams.dto.response.EnquiryUserResponseDto;
import com.deodardreams.model.EnquiryUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring") // Registers the generated MapStruct implementation as a Spring bean for dependency injection.
public interface EnquiryUserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    EnquiryUser toEnquiryUserEntity(CreateEnquiryUserRequestDto requestDto);

    //EnquiryUserResponseDto toEnquiryUserResponse(EnquiryUser enquiryUser);

    // Straight field-for-field copy — every EnquiryAlertResponseDto field name
    // matches EnquiryUser's exactly, so no @Mapping hints are needed here.
    // Nulls are copied as-is on purpose (see EmailServiceImpl for where
    // null/blank fields get skipped when the actual email text is built).
    EnquiryAlertResponseDto toEnquiryAlertResponseDto(EnquiryUser enquiryUser);
}
