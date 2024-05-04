package controllers;

import domain.user.User;
import lombok.AllArgsConstructor;
import services.UserService;

@AllArgsConstructor
public final class UserController {
    private final UserService service;

    public User create() {

        final String username = service.receiveUsername();
        service.findUsername(username);

        String password = service.receivePassword();

        User user = new User(username, password);
        service.saveUser(user);

        return user;
    }

    public User find() {
        final String username = service.receiveUsername();
        final String password = service.receivePassword();
        return service.findUser(username, password);
    }

    public int delete(final User user) {
        return service.deleteUser(user.getId());
    }

}
