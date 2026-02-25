//package gaiden.da.guildservice.client;
//import gaiden.da.user.GetUsersByIdsRequest;
//import gaiden.da.user.GetUsersByIdsResponse;
//import gaiden.da.user.UserResponse;
//import gaiden.da.user.UserServiceGrpcGrpc;
//import net.devh.boot.grpc.client.inject.GrpcClient;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//public class UserGrpcClient {
//
//    // "user-service" — это имя клиента, которое нужно прописать в application.yml (см. ниже)
//    @GrpcClient("user-service")
//    private UserServiceGrpcGrpc.UserServiceGrpcBlockingStub userServiceStub;
//
//    /**
//     * Возвращает Map: UserId -> Username
//     */
//    public Map<Long, String> getUsernames(List<Long> userIds) {
//        if (userIds.isEmpty()) {
//            return Map.of();
//        }
//
//        GetUsersByIdsRequest request = GetUsersByIdsRequest.newBuilder()
//                .addAllUserIds(userIds)
//                .build();
//
//        GetUsersByIdsResponse response = userServiceStub.getUsersByIds(request);
//
//        // Превращаем список UserResponse в удобную Map<ID, Username>
//        return response.getUsersList().stream()
//                .collect(Collectors.toMap(
//                        UserResponse::getId,
//                        UserResponse::getUsername,
//                        (existing, replacement) -> existing // На случай дубликатов (хотя id уникальны)
//                ));
//    }
//}
