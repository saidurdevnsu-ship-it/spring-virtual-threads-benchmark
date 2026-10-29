# Windows equivalent of load-test.sh. Requires PowerShell 7+ (ForEach-Object -Parallel).
# Usage: ./scripts/load-test.ps1 -Requests 2000 -Concurrency 1000
param(
    [int]$Requests = 2000,
    [int]$Concurrency = 1000,
    [string]$Url = "http://localhost:8080/thread"
)

if (Get-Command hey -ErrorAction SilentlyContinue) {
    hey -n $Requests -c $Concurrency -t 120 $Url
    return
}

Write-Host "Requests: $Requests  Concurrency: $Concurrency  URL: $Url"
$sw = [System.Diagnostics.Stopwatch]::StartNew()
$codes = 1..$Requests | ForEach-Object -ThrottleLimit $Concurrency -Parallel {
    try { (Invoke-WebRequest -Uri $using:Url -TimeoutSec 120 -UseBasicParsing).StatusCode } catch { 0 }
}
$sw.Stop()
$ok = ($codes | Where-Object { $_ -eq 200 }).Count
Write-Host ("Total time: {0:N1} s   HTTP 200: {1} / {2}" -f $sw.Elapsed.TotalSeconds, $ok, $Requests)
