from pathlib import Path
root = Path(r"C:\WorkSpace\AI-SEP")
ev = root / "ai/runs/RUN-WSC-002/tester-wsc-101-r3"
app = (root / "backend/app/wsc-service/src/main/resources/application.yml").read_text(encoding="utf-8", errors="replace")
ex = (root / "backend/app/wsc-service/src/main/resources/application-local.example.yml").read_text(encoding="utf-8", errors="replace")
# find profile docs in pom or resources
hits = []
for p in (root / "backend/app/wsc-service").rglob("*"):
    if p.is_file() and ("application" in p.name.lower() or p.suffix in {".md",".yml",".yaml","properties"}):
        if "dev" in p.name.lower() or "h2" in p.name.lower():
            hits.append(str(p.relative_to(root)))
(ev / "00-app-yml-excerpt.txt").write_text("=== application.yml ===\n"+app+"\n=== local.example ===\n"+ex+"\n=== hits ===\n"+"\n".join(hits)+"\n", encoding="utf-8")
print("wrote excerpt", "dev-hits", hits)
# also search for spring.profiles in java
for p in (root / "backend/app/wsc-service/src").rglob("*.java"):
    t = p.read_text(encoding="utf-8", errors="replace")
    if "H2" in t or "profiles" in t.lower() and "dev" in t:
        if "h2" in t.lower() or "Profile" in t:
            print("java", p.relative_to(root))
