package com.ai.expense.tracker.service;

import com.ai.expense.tracker.dto.ExpenseRequest;
import com.ai.expense.tracker.dto.ExpenseResponse;
import com.ai.expense.tracker.model.Expense;
import com.ai.expense.tracker.repository.ExpenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExpenseService {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseRepository expenseRepository;
    private final AIExpenseService aiExpenseService;

    public ExpenseService(ExpenseRepository expenseRepository, AIExpenseService aiExpenseService) {
        this.expenseRepository = expenseRepository;
        this.aiExpenseService = aiExpenseService;
    }

    public List<ExpenseResponse> getAllExpenses() {
        logger.info("Fetching all expenses");
        return expenseRepository.findAllByOrderByDateDesc().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ExpenseResponse createExpense(ExpenseRequest request) {
        logger.info("Creating new expense: {}", request.getDescription());

        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        if (request.getAmount() != null) {
            expense.setAmount(BigDecimal.valueOf(request.getAmount()));
        }

        // Set date - use provided or current time
        if (request.getDate() != null && !request.getDate().isEmpty()) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(request.getDate());
                expense.setDate(dateTime);
            } catch (Exception e) {
                logger.warn("Invalid date format: {}, using current time", request.getDate());
                expense.setDate(LocalDateTime.now());
            }
        } else {
            expense.setDate(LocalDateTime.now());
        }

        // AI categorization
        String category = aiExpenseService.categorizeExpense(request.getDescription());
        expense.setCategory(category);

        Expense savedExpense = expenseRepository.save(expense);
        logger.info("Expense created with ID: {}", savedExpense.getId());

        return convertToResponse(savedExpense);
    }

    public void deleteExpense(Long id) {
        logger.info("Deleting expense with ID: {}", id);
        if (!expenseRepository.existsById(id)) {
            throw new RuntimeException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
    }

    public String generateInsights() {
        logger.info("Generating AI insights");
        List<Expense> expenses = expenseRepository.findAll();
        return aiExpenseService.generateSpendingInsights(expenses);
    }

    public List<ExpenseResponse> getExpensesByCategory(String category) {
        logger.info("Fetching expenses for category: {}", category);
        return expenseRepository.findByCategoryOrderByDateDesc(category).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ExpenseResponse> searchExpenses(String query) {
        logger.info("Searching expenses with query: {}", query);
        return expenseRepository.findByDescriptionContainingIgnoreCase(query).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Double getTotalSpent() {
        BigDecimal total = expenseRepository.getTotalAmount();
        return total != null ? total.doubleValue() : 0.0;
    }

    public Long getTotalCount() {
        return expenseRepository.getTotalCount();
    }

    private ExpenseResponse convertToResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setDescription(expense.getDescription());
        if (expense.getAmount() != null) {
            response.setAmount(expense.getAmount().doubleValue());
        }
        response.setCategory(expense.getCategory());
        response.setAiInsights(expense.getAiInsights());
        response.setDate(expense.getDate());
        response.setCreatedAt(expense.getCreatedAt());
        response.setUpdatedAt(expense.getUpdatedAt());
        return response;
    }
}
