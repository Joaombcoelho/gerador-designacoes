$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$stageRoot = Join-Path $projectRoot "dist\GeradorDesignacoes-1.2.1"
$appDir = Join-Path $stageRoot "app"
$runtimeDir = Join-Path $stageRoot "runtime"
$installerDir = Join-Path $projectRoot "Instalador"
$installerPath = Join-Path $installerDir "GeradorDesignacoes-1.2.1.exe"
$iconPath = Join-Path $projectRoot "src\main\resources\logo.ico"

function Require-Command {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "PrÃ©-requisito nÃ£o encontrado: $Name"
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
    throw "JAVA_HOME nÃ£o aponta para um JDK com a pasta jmods."
}

if (Test-Path $stageRoot) {
    throw "A pasta de preparaÃ§Ã£o jÃ¡ existe; remova-a manualmente para evitar sobrescrever artefatos."
}

if (Test-Path $installerPath) {
    throw "O instalador de destino jÃ¡ existe; nÃ£o serÃ¡ sobrescrito: $installerPath"
}

New-Item -ItemType Directory -Force $appDir | Out-Null
New-Item -ItemType Directory -Force $installerDir | Out-Null

Push-Location $projectRoot
try {
    Invoke-Checked "Compilando a aplicaÃ§Ã£o" {
        mvn clean package -DskipTests
    }

    Invoke-Checked "Copiando dependÃªncias de runtime" {
        mvn dependency:copy-dependencies `
            -DincludeScope=runtime `
            -DoutputDirectory="$appDir"
    }
} finally {
    Pop-Location
}

$jarPath = Join-Path $projectRoot "target\gerador-designacoes-1.2.1.jar"
if (-not (Test-Path $jarPath)) {
    throw "JAR da aplicaÃ§Ã£o nÃ£o encontrado: $jarPath"
}
Copy-Item $jarPath $appDir

$modelDir = Join-Path $appDir "modelos"
New-Item -ItemType Directory -Force $modelDir | Out-Null
$modelPath = Join-Path $projectRoot "modelos\S-89_T.pdf"
if (-not (Test-Path $modelPath)) {
    throw "Modelo S-89 nÃ£o encontrado: $modelPath"
}
Copy-Item $modelPath $modelDir

if (-not (Test-Path $iconPath)) {
    throw "Ãcone do aplicativo nÃ£o encontrado: $iconPath"
}

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
        throw "DependÃªncia de runtime ausente na Ã¡rea de empacotamento: $pattern"
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
        --app-version 1.2.1 `
        --input $appDir `
        --main-jar "gerador-designacoes-1.2.1.jar" `
        --main-class "br.com.geradordesignacoes.MainApp" `
        --runtime-image $runtimeDir `
        --icon $iconPath `
        --java-options '--module-path $APPDIR --add-modules javafx.controls,javafx.fxml' `
        --dest $installerDir `
        --win-shortcut `
        --win-menu `
        --win-menu-group "Gerador de DesignaÃ§Ãµes"
}

if (-not (Test-Path $installerPath)) {
    throw "O jpackage terminou sem criar o instalador esperado: $installerPath"
}

Write-Host "Instalador criado: $installerPath"

