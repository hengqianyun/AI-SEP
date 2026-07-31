import json
from pathlib import Path
root = Path(r"C:\WorkSpace\AI-SEP")
ev = root / "ai/runs/RUN-WSC-002/tester-wsc-101-r3"
lines = []
for rel in ["frontend/package.json", "package.json"]:
    p = root / rel
    if not p.exists():
        lines.append(f"MISSING {rel}")
        continue
    data = json.loads(p.read_text(encoding="utf-8"))
    scripts = data.get("scripts", {})
    lines.append(f"== {rel} ==")
    for k,v in scripts.items():
        lines.append(f"  {k}: {v}")
    lines.append(f"name={data.get('name')} packageManager={data.get('packageManager')}")
(ev / "05a-frontend-scripts.txt").write_text("\n".join(lines) + "\nEXIT:0\n", encoding="utf-8")
print("scripts-ok")
