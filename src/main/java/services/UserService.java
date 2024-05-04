package services;

import dao.impl.UserDAOImpl;
import dao.interfaces.UserDAO;
import domain.user.User;
import enums.menu.DefaultMessage;
import exceptions.UserException;
import mappers.UserMapper;
import utilities.FormatterUtil;
import utilities.ReaderUtil;

public final class UserService {
    private final UserDAO dao;
    private final UserMapper mapper;

    public UserService() {
        this.dao = new UserDAOImpl();
        this.mapper = new UserMapper();
    }

    public String receiveUsername() {

        String name;
        if (!validUsername(name = ReaderUtil.readString("username (with more than 3 characters)"))) {
            throw new UserException(DefaultMessage.INVALID.getValue() +
                    " Your username has less than 3 characters!");
        }

        name = FormatterUtil.formatName(name);

        return name;
    }

    public boolean validUsername(final String username) {

        if (username.length() < 3) {
            throw new UserException(DefaultMessage.INVALID.getValue() +
                    " Your username has less than 3 characters!");
        }

        return username.length() > 3;
    }

    public String receivePassword() {

        final String password;
        if (!validPassword(password = ReaderUtil.readString("password (with more than 1 special character)"))) {
            throw new UserException(DefaultMessage.INVALID.getValue() +
                    " Your password has less than 1 special characters!");
        }

        return password;
    }

    private boolean validPassword(final String password) {
        //Contains at least 1 special character
        return password.matches(".*[!@#$%^&*()\\-+=_{}|:;',.?/\\\\].*");
    }

    public User findUser(final String username, final String password) {
        return dao.findUser(username, password)
                .map(mapper::dtoToEntity)
                .orElseThrow(() -> new UserException("User not found!"));
    }

    public void findUsername(final String username) {
        dao.findUsername(username).ifPresent((name) -> {
            throw new UserException("User " + name + " alredy exists!");
        });
    }

    public void saveUser(final User user) {
        dao.save(user);
    }

    public int deleteUser(final long id) {
        return dao.deleteById(id);
    }
}
