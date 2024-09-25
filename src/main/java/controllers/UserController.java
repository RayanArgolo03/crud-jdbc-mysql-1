package controllers;

import dtos.UserResponse;
import model.user.User;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import services.UserService;
import utils.ReaderUtils;

@AllArgsConstructor
public final class UserController {

    private final UserService service;

    public User create() {

        final String username = ReaderUtils.readString("username(with more than 3 characters and contain at least 1 special character):");
        service.validateUsername(username);

        //Not allow continue if user already exists in the database
        service.findUsername(username);

        final String password = ReaderUtils.readString("password (with more than 1 special character)");
        service.validatePassword(password);

        User user = new User(username, password);
        service.saveUser(user);

        return user;
    }

    public UserResponse find() {
        final String username = ReaderUtils.readString("username");
        final String password = ReaderUtils.readString("password");
        return service.findUser(username, password);
    }

    public int delete(final ObjectId id) {
        return service.deleteById(id);
    }

}
