package dtos.response;

import org.bson.types.ObjectId;

public record UserResponse(ObjectId id, String username, String password) {
}
