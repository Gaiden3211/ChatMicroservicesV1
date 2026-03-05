package gaiden.da.notificationservice.repository;

import gaiden.da.notificationservice.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    List<PushSubscription> findAllByUserId(String userId);

    Optional<PushSubscription> findByEndpoint(String endpoint);
}
