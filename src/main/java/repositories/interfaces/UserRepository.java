package repositories.interfaces;

import model.user.User;

import java.util.Optional;

public interface UserRepository extends EntityRepository<User> {
    Optional<String> findUsername(String username);

    Optional<User> findUser(String username, String password);

}
