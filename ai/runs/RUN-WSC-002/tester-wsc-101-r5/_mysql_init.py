from pathlib import Path
import re, subprocess, os
text = Path("backend/app/wsc-service/src/main/resources/application-local.yml").read_text(encoding="utf-8")
block_m = re.search(r"datasource:\n((?:[ \t]+.+\n)+)", text)
b = block_m.group(1)
user = re.search(r"username:\s*(.+)$", b, re.M).group(1).strip().strip("\"'")
pw = re.search(r"password:\s*(.+)$", b, re.M).group(1).strip().strip("\"'")
mysql = r"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.EXE"
sql_dir = Path("backend/app/wsc-service/src/main/resources/sql/init")
env = os.environ.copy()
env["MYSQL_PWD"] = pw
out = []
# show current DBs briefly
r0 = subprocess.run([mysql, "-u", user, "-h", "127.0.0.1", "-P", "3306", "--protocol=TCP", "-e", "SHOW DATABASES LIKE 'wsc';"], capture_output=True, text=True, env=env, timeout=15)
out.append("before_show_wsc exit=%s" % r0.returncode)
out.append(r0.stdout); out.append(r0.stderr)
# drop and recreate empty per empty-db flow - 00 creates DB; if wsc exists with data, drop first for true empty
r_drop = subprocess.run([mysql, "-u", user, "-h", "127.0.0.1", "-P", "3306", "--protocol=TCP", "-e", "DROP DATABASE IF EXISTS wsc;"], capture_output=True, text=True, env=env, timeout=30)
out.append("drop_wsc exit=%s" % r_drop.returncode)
out.append(r_drop.stdout); out.append(r_drop.stderr)
for script in ["00_create_database.sql", "01_flyway_baseline.sql"]:
    path = sql_dir / script
    sql = path.read_text(encoding="utf-8")
    # 01 needs to run against wsc
    args = [mysql, "-u", user, "-h", "127.0.0.1", "-P", "3306", "--protocol=TCP"]
    if script.startswith("01_"):
        args += ["-D", "wsc"]
    args += ["-e", sql]
    r = subprocess.run(args, capture_output=True, text=True, env=env, timeout=30)
    out.append("run %s exit=%s" % (script, r.returncode))
    out.append(r.stdout); out.append(r.stderr)
r1 = subprocess.run([mysql, "-u", user, "-h", "127.0.0.1", "-P", "3306", "--protocol=TCP", "-D", "wsc", "-e", "SHOW TABLES; SELECT * FROM flyway_schema_history;"], capture_output=True, text=True, env=env, timeout=15)
out.append("after_tables exit=%s" % r1.returncode)
out.append(r1.stdout); out.append(r1.stderr)
Path("ai/runs/RUN-WSC-002/tester-wsc-101-r5/02-mysql-init.txt").write_text("\n".join(out)+"\n", encoding="utf-8")
print("\n".join(out))
