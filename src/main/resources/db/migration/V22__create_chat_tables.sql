-- Bảng chat_rooms
CREATE TABLE chat_rooms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    course_id INT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    created_by_user_id INT REFERENCES users(id),
    active BOOLEAN DEFAULT TRUE,
    deleted_on TIMESTAMP NULL,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP,
    created_by INT,
    updated_by INT
);

-- Bảng chat_room_members (many-to-many relationship)
CREATE TABLE chat_room_members (
    id SERIAL PRIMARY KEY,
    chat_room_id INT NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    last_seen_at TIMESTAMP,
    is_online BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    deleted_on TIMESTAMP NULL,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP,
    created_by INT,
    updated_by INT,
    UNIQUE(chat_room_id, user_id) -- Mỗi user chỉ có thể là member 1 lần
);

-- Bảng chat_messages
CREATE TABLE chat_messages (
    id SERIAL PRIMARY KEY,
    chat_room_id INT NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    sender_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT,
    message_type VARCHAR(20) DEFAULT 'TEXT', -- TEXT, IMAGE, FILE, SYSTEM
    file_url VARCHAR(500),
    file_name VARCHAR(255),
    file_size BIGINT,
    file_type VARCHAR(100),
    edited BOOLEAN DEFAULT FALSE,
    deleted BOOLEAN DEFAULT FALSE,
    deleted_on TIMESTAMP NULL,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP,
    created_by INT,
    updated_by INT
);

-- Indexes để tối ưu performance
CREATE INDEX idx_chat_rooms_course_id ON chat_rooms(course_id);
CREATE INDEX idx_chat_room_members_room_id ON chat_room_members(chat_room_id);
CREATE INDEX idx_chat_room_members_user_id ON chat_room_members(user_id);
CREATE INDEX idx_chat_room_members_online ON chat_room_members(chat_room_id, is_online) WHERE is_online = TRUE;
CREATE INDEX idx_chat_messages_room_id ON chat_messages(chat_room_id, deleted, created_on DESC);
CREATE INDEX idx_chat_messages_sender_id ON chat_messages(sender_id);

