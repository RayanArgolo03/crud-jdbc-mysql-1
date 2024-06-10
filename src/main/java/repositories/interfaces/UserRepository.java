package repositories.interfaces;

import domain.user.User;
import dto.user.UserDTO;

import java.util.Optional;

public interface UserRepository extends EntityRepository<User> {
    Optional<String> findUsername(String username);

    Optional<UserDTO> findUser(String username, String password);

}
