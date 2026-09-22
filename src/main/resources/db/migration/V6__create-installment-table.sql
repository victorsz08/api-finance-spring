CREATE TABLE installment_purchases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    description VARCHAR(255) NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL,
    total_installments INTEGER NOT NULL CHECK(total_installments > 0),
    purchase_date DATE NOT NULL,
    category_id UUID,
    user_id UUID,
    FOREIGN KEY (category_id) REFERENCES categories (id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE cascade
);

CREATE TABLE installments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    number INTEGER NOT NULL CHECK(number > 0),
    amount NUMERIC(10,2) NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    purchase_id UUID,
    FOREIGN KEY (purchase_id) REFERENCES installment_purchases (id) ON DELETE cascade
)