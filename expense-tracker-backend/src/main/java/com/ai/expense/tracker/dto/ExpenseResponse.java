package com.ai.expense.tracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ExpenseResponse {
    private Long id;
    private String description;
    private Double amount;
    private String category;
    private String aiInsights;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructors
    public ExpenseResponse() {
    }

    public ExpenseResponse(Long id, String description, Double amount, String category,
                           LocalDateTime date, String aiInsights) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.aiInsights = aiInsights;
    }

    // Lombok generates getters and setters
}