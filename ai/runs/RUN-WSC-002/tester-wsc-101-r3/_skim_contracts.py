from pathlib import Path
root = Path(r"C:\WorkSpace\AI-SEP")
ev = root / "ai/runs/RUN-WSC-002/tester-wsc-101-r3"
notes = []
ver = (root / "contracts/VERSION").read_text(encoding="utf-8", errors="replace").strip()
notes.append(f"contracts/VERSION={ver}")
# find openapi
cands = list((root / "contracts").rglob("*.yaml")) + list((root / "contracts").rglob("*.yml")) + list((root / "contracts").rglob("*.json"))
notes.append(f"openapi_candidates={len(cands)}")
keys = ["page", "pageSize", "total", "import", "l3", "L3"]
for p in cands[:40]:
    try:
        t = p.read_text(encoding="utf-8", errors="replace")
    except Exception as e:
        continue
    hits = [k for k in keys if k in t]
    if hits:
        notes.append(f"{p.relative_to(root)}: hits={hits}")
# also skim openapi dirs
for p in cands:
    if "openapi" in str(p).lower() or p.name.lower().startswith("openapi"):
        notes.append(f"openapi_file={p.relative_to(root)}")
(ev / "03c-static-contract-notes.txt").write_text("\n".join(notes) + "\nEXIT:0\n", encoding="utf-8")
print("notes-ok", len(notes))
