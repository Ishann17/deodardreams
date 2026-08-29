package com.deodardreams.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAdminUserRequestDto {

    private String name;

    private String email;

    private String phoneNumber;

    private String password;
}
