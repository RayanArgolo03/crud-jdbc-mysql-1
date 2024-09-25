package dtos;

import org.bson.types.ObjectId;


public record UserResponse(ObjectId id, String username, String password) {
}
