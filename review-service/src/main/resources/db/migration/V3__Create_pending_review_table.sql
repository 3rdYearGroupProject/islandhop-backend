CREATE TABLE pending_reviews (
    review_id SERIAL PRIMARY KEY,
    review TEXT NOT NULL,
    approved BOOLEAN DEFAULT FALSE,
    approved_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);