package dtos.response;

import lombok.Builder;

public record UserResponse(String username) {

    @Override
    public String toString() {
        return username;
    }
}
