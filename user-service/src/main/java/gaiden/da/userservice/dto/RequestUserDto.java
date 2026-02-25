package gaiden.da.userservice.dto;

import lombok.Data;

@Data
public class RequestUserDto {
    private String username;
    private String email;
    private String password;
}
