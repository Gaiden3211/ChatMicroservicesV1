//package gaiden.da.authservice.client;
//
//import feign.Response;
//import feign.codec.ErrorDecoder;
//import gaiden.da.authservice.exceptionHandler.exceptions.CustomFeignException;
//import org.springframework.stereotype.Component;
//
//@Component
//public class FeignErrorDecoder implements ErrorDecoder {
//    private final ErrorDecoder defaultDecoder = new Default();
//
//    @Override
//    public Exception decode(String methodKey, Response response) {
//        String message = "Error in user-service: " + response.reason();
//
//        if(methodKey.contains("createUser")) {
//            if (response.status() == 409) {
//                return new CustomFeignException.UserAlreadyExists("User already exists");
//            }
//            if (response.status() == 400) {
//                return new CustomFeignException.BadRequest("Bad data for user creation.");
//            }
//        }
//
//        if (response.status() == 404) {
//            return new CustomFeignException.UserNotFound("User not found");
//        }
//        return defaultDecoder.decode(methodKey, response);
//    }
//}
