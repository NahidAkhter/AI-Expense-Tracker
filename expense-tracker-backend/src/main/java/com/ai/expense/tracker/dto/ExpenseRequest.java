package com.ai.expense.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpenseRequest {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    private String date; // Optional, will use current time if not provided

    // Default constructor
    public ExpenseRequest() {
    }

    // Constructor for easy testing
    public ExpenseRequest(String description, Double amount) {
        this.description = description;
        this.amount = amount;
    }

    // Lombok generates getters and setters
}