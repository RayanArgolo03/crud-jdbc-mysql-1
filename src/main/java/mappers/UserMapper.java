package mappers;

import dtos.request.UserRequest;
import dtos.response.UserResponse;
import model.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    UserResponse userToResponse(User user);
    User requestToUser(UserRequest request);
}
