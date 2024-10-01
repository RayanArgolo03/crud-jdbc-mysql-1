package repositories;

import database.MongoConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repositories.impl.UserRepositoryImpl;
import repositories.interfaces.UserRepository;

class UserRepositoryTest {

    UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserRepositoryImpl(MongoConnection.getINSTANCE());
    }

    @Test
    void test(){
        System.out.println();
    }
}