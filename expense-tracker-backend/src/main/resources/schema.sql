-- filepath: src/main/resources/schema.sql
-- Schema-level DDL: indexes and views for the expenses table

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_expenses_date_desc ON expenses(date DESC);
CREATE INDEX IF NOT EXISTS idx_expenses_category ON expenses(category);
CREATE INDEX IF NOT EXISTS idx_expenses_amount ON expenses(amount);
CREATE INDEX IF NOT EXISTS idx_expenses_created_at ON expenses(created_at);

-- Create a view for category summaries
CREATE OR REPLACE VIEW expense_category_summary AS
SELECT
    category,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    AVG(amount) as average_amount,
    MAX(amount) as max_amount,
    MIN(amount) as min_amount
FROM expenses
GROUP BY category
ORDER BY total_amount DESC;

-- Create a view for monthly spending
CREATE OR REPLACE VIEW expense_monthly_summary AS
SELECT
    DATE_TRUNC('month', date) as month,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    AVG(amount) as average_amount
FROM expenses
GROUP BY DATE_TRUNC('month', date)
ORDER BY month DESC;

