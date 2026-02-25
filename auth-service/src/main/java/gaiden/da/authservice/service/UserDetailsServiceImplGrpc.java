package gaiden.da.authservice.service;

import gaiden.da.user.GetUserCredentialsRequest;
import gaiden.da.user.UserCredentialDto;
import gaiden.da.user.*;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImplGrpc implements UserDetailsService {

    @GrpcClient("user-service")
    private UserServiceGrpcGrpc.UserServiceGrpcBlockingStub userStub;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {

            GetUserCredentialsRequest request = GetUserCredentialsRequest.newBuilder()
                    .setUsername(username)
                    .build();


            UserCredentialDto credentials = userStub.getUserCredentialsByUsername(request);

            if (credentials == null) {
                throw new UsernameNotFoundException("User credentials not found for: " + username);
            }


            return new User(
                    credentials.getUsername(),
                    credentials.getPassword(),
                    Collections.emptyList()
            );

        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                throw new UsernameNotFoundException("User not found: " + username, e);
            } else {
                throw new RuntimeException("Error fetching user details: " + e.getMessage(), e);
            }
        }
    }
}