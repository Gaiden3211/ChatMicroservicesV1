//package gaiden.da.authservice.service;
//
//import feign.FeignException;
//import gaiden.da.authservice.client.UserServiceClient;
//import gaiden.da.authservice.dto.UserCredentialDto;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.util.Collections;
//
//@Service
//@RequiredArgsConstructor
//public class UserDetailsServiceImpl implements UserDetailsService {
//
//    private final UserServiceClient userServiceClient;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//
//        try{
//            UserCredentialDto userCredentialDto = userServiceClient.getUserCredentialsByUsername(username);
//
//            if (userCredentialDto == null) {
//                throw new UsernameNotFoundException("User not found : " + username);
//            }
//
//            return new User(userCredentialDto.getUsername(),userCredentialDto.getPassword(), Collections.emptyList());
//        } catch (FeignException.NotFound e) {
//            throw new UsernameNotFoundException("User not found : " + username, e);
//        }catch (Exception e) {
//            throw new UsernameNotFoundException("Error fetching user details for: " + username, e);
//        }
//    }
//
//
//
//
//}
