from pathlib import Path
import re, subprocess
text = Path(r"C:\WorkSpace\AI-SEP\backend\app\wsc-service\src\main\resources\application-local.yml").read_text(encoding="utf-8")
out = Path(r"C:\WorkSpace\AI-SEP\ai\runs\RUN-WSC-002\tester-wsc-101-r4\02-yml-shape.txt")
lines = []
quotes = set(['"', "'"])
for i, line in enumerate(text.splitlines(), 1):
    stripped = line.strip()
    if not stripped or stripped.startswith("#"):
        lines.append("%d: COMMENT_OR_BLANK" % i)
        continue
    key = stripped.split(":")[0]
    low = key.lower()
    if "password" in low or "secret" in low or "pwd" in low:
        parts = line.split(":", 1)
        val = parts[1].strip() if len(parts) > 1 else ""
        quoted = (len(val) > 0 and val[0] in quotes)
        raw = val.strip().strip("\"'")
        if "#" in raw:
            raw = raw.split("#")[0].strip().strip("\"'")
        lines.append("%d: %s: <redacted len=%d quoted=%s empty=%s>" % (i, key, len(raw), quoted, len(raw)==0))
    else:
        safe = re.sub(r"(password=)[^&\s]+", r"\1***", line, flags=re.I)
        lines.append("%d: %s" % (i, safe))
mysql = r"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
# try without password flag
p = subprocess.run([mysql, "--host=127.0.0.1", "--port=3306", "--user=root", "--execute=SELECT 1;"], capture_output=True, text=True)
lines.append("try_no_pass_flag exit=%s stderr=%s" % (p.returncode, (p.stderr or "")[:300].replace("\n"," | ")))
# check if docker mysql container
p2 = subprocess.run(["docker", "ps", "--format", "{{.Names}} {{.Ports}}"], capture_output=True, text=True)
lines.append("docker_ps:\n" + (p2.stdout or p2.stderr or ""))
out.write_text("\n".join(lines)+"\n", encoding="utf-8")
print("OK")