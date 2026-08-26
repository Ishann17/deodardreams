package com.deodardreams.controller.enquiry;


import com.deodardreams.dto.request.CreateEnquiryUserRequestDto;
import com.deodardreams.dto.response.EnquiryUserResponseDto;
import com.deodardreams.service.enquiry.EnquiryUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping ("/api/enquiries")
public class EnquiryUserController {

    private final EnquiryUserService enquiryUserService;

    public EnquiryUserController(EnquiryUserService enquiryUserService) {
        this.enquiryUserService = enquiryUserService;
    }

    @PostMapping
    public ResponseEntity<?> createEnquiry(
            @Valid @RequestBody CreateEnquiryUserRequestDto request) {

        EnquiryUserResponseDto response =
                enquiryUserService.createEnquiry(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
