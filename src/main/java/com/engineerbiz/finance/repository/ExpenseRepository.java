package com.engineerbiz.finance.repository;

import com.engineerbiz.finance.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    Optional<Expense> findByExpenseNumber(String expenseNumber);

    boolean existsByExpenseNumber(String expenseNumber);

    List<Expense> findByProjectIdOrderByExpenseDateDesc(UUID projectId);
}
