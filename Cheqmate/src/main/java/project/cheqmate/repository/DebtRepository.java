package project.cheqmate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.cheqmate.model.Debt;
import project.cheqmate.model.User;

import java.util.List;
import java.util.Optional;

public interface DebtRepository extends JpaRepository<Debt, Integer> {
    List<Debt> findByCreditor(User creditor);
    List<Debt> findByDebtor(User debtor);
    Optional<Debt> findByCreditorAndDebtor(User creditor, User debtor);
}
