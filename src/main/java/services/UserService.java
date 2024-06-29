package services;

import domain.user.User;
import dto.user.UserDTO;
import exceptions.UserException;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import mappers.interfaces.Mapper;
import repositories.interfaces.UserRepository;
import utils.ReaderUtils;

import java.util.Objects;

@FieldDefaults(makeFinal = true)
@AllArgsConstructor
public final class UserService {

    private UserRepository repository;
    private Mapper<UserDTO, User> mapper;

    public String receiveStringInput(final String title) {
        return ReaderUtils.readString(title);
    }

    public void validateUsername(final String username) {

        Objects.requireNonNull(username, "Username can´t be null!");

        if (username.length() < 3) {
            throw new UserException(String.format("Username %s has less than 3 characters!", username));
        }

        if (!username.matches(".*[!@#$%^&*()\\-+=_{}|:;',.?/\\\\].*")) {
            throw new UserException(String.format("Username %s not contains at least 1 special character!", username));
        }
    }

    public void validatePassword(final String password) {

        Objects.requireNonNull(password, "Password can´t be null!");

        if (!password.matches(".*[!@#$%^&*()\\-+=_{}|:;',.?/\\\\].*")) {
            throw new UserException(String.format("Password %s not contains at least 1 special character!", password));
        }
    }

    public void findUsername(final String username) {
        repository.findUsername(username).ifPresent((name) -> {
            throw new UserException(String.format("User with username %s already exists!", name));
        });
    }

    public User findUser(final String username, final String password) {

        Objects.requireNonNull(username, "Username can´t be null!");
        Objects.requireNonNull(password, "Password can´t be null!");

        return repository.findUser(username, password)
                .map(mapper::dtoToEntity)
                .orElseThrow(() -> new UserException(String.format("User %s not found!", username)));
    }

    public void saveUser(final User user) {
        Objects.requireNonNull(user, "User can´t be null!");
        repository.save(user);
    }

    public int deleteUser(final User user) {
        Objects.requireNonNull(user, "User can´t be null!");
        try {
            return repository.deleteById(user.getId());
        } catch (NumberFormatException e) {
            throw new UserException(String.format("Id %d is too long to convert, undo workaround :)", user.getId()), e);
        }
    }
}
