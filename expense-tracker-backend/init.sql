-- Initialize expense database with sample data and optimizations

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

-- Insert sample data for development
INSERT INTO expenses (description, amount, date, category, created_at, updated_at)
VALUES
    ('Groceries at Whole Foods', 85.50, NOW() - INTERVAL '1 day', 'FOOD', NOW(), NOW()),
    ('Uber ride to airport', 45.25, NOW() - INTERVAL '2 days', 'TRANSPORT', NOW(), NOW()),
    ('Netflix subscription', 15.99, NOW() - INTERVAL '5 days', 'ENTERTAINMENT', NOW(), NOW()),
    ('Electricity bill', 120.00, NOW() - INTERVAL '7 days', 'BILLS', NOW(), NOW()),
    ('Dinner at Italian restaurant', 65.80, NOW() - INTERVAL '1 day', 'FOOD', NOW(), NOW()),
    ('Amazon shopping', 89.99, NOW() - INTERVAL '3 days', 'SHOPPING', NOW(), NOW()),
    ('Gym membership', 35.00, NOW() - INTERVAL '10 days', 'HEALTH', NOW(), NOW()),
    ('Gas station', 45.75, NOW() - INTERVAL '2 days', 'TRANSPORT', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Grant permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO expenseuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO expenseuser;