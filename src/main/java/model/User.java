package model;

import lombok.Getter;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Getter
@Setter
public final class User {

    @BsonId
    private ObjectId id;

    @BsonProperty("user_name")
    private final String username;

    @BsonProperty("password")
    private final String password;

    @BsonCreator
    public User(@BsonProperty("user_name") String username, @BsonProperty("password") String password) {
        this.username = username;
        this.password = password;
    }
}
