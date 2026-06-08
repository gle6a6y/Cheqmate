package project.cheqmate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.cheqmate.model.DeviceToken;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Integer> {

    List<DeviceToken> findByUsername(String username);

    Optional<DeviceToken> findByToken(String token);

    void deleteByToken(String token);
}
