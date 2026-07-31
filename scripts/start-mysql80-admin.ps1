#Requires -RunAsAdministrator
$ErrorActionPreference = "Stop"

$ini = "C:\ProgramData\MySQL\MySQL Server 8.0\my.ini"
$data = "C:\ProgramData\MySQL\MySQL Server 8.0\Data"
$mysqld = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe"
$mysqladmin = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqladmin.exe"
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"

Write-Host "== Fix my.ini log paths (ASCII only) ==" -ForegroundColor Cyan
$backup = "$ini.bak-admin-$(Get-Date -Format yyyyMMddHHmmss)"
Copy-Item $ini $backup -Force

# Read as bytes and decode UTF-8 (installer writes UTF-8)
$bytes = [System.IO.File]::ReadAllBytes($ini)
if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
  $text = [System.Text.Encoding]::UTF8.GetString($bytes, 3, $bytes.Length - 3)
} else {
  $text = [System.Text.Encoding]::UTF8.GetString($bytes)
}

function Set-IniLine([string]$src, [string]$pattern, [string]$replacement) {
  return [regex]::Replace($src, $pattern, $replacement, [System.Text.RegularExpressions.RegexOptions]::Multiline)
}

$text = Set-IniLine $text '^general_log_file=.*$' 'general_log_file="mysql.log"'
$text = Set-IniLine $text '^slow_query_log_file=.*$' 'slow_query_log_file="mysql-slow.log"'
$text = Set-IniLine $text '^log-error=.*$' 'log-error="mysql.err"'
$text = Set-IniLine $text '^log-bin=.*$' '# log-bin="mysql-bin"'
$text = Set-IniLine $text '^basedir=.*$' 'basedir="C:/Program Files/MySQL/MySQL Server 8.0/"'
$text = Set-IniLine $text '^datadir=.*$' 'datadir="C:/ProgramData/MySQL/MySQL Server 8.0/Data"'

if ($text -notmatch '(?m)^skip-log-bin\s*$') {
  $text = $text -replace '(?m)^\[mysqld\]\s*$', "[mysqld]`r`nskip-log-bin"
}

[System.IO.File]::WriteAllBytes($ini, [System.Text.Encoding]::UTF8.GetBytes($text))
Write-Host "Saved $ini (backup: $backup)"

Write-Host "== Grant NetworkService permissions ==" -ForegroundColor Cyan
icacls "C:\ProgramData\MySQL\MySQL Server 8.0" /grant "NT AUTHORITY\NETWORK SERVICE:(OI)(CI)F" /T | Out-Null

$needInit = -not (Test-Path (Join-Path $data "mysql"))
if ($needInit) {
  Write-Host "== Data incomplete, initializing (insecure root) ==" -ForegroundColor Yellow
  if (Test-Path $data) {
    Get-ChildItem $data -Force | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
  } else {
    New-Item -ItemType Directory -Path $data | Out-Null
  }
  icacls $data /grant "NT AUTHORITY\NETWORK SERVICE:(OI)(CI)F" /T | Out-Null
  & $mysqld --defaults-file="$ini" --initialize-insecure --lower-case-table-names=1 --console
  if ($LASTEXITCODE -ne 0) { throw "mysqld --initialize-insecure failed: $LASTEXITCODE" }
} else {
  Write-Host "== Data directory looks initialized ==" -ForegroundColor Green
}

Write-Host "== Start MYSQL80 service ==" -ForegroundColor Cyan
Stop-Service MYSQL80 -Force -ErrorAction SilentlyContinue
Start-Service MYSQL80
Start-Sleep -Seconds 2
Get-Service MYSQL80 | Format-Table Name, Status, StartType -AutoSize

Write-Host ""
Write-Host "SUCCESS: MYSQL80 service is Running." -ForegroundColor Green

$pingEmpty = & $mysqladmin -uroot ping 2>&1
if ($LASTEXITCODE -eq 0) {
  Write-Host "root password is EMPTY. Set one, for example:" -ForegroundColor Yellow
  Write-Host "  & `"$mysql`" -uroot -e `"ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';`""
} else {
  Write-Host "root requires a password (Installer config). Empty-password login failed:" -ForegroundColor Yellow
  Write-Host $pingEmpty
  Write-Host "Try the password you set in MySQL Installer, e.g.:" -ForegroundColor Yellow
  Write-Host "  & `"$mysqladmin`" -uroot -p ping"
  Write-Host "Project default DB password is 'root'. To match it:" -ForegroundColor Yellow
  Write-Host "  & `"$mysql`" -uroot -p -e `"ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';`""
}

Write-Host ""
Write-Host "IMPORTANT: Do NOT use MySQL Installer -> Reconfigure again;" -ForegroundColor Yellow
Write-Host "it will rewrite garbled Chinese log file names into my.ini." -ForegroundColor Yellow
