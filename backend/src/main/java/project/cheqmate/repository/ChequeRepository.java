package project.cheqmate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.cheqmate.model.Cheque;
import project.cheqmate.model.User;

import java.util.List;

public interface ChequeRepository extends JpaRepository<Cheque, Integer> {
    long countByOwner(User owner);
    long countByWhoPaid(User whoPaid);

    List<Cheque> findByWhoPaid(User whoPaid);
}
