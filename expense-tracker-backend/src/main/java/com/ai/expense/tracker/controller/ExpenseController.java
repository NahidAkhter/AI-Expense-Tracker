package com.ai.expense.tracker.controller;

import com.ai.expense.tracker.dto.ApiResponse;
import com.ai.expense.tracker.dto.ExpenseRequest;
import com.ai.expense.tracker.dto.ExpenseResponse;
import com.ai.expense.tracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class ExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getAllExpenses() {
        try {
            logger.info("GET /api/expenses - Fetching all expenses");
            List<ExpenseResponse> expenses = expenseService.getAllExpenses();
            return ResponseEntity.ok(ApiResponse.success(expenses));
        } catch (Exception e) {
            logger.error("Error fetching expenses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch expenses: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @Valid @RequestBody ExpenseRequest expenseRequest) {
        try {
            logger.info("POST /api/expenses - Creating new expense: {}", expenseRequest.getDescription());
            ExpenseResponse createdExpense = expenseService.createExpense(expenseRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Expense created successfully", createdExpense));
        } catch (Exception e) {
            logger.error("Error creating expense", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create expense: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable Long id) {
        try {
            logger.info("DELETE /api/expenses/{} - Deleting expense", id);
            expenseService.deleteExpense(id);
            return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully", null));
        } catch (RuntimeException e) {
            logger.error("Error deleting expense with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Expense not found"));
        } catch (Exception e) {
            logger.error("Error deleting expense", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete expense: " + e.getMessage()));
        }
    }

    @GetMapping("/insights")
    public ResponseEntity<ApiResponse<String>> getInsights() {
        try {
            logger.info("GET /api/expenses/insights - Generating AI insights");
            String insights = expenseService.generateInsights();
            return ResponseEntity.ok(ApiResponse.success(insights));
        } catch (Exception e) {
            logger.error("Error generating insights", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate insights: " + e.getMessage()));
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesByCategory(
            @PathVariable String category) {
        try {
            logger.info("GET /api/expenses/category/{} - Fetching expenses by category", category);
            List<ExpenseResponse> expenses = expenseService.getExpensesByCategory(category);
            return ResponseEntity.ok(ApiResponse.success(expenses));
        } catch (Exception e) {
            logger.error("Error fetching expenses by category: {}", category, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch expenses by category: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> searchExpenses(
            @RequestParam String q) {
        try {
            logger.info("GET /api/expenses/search?q={} - Searching expenses", q);
            List<ExpenseResponse> expenses = expenseService.searchExpenses(q);
            return ResponseEntity.ok(ApiResponse.success(expenses));
        } catch (Exception e) {
            logger.error("Error searching expenses with query: {}", q, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search expenses: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/total")
    public ResponseEntity<ApiResponse<Double>> getTotalSpent() {
        try {
            logger.info("GET /api/expenses/stats/total - Getting total spent");
            Double total = expenseService.getTotalSpent();
            return ResponseEntity.ok(ApiResponse.success(total));
        } catch (Exception e) {
            logger.error("Error getting total spent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get total spent: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/count")
    public ResponseEntity<ApiResponse<Long>> getTotalCount() {
        try {
            logger.info("GET /api/expenses/stats/count - Getting total count");
            Long count = expenseService.getTotalCount();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            logger.error("Error getting total count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get total count: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Service is healthy", null));
    }
}