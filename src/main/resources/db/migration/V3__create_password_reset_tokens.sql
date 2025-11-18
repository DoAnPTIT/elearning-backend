CREATE TABLE password_reset_tokens (
                                       id SERIAL PRIMARY KEY,
                                       user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                       token VARCHAR(255) NOT NULL,
                                       expiry_date TIMESTAMP NOT NULL,
                                       created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       delete_on BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
