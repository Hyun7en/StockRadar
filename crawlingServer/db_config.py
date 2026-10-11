"""
크롤러 DB 접속 정보. 값은 환경변수 또는 crawlingServer/.env(KEY=VALUE)에서 읽는다.
.env는 git에 올리지 않는다 (.env.example 참고).
"""
import os

_ENV_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".env")


def _load_env_file(path=_ENV_FILE):
    if not os.path.exists(path):
        return
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            # 이미 설정된 환경변수가 우선
            os.environ.setdefault(key.strip(), value.strip())


_load_env_file()

DB_CONFIG = {
    "host": os.getenv("CRAWLER_DB_HOST", "localhost"),
    "port": int(os.getenv("CRAWLER_DB_PORT", "3306")),
    "user": os.getenv("CRAWLER_DB_USER", ""),
    "password": os.getenv("CRAWLER_DB_PASSWORD", ""),
    "database": os.getenv("CRAWLER_DB_NAME", "stockradar"),
}
