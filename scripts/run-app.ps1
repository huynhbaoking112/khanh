$ErrorActionPreference = 'Stop'

function Get-JavaHome {
    if ($env:JAVA_HOME -and (Test-Path $env:JAVA_HOME)) {
        return $env:JAVA_HOME
    }

    $jdkRoot = 'C:\Program Files\Java'
    if (Test-Path $jdkRoot) {
        $jdk = Get-ChildItem $jdkRoot -Directory -Filter 'jdk-*' |
            Sort-Object Name -Descending |
            Select-Object -First 1

        if ($jdk) {
            return $jdk.FullName
        }
    }

    throw 'JAVA_HOME is not set and no JDK was found under C:\Program Files\Java.'
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$env:JAVA_HOME = Get-JavaHome

Push-Location $repoRoot
try {
    & .\mvnw.cmd -q -DskipTests compile
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    & "$env:JAVA_HOME\bin\java.exe" -cp 'target\classes;lib\*' com.thayhoang.quanly.Main
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}