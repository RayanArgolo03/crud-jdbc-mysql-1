package domain.user;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;


@Getter
public final class User {

    @Setter
    private Long id;

    private final String username;
    private final String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
