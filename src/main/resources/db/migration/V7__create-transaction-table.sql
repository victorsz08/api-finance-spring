CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    type VARCHAR(10) NOT NULL,
    date DATE NOT NULL,
    user_id UUID NOT NULL,
    category_id UUID NOT NULL,
    installment_id UUID,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (installment_id) REFERENCES installments(id)
);

CREATE INDEX idx_transactions_user_date ON transactions (user_id, date);