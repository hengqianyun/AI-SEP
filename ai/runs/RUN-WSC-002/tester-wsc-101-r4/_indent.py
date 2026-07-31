from pathlib import Path
import re
text = Path(r"C:\WorkSpace\AI-SEP\backend\app\wsc-service\src\main\resources\application-local.yml").read_text(encoding="utf-8")
out = []
for i, line in enumerate(text.splitlines(), 1):
    if "password" in line.lower() or "secret" in line.lower():
        # indent spaces count + key only
        ws = len(line) - len(line.lstrip(" "))
        key = line.strip().split(":")[0]
        out.append("line %d indent=%d key=%s" % (i, ws, key))
    elif ":" in line:
        ws = len(line) - len(line.lstrip(" "))
        key = line.strip().split(":")[0]
        if key in ("url","username","host","port","driver-class-name","enabled","locations","baseline-on-migrate","baseline-version","database","timeout","ddl-auto"):
            # show non-secret values for these
            val = line.split(":",1)[1].strip()
            if "password=" in val.lower():
                val = re.sub(r"(password=)[^&\s]+", r"\1***", val, flags=re.I)
            out.append("line %d indent=%d %s: %s" % (i, ws, key, val))
Path(r"C:\WorkSpace\AI-SEP\ai\runs\RUN-WSC-002\tester-wsc-101-r4\02-yml-indent.txt").write_text("\n".join(out)+"\n", encoding="utf-8")
print("\n".join(out))