package gaiden.da.guildservice.grpc.guild;

import gaiden.da.guildservice.repository.interfaces.MemberRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import gaiden.da.grpc.guild.GuildServiceGrpc;
import gaiden.da.grpc.guild.AccessRequest;
import gaiden.da.grpc.guild.AccessResponse;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class GuildGrpcService extends GuildServiceGrpc.GuildServiceImplBase {

    private final MemberRepository memberRepository;

    @Override
    public void checkGuildAccess(AccessRequest request, StreamObserver<AccessResponse> responseObserver) {


        boolean isMember = memberRepository.existsByGuildIdAndUserId(
                Long.parseLong(request.getGuildId()),
                Long.parseLong(request.getUserId())
        );


        AccessResponse response = AccessResponse.newBuilder()
                .setHasAccess(isMember)
                .build();


        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
