package kr.pianobear.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Schema(description = "비밀번호 수정 DTO")
public class PasswordUpdateRequestDTO {

    public PasswordUpdateRequestDTO(){}

    public PasswordUpdateRequestDTO(String newPassword){
        this.newPassword = newPassword;
    }

    @NotBlank
    @Schema(description = "이전 비밀번호")
    private String oldPassword;

    @NotBlank
    @Schema(description = "새 비밀번호")
    private String newPassword;
}
