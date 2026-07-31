from pathlib import Path
import re, subprocess, os
text = Path("backend/app/wsc-service/src/main/resources/application-local.yml").read_text(encoding="utf-8")
b = re.search(r"datasource:\n((?:[ \t]+.+\n)+)", text).group(1)
user = re.search(r"username:\s*(.+)$", b, re.M).group(1).strip().strip("\"'")
pw = re.search(r"password:\s*(.+)$", b, re.M).group(1).strip().strip("\"'")
mysql = r"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.EXE"
env = os.environ.copy(); env["MYSQL_PWD"] = pw
r = subprocess.run([mysql,"-u",user,"-h","127.0.0.1","-P","3306","--protocol=TCP","-D","wsc","-e","SHOW TABLES; SELECT version,description,success FROM flyway_schema_history ORDER BY installed_rank;"], capture_output=True, text=True, env=env, timeout=15)
Path("ai/runs/RUN-WSC-002/tester-wsc-101-r5/02-mysql-post-boot.txt").write_text("exit=%s\n%s\n%s\n"%(r.returncode,r.stdout,r.stderr), encoding="utf-8")
print(r.stdout)
