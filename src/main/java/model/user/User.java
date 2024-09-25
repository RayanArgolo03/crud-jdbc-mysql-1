package model.user;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

public final class User {

    @BsonId
    private ObjectId id;

    @BsonProperty("user_name")
    private final String username;

    private final String password;

    @BsonCreator
    public User(@BsonProperty("user_name") String username, @BsonProperty("password") String password) {
        this.username = username;
        this.password = password;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public ObjectId getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
