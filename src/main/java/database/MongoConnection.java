package database;

import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.bson.codecs.pojo.PojoCodecProvider;
import utils.ReaderUtils;

import java.util.concurrent.TimeUnit;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Log4j2
@Getter
public final class MongoConnection {

    private static MongoConnection INSTANCE;
    private final MongoDatabase database;

    private MongoConnection() {
        database = initDatabaseWithCodec();
        testConnection();
        dropLastDatabase();
    }

    public static MongoConnection getINSTANCE() {
        if (INSTANCE == null) INSTANCE = new MongoConnection();
        return INSTANCE;
    }


    private MongoDatabase initDatabaseWithCodec() {

        MongoClientSettings serverSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString("mongodb://root:root@localhost:27017"))
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(1000, TimeUnit.MILLISECONDS))
                .build();

        return MongoClients.create(serverSettings)
                .getDatabase("app")
                .withCodecRegistry(
                        fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                                fromProviders(PojoCodecProvider.builder()
                                        .register("model")
                                        .build()))
                );
    }

    private void testConnection() {

        try {
            //Test connection
            database.runCommand(new BasicDBObject("ping", "1000"));
            log.info("MongoConnection initiated!");

        } catch (Exception e) {
            log.fatal("MongoConnection not initiated! Run Docker-Compose Container or verify your connection!");
            System.exit(0);
        }

    }

    private void dropLastDatabase(){
        log.info("Dropping the last database.. if you want to keep the last database, delete the method in MongoConnection");
        database.drop();
    }


}
