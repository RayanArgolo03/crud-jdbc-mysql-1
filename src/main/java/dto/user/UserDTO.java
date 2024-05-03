package dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

//Not using builder pattern because the class has few attributes
@AllArgsConstructor
@Getter
@FieldDefaults(makeFinal = true)
public final class UserDTO {
    private Long id;
    private String username;
    private String password;
}
