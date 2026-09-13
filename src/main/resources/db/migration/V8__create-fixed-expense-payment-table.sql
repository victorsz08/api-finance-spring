CREATE TABLE fixed_expense_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    paidAt DATE NOT NULL,
    fixed_expense_id UUID,
    FOREIGN KEY (fixed_expense_id) REFERENCES fixed_expenses (id)
);