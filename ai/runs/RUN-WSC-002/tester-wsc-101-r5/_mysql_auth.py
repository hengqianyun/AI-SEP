from pathlib import Path
import re, subprocess, os, hashlib
text = Path("backend/app/wsc-service/src/main/resources/application-local.yml").read_text(encoding="utf-8")
for i, line in enumerate(text.splitlines(), 1):
    if "password" in line.lower():
        m = re.match(r"^(\s*password\s*:\s*)(.*)$", line, re.I)
        if m:
            raw = m.group(2)
            print("line", i, "value_len", len(raw), "first", repr(raw[:1]), "last", repr(raw[-1:] if raw else ""), "sha12", hashlib.sha256(raw.encode()).hexdigest()[:12])
user = None
pw = None
try:
    import yaml
    data = yaml.safe_load(text)
    ds = data["spring"]["datasource"]
    user = ds.get("username")
    pw = ds.get("password")
    print("pyyaml_user", user, "pw_type", type(pw).__name__, "pw_len", len(str(pw)))
except Exception as e:
    print("pyyaml_fail", type(e).__name__, str(e)[:120])
    block_m = re.search(r"datasource:\n((?:[ \t]+.+\n)+)", text)
    if block_m:
        b = block_m.group(1)
        mu = re.search(r"username:\s*(.+)$", b, re.M)
        mp = re.search(r"password:\s*(.+)$", b, re.M)
        user = mu.group(1).strip().strip("\"'") if mu else None
        pw = mp.group(1).strip().strip("\"'") if mp else None
        print("regex_user", user, "regex_pw_len", len(pw) if pw else None)
mysql = r"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.EXE"
lines = []
for host in ["127.0.0.1", "localhost"]:
    env = os.environ.copy()
    env["MYSQL_PWD"] = str(pw)
    r = subprocess.run([mysql, "-u", str(user), "-h", host, "-P", "3306", "--protocol=TCP", "-e", "SELECT 1 AS ok;"], capture_output=True, text=True, env=env, timeout=15)
    scrub = lambda s: s.replace(str(pw), "***")
    lines.append("host=%s exit=%s" % (host, r.returncode))
    lines.append(scrub(r.stdout))
    lines.append(scrub(r.stderr))
cfg = Path("ai/runs/RUN-WSC-002/tester-wsc-101-r5/_mycnf.ini")
cfg.write_text("[client]\nuser=%s\npassword=%s\nhost=127.0.0.1\nport=3306\n" % (user, pw), encoding="utf-8")
r = subprocess.run([mysql, "--defaults-extra-file=" + str(cfg.resolve()), "--protocol=TCP", "-e", "SELECT 1 AS ok;"], capture_output=True, text=True, timeout=15)
cfg.write_text("[client]\nuser=%s\npassword=***REDACTED***\nhost=127.0.0.1\nport=3306\n" % user, encoding="utf-8")
lines.append("defaults-extra-file exit=" + str(r.returncode))
lines.append(r.stdout.replace(str(pw), "***"))
lines.append(r.stderr.replace(str(pw), "***"))
Path("ai/runs/RUN-WSC-002/tester-wsc-101-r5/01-mysql-auth-retry.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")
print("\n".join(lines))
