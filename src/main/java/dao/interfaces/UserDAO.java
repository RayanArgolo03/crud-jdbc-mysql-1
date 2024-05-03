package dao.interfaces;

import domain.user.User;
import dto.user.UserDTO;

import java.util.Optional;

public interface UserDAO extends EntityDAO<User> {
    Optional<String> findUsername(String username);

    Optional<UserDTO> findUser(String username, String password);

}
