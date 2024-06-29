package controllers;

import domain.user.User;
import lombok.AllArgsConstructor;
import services.UserService;

@AllArgsConstructor
public final class UserController {

    private final UserService service;

    public User create() {

        final String username = service.receiveStringInput("username(with more than 3 characters and contain at least 1 special character):");
        service.validateUsername(username);

        //Not allow continue if user already exists in the database
        service.findUsername(username);

        final String password = service.receiveStringInput("password (with more than 1 special character)");
        service.validatePassword(password);

        User user = new User(username, password);
        service.saveUser(user);

        return user;
    }

    public User find() {
        final String username = service.receiveStringInput("username");
        final String password = service.receiveStringInput("password");
        return service.findUser(username, password);
    }

    public int delete(final User user) {
        return service.deleteUser(user);
    }

}
