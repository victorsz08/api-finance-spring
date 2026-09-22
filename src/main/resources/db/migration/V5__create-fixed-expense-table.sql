CREATE TABLE fixed_expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    due_day INTEGER NOT NULL CHECK (due_day BETWEEN 1 AND 31),
    active BOOLEAN NOT NULL DEFAULT true,
    user_id UUID,
    category_id UUID,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE cascade,
    FOREIGN KEY (category_id) REFERENCES categories (id)
);