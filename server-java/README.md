Build & Run

Prerequisites

- JDK 17+
- MySQL running (update DB in `src/main/resources/application.yml`)

Run (dev)

```bash
./gradlew bootRun
```

ENV (optional)

- Set admin user from ENV in `application.yml` keys under `app.admin.*` or env vars: `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NAME`, `ADMIN_ADDRESS`, `ADMIN_AGE`, `ADMIN_GENDER`.

Seed scripts (giống server-python)

Enable seed runners in `application.yml`:

```yaml
app:
  seed:
    enabled: true
```

Các lệnh seed (dùng thêm tham số cho bootRun):

```bash
# 1) Seed admin (SUPER_ADMIN + admin từ ENV)
./gradlew bootRun --args="--seed-admin"

# 2) Quét endpoints và seed permissions, gán SUPER_ADMIN
./gradlew bootRun --args="--seed-permissions"

# 3) Import data mẫu (companies/jobs) từ thư mục data/
./gradlew bootRun --args="--seed-data"

# 4) Seed logo công ty từ data/logo:
#    - Mặc định sẽ gọi API upload (cần auth), nếu lỗi sẽ fallback copy trực tiếp
#    - Có thể ép chỉ copy trực tiếp (không gọi API) bằng --seed-logos-direct
./gradlew bootRun --args="--seed-logos"
./gradlew bootRun --args="--seed-logos-direct"

# 5) Chạy toàn bộ (admin + permissions + data + logos)
./gradlew bootRun --args="--seed-all"
```

Data & Logos

- Đặt `data/companies.json`, `data/jobs.json` ở `server-java/` hoặc thư mục cha.
- Đặt ảnh logo vào `server-java/data/logo` (hoặc thư mục cha `data/logo`). Tên file nên chứa tên công ty để map, ví dụ: `google-*.png`, `apple-*.jpg`...
- Seed logo sẽ gọi API `POST /v1/files/upload` với header `folder_type: company`, cần service đang chạy tại `server.port` + `context-path`. Có thể override base bằng ENV `UPLOAD_BASE_URL`.
- Dùng `--seed-logos-direct` nếu muốn bỏ qua API và copy trực tiếp (không cần auth).
