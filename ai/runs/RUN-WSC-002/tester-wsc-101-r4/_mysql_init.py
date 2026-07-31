import re, subprocess, pathlib, sys
from pathlib import Path

EVID = Path(r"C:\WorkSpace\AI-SEP\ai\runs\RUN-WSC-002\tester-wsc-101-r4")
LOCAL = Path(r"C:\WorkSpace\AI-SEP\backend\app\wsc-service\src\main\resources\application-local.yml")
MYSQL = Path(r"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe")
SQL_DIR = Path(r"C:\WorkSpace\AI-SEP\backend\app\wsc-service\src\main\resources\sql")

def redact(s: str) -> str:
    s = re.sub(r"(password=)\S+", r"\1***", s, flags=re.I)
    s = re.sub(r"(pwd=)\S+", r"\1***", s, flags=re.I)
    return s

text = LOCAL.read_text(encoding="utf-8")
# crude yaml scrape — do not print secrets
user_m = re.search(r"^\s*username:\s*[\"']?([^\"'\s#]+)", text, re.M)
pass_m = re.search(r"^\s*password:\s*[\"']?([^\"'\n#]+)", text, re.M)
url_m = re.search(r"^\s*url:\s*[\"']?([^\"'\s#]+)", text, re.M)
# also jdbc under datasource
if not user_m:
    user_m = re.search(r"username:\s*[\"']?([^\"'\s#]+)", text)
if not pass_m:
    pass_m = re.search(r"password:\s*[\"']?([^\"'\n#]+)", text)
if not url_m:
    url_m = re.search(r"url:\s*[\"']?(jdbc:[^\s\"'#]+)", text)

log = []
log.append(f"local_yml_exists={LOCAL.exists()}")
log.append(f"username_found={bool(user_m)}")
log.append(f"password_found={bool(pass_m)}")
log.append(f"url_found={bool(url_m)}")
if url_m:
    url = url_m.group(1).strip().strip("'\"")
    # redact password query params in url for log
    log.append("url_redacted=" + redact(re.sub(r"(password=)[^&]+", r"\1***", url, flags=re.I)))
    # parse host/port/db from jdbc:mysql://host:port/db?...
    m = re.search(r"jdbc:mysql://([^:/]+):?(\d+)?/([^?]+)", url)
    if m:
        host, port, db = m.group(1), m.group(2) or "3306", m.group(3)
        log.append(f"parsed_host={host} port={port} db={db}")
    else:
        host, port, db = "127.0.0.1", "3306", "wsc"
        log.append("parse_url_failed_using_defaults_host_port_db")
else:
    host, port, db = "127.0.0.1", "3306", "wsc"
    log.append("no_url_using_defaults")

user = user_m.group(1).strip().strip("'\"") if user_m else "root"
password = pass_m.group(1).strip().strip("'\"") if pass_m else "root"
log.append(f"using_username={user}")  # username ok to log; never password

def run_mysql(args, infile=None, label=""):
    cmd = [str(MYSQL), f"--host={host}", f"--port={port}", f"--user={user}", f"--password={password}"] + args
    log.append(f"=== {label} ===")
    log.append("cmd_redacted=" + redact(" ".join(cmd)))
    try:
        if infile:
            data = Path(infile).read_bytes()
            p = subprocess.run(cmd, input=data, capture_output=True)
        else:
            p = subprocess.run(cmd, capture_output=True)
        out = (p.stdout or b"").decode("utf-8", "replace")
        err = (p.stderr or b"").decode("utf-8", "replace")
        log.append(f"exit={p.returncode}")
        log.append("stdout:\n" + redact(out))
        log.append("stderr:\n" + redact(err))
        return p.returncode
    except Exception as e:
        log.append(f"exception={type(e).__name__}: {e}")
        return 99

# ping
rc = run_mysql(["--execute=SELECT 1 AS ping;"], label="ping")
(EVID / "02-mysql-ping.txt").write_text("\n".join(log) + "\n", encoding="utf-8")
if rc != 0:
    print("AUTH_OR_PING_FAILED")
    sys.exit(2)

log2 = ["ping OK"]
# apply init scripts
rc0 = run_mysql([], infile=SQL_DIR / "init" / "00_create_database.sql", label="00_create_database")
# after create, use database for baseline
rc1 = run_mysql([f"--database={db}"], infile=SQL_DIR / "init" / "01_flyway_baseline.sql", label="01_flyway_baseline")
# show flyway table
rc2 = run_mysql([f"--database={db}", "--execute=SHOW TABLES LIKE 'flyway%'; SELECT * FROM flyway_schema_history LIMIT 5;"], label="verify_flyway")
(EVID / "02-mysql-init.txt").write_text("\n".join(log) + "\n", encoding="utf-8")
print(f"INIT_DONE rc0={rc0} rc1={rc1} rc2={rc2}")
sys.exit(0 if rc0 == 0 and rc1 == 0 else 3)
