CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    user_id UUID,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE cascade
);