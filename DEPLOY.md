# 🚀 Hướng dẫn Deploy Production - Quick Start

## Bước nhanh để deploy

### 1. Tạo file cấu hình
```bash
cp env.prod.example .env.prod
nano .env.prod  # Điền các giá trị thực tế
```

### 2. Deploy
```bash
./deploy.sh
```

### 3. Kiểm tra
```bash
# Xem logs
docker compose -f docker-compose.prod.yml logs -f app

# Kiểm tra health
curl http://localhost:8080/actuator/health
```

## Checklist trước khi deploy

- [ ] Đã tạo và cấu hình file `.env.prod`
- [ ] Đã tạo JWT secret mạnh (tối thiểu 64 ký tự)
- [ ] Đã cấu hình mật khẩu database và Redis mạnh
- [ ] Đã cấu hình AWS credentials (nếu dùng AWS)
- [ ] Đã cấu hình CORS origins cho frontend
- [ ] Đã backup database (nếu đang update)
- [ ] Đã test trên môi trường staging (nếu có)

## Các biến môi trường bắt buộc

| Biến | Mô tả | Ví dụ |
|------|-------|-------|
| `POSTGRES_PASSWORD` | Mật khẩu PostgreSQL | `StrongPass123!` |
| `REDIS_PASSWORD` | Mật khẩu Redis | `RedisPass456!` |
| `JWT_SECRET` | Secret key cho JWT (≥64 ký tự) | `openssl rand -base64 64` |
| `APP_CORS_ALLOWED_ORIGINS` | Domain frontend | `https://yourdomain.com` |
| `AWS_ACCESS_KEY_ID` | AWS Access Key | (nếu dùng AWS) |
| `AWS_SECRET_ACCESS_KEY` | AWS Secret Key | (nếu dùng AWS) |
| `AWS_S3_BUCKET` | Tên S3 bucket | `my-bucket-name` |
| `SPRING_AI_OPENAI_API_KEY` | OpenAI API key | (nếu dùng AI) |

## Troubleshooting

### Container không start
```bash
# Xem logs chi tiết
docker compose -f docker-compose.prod.yml logs app

# Kiểm tra cấu hình
docker compose -f docker-compose.prod.yml config
```

### Database connection error
- Kiểm tra PostgreSQL đã start: `docker ps | grep postgres`
- Kiểm tra credentials trong `.env.prod`
- Kiểm tra network: `docker network ls`

### Application không healthy
```bash
# Kiểm tra health endpoint
curl http://localhost:8080/actuator/health

# Xem logs
docker compose -f docker-compose.prod.yml logs -f app
```

## Rollback

Nếu cần rollback về version cũ:

```bash
# Dừng services
docker compose -f docker-compose.prod.yml down

# Checkout code cũ
git checkout <old-commit-hash>

# Deploy lại
./deploy.sh
```

## Liên hệ

Nếu gặp vấn đề, xem file `README.md` để biết thêm chi tiết.

