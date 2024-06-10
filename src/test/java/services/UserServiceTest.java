package services;

import domain.user.User;
import exceptions.UserException;
import mappers.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repositories.interfaces.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;
    @Mock
    private UserMapper mapper;
    @InjectMocks
    private UserService service;

    private User mockUser;

    @Nested
    @DisplayName("** Validate methods **")
    class ValidateTests {

        @Test
        @DisplayName("Should be throw NPEException when the username is null")
        void givenValidateUsername_whenUsernameIsNull_thenThrowNullPointerException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.validateUsername(null));

            final String expected = "Username can´t be null!";
            assertEquals(expected, e.getMessage());
        }

        @Test
        @DisplayName("Should be throw User Exception when the username length is less than 3")
        void givenValidateUsername_whenUsernameLenghtIsLessThan3_thenThrowUserException() {

            final String username = "a";
            final UserException e = assertThrows(UserException.class,
                    () -> service.validateUsername(username)
            );

            final String expectedMessage = String.format("Username %s has less than 3 characters!", username);
            assertEquals(expectedMessage, e.getMessage());
        }

        @Test
        @DisplayName("Should be throw User Exception when the username not contains at least 1 special symbol")
        void givenValidateUsername_whenUsernameNotContainsSpecialChar_thenThrowUserException() {

            final String username = "Abc";
            final UserException e = assertThrows(UserException.class,
                    () -> service.validateUsername(username));

            final String expectedMessage = String.format("Username %s not contains at least 1 special character!", username);
            assertEquals(expectedMessage, e.getMessage());
        }


        @Test
        @DisplayName("Should be throw NPEException when password passed is null")
        void givenValidatePassword_whenPasswordIsNull_thenThrowNullPointerException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.validatePassword(null));

            final String expectedMessage = "Password can´t be null!";
            assertEquals(expectedMessage, e.getMessage());
        }

        @Test
        @DisplayName("Should be throw User Exception when password not contains at least 1 special symbol")
        void givenValidatePassword_whenPasswordNotContainsAtLeast1SpecialSymbol_thenThrowUserException() {

            final String password = "Without special char";
            UserException e = assertThrows(UserException.class,
                    () -> service.validatePassword(password));

            final String expectedMessage = String.format("Password %s not contains at least 1 special character!", password);
            assertEquals(expectedMessage, e.getMessage());
        }

        //Generate with GPT
        @ParameterizedTest
        @ValueSource(strings = {"senha@123", "usuário#2023", "pass_word$", "email@example.com", "Nome&Sobrenome", "!Bem-vindo!", "chave*valor", "endereço@home", "nome%completo", "telefone(123)456-7890"})
        void givenValidatePassword_whenPasswordContainsSpecialSymbol_thenNotThrowException(String password) {
            assertNotNull(password);
            assertDoesNotThrow(() -> service.validatePassword(password));
        }
    }

    @DisplayName("** Find username methods **")
    @Nested
    class FindUsernameTests {
        private String username;
        @BeforeEach
        void setUp() {
            username = "Any username";
        }

        @Test
        @DisplayName("Should be throw User Exception when the username already exists")
        void givenFindUsername_whenUsernameAlreadyExists_thenThrowUserException() {

            when(repository.findUsername(username)).thenReturn(Optional.of(username));

            final UserException e = assertThrows(UserException.class,
                    () -> service.findUsername(username));

            final String expectedMessage = String.format("User with username %s already exists!", username);
            assertEquals(expectedMessage, e.getMessage());

            verify(repository).findUsername(username);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("Should be continue when the username not exists")
        void givenFindUsername_whenUsernameNotExists_thenNotThrowException() {
            when(repository.findUsername(username)).thenReturn(Optional.empty());
            assertDoesNotThrow(() -> service.findUsername(username));
            verify(repository).findUsername(username);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    class FindUserTests {
        //Todo
    }

    @Nested
    @DisplayName("** Save user methods **")
    class SaveUserTests {

        @DisplayName("Should be throw NullPointerException when the user is null")
        @Test
        void givenSaveUser_whenUserIsNull_thenThrowNPEException() {

            NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.saveUser(null));

            String message = "User can´t be null!";
            assertEquals(message, e.getMessage());

        }

        @DisplayName("Should be set id in user when the user is correctly saved")
        @Test
        void givenSaveUser_whenUserIsSaved_thenSetIdInUser() {

            final User user = new User("any", "any");

            doAnswer(invocation -> {
                User userSaved = invocation.getArgument(0);
                userSaved.setId(1L);
                return null;
            }).when(repository).save(user);

            service.saveUser(user);
            assertNotNull(user.getId());

            verify(repository).save(user);
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