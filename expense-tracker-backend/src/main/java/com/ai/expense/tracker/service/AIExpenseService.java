package com.ai.expense.tracker.service;

import com.ai.expense.tracker.model.Expense;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIExpenseService {

    private static final Logger logger = LoggerFactory.getLogger(AIExpenseService.class);

    private final ChatClient chatClient;

    private final boolean aiEnabled;

    public AIExpenseService(ObjectProvider<ChatClient> chatClientProvider, @Value("${app.ai.enabled:true}") boolean aiEnabled) {
        this.chatClient = chatClientProvider.getIfAvailable();
        // disable AI if no ChatClient bean available
        this.aiEnabled = aiEnabled && this.chatClient != null;
    }

    public String categorizeExpense(String description) {
        if (!aiEnabled) {
            logger.info("AI categorization disabled, using rule-based fallback");
            return fallbackCategorization(description);
        }

        try {
            String prompt = """
                    Categorize this expense description into exactly one of these categories: 
                    FOOD, TRANSPORT, ENTERTAINMENT, SHOPPING, BILLS, HEALTH, OTHER.
                    
                    Description: '{description}'
                    
                    Respond with ONLY the category name in uppercase. No explanations.
                    """;

            PromptTemplate promptTemplate = new PromptTemplate(prompt);
            Map<String, Object> params = Map.of("description", description);

            String category = chatClient.prompt()
                    .user(promptTemplate.render(params))
                    .call()
                    .content();

            String cleanedCategory = cleanCategoryResponse(category);
            logger.info("AI categorized '{}' as: {}", description, cleanedCategory);
            return cleanedCategory;

        } catch (Exception e) {
            logger.warn("AI categorization failed for '{}', using fallback", description, e);
            return fallbackCategorization(description);
        }
    }

    public String generateSpendingInsights(List<Expense> expenses) {
        if (!aiEnabled || expenses.isEmpty()) {
            return generateFallbackInsights(expenses);
        }

        try {
            String expenseSummary = buildExpenseSummary(expenses);
            double total = expenses.stream()
                    .map(Expense::getAmount)
                    .filter(a -> a != null)
                    .mapToDouble(BigDecimal::doubleValue)
                    .sum();

            String prompt = """
                    Analyze these expenses and provide 2-3 concise, actionable insights about spending patterns.
                    Focus on practical advice and specific recommendations.
                    Keep it under 200 words and use a friendly, helpful tone.
                    
                    Total Expenses: {count}
                    Total Amount: ${total}
                    
                    Recent Expenses:
                    {expenses}
                    
                    Provide insights in this format:
                    🎯 **Spending Patterns:**
                    [Your analysis here]
                    
                    💡 **Smart Recommendations:**
                    [Your recommendations here]
                    
                    📈 **Optimization Tips:**
                    [Your tips here]
                    """;

            PromptTemplate promptTemplate = new PromptTemplate(prompt);
            Map<String, Object> params = Map.of(
                    "count", expenses.size(),
                    "total", String.format("%.2f", total),
                    "expenses", expenseSummary
            );

            String insights = chatClient.prompt()
                    .user(promptTemplate.render(params))
                    .call()
                    .content();

            logger.info("Generated AI insights for {} expenses", expenses.size());
            return insights.trim();

        } catch (Exception e) {
            logger.error("AI insights generation failed", e);
            return generateFallbackInsights(expenses);
        }
    }

    private String buildExpenseSummary(List<Expense> expenses) {
        return expenses.stream()
                .limit(15) // Limit to prevent token overflow
                .map(expense -> String.format("- %s: $%.2f (%s)",
                        expense.getDescription(),
                        expense.getAmount() != null ? expense.getAmount().doubleValue() : 0.0,
                        expense.getCategory()))
                .collect(Collectors.joining("\n"));
    }

    private String fallbackCategorization(String description) {
        String lowerDesc = description.toLowerCase();

        if (lowerDesc.contains("food") || lowerDesc.contains("grocery") || lowerDesc.contains("restaurant")) {
            return "FOOD";
        } else if (lowerDesc.contains("bus") || lowerDesc.contains("train") || lowerDesc.contains("uber") ||
                lowerDesc.contains("taxi") || lowerDesc.contains("gas") || lowerDesc.contains("fuel")) {
            return "TRANSPORT";
        } else if (lowerDesc.contains("movie") || lowerDesc.contains("netflix") || lowerDesc.contains("game") ||
                lowerDesc.contains("concert") || lowerDesc.contains("entertainment")) {
            return "ENTERTAINMENT";
        } else if (lowerDesc.contains("bill") || lowerDesc.contains("electric") || lowerDesc.contains("water") ||
                lowerDesc.contains("rent") || lowerDesc.contains("mortgage")) {
            return "BILLS";
        } else if (lowerDesc.contains("medical") || lowerDesc.contains("doctor") || lowerDesc.contains("hospital") ||
                lowerDesc.contains("pharmacy")) {
            return "HEALTH";
        } else if (lowerDesc.contains("shop") || lowerDesc.contains("store") || lowerDesc.contains("mall") ||
                lowerDesc.contains("amazon")) {
            return "SHOPPING";
        } else {
            return "OTHER";
        }
    }

    private String generateFallbackInsights(List<Expense> expenses) {
        if (expenses.isEmpty()) {
            return "No expenses to analyze. Start adding expenses to get insights!";
        }

        double total = expenses.stream()
                .map(Expense::getAmount)
                .filter(a -> a != null)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
        Map<String, Double> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory,
                        Collectors.summingDouble(e -> e.getAmount() != null ? e.getAmount().doubleValue() : 0.0)));

        String topCategory = categoryTotals.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("OTHER");

        return String.format("""
                🎯 **Spending Patterns:**
                You've spent $%.2f across %d expenses. Your top spending category is %s.
                
                💡 **Smart Recommendations:**
                Track your %s spending closely as it's your largest category. Consider setting a monthly budget.
                
                📈 **Optimization Tips:**
                Review recurring expenses and identify areas where you can reduce costs. Small savings add up over time!
                """, total, expenses.size(), topCategory, topCategory);
    }

    private String cleanCategoryResponse(String category) {
        if (category == null) return "OTHER";

        String cleaned = category.trim().toUpperCase();

        // Extract category from potential AI responses
        if (cleaned.contains("FOOD")) return "FOOD";
        if (cleaned.contains("TRANSPORT")) return "TRANSPORT";
        if (cleaned.contains("ENTERTAINMENT")) return "ENTERTAINMENT";
        if (cleaned.contains("SHOPPING")) return "SHOPPING";
        if (cleaned.contains("BILLS")) return "BILLS";
        if (cleaned.contains("HEALTH")) return "HEALTH";

        return "OTHER";
    }
}