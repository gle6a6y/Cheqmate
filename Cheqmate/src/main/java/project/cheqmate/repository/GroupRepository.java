package project.cheqmate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.cheqmate.model.Group;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Integer> {
    Optional<Group> findByGroupName(String groupName);
}
