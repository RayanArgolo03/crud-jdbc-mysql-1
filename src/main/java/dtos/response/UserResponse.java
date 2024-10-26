package dtos.response;


public record UserResponse(String username) {

    @Override
    public String toString() {
        return username;
    }
}
