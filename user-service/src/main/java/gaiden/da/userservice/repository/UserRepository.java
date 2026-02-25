package gaiden.da.userservice.repository;

import gaiden.da.userservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    @NonNull
    Optional<User> findById(Long userId);

    List<User> findByUsernameNot(String username);

    List<User> findUsersByIdIn(List<Long> id);

    boolean existsById(Long userId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);


}
