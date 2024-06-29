package mappers.impl;

import domain.user.User;
import dto.user.UserDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import mappers.interfaces.Mapper;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
public final class UserMapperImpl implements Mapper<UserDTO, User> {
    @Override
    public User dtoToEntity(UserDTO dto) {
        final User user = new User(dto.getUsername(), dto.getPassword());
        user.setId(dto.getId());
        return user;
    }
}
