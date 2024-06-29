package dto.user;

import dto.base.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

//Not using builder pattern because the class has few attributes

@Getter
@FieldDefaults(makeFinal = true)
public final class UserDTO extends BaseDto {

    private String username;
    private String password;

    public UserDTO(Long id, String username, String password) {
        super(id);
        this.username = username;
        this.password = password;
    }
}
