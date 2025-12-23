CREATE TABLE IF NOT EXISTS attendance (
  id SERIAL PRIMARY KEY,
  user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  attended_date DATE NOT NULL,
  created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (user_id, attended_date)
);


