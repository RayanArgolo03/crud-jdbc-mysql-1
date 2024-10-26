package services;

import dtos.request.UserRequest;
import dtos.response.UserResponse;
import exceptions.UserException;
import mappers.UserMapper;
import model.User;
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

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserService service;


    @Nested
    @DisplayName("*** ValidadeAndFormatUsername tests ***")
    class ValidadeAndFormatUsernameTests {

        @Test
        @DisplayName("Should be throw UserException when the username has less than 3 characters")
        void givenValidateAndFormatUsername_whenUsernameIsShort_thenThrowUserException() {

            final String username = "as";

            final UserException e = assertThrows(UserException.class, () ->
                    service.validateAndFormatUsername(username));

            final String expected = format("Username %s has less than 3 characters!", username);

            assertEquals(expected, e.getMessage());

        }

        @Test
        @DisplayName("Should be throw UserException when the username contains special character")
        void givenValidateAndFormatUsername_whenUsernameContainsSpecialCharacter_thenThrowUserException() {

            final String username = "ass*";

            final UserException e = assertThrows(UserException.class, () ->
                    service.validateAndFormatUsername(username));

            final String expected = format("Username %s contains special character!", username);

            assertEquals(expected, e.getMessage());

        }

        @Test
        @DisplayName("Should be throw UserException when the username contains special character")
        void givenValidateAndFormatUsername_whenUsernameIsValid_thenDoesNotThrow() {
            final String username = "ass";
            assertDoesNotThrow(() -> service.validateAndFormatUsername(username));
        }

    }

    @Nested
    @DisplayName("*** ValidadePassword tests ***")
    class ValidadePasswordTests {

        @Test
        @DisplayName("Should be throw UserException when the password not contains special characters")
        void givenValidadePassword_whenPasswordNotContainsSpecialSymbol_thenThrowUserException() {

            final String password = "as";

            final UserException e = assertThrows(UserException.class, () ->
                    service.validatePassword(password));

            final String expected = format("Password %s not contains at least 1 special character!", password);

            assertEquals(expected, e.getMessage());

        }

        @Test
        @DisplayName("Should be throw UserException when the password contains special character")
        void givenValidadePassword_whenPasswordContainsSpecialSymbol_thenDoesNotThrowException() {
            final String password = "as*";
            assertDoesNotThrow(() -> service.validatePassword(password));
        }

    }

    @Nested
    @DisplayName("*** ContainsAtLeastOneSpecialCharacter tests ***")
    class ContainsAtLeastOneSpecialCharacterTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "Hello!", "Test@", "Value#", "Data$", "Info%", "Number^", "Check&", "String*", "Symbol(", "Close)", "Minus-", "Plus+", "Equals=", "Under_score", "Curly{braces}", "Pipe|", "Colon:", "Semi;", "Comma,", "Slash/"
        })
        @DisplayName("Should be return true when all values contains at least one special character")
        void givenContainsAtLeastOneSpecialCharacter_whenValueContains_thenReturnTrue(String value) {
            assertTrue(service.containsAtLeastOneSpecialCharacter(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Hello", "Test", "Value", "Data", "Info", "Number", "Check", "String", "Symbol", "Close", "Minus", "Plus", "Equals", "Underscore", "CurlyBraces", "Pipe", "Colon", "Semi", "Comma", "Slash"

        })
        @DisplayName("Should be return false when all values not contains at least one special character")
        void givenContainsAtLeastOneSpecialCharacter_whenValueNotContains_thenReturnFalse(String value) {
            assertFalse(service.containsAtLeastOneSpecialCharacter(value));
        }

    }

    @Nested
    @DisplayName("*** CheckIfUsernameExists tests ***")
    class CheckIfUsernameExistsTests {

        private String username = "as";

        @Test
        @DisplayName("Should be throw UserException when the username already exists")
        void givenCheckIfUsernameExists_whenUserNameAlreadyExists_thenThrowUserException() {

            when(repository.findByUsername(username)).thenReturn(Optional.of(new User(username, null)));

            final UserException e = assertThrows(UserException.class, () ->
                    service.checkIfUsernameExists(username));

            final String expected = format("User with username %s already exists!", username);

            assertEquals(expected, e.getMessage());

            verify(repository).findByUsername(username);
        }

        @Test
        @DisplayName("Should be does not throw UserException when the username not exists")
        void givenCheckIfUsernameExists_whenUserNameNotExists_thenDoesNotThrow() {
            when(repository.findByUsername(username)).thenReturn(Optional.empty());
            assertDoesNotThrow(() -> service.checkIfUsernameExists(username));
        }

    }

    //Todo
    @Nested
    @DisplayName("*** FindUser tests ***")
    class FindUserTests {

        private String username = "saaas", password = "sass";

        @Test
        @DisplayName("Should be return UserResponse mapped by User when user is found")
        void givenFindUser_whenUserIsFound_thenReturnUserMappedToUserResponse() {

            final UserResponse response = new UserResponse(username);

            when(repository.findUser(username, password)).thenReturn(Optional.of(new User(username, password)));
            when(mapper.userToResponse(any())).thenReturn(response);

            assertEquals(response, service.findUser(username, password));

            verify(repository).findUser(username, password);
            verify(mapper).userToResponse(any());
        }

        @Test
        @DisplayName("Should be throw UserException when user not found")
        void givenFindUser_whenUserNotFound_thenThrowUserException() {

            when(repository.findUser(username, password)).thenReturn(Optional.empty());

            final UserException e = assertThrows(UserException.class, () ->
                    service.findUser(username, password));

            final String expected = format("User of username %s not found!", username);

            assertEquals(expected, e.getMessage());

            verify(repository).findUser(username, password);
            verify(mapper, never()).userToResponse(any());
        }

    }

    @Nested
    @DisplayName("*** SaveUser tests ***")
    class SaveUserTests {

        @Test
        @DisplayName("Should be return UserResponse mapped by User when User is saved")
        void givenSaveUser_whenUserIsSaved_thenReturnUserMappedToUserResponse() {

            final UserRequest request = new UserRequest("asas", "asas");
            final UserResponse response = new UserResponse(request.username());

            when(mapper.requestToUser(request)).thenReturn(new User(request.username(), request.password()));
            when(mapper.userToResponse(any())).thenReturn(response);
            doNothing().when(repository).save(any());

            assertEquals(response, service.saveUser(request));

            verify(mapper).requestToUser(request);
            verify(mapper).userToResponse(any());
            verify(repository).save(any());

        }


    }

    @Nested
    @DisplayName("*** FindAnDelete tests ***")
    class FindAndDeleteTests {

        private String username = "asaas", password = "asasa";

        @Test
        @DisplayName("Should be return UserResponse mapped by User when User is found and deleted")
        void givenFindAnDelete_whenUserIsFoundAndDeleted_thenReturnUserMappedToUserResponse() {

            final UserResponse response = new UserResponse(username);

            when(repository.findAndDelete(username, password)).thenReturn(Optional.of(new User(username, password)));
            when(mapper.userToResponse(any())).thenReturn(response);

            assertEquals(response, service.findAndDelete(username, password));

            verify(repository).findAndDelete(username, password);
            verify(mapper).userToResponse(any());

        }


        @Test
        @DisplayName("Should be throw UserException when user not found and not deleted")
        void givenFindAnDelete_whenUserNotFoundAndNotDeleted_thenThrowUserException() {

            when(repository.findAndDelete(username, password)).thenReturn(Optional.empty());

            final UserException e = assertThrows(UserException.class, () ->
                    service.findAndDelete(username, password));

            final String expected = format("User %s not found, not deleted!", username);

            assertEquals(expected, e.getMessage());

            verify(repository).findAndDelete(username, password);
            verify(mapper, never()).userToResponse(any());

        }

    }


}