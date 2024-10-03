package services;

import com.mongodb.MongoException;
import dtos.request.UserRequest;
import dtos.response.UserResponse;
import exceptions.UserException;
import mappers.UserMapper;
import model.User;
import repositories.interfaces.UserRepository;
import utils.FormatterUtils;

import static java.lang.String.format;

public final class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    public UserService(UserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public String validateAndFormatUsername(final String username) {

        if (username.length() < 3) {
            throw new UserException(format("Username %s has less than 3 characters!", username));
        }

        if (!containsAtLeastOneSpecialCharacter(username)) {
            throw new UserException(format("Username %s not contains at least 1 special character!", username));
        }

        return FormatterUtils.formatName(username);
    }

    public void validatePassword(final String password) {

        if (!containsAtLeastOneSpecialCharacter(password)) {
            throw new UserException(format("Password %s not contains at least 1 special character!", password));
        }
    }

    public boolean containsAtLeastOneSpecialCharacter(final String value) {
        return value.matches(".*[!@#$%^&*()\\-+=_{}|:;',.?/\\\\].*");
    }

    public void checkIfUsernameExists(final String username) {

        if (repository.findByUsername(username).isPresent()) {
            throw new UserException(format("User with username %s already exists!", username));
        }

    }

    public UserResponse findUser(final String username, final String password) {

        return repository.findUser(username, password)
                .map(mapper::userToResponse)
                .orElseThrow(() -> new UserException(format("User of username %s not found!", username)));
    }

    public UserResponse saveUser(final UserRequest request) {

        final User user = mapper.requestToUser(request);

        try {
            repository.save(user);
            return mapper.userToResponse(user);

        } catch (MongoException e) {
            throw new UserException(format("Error ocurred on save user: %s", e.getMessage()), e.getCause());
        }

    }

    public UserResponse findAndDelete(final String username, final String password) {

        return repository.findAndDelete(username, password)
                .map(mapper::userToResponse)
                .orElseThrow(() -> new UserException("User not found, not deleted!"));

    }
}
