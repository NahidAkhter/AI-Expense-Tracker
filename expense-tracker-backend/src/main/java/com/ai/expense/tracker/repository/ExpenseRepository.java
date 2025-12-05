package com.ai.expense.tracker.repository;

import com.ai.expense.tracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByOrderByDateDesc();

    List<Expense> findByCategoryOrderByDateDesc(String category);

    @Query("SELECT e.category, SUM(e.amount) FROM Expense e GROUP BY e.category")
    List<Object[]> findCategoryTotals();

    @Query("SELECT e FROM Expense e WHERE LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Expense> findByDescriptionContainingIgnoreCase(String query);

    @Query("SELECT SUM(e.amount) FROM Expense e")
    BigDecimal getTotalAmount();

    @Query("SELECT COUNT(e) FROM Expense e")
    Long getTotalCount();
}