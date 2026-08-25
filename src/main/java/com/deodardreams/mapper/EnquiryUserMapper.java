package com.deodardreams.mapper;

/**
 * Maps enquiry request/response DTOs to and from the EnquiryUser entity.
 *
 * MapStruct generates the implementation at compile time, keeping mapping
 * logic out of the service layer.
 */

import com.deodardreams.dto.request.CreateEnquiryUserRequestDto;
import com.deodardreams.dto.response.EnquiryUserResponseDto;
import com.deodardreams.model.EnquiryUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring") // Registers the generated MapStruct implementation as a Spring bean for dependency injection.
public interface EnquiryUserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    EnquiryUser toEnquiryUserEntity(CreateEnquiryUserRequestDto requestDto);

    //EnquiryUserResponseDto toEnquiryUserResponse(EnquiryUser enquiryUser);
}
