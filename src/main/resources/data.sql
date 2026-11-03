-- Sample product data
INSERT INTO products (name, description, price, quantity) VALUES
('Laptop', '14-inch lightweight laptop', 899.99, 25),
('Wireless Mouse', 'Ergonomic Bluetooth mouse', 24.50, 150),
('Mechanical Keyboard', 'RGB backlit, blue switches', 79.90, 60),
('Monitor', '27-inch 4K IPS display', 329.00, 40),
('USB-C Hub', '7-in-1 multiport adapter', 39.99, 200)
ON CONFLICT DO NOTHING;
