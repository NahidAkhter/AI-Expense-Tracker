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