CREATE TABLE fixed_expense_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    paidAt DATE NOT NULL,
    FOREIGN KEY (fixed_expense_id) REFERENCES fixed_expenses(id)
);