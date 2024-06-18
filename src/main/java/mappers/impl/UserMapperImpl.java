package mappers;

import domain.user.User;
import dto.base.BaseDto;
import dto.user.UserDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import mappers.interfaces.Mapper;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
public final class UserMapper implements Mapper<UserDTO, User> {
    @Override
    public User dtoToEntity(UserDTO baseDto) {
        final User user = new User(baseDto.getUsername(), baseDto.getPassword());
        user.setId(baseDto.getId());
        return user;
    }
}
