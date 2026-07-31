import socket, pathlib
out = pathlib.Path(r"C:\WorkSpace\AI-SEP\ai\runs\RUN-WSC-002\tester-wsc-101-r3\02-mysql.txt")
lines = []
for name, port in [("MySQL", 3306), ("Redis", 6379)]:
    s = socket.socket()
    s.settimeout(3)
    try:
        s.connect(("127.0.0.1", port))
        ok = True
    except OSError:
        ok = False
    finally:
        s.close()
    lines.append(f"{name} {port} TcpTestSucceeded={ok}")
    if name == "MySQL" and not ok:
        lines.append("BLOCKED: MySQL unreachable")
    if name == "Redis" and not ok:
        lines.append("NOTE: Redis unreachable")
lines.append("EXIT:0")
out.write_text("\n".join(lines) + "\n", encoding="utf-8")
print("wrote", out)
