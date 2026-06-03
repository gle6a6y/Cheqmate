package project.cheqmate.dto;

import lombok.Data;

@Data
public class RegisterDeviceRequest {
    private String token;
    private String platform;
}
