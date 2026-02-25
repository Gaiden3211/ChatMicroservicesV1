package gaiden.da.userservice.grpc;

import gaiden.da.user.*;
import gaiden.da.user.UserServiceGrpcGrpc;
import gaiden.da.user.GetUserCredentialsRequest;
import gaiden.da.user.UserCredentialDto;
import gaiden.da.userservice.dto.UserDto;
import gaiden.da.userservice.service.UserService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class UserServiceGrpcImpl extends UserServiceGrpcGrpc.UserServiceGrpcImplBase {

    private final UserService userService;

    @Override
    public void getUserCredentialsByUsername(GetUserCredentialsRequest request, StreamObserver<UserCredentialDto> responseObserver) {
        try {
            gaiden.da.userservice.dto.UserCredentialDto dto = userService.findUserCredentialDtoByUsername(request.getUsername());

            UserCredentialDto response = UserCredentialDto.newBuilder()
                    .setId(dto.getId())
                    .setUsername(dto.getUsername())
                    .setPassword(dto.getPassword())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (UsernameNotFoundException e){
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {

        UserDto dto = userService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );


        mapToUserResponse(responseObserver, dto);
    }


    @Override
    public void getUserByUsername(GetUserByUsernameRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UserDto dto = userService.findUserByUsername(request.getUsername());


            mapToUserResponse(responseObserver, dto);
        } catch (UsernameNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getUserByEmail(GetUserByEmailRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UserDto dto = userService.findUserByEmail(request.getEmail()); // Припускаючи, що такий метод є в UserService


            mapToUserResponse(responseObserver, dto);
        } catch (UsernameNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }


    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UserDto dto = userService.getUser(request.getUserId()); // Використовуємо твій існуючий метод


            mapToUserResponse(responseObserver, dto);
        } catch (RuntimeException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private void mapToUserResponse(StreamObserver<UserResponse> responseObserver, UserDto dto) {
        UserResponse.Builder responseBuilder = UserResponse.newBuilder()
                .setId(dto.getId())
                .setUsername(dto.getUsername())
                .setEmail(dto.getEmail())
                .setCreatedAt(dto.getCreatedAt().toString())
                .setUpdatedAt(dto.getUpdatedAt().toString());

        if (dto.getOwnGuildIds() != null) {
            responseBuilder.addAllOwnGuild(dto.getOwnGuildIds());
        }


        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
