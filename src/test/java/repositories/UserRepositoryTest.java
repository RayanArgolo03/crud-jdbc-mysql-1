package repositories;

import com.mongodb.client.MongoClient;
import database.FlapdoodleConnection;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import repositories.impl.UserRepositoryImpl;
import repositories.interfaces.UserRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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

            try (TransitionWalker.ReachedState<RunningMongodProcess> process = FlapdoodleConnection.startMongoDbServer()) {
                try (MongoClient mongo = FlapdoodleConnection.startMongoDb(process)) {
                    repository = new UserRepositoryImpl(FlapdoodleConnection.getCollection(mongo, COLLECTION_NAME, COLLECTION_CLASS));

                }

            }

        }

        @Test
        @DisplayName("Should be define id in user when user is saved")
        void asas() {

            try (TransitionWalker.ReachedState<RunningMongodProcess> process = FlapdoodleConnection.startMongoDbServer()) {
                try (MongoClient mongo = FlapdoodleConnection.startMongoDb(process)) {
                    repository = new UserRepositoryImpl(FlapdoodleConnection.getCollection(mongo, COLLECTION_NAME, COLLECTION_CLASS));

                    repository.save(user);
                    assertNotNull(user.getId());
                }

            }

        }

    }
}