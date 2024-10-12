package database;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;
import org.bson.codecs.pojo.PojoCodecProvider;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public final class FlapdoodleConnection {

    private FlapdoodleConnection() {
    }

    public static TransitionWalker.ReachedState<RunningMongodProcess> startMongoDbServer() {
        return Mongod.instance().start(Version.Main.PRODUCTION);
    }

    public static MongoClient startMongoDb(final TransitionWalker.ReachedState<RunningMongodProcess> process) {
        return MongoClients.create("mongodb://" + process.current().getServerAddress());
    }

    public static <T> MongoCollection<T> getCollection(final MongoClient mongo, final String collection, final Class<T> entityClass) {
        return mongo.getDatabase("app")
                .withCodecRegistry(
                        fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                                fromProviders(PojoCodecProvider.builder()
                                        .register("model")
                                        .build())))
                .getCollection(collection, entityClass);
    }
}
