CREATE TABLE guide_review (
    review_id SERIAL PRIMARY KEY,
    guide_email VARCHAR(255) NOT NULL,
    review TEXT NOT NULL,
    rating INT CHECK (rating >= 0 AND rating <= 5),
    status VARCHAR(20) CHECK (status IN ('approved', 'banned', 'pending')) DEFAULT 'pending'
);