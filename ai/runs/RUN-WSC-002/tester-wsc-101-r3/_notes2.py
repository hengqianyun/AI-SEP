from pathlib import Path
import re
root = Path(r"C:\WorkSpace\AI-SEP")
ev = root / "ai/runs/RUN-WSC-002/tester-wsc-101-r3"
oa = (root / "contracts/openapi/openapi.yaml").read_text(encoding="utf-8", errors="replace")
notes = []
notes.append("Static contract skim for TASK-WSC-101")
notes.append(f"contracts/VERSION={(root/'contracts/VERSION').read_text(encoding='utf-8').strip()}")
notes.append("check-contracts.mjs currently pins expected VERSION 1.1.0 -> fails against 2.0.0 (tool lag)")
for key in ["page", "pageSize", "total", "import", "l3", "L3"]:
    notes.append(f"openapi.yaml contains '{key}': {key in oa}")
# extract relevant path snippets
for pat in [r"pageSize", r"/catalog", r"import", r"l3", r"L3", r"/overview"]:
    if re.search(pat, oa, re.I):
        notes.append(f"pattern hit: {pat}")
# find parameters named page/pageSize/total near catalog
for m in re.finditer(r"(?m)^  /[^\n]+:", oa):
    path = m.group(0).strip().rstrip(":")
    chunk = oa[m.start(): m.start()+800]
    if any(x in chunk for x in ["page", "pageSize", "total", "import", "l3", "L3"]):
        if any(x in path.lower() for x in ["catalog", "import", "product", "overview", "category"]):
            notes.append(f"path {path} nearby keys present")
(ev/"03c-static-contract-notes.txt").write_text("\n".join(notes)+"\nEXIT:0\n", encoding="utf-8")
print("updated notes", len(notes))
