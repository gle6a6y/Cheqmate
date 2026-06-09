package project.cheqmate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.cheqmate.model.Group;
import project.cheqmate.model.User;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Integer> {
    Optional<Group> findByGroupName(String groupName);
    List<Group> findByMembersContaining(User member);
    long countByMembersContaining(User member);

    @Query("SELECT DISTINCT g FROM Group g LEFT JOIN FETCH g.cheques WHERE g.id = :id")
    Optional<Group> findByIdWithCheques(@Param("id") int id);
}
