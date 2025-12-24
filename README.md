## 🚀 Yêu cầu môi trường

- **Java**: 17+
- **Gradle**: Wrapper có sẵn (`./gradlew`)
- **Database**: PostgreSQL 17+
- **Git**
- **Docker** và **Docker Compose**

---

## 📦 Cài đặt và chạy môi trường Development

### Bước 1: Clone repository
```bash
git clone https://github.com/DoAnPTIT/elearning-backend.git
cd elearning-backend
```

### Bước 2: Chạy các services (PostgreSQL, MinIO, LocalStack)
```bash
docker compose up -d
```

### Bước 3: Build và chạy ứng dụng
```bash
./gradlew clean build
./gradlew bootRun
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

---

## 🚀 Deploy lên môi trường Production

### Bước 1: Chuẩn bị file cấu hình môi trường

Tạo file `.env.prod` từ template:
```bash
cp env.prod.example .env.prod
```

Chỉnh sửa file `.env.prod` và điền các giá trị thực tế:
```bash
nano .env.prod  # hoặc dùng editor khác
```

**Các biến môi trường quan trọng cần cấu hình:**

1. **Database (PostgreSQL)**
   - `POSTGRES_DB`: Tên database
   - `POSTGRES_USER`: Username database
   - `POSTGRES_PASSWORD`: Mật khẩu mạnh cho database
   - `POSTGRES_PORT`: Port (mặc định 5432)

2. **Redis**
   - `REDIS_PASSWORD`: Mật khẩu cho Redis

3. **JWT Secret**
   - `JWT_SECRET`: Key bí mật để sign JWT (tối thiểu 64 ký tự)
   - Tạo key mạnh: `openssl rand -base64 64`

4. **CORS**
   - `APP_CORS_ALLOWED_ORIGINS`: Domain frontend được phép (ví dụ: `https://yourdomain.com`)

5. **AWS Configuration**
   - `AWS_ACCESS_KEY_ID`: AWS Access Key
   - `AWS_SECRET_ACCESS_KEY`: AWS Secret Key
   - `AWS_REGION`: AWS Region (ví dụ: `us-east-1`)
   - `AWS_S3_BUCKET`: Tên S3 bucket để lưu file
   - `AWS_DYNAMODB_ENDPOINT`: DynamoDB endpoint
   - `AWS_DYNAMODB_BLACKLIST_TABLE_NAME`: Tên bảng DynamoDB cho JWT blacklist

6. **OpenAI Configuration**
   - `SPRING_AI_OPENAI_API_KEY`: OpenAI API key
   - `SPRING_AI_OPENAI_BASE_URL`: OpenAI API URL
   - `SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL`: Model name (ví dụ: `gpt-4`)

### Bước 2: Deploy bằng script tự động (Khuyến nghị)

```bash
./deploy.sh
```

Script này sẽ:
- Kiểm tra file `.env.prod` có tồn tại
- Dừng các container cũ
- Build và khởi động các services mới
- Kiểm tra health của services

### Bước 3: Deploy thủ công

Nếu muốn deploy thủ công:

```bash
# Dừng các container cũ
docker compose -f docker-compose.prod.yml down

# Build và khởi động services
docker compose -f docker-compose.prod.yml up -d --build

# Xem logs
docker compose -f docker-compose.prod.yml logs -f app
```

### Bước 4: Kiểm tra deployment

1. **Kiểm tra containers đang chạy:**
```bash
docker ps --filter "name=elearning"
```

2. **Kiểm tra health endpoint:**
```bash
curl http://localhost:8080/actuator/health
```

3. **Xem logs:**
```bash
# Logs của ứng dụng
docker compose -f docker-compose.prod.yml logs -f app

# Logs của tất cả services
docker compose -f docker-compose.prod.yml logs -f
```

---

## 🔧 Các lệnh hữu ích

### Quản lý services
```bash
# Dừng tất cả services
docker compose -f docker-compose.prod.yml down

# Dừng và xóa volumes (⚠️ Xóa dữ liệu)
docker compose -f docker-compose.prod.yml down -v

# Restart services
docker compose -f docker-compose.prod.yml restart

# Xem status
docker compose -f docker-compose.prod.yml ps
```

### Backup và Restore Database
```bash
# Backup database
docker exec elearning-postgres-prod pg_dump -U elearning_user elearning_prod > backup.sql

# Restore database
docker exec -i elearning-postgres-prod psql -U elearning_user elearning_prod < backup.sql
```

### Cập nhật ứng dụng
```bash
# Pull code mới nhất
git pull origin main

# Rebuild và restart
docker compose -f docker-compose.prod.yml up -d --build app
```

---

## ⚠️ Lưu ý quan trọng

1. **Bảo mật**: 
   - Không commit file `.env.prod` lên Git
   - Sử dụng mật khẩu mạnh cho database và Redis
   - JWT secret phải có độ dài tối thiểu 64 ký tự

2. **Database**:
   - Đảm bảo có backup database thường xuyên
   - Sử dụng SSL connection trong production (đã cấu hình trong docker-compose.prod.yml)

3. **Monitoring**:
   - Thiết lập monitoring cho services
   - Theo dõi logs thường xuyên
   - Thiết lập alerts cho health checks

4. **Network**:
   - Cấu hình firewall để chỉ mở các port cần thiết
   - Sử dụng reverse proxy (Nginx/Traefik) nếu cần
   - Cấu hình SSL/TLS cho HTTPS

---

## 📚 Tài liệu tham khảo

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Production Ready](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
