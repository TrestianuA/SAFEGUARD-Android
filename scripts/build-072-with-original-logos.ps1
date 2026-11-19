# SAFEGUARD 0.7.2 - include approved logos and compile using original local debug key
param(
  [string]$LogoZip = "$env:USERPROFILE\Downloads\SAFEGUARD_072_logo_originale.zip"
)
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot
if (-not (Test-Path $LogoZip)) { throw "Nu găsesc arhiva: $LogoZip" }
$dest = Join-Path $repoRoot 'app\src\main\res\drawable-nodpi'
New-Item -ItemType Directory -Force -Path $dest | Out-Null
$temp = Join-Path $env:TEMP ('safeguard-logo-' + [guid]::NewGuid())
New-Item -ItemType Directory -Path $temp | Out-Null
try {
  Expand-Archive -LiteralPath $LogoZip -DestinationPath $temp -Force
  foreach ($name in @('safeguard_logo_simplu.png', 'safeguard_logo_complet.png')) {
    $source = Get-ChildItem -Path $temp -Recurse -File -Filter $name | Select-Object -First 1
    if (-not $source) { throw "Lipsește din arhivă: $name" }
    Copy-Item -LiteralPath $source.FullName -Destination (Join-Path $dest $name) -Force
    # Android nu permite doua resurse cu acelasi nume (PNG si JPG).
    $oldJpg = Join-Path $dest ($name -replace '\.png
  }
} finally { Remove-Item -LiteralPath $temp -Recurse -Force -ErrorAction SilentlyContinue }
$key = Join-Path $env:USERPROFILE '.android\debug.keystore'
if (-not (Test-Path $key)) { throw "Cheia originală nu există la: $key" }
& keytool -list -v -keystore $key -alias androiddebugkey -storepass android -keypass android |
  Select-String 'SHA256:'
if ($LASTEXITCODE -ne 0) { throw 'Nu am putut verifica cheia' }
Write-Host 'Verifică manual că amprenta este B0:58:08:CC:91:0E:75:24:42:6D:E7:28:51:9D:F7:46:28:A0:45:62:98:42:AA:11:FB:DC:62:2A:10:4C:94:86'
& .\gradlew.bat --no-daemon testDebugUnitTest assembleDebug
if ($LASTEXITCODE -ne 0) { throw 'Compilarea a eșuat' }
Write-Host 'APK: app\build\outputs\apk\debug\app-debug.apk'
Write-Host 'Nu instala APK-ul înainte de verificarea semnăturii conform docs/TEST_VIZUAL_072_WINDOWS.md'
, '.jpg')
    if (Test-Path $oldJpg) { Remove-Item -LiteralPath $oldJpg -Force }
    Write-Host "Copiat: $name"
  }
} finally { Remove-Item -LiteralPath $temp -Recurse -Force -ErrorAction SilentlyContinue }
$key = Join-Path $env:USERPROFILE '.android\debug.keystore'
if (-not (Test-Path $key)) { throw "Cheia originală nu există la: $key" }
& keytool -list -v -keystore $key -alias androiddebugkey -storepass android -keypass android |
  Select-String 'SHA256:'
if ($LASTEXITCODE -ne 0) { throw 'Nu am putut verifica cheia' }
Write-Host 'Verifică manual că amprenta este B0:58:08:CC:91:0E:75:24:42:6D:E7:28:51:9D:F7:46:28:A0:45:62:98:42:AA:11:FB:DC:62:2A:10:4C:94:86'
& .\gradlew.bat --no-daemon testDebugUnitTest assembleDebug
if ($LASTEXITCODE -ne 0) { throw 'Compilarea a eșuat' }
Write-Host 'APK: app\build\outputs\apk\debug\app-debug.apk'
Write-Host 'Nu instala APK-ul înainte de verificarea semnăturii conform docs/TEST_VIZUAL_072_WINDOWS.md'
