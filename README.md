## 🚀 Yêu cầu môi trường

- **Java**: 17+
- **Gradle**: Wrapper có sẵn (`./gradlew`)
- **Database**: PostgreSQL 17+
- **Git**

-- Cài đặt Docker
-- Lệnh chạy:
 - Bước 1: git clone https://github.com/DoAnPTIT/elearning-backend.git
 - Bước 2: docker compose up -d
 - Bước 3: Chạy sql migration trong resource/db
 - Bước 4: ./gradlew clean build
 - Bước 5: ./gradlew bootRun
 