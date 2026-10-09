$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$stageRoot = Join-Path $projectRoot "dist\GeradorDesignacoes-1.2.0"
$appDir = Join-Path $stageRoot "app"
$runtimeDir = Join-Path $stageRoot "runtime"
$installerDir = Join-Path $projectRoot "Instalador"
$installerPath = Join-Path $installerDir "GeradorDesignacoes-1.2.0.exe"

function Require-Command {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Pré-requisito não encontrado: $Name"
    }
}

function Invoke-Checked {
    param(
        [string]$Name,
        [scriptblock]$Command
    )

    Write-Host $Name
    & $Command
    if ($LASTEXITCODE -ne 0) {
        throw "Falha ao executar: $Name"
    }
}

Require-Command "mvn"
Require-Command "java"
Require-Command "jlink"
Require-Command "jpackage"

if (-not $env:JAVA_HOME) {
    throw "JAVA_HOME deve apontar para um JDK 22."
}

$javaHome = (Resolve-Path $env:JAVA_HOME).Path
$jmods = Join-Path $javaHome "jmods"
if (-not (Test-Path (Join-Path $jmods "java.base.jmod"))) {
    throw "JAVA_HOME não aponta para um JDK com a pasta jmods."
}

if (Test-Path $stageRoot) {
    throw "A pasta de preparação já existe; remova-a manualmente para evitar sobrescrever artefatos."
}

if (Test-Path $installerPath) {
    throw "O instalador de destino já existe; não será sobrescrito: $installerPath"
}

New-Item -ItemType Directory -Force $appDir | Out-Null
New-Item -ItemType Directory -Force $installerDir | Out-Null

Push-Location $projectRoot
try {
    Invoke-Checked "Compilando a aplicação" {
        mvn clean package -DskipTests
    }

    Invoke-Checked "Copiando dependências de runtime" {
        mvn dependency:copy-dependencies `
            -DincludeScope=runtime `
            -DoutputDirectory="$appDir"
    }
} finally {
    Pop-Location
}

$jarPath = Join-Path $projectRoot "target\gerador-designacoes-1.2.0.jar"
if (-not (Test-Path $jarPath)) {
    throw "JAR da aplicação não encontrado: $jarPath"
}
Copy-Item $jarPath $appDir

$modelDir = Join-Path $appDir "modelos"
New-Item -ItemType Directory -Force $modelDir | Out-Null
$modelPath = Join-Path $projectRoot "modelos\S-89_T.pdf"
if (-not (Test-Path $modelPath)) {
    throw "Modelo S-89 não encontrado: $modelPath"
}
Copy-Item $modelPath $modelDir

$requiredPatterns = @(
    "javafx-base-*-win.jar",
    "javafx-controls-*-win.jar",
    "javafx-fxml-*-win.jar",
    "javafx-graphics-*-win.jar",
    "sqlite-jdbc-*.jar",
    "ikonli-javafx-*.jar",
    "ikonli-fontawesome5-pack-*.jar",
    "pdfbox-*.jar"
)

foreach ($pattern in $requiredPatterns) {
    if (-not (Get-ChildItem $appDir -Filter $pattern -File)) {
        throw "Dependência de runtime ausente na área de empacotamento: $pattern"
    }
}

Invoke-Checked "Criando runtime Java 22" {
    jlink `
        --module-path $jmods `
        --add-modules "java.base,java.datatransfer,java.xml,java.prefs,java.desktop,java.logging,java.scripting,java.transaction.xa,java.sql,java.sql.rowset,jdk.unsupported" `
        --output $runtimeDir `
        --strip-debug `
        --no-header-files `
        --no-man-pages `
        --compress=2
}

Invoke-Checked "Gerando instalador Windows" {
    jpackage `
        --type exe `
        --name GeradorDesignacoes `
        --app-version 1.2.0 `
        --input $appDir `
        --main-jar "gerador-designacoes-1.2.0.jar" `
        --main-class "br.com.geradordesignacoes.MainApp" `
        --runtime-image $runtimeDir `
        --java-options '--module-path $APPDIR --add-modules javafx.controls,javafx.fxml' `
        --dest $installerDir `
        --win-shortcut `
        --win-menu `
        --win-menu-group "Gerador de Designações"
}

if (-not (Test-Path $installerPath)) {
    throw "O jpackage terminou sem criar o instalador esperado: $installerPath"
}

Write-Host "Instalador criado: $installerPath"
