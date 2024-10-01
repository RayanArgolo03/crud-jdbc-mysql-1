package repositories.interfaces;

import dtos.response.UserResponse;
import model.user.User;

import java.util.Optional;

public interface UserRepository extends EntityRepository<User> {

    Optional<User> findByUsername(String username);

    Optional<User> findUser(String username, String password);

    Optional<User> findAndDelete(String username, String password);

}
