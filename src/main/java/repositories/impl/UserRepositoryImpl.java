package repositories.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import database.MongoConnection;
import lombok.extern.log4j.Log4j2;
import model.user.User;
import org.bson.Document;
import org.bson.conversions.Bson;
import repositories.interfaces.UserRepository;

import java.util.Optional;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

@Log4j2
public final class UserRepositoryImpl implements UserRepository {

    private final MongoCollection<User> collection;

    public UserRepositoryImpl(MongoConnection connection) {

        collection = connection.getDatabase().getCollection("users", User.class);

        //Create username unique constraint
        collection.createIndex(
                new Document("user_name", 1),
                new IndexOptions().unique(true)
        );
    }

    @Override
    public void save(final User user) {
        log.info("Tryning to save {} in the database.. \n", user.getUsername());
        collection.insertOne(user);
    }


    @Override
    public Optional<User> findByUsername(final String username) {

        log.info("Tryning to find user with username {} in the database.. \n", username);

        return Optional.ofNullable(collection.find(eq("user_name", username))
                .projection(fields(include("user_name")))
                .first());
    }

    @Override
    public Optional<User> findAndDelete(final String username, final String password) {

        return Optional.ofNullable(
                collection.findOneAndDelete(getFindFilter(username, password))
        );

    }


    @Override
    public Optional<User> findUser(final String username, final String password) {

        log.info("Tryning to find {} in the database.. \n", username);

        return Optional.ofNullable(
                collection.find(getFindFilter(username, password))
                        .first()
        );
    }

    private Bson getFindFilter(final String username, final String password) {
        return and(
                eq("user_name", username),
                eq("password", password)
        );
    }


}
