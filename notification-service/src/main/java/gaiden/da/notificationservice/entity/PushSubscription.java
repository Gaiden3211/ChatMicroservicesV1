package gaiden.da.notificationservice.entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "push_subscriptions")
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String userId;


    @Column(nullable = false, length = 500)
    private String endpoint;

    @Column(nullable = false)
    private String p256dh;

    @Column(nullable = false)
    private String auth;


}
