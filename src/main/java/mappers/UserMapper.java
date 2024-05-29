package mappers;

import domain.user.User;
import dto.user.UserDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
public final class UserMapper {

    //Not using builder because the small number of atributtes
    public User dtoToEntity(final UserDTO dto) {
        User user = new User(dto.getUsername(), dto.getPassword());
        user.setId(dto.getId());
        return user;
    }


}
