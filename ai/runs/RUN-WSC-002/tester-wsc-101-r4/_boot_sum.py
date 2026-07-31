from pathlib import Path
import re
raw = Path(r"C:\WorkSpace\AI-SEP\ai\runs\RUN-WSC-002\tester-wsc-101-r4\04-spring-boot-local-raw.txt")
err = Path(r"C:\WorkSpace\AI-SEP\ai\runs\RUN-WSC-002\tester-wsc-101-r4\04-spring-boot-local-err.txt")
log = Path(r"C:\WorkSpace\AI-SEP\ai\runs\RUN-WSC-002\tester-wsc-101-r4\04-spring-boot-local.txt")
parts = [log.read_text(encoding="utf-8", errors="replace")]
for label, p in [("stdout", raw), ("stderr", err)]:
    if not p.exists():
        parts.append(label + ": MISSING")
        continue
    t = p.read_text(encoding="utf-8", errors="replace")
    t = re.sub(r"(password=)[^&\s,\"']+", r"\1***", t, flags=re.I)
    t = re.sub(r"(password:\s*)\S+", r"\1***", t, flags=re.I)
    lines = t.splitlines()
    # keep interesting lines + last 80
    interesting = [ln for ln in lines if any(k in ln for k in ("Flyway","Started ","FAILED","Access denied","profile","local","Tomcat","Error","Exception","BUILD","datasource","Redis","Caused by"))]
    parts.append("=== %s interesting (%d) ===" % (label, len(interesting)))
    parts.extend(interesting[-60:])
    parts.append("=== %s tail ===" % label)
    parts.extend(lines[-40:])
log.write_text("\n".join(parts)+"\n", encoding="utf-8")
print("summarized")