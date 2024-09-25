package services;

import com.mongodb.MongoException;
import dtos.UserResponse;
import exceptions.UserException;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import mappers.UserMapper;
import model.user.User;
import org.bson.types.ObjectId;
import repositories.interfaces.UserRepository;

import java.util.Objects;

import static java.lang.String.format;

@FieldDefaults(makeFinal = true)
@AllArgsConstructor
public final class UserService {

    private UserRepository repository;
    private UserMapper mapper;

    public void validateUsername(final String username) {

        Objects.requireNonNull(username, "Username can´t be null!");

        if (username.length() < 3) {
            throw new UserException(format("Username %s has less than 3 characters!", username));
        }

        if (!username.matches(".*[!@#$%^&*()\\-+=_{}|:;',.?/\\\\].*")) {
            throw new UserException(format("Username %s not contains at least 1 special character!", username));
        }
    }

    public void validatePassword(final String password) {

        Objects.requireNonNull(password, "Password can´t be null!");

        if (!password.matches(".*[!@#$%^&*()\\-+=_{}|:;',.?/\\\\].*")) {
            throw new UserException(format("Password %s not contains at least 1 special character!", password));
        }
    }

    public void findUsername(final String username) {
        repository.findUsername(username).ifPresent((name) -> {
            throw new UserException(format("User with username %s already exists!", name));
        });
    }

    public UserResponse findUser(final String username, final String password) {

        Objects.requireNonNull(username, "Username can´t be null!");
        Objects.requireNonNull(password, "Password can´t be null!");

        return repository.findUser(username, password)
                .map(mapper::userToResponse)
                .orElseThrow(() -> new UserException(format("User %s not found!", username)));
    }

    public void saveUser(final User user) {
        Objects.requireNonNull(user, "User can´t be null!");
        repository.save(user);
    }

    public int deleteById(final ObjectId id) {
        try {
            return repository.deleteById(id);
        } catch (MongoException e) {
            throw new UserException(format("Error ocurred on delete: %s", e.getMessage()), e);
        }
    }
}
