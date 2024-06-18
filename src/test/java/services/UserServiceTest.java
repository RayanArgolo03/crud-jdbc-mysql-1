package services;

import domain.user.User;
import dto.user.UserDTO;
import exceptions.DbConnectionException;
import exceptions.UserException;
import mappers.interfaces.Mapper;
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
    private Mapper<UserDTO, User> mapper;
    @InjectMocks
    private UserService service;

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
            final UserException e = assertThrows(UserException.class,
                    () -> service.validatePassword(password));

            final String expectedMessage = String.format("Password %s not contains at least 1 special character!", password);
            assertEquals(expectedMessage, e.getMessage());
        }

        //Generate with GPT
        @ParameterizedTest
        @ValueSource(strings = {"senha@123", "usuário#2023", "pass_word$", "email@example.com", "Nome&Sobrenome", "!Bem-vindo!", "chave*valor", "endereço@home", "nome%completo", "telefone(123)456-7890"})
        @DisplayName("Should be continue when the password contains any special symbol")
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
    @DisplayName("** Find user methods **")
    class FindUserTests {
        private String username, password;

        @BeforeEach
        void setUp() {
            username = "any";
            password = "any";
        }

        @Test
        @DisplayName("Should be throw Null Pointer Exception when the username is null")
        void givenFindUser_whenUsernameIsNull_thenThrowNPEException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.findUser(null, password));

            String expectedMessage = "Username can´t be null!";
            assertEquals(expectedMessage, e.getMessage());
        }

        @Test
        @DisplayName("Should be throw Null Pointer Exception when the password is null")
        void givenFindUser_whenPasswordIsNull_thenThrowNPEException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.findUser(username, null));

            String expectedMessage = "Password can´t be null!";
            assertEquals(expectedMessage, e.getMessage());
        }


        @Test
        @DisplayName("Should be return an user with id when the username and password already exists")
        void givenFindUser_whenUserIsFound_thenReturnUser() {

            final UserDTO dto = new UserDTO(1L, username, password);
            final User userExpected = new User(username, password);

            when(repository.findUser(username, password)).thenReturn(Optional.of(dto));

            when(mapper.dtoToEntity(dto)).thenReturn(userExpected);
            //Mapper action
            userExpected.setId(dto.getId());

            assertEquals(userExpected, service.findUser(username, password));
            assertEquals(dto.getId(), userExpected.getId());

            verify(repository).findUser(username, password);
            verify(mapper).dtoToEntity(dto);

        }

        @Test
        @DisplayName("Should be throw User Exception when the user not exists")
        void givenFindUser_whenUserNotExists_thenThrowUserException() {

            when(repository.findUser(username, password)).thenReturn(Optional.empty());

            final UserException e = assertThrows(UserException.class,
                    () -> service.findUser(username, password));

            final String expectedMessage = String.format("User %s not found!", username);
            assertEquals(expectedMessage, e.getMessage());

            verify(repository).findUser(username, password);

        }

    }

    @Nested
    @DisplayName("** Save user methods **")
    class SaveUserTests {

        @Test
        @DisplayName("Should be Throw Null Pointer Exception when the user is null")
        void givenSaveUser_whenUserIsNull_thenThrowNPEEXception() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.saveUser(null));

            final String expectedMessage = "User can´t be null!";
            assertEquals(expectedMessage, e.getMessage());

        }

        @Test
        @DisplayName("Should be set id in the saved user")
        void givenSaveUser_whenUserIsSaved_thenSetIdInTheUser() {

            final User user = new User("any", "any");
            final Long expectedId = 1L;

            doAnswer(input -> {
                        User userSaved = input.getArgument(0);
                        userSaved.setId(expectedId);
                        return null;
                    }
            ).when(repository).save(user);


            service.saveUser(user);
            assertEquals(expectedId, user.getId());

            verify(repository).save(user);

        }

    }

    @Nested
    @DisplayName("** Delete user methods **")
    class DeleteUserTests {

        private User user;

        @BeforeEach
        void setUp() {
            user = new User("any", "any");
            user.setId(1L);
        }

        @Test
        @DisplayName("Should be throw Null Pointer Exception when the user is null")
        void givenDeleteUser_whenUserIsNull_thenThrowNPEException() {

            final NullPointerException e = assertThrows(NullPointerException.class,
                    () -> service.deleteUser(null));

            final String expectedMessage = "User can´t be null!";
            assertEquals(expectedMessage, e.getMessage());
        }

        @Test
        @DisplayName("Should be return the id of user deleted when user was correctly deleted")
        void givenDeleteUser_whenUserHasBeenDeleted_thenReturnTheUserId() {

            final int idIntExpected = Integer.parseInt(String.valueOf(
                    user.getId()
            ));

            when(repository.deleteById(user.getId())).thenReturn(idIntExpected);

            assertEquals(idIntExpected, service.deleteUser(user));

            verify(repository).deleteById(user.getId());
        }


        @Test
        @DisplayName("Should be throw User Exception when the id is too long to converted in integer (workaround)")
        void givenDeleteUser_whenUserHasBeenDeletedButTheIdIsTooLong_thenThrowUserException() {

            final NumberFormatException expectedCause = new NumberFormatException();
            when(repository.deleteById(user.getId())).thenThrow(expectedCause);

            final UserException e = assertThrows(UserException.class,
                    () -> service.deleteUser(user));
            assertEquals(expectedCause, e.getCause());

            final String expectedMessage = String.format("Id %d is too long to convert, undo workaround :)", user.getId());
            assertEquals(expectedMessage, e.getMessage());

        }
    }


}