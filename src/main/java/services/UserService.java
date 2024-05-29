package services;

import dao.interfaces.UserDAO;
import domain.user.User;
import exceptions.UserException;
import lombok.AllArgsConstructor;
import mappers.UserMapper;
import utils.ReaderUtils;

import java.util.Objects;

@AllArgsConstructor
public final class UserService {

    private final UserDAO dao;
    private final UserMapper mapper;

    public String receiveInput(String title) {
        return ReaderUtils.readString(title);
    }

    public void validateUsername(final String username) {

        Objects.requireNonNull(username, "Username can´t be null");

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
        dao.findUsername(username).ifPresent((name) -> {
            throw new UserException(String.format("User %s alredy exists!", name));
        });
    }

    public User findUser(final String username, final String password) {

        Objects.requireNonNull(username, "Username can´t be null!");
        Objects.requireNonNull(password, "Password can´t be null!");

        return dao.findUser(username, password)
                .map(mapper::dtoToEntity)
                .orElseThrow(() -> new UserException(String.format("User %s not found!", username)));
    }

    public void saveUser(final User user) {
        Objects.requireNonNull(user, "User can´t be null!");
        dao.save(user);
    }

    public int deleteUser(final User user) {
        Objects.requireNonNull(user, "User can´t be null!");
        return dao.deleteById(user.getId());
    }
}
