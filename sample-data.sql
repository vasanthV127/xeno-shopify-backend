-- Sample data for demo purposes
-- Run this after deploying to populate the database with realistic data

-- Insert sample tenant (Use this for demo)
INSERT INTO tenants (tenant_id, store_name, shopify_domain, shopify_access_token, email, password, active, created_at, updated_at)
VALUES 
  ('550e8400-e29b-41d4-a716-446655440000', 
   'Demo Fashion Store', 
   'demo-fashion.myshopify.com', 
   'shpat_demo_token_for_testing_purposes_only', 
   'demo@example.com', 
   '$2a$10$rXQ3K7JZ9fZxHxQ8nYyJHO8kZHxHxQ8nYyJHO8kZHxHxQ8nYyJHO8k', -- password: demo123
   true, 
   NOW(), 
   NOW());

-- Insert sample customers
INSERT INTO customers (tenant_id, shopify_customer_id, email, first_name, last_name, phone, orders_count, total_spent, last_order_date, created_at, updated_at)
VALUES 
  ('550e8400-e29b-41d4-a716-446655440000', 'cust_001', 'john.doe@email.com', 'John', 'Doe', '+1234567890', 25, 5240.80, '2025-12-03', NOW(), NOW()),
  ('550e8400-e29b-41d4-a716-446655440000', 'cust_002', 'jane.smith@email.com', 'Jane', 'Smith', '+1234567891', 38, 4890.25, '2025-12-02', NOW(), NOW()),
  ('550e8400-e29b-41d4-a716-446655440000', 'cust_003', 'mike.johnson@email.com', 'Mike', 'Johnson', '+1234567892', 42, 6780.50, '2025-12-04', NOW(), NOW()),
  ('550e8400-e29b-41d4-a716-446655440000', 'cust_004', 'sarah.williams@email.com', 'Sarah', 'Williams', '+1234567893', 18, 3240.75, '2025-12-01', NOW(), NOW()),
  ('550e8400-e29b-41d4-a716-446655440000', 'cust_005', 'david.brown@email.com', 'David', 'Brown', '+1234567894', 31, 4120.60, '2025-12-03', NOW(), NOW()),
  ('550e8400-e29b-41d4-a716-446655440000', 'cust_006', 'emily.davis@email.com', 'Emily', 'Davis', '+1234567895', 15, 2850.40, '2025-11-30', NOW(), NOW()),
  ('550e8400-e29b-41d4-a716-446655440000', 'cust_007', 'chris.wilson@email.com', 'Chris', 'Wilson', '+1234567896', 22, 3560.90, '2025-12-02', NOW(), NOW()),
  ('550e8400-e29b-41d4-a716-446655440000', 'cust_008', 'lisa.moore@email.com', 'Lisa', 'Moore', '+1234567897', 28, 4670.30, '2025-12-04', NOW(), NOW());

-- Insert sample products
INSERT INTO products (tenant_id, shopify_product_id, title, description, vendor, product_type, price, inventory_quantity, image_url, status, created_at, updated_at)
VALUES 
  ('550e8400-e29b-41d4-a716-446655440000', 'prod_001', 'Classic Blue T-Shirt', 'Comfortable cotton t-shirt', 'FashionBrand', 'Apparel', 29.99, 150, 'https://via.placeholder.com/300', 'active', NOW(), NOW()),
  ('550e8400-e29b-41d4-a716-446655440000', 'prod_002', 'Denim Jeans', 'Stylish slim-fit jeans', 'DenimCo', 'Apparel', 79.99, 85, 'https://via.placeholder.com/300', 'active', NOW(), NOW()),
  ('550e8400-e29b-41d4-a716-446655440000', 'prod_003', 'Leather Jacket', 'Premium leather jacket', 'LuxuryWear', 'Outerwear', 199.99, 30, 'https://via.placeholder.com/300', 'active', NOW(), NOW()),
  ('550e8400-e29b-41d4-a716-446655440000', 'prod_004', 'Sneakers', 'Comfortable running shoes', 'SportGear', 'Footwear', 89.99, 120, 'https://via.placeholder.com/300', 'active', NOW(), NOW()),
  ('550e8400-e29b-41d4-a716-446655440000', 'prod_005', 'Cotton Hoodie', 'Warm hooded sweatshirt', 'ComfortWear', 'Apparel', 49.99, 95, 'https://via.placeholder.com/300', 'active', NOW(), NOW()),
  ('550e8400-e29b-41d4-a716-446655440000', 'prod_006', 'Baseball Cap', 'Adjustable sports cap', 'CapCo', 'Accessories', 24.99, 200, 'https://via.placeholder.com/300', 'active', NOW(), NOW());

-- Insert sample orders with realistic date distribution (last 30 days)
INSERT INTO orders (tenant_id, customer_id, shopify_order_id, order_number, order_date, total_price, subtotal_price, total_tax, financial_status, fulfillment_status, currency, item_count, created_at, updated_at)
SELECT 
  '550e8400-e29b-41d4-a716-446655440000',
  (SELECT id FROM customers WHERE tenant_id = '550e8400-e29b-41d4-a716-446655440000' ORDER BY RANDOM() LIMIT 1),
  'ord_' || LPAD(generate_series::text, 6, '0'),
  '#' || (1000 + generate_series),
  CURRENT_DATE - (RANDOM() * 30)::integer,
  50 + (RANDOM() * 450)::numeric(10,2),
  45 + (RANDOM() * 400)::numeric(10,2),
  (5 + (RANDOM() * 50))::numeric(10,2),
  (ARRAY['paid', 'pending', 'refunded'])[floor(random() * 3 + 1)],
  (ARRAY['fulfilled', 'unfulfilled', 'partial'])[floor(random() * 3 + 1)],
  'USD',
  (1 + (RANDOM() * 5))::integer,
  NOW(),
  NOW()
FROM generate_series(1, 150);

-- Insert order items for each order
INSERT INTO order_items (order_id, shopify_product_id, product_title, variant_title, quantity, price, total_discount, created_at, updated_at)
SELECT 
  o.id,
  p.shopify_product_id,
  p.title,
  'Standard',
  (1 + (RANDOM() * 3))::integer,
  p.price,
  (RANDOM() * 10)::numeric(10,2),
  NOW(),
  NOW()
FROM orders o
CROSS JOIN LATERAL (
  SELECT * FROM products WHERE tenant_id = '550e8400-e29b-41d4-a716-446655440000' 
  ORDER BY RANDOM() LIMIT (1 + (RANDOM() * 2)::integer)
) p
WHERE o.tenant_id = '550e8400-e29b-41d4-a716-446655440000';

-- Update customer statistics based on actual orders
UPDATE customers c
SET 
  orders_count = (SELECT COUNT(*) FROM orders WHERE customer_id = c.id),
  total_spent = (SELECT COALESCE(SUM(total_price), 0) FROM orders WHERE customer_id = c.id),
  last_order_date = (SELECT MAX(order_date) FROM orders WHERE customer_id = c.id)
WHERE tenant_id = '550e8400-e29b-41d4-a716-446655440000';
