from pathlib import Path
import zipfile, hashlib, re
jar = Path("backend/app/wsc-service/target/wsc-service-0.2.0-SNAPSHOT.jar")
src = Path("backend/app/wsc-service/src/main/resources/application-local.yml")
tgt = Path("backend/app/wsc-service/target/classes/application-local.yml")
src_text = src.read_text(encoding="utf-8")
# hash password line only structure
mp = re.search(r"datasource:\n((?:[ \t]+.+\n)+)", src_text)
b = mp.group(1)
pw = re.search(r"password:\s*(.+)$", b, re.M).group(1).strip().strip("\"'")
print("src_exists", src.exists(), "src_pw_len", len(pw), "src_pw_sha12", hashlib.sha256(pw.encode()).hexdigest()[:12])
print("tgt_exists", tgt.exists())
if tgt.exists():
    tt = tgt.read_text(encoding="utf-8")
    mb = re.search(r"datasource:\n((?:[ \t]+.+\n)+)", tt)
    if mb:
        pw2 = re.search(r"password:\s*(.+)$", mb.group(1), re.M).group(1).strip().strip("\"'")
        print("tgt_pw_len", len(pw2), "tgt_pw_sha12", hashlib.sha256(pw2.encode()).hexdigest()[:12], "match_src", pw2==pw)
with zipfile.ZipFile(jar) as z:
    names = [n for n in z.namelist() if "application-local" in n]
    print("jar_entries", names)
    for n in names:
        data = z.read(n).decode("utf-8", errors="replace")
        mb = re.search(r"datasource:\n((?:[ \t]+.+\n)+)", data)
        if mb:
            pw3 = re.search(r"password:\s*(.+)$", mb.group(1), re.M).group(1).strip().strip("\"'")
            print("jar_pw_len", len(pw3), "jar_pw_sha12", hashlib.sha256(pw3.encode()).hexdigest()[:12], "match_src", pw3==pw)
