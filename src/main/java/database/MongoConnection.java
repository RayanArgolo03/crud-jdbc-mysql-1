package database;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.extern.log4j.Log4j2;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.concurrent.TimeUnit;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Log4j2
public final class MongoConnection {

    private static MongoConnection INSTANCE;
    private final MongoDatabase database;

    private MongoConnection() {
        database = initDatabaseWithCodec();
        testConnection(database);
    }

    private MongoDatabase initDatabaseWithCodec() {

        MongoClientSettings serverSettings = MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(1000, TimeUnit.MILLISECONDS))
                .build();

        return MongoClients.create(serverSettings)
                .getDatabase("app")
                .withCodecRegistry(
                        fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                                fromProviders(PojoCodecProvider.builder()
                                        .build()))
                );
    }

    private void testConnection(final MongoDatabase database) {

        try {
            database.runCommand(new BasicDBObject("ping", "1000"));
        } catch (Exception e) {
            log.fatal("MongoConnection not initiated! Run Docker-Compose Container or verify your connection!");
            System.exit(0);
        }

    }

    public static MongoConnection getINSTANCE() {
        if (INSTANCE == null) INSTANCE = new MongoConnection();
        return INSTANCE;
    }

    public MongoDatabase getDatabase() {
        return database;
    }
}
