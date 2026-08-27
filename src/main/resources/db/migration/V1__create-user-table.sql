CREATE EXTENSION IF NOT EXISTS pgcrypto;


CREATE TABLE Users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    profileImageUrl VARCHAR(255),
    password VARCHAR(100) NOT NULL 
)