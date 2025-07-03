CREATE TABLE driver_reviews (
    review_id SERIAL PRIMARY KEY,
    driver_email VARCHAR(255) NOT NULL,
    review TEXT NOT NULL,
    rating INT CHECK (rating >= 0 AND rating <= 5),
    status VARCHAR(20) CHECK (status IN ('approved', 'banned', 'pending')) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);