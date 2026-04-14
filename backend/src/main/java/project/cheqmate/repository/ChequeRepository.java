package project.cheqmate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.cheqmate.model.Cheque;

public interface ChequeRepository extends JpaRepository<Cheque, Integer> {
}
