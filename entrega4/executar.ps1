param([switch]$Testar)
$ErrorActionPreference = 'Stop'
$jdk = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin' } else { $null }
$javac = if ($jdk) { Join-Path $jdk 'javac.exe' } else { 'javac' }
$java = if ($jdk) { Join-Path $jdk 'java.exe' } else { 'java' }
Push-Location $PSScriptRoot
try {
    New-Item -ItemType Directory -Force build | Out-Null
    $fontes = @(Get-ChildItem src -Recurse -Filter '*.java' | ForEach-Object FullName)
    if ($Testar) { $fontes += @(Get-ChildItem test -Recurse -Filter '*.java' | ForEach-Object FullName) }
    & $javac --release 17 -encoding UTF-8 -d build $fontes
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    if ($Testar) { & $java -cp build minharefeicao.Cenarios evidencias }
    else { & $java -cp build minharefeicao.Main dados }
    exit $LASTEXITCODE
} finally { Pop-Location }
