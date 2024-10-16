package repositories;

import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;
import model.User;
import org.junit.jupiter.api.*;
import repositories.impl.UserRepositoryImpl;
import repositories.interfaces.UserRepository;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static database.FlapdoodleConnection.*;
import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private static final String COLLECTION_NAME = "user";
    private static final Class<User> COLLECTION_CLASS = User.class;

    private UserRepository repository;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("pizarro", "pizarro");
    }


    @Nested
    @DisplayName("*** Save user tests ***")
    class SaveUserTests {

        @Test
        @DisplayName("Should be define id in user when user is saved")
        void givenSave_whenUserIsSaved_thenSetIdInUser() {

            try (TransitionWalker.ReachedState<RunningMongodProcess> process = startMongoDbServer()) {
                try (MongoClient mongo = startMongoDb(process)) {
                    repository = new UserRepositoryImpl(getCollection(mongo, COLLECTION_NAME, COLLECTION_CLASS));

                    repository.save(user);
                    assertNotNull(user.getId());
                }

            }

        }

        @Test
        @DisplayName("Should be throw MongoException when the user saved already exists")
        void givenSave_whenUserSavedAlreadyExists_thenThrowMongoException() {

            try (TransitionWalker.ReachedState<RunningMongodProcess> process = startMongoDbServer()) {
                try (MongoClient mongo = startMongoDb(process)) {
                    repository = new UserRepositoryImpl(getCollection(mongo, COLLECTION_NAME, COLLECTION_CLASS));

                    repository.save(user);
                    assertNotNull(user.getId());

                    final MongoException e = assertThrows(MongoException.class, () ->
                            repository.save(user));

                    assertInstanceOf(MongoWriteException.class, e);
                }

            }

        }

    }

    @Nested
    @DisplayName("*** FindByUsername tests ***")
    class FindByUsernameTests {

        @Test
        @DisplayName("Should be return Empty Optional when username not exists")
        void givenFindByUsername_whenUsernameNotExists_thenReturnEmptyOptional() {

            try (TransitionWalker.ReachedState<RunningMongodProcess> process = startMongoDbServer()) {
                try (MongoClient mongo = startMongoDb(process)) {
                    repository = new UserRepositoryImpl(getCollection(mongo, COLLECTION_NAME, COLLECTION_CLASS));

                    assertEquals(Optional.empty(), repository.findByUsername(user.getUsername()));
                }
            }

        }

        @Test
        @DisplayName("Should be return User Optional when username already exists")
        void givenFindByUsername_whenUsernameAlreadyExists_thenReturnUserOptional() {

            try (TransitionWalker.ReachedState<RunningMongodProcess> process = startMongoDbServer()) {
                try (MongoClient mongo = startMongoDb(process)) {
                    repository = new UserRepositoryImpl(getCollection(mongo, COLLECTION_NAME, COLLECTION_CLASS));

                    repository.save(user);

                    final Optional<User> optional = repository.findByUsername(user.getUsername());
                    assertTrue(optional.isPresent());

                    final User userFound = optional.get();

                    //Include only the username in User found
                    assertAll(
                            () -> assertNotNull(userFound.getUsername()),
                            () -> assertNull(userFound.getId()),
                            () -> assertNull(userFound.getPassword())
                    );
                }
            }

        }
    }

    @Nested
    @DisplayName("*** FindAndDelete tests ***")
    class FindAndDeleteTests {


        @Test
        @DisplayName("Should be return user optional when user already exists")
        void givenFindOneAndDelete_whenUserAlreadyExists_thenReturnUserOptional() {

            try (TransitionWalker.ReachedState<RunningMongodProcess> process = startMongoDbServer()) {
                try (MongoClient mongo = startMongoDb(process)) {
                    repository = new UserRepositoryImpl(getCollection(mongo, COLLECTION_NAME, COLLECTION_CLASS));

                    repository.save(user);

                    assertTrue(repository.findAndDelete(user.getUsername(), user.getPassword())
                            .isPresent());
                }
            }

        }

        @Test
        @DisplayName("Should be return empty optional when user exists")
        void givenFindOneAndDelete_whenUserNotExists_thenReturnEmptyOptional() {

            try (TransitionWalker.ReachedState<RunningMongodProcess> process = startMongoDbServer()) {
                try (MongoClient mongo = startMongoDb(process)) {
                    repository = new UserRepositoryImpl(getCollection(mongo, COLLECTION_NAME, COLLECTION_CLASS));


                    assertTrue(repository.findAndDelete(user.getUsername(), user.getPassword())
                            .isEmpty());
                }
            }

        }

    }

    @Nested
    @DisplayName("*** FindUser tests ***")
    class FindUserTests {

        @Test
        @DisplayName("Should be return user optional when user already exists")
        void givenFindUser_whenUserAlreadyExists_thenReturnUserOptional() {

            try (TransitionWalker.ReachedState<RunningMongodProcess> process = startMongoDbServer()) {
                try (MongoClient mongo = startMongoDb(process)) {
                    repository = new UserRepositoryImpl(getCollection(mongo, COLLECTION_NAME, COLLECTION_CLASS));

                    repository.save(user);

                    assertTrue(repository.findUser(user.getUsername(), user.getPassword())
                            .isPresent());
                }
            }

        }

        @Test
        @DisplayName("Should be return empty optional when user not exists")
        void givenFindUser_whenUserNotExists_thenReturnEmptyOptional() {

            try (TransitionWalker.ReachedState<RunningMongodProcess> process = startMongoDbServer()) {
                try (MongoClient mongo = startMongoDb(process)) {
                    repository = new UserRepositoryImpl(getCollection(mongo, COLLECTION_NAME, COLLECTION_CLASS));


                    assertTrue(repository.findUser(user.getUsername(), user.getPassword())
                            .isEmpty());
                }
            }

        }

    }
}