import subprocess, time, urllib.request, urllib.error, json, pathlib, signal, sys, shutil, http.cookiejar
ev = pathlib.Path(r"C:\WorkSpace\AI-SEP\ai\runs\RUN-WSC-002\tester-wsc-101-r3")
log = ev / "06-overview-smoke.txt"
root = pathlib.Path(r"C:\WorkSpace\AI-SEP")
lines = []
lines.append("MODE: H2 smoke via spring.profiles.active=dev (NOT MySQL empty-db)")
lines.append("MySQL/Redis unreachable; empty-db migrate NOT executed")
mvn = shutil.which("mvn.cmd") or shutil.which("mvn") or r"C:\Path\apache-maven\apache-maven-3.6.3\bin\mvn.cmd"
lines.append(f"mvn={mvn}")
cmd = [mvn, "-f", "backend/app/wsc-service/pom.xml", "spring-boot:run", "-Dspring-boot.run.profiles=dev"]
creationflags = 0x00000200
proc = subprocess.Popen(cmd, cwd=str(root), stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, encoding="utf-8", errors="replace", creationflags=creationflags)
started = False
deadline = time.time() + 150
buf = []
try:
    while time.time() < deadline:
        line = proc.stdout.readline()
        if line:
            buf.append(line.rstrip())
            if "Started WscApplication" in line:
                started = True
                break
        elif proc.poll() is not None:
            break
        else:
            time.sleep(0.2)
    lines.append(f"server_started={started} poll={proc.poll()}")
    lines.append("--- boot_tail ---")
    lines.extend(buf[-60:])
    if not started:
        try:
            more, _ = proc.communicate(timeout=8)
            if more:
                lines.extend(more.splitlines()[-40:])
        except Exception as e:
            lines.append(f"communicate_err={e}")
        lines.append("SMOKE: FAILED (server did not start)")
        lines.append("EXIT:1")
        log.write_text("\n".join(lines)+"\n", encoding="utf-8")
        print("failed start")
        sys.exit(1)

    def hit(method, url, data=None, headers=None, cookie_jar=None):
        req = urllib.request.Request(url, data=data, method=method, headers=headers or {})
        opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cookie_jar)) if cookie_jar is not None else urllib.request.build_opener()
        try:
            with opener.open(req, timeout=15) as resp:
                body = resp.read().decode("utf-8", errors="replace")
                return resp.status, body[:2000]
        except urllib.error.HTTPError as e:
            body = e.read().decode("utf-8", errors="replace")
            return e.code, body[:2000]
        except Exception as e:
            return None, str(e)

    cj = http.cookiejar.CookieJar()
    st, body = hit("GET", "http://localhost:8080/health")
    lines.append(f"GET /health -> {st} body={body[:400]}")
    payload = json.dumps({"username":"admin","password":"demo"}).encode("utf-8")
    st2, body2 = hit("POST", "http://localhost:8080/api/v1/auth/login", data=payload, headers={"Content-Type":"application/json"}, cookie_jar=cj)
    lines.append(f"POST /api/v1/auth/login -> {st2} body={body2[:500]}")
    for path in ["/api/v1/overview/metrics", "/api/v1/overview/stream", "/api/v1/overview/summary", "/api/v1/overview"]:
        st3, body3 = hit("GET", f"http://localhost:8080{path}", cookie_jar=cj)
        lines.append(f"GET {path} -> {st3} body={(body3 or '')[:400]}")
    ok = st == 200
    lines.append("SMOKE_RESULT: H2_DEV_OK" if ok else "SMOKE_RESULT: PARTIAL_OR_FAIL")
    lines.append("DISTINCTION: H2+dev smoke only; MySQL empty-db init/migration NOT verified")
    lines.append("EXIT:0" if ok else "EXIT:1")
finally:
    if proc.poll() is None:
        try:
            subprocess.run(["taskkill", "/PID", str(proc.pid), "/T", "/F"], capture_output=True)
        except Exception:
            pass
        try:
            proc.wait(timeout=10)
        except Exception:
            pass
    log.write_text("\n".join(lines)+"\n", encoding="utf-8")
    print("wrote", log, "started", started)
