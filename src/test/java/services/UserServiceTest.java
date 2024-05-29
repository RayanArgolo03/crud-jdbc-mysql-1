package services;

import dao.impl.UserDAOImpl;
import domain.user.User;
import exceptions.UserException;
import jdk.jshell.spi.ExecutionControl;
import mappers.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import static org.junit.jupiter.api.Assertions.*;


//Todo add mockito
class UserServiceTest {
    private User mockUser;
    private UserService service;

    @BeforeEach
    void setUp() {
        mockUser = new User("Ja", "Ja");
        service = new UserService(
                new UserDAOImpl(), new UserMapper()
        );
    }

    @Nested
    @DisplayName("** Validate username methods **")
    class ValidateUsernameTests {

        @DisplayName("Should be throw NPE because username is null, with message can´t be null")
        @Test
        void givenValidateUsername_whenUsernameIsNull_thenThrowNPEException() {
            NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.validateUsername(null)
            );
            String message = "Username can´t be null";
            assertEquals(message, e.getMessage());
        }

        @DisplayName("Should be throw User Exception when the username length is less than 3, with message")
        @Test
        void givenValidateUsername_whenUsernameLenghtIsLessThen3_thenThrowUserException() {

            String username = "Ab";
            UserException e = assertThrows(UserException.class,
                    () -> service.validateUsername(username)
            );

            String message = String.format("Username %s has less than 3 characters!", username);
            assertEquals(message, e.getMessage());
        }

        @DisplayName("Should be throw User Exception when the username not contains at least 1 special symbol, with message")
        @Test
        void givenValidateUsername_whenUsernameNotContainsSpecialChar_thenThrowUserException() {

            String username = "Ab";
            UserException e = assertThrows(UserException.class,
                    () -> service.validateUsername(username));

            String message = String.format("Username %s not contains at least 1 special character!", username);
            assertEquals(message, e.getMessage());
        }
    }


    @Nested
    @DisplayName("** Validate password methods **")
    class ValidatePasswordTests {

        @DisplayName("Should be throw null pointer exception when password passed is null, with message")
        @Test
        void givenValidatePassword_whenPasswordIsNull_thenThrowNPEException() {

            NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.validatePassword(null));

            String message = "Password can´t be null!";
            assertEquals(message, e.getMessage());

        }

        @DisplayName("Should be throw User Exception when password not contains special characters, with message")
        @Test
        void givenValidatePassword_whenPasswordNotContainsAtLeast1SpecialChar_thenThrowUserException() {

            String password = "Without special char";
            UserException e = assertThrows(UserException.class,
                    () -> service.validatePassword(password));

            String message = String.format("Password %s not contains at least 1 special character!", password);
            assertEquals(message, e.getMessage());
        }
    }


    @Nested
    @DisplayName("** Find username methods **")
    class FindUsernameTests {

        @DisplayName("Should be throw User Exception when user with the username passed alredy exists, with message")
        @Test
            //Need mock user
        void givenFindUsername_whenUserWithUsernameAlredyExists_thenThrowUserException() {

            String username = "Jayy";
            UserException e = assertThrows(UserException.class,
                    () -> service.findUsername(username));

            String message = String.format("User %s alredy exists!", username);
            assertEquals(message, e.getMessage());

        }


        @DisplayName("Should be continue when user with the username passed not exists")
        @Test
            //Need mock user
        void givenFindUsername_whenUserWithUsernameNotExists_thenContinue() {
            String username = "Username non-existent";
            assertDoesNotThrow(() -> service.findUsername(username));
        }

    }

    @Nested
    @DisplayName("** Save user methods **")
    class SaveUserTests {

        @DisplayName("Should be throw Null Pointer Exception when user is null, with message")
        @Test
        void givenSaveUser_whenIsANullUser_thenThrowNPEException() {

            mockUser = null;
            NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.saveUser(mockUser));

            String message = "User can´t be null!";
            assertEquals(message, e.getMessage());

        }

        @DisplayName("Should be set id in mock user when the user was been save in database")
        @Test
        void givenSaveUser_whenIsANewUser_thenSetId() {
            assertDoesNotThrow(() -> service.saveUser(mockUser));
            assertNotNull(mockUser.getId());
        }

    }

    @Nested
    @DisplayName("** Find user methods **")
    class FindUserTests {

        @DisplayName("Should be throw NPE Exception when username is null")
        @Test
        void givenFindUser_whenUsernameIsNull_thenThrowNPEException() {

            NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.findUser(null, "Any password"));

            String message = "Username can´t be null!";
            assertEquals(message, e.getMessage());
        }

        @DisplayName("Should be throw NPE Exception when password is null")
        @Test
        void givenFindUser_whenPasswordIsNull_thenThrowNPEException() {

            NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.findUser("Any username", null));

            String message = "Password can´t be null!";
            assertEquals(message, e.getMessage());
        }


        @DisplayName("Should be throw UserException when user not found, with message")
        @Test
        void givenFindUser_whenUserNotFound_thenThrowUserException() {

            String username = "Any username";
            UserException e = assertThrows(UserException.class,
                    () -> service.findUser(username, "Any password"));

            String message = String.format("User %s not found!", username);
            assertEquals(message, e.getMessage());

        }

        @DisplayName("Should be return mock user previously saved when we find user with mock username and password")
        @Test
        void givenFindUser_whenUserIsFound_thenReturnUser() {
            assertEquals(mockUser, service.findUser(mockUser.getUsername(), mockUser.getPassword()));
        }
    }


    @Nested
    @DisplayName("** Delete user methods **")
    class DeleteUserTests {

        @DisplayName("Should be throw NPE Exception when user is null")
        @Test
        void givenDeleteUser_whenUserIsNull_thenThrowNPEException() {

            mockUser = null;
            NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.deleteUser(mockUser));

            String message = "User can´t be null!";
            assertEquals(message, e.getMessage());
        }

        @DisplayName("Should be return user id when user has been deleted")
        @Test
        void givenDeleteUser_whenUserHasBeenDeleted_thenReturnId() {
            mockUser = service.findUser(mockUser.getUsername(), mockUser.getPassword());
            assertEquals(mockUser.getId(), service.deleteUser(mockUser));
        }
    }


}