
import subprocess, time, urllib.request, urllib.error, json, pathlib, sys, shutil, http.cookiejar
ev = pathlib.Path(r'C:\WorkSpace\AI-SEP\ai\runs\RUN-WSC-002\tester-wsc-101-r3')
log = ev / '06-overview-smoke.txt'
root = pathlib.Path(r'C:\WorkSpace\AI-SEP')
prev = log.read_text(encoding='utf-8', errors='replace') if log.exists() else ''
lines = ['=== RETRY with POST /api/v1/auth/session ===']
mvn = shutil.which('mvn.cmd') or r'C:\Path\apache-maven\apache-maven-3.6.3\bin\mvn.cmd'
cmd = [mvn, '-f', 'backend/app/wsc-service/pom.xml', 'spring-boot:run', '-Dspring-boot.run.profiles=dev']
proc = subprocess.Popen(cmd, cwd=str(root), stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, encoding='utf-8', errors='replace', creationflags=0x00000200)
started = False
deadline = time.time() + 150
buf = []
try:
    while time.time() < deadline:
        line = proc.stdout.readline()
        if line:
            buf.append(line.rstrip())
            if 'Started WscApplication' in line:
                started = True
                break
        elif proc.poll() is not None:
            break
    lines.append('server_started=' + str(started))
    if not started:
        lines.extend(buf[-30:])
        lines.append('EXIT:1')
        log.write_text(prev + '\n' + '\n'.join(lines) + '\n', encoding='utf-8')
        sys.exit(1)

    def hit(method, url, data=None, headers=None, cookie_jar=None):
        req = urllib.request.Request(url, data=data, method=method, headers=headers or {})
        opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cookie_jar))
        try:
            with opener.open(req, timeout=15) as resp:
                return resp.status, resp.read().decode('utf-8', errors='replace')[:2000]
        except urllib.error.HTTPError as e:
            return e.code, e.read().decode('utf-8', errors='replace')[:2000]
        except Exception as e:
            return None, str(e)

    cj = http.cookiejar.CookieJar()
    st, body = hit('GET', 'http://localhost:8080/health', cookie_jar=cj)
    lines.append('GET /health -> %s %s' % (st, body[:200]))
    payload = json.dumps({'username':'admin','password':'demo'}).encode('utf-8')
    st2, body2 = hit('POST', 'http://localhost:8080/api/v1/auth/session', data=payload, headers={'Content-Type':'application/json'}, cookie_jar=cj)
    lines.append('POST /api/v1/auth/session -> %s %s' % (st2, body2[:600]))
    for path in ['/api/v1/overview/metrics', '/api/v1/overview/stream', '/api/v1/overview/trend', '/api/v1/overview/top', '/api/v1/overview/distribution']:
        st3, body3 = hit('GET', 'http://localhost:8080' + path, cookie_jar=cj)
        lines.append('GET %s -> %s %s' % (path, st3, (body3 or '')[:500]))
    metrics_ok = any(('overview/metrics' in ln and '-> 200' in ln) for ln in lines)
    stream_ok = any(('overview/stream' in ln and '-> 200' in ln) for ln in lines)
    lines.append('H2_SMOKE_AUTHED metrics_200=%s stream_200=%s' % (metrics_ok, stream_ok))
    lines.append('DISTINCTION: H2+dev smoke (Flyway on H2 OK); MySQL empty-db BLOCKED (port 3306 down)')
    lines.append('EXIT:0')
finally:
    if proc.poll() is None:
        subprocess.run(['taskkill', '/PID', str(proc.pid), '/T', '/F'], capture_output=True)
        try:
            proc.wait(timeout=10)
        except Exception:
            pass
    log.write_text(prev + '\n' + '\n'.join(lines) + '\n', encoding='utf-8')
    print('retry done', started)
