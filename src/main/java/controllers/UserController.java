package controllers;

import dtos.request.UserRequest;
import dtos.response.UserResponse;
import services.UserService;
import utils.ReaderUtils;

public final class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    public UserResponse create() {

        String username = ReaderUtils.readString("username (with more than 3 characters and contains at least 1 special character)");
        username = service.validateAndFormatUsername(username);

        //Not allow continue if user already exists in the database
        service.checkIfUsernameExists(username);

        final String password = ReaderUtils.readString("password (at least 1 special character)");
        service.validatePassword(password);

        return service.saveUser(new UserRequest(username, password));
    }

    public UserResponse find() {

        final String username = ReaderUtils.readString("username");
        final String password = ReaderUtils.readString("password");

        return service.findUser(username, password);
    }

    public UserResponse delete() {

        final String username = ReaderUtils.readString("username");
        final String password = ReaderUtils.readString("password");

        return service.findAndDelete(username, password);
    }

}
