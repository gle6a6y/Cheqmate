package project.cheqmate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.cheqmate.model.PersonalExpense;
import project.cheqmate.model.User;

import java.time.LocalDate;
import java.util.List;

public interface PersonalExpenseRepository extends JpaRepository<PersonalExpense, Integer> {
    List<PersonalExpense> findByUserOrderByDateDesc(User user);

    List<PersonalExpense> findByUserAndDateBetweenOrderByDateDesc(
            User user, LocalDate from, LocalDate to);
}
