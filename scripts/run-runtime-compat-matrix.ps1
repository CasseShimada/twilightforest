[CmdletBinding()]
param(
    [ValidateSet('server', 'client', 'all')]
    [string] $Mode = 'all',

    [string[]] $Scenarios = @('base', 'diggus', 'carry', 'wthit', 'diggus-carry', 'diggus-wthit', 'carry-wthit', 'all', 'jade-wthit'),

    [string] $DiggusJar,
    [string] $CarryOnJar,
    [string] $WthitJar,
    [string] $BadPacketsJar,
    [string] $JadeJar,

    [int] $ServerTimeoutSeconds = 240,
    [int] $ClientTimeoutSeconds = 300
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$buildRoot = Join-Path $repoRoot 'build'
$matrixRoot = Join-Path $buildRoot 'runtime-matrix'
$gradleWrapper = Join-Path $repoRoot 'gradlew.bat'

$scenarioDefinitions = [ordered]@{
    'base' = @{ jars = @(); mods = @() }
    'api-consumer' = @{ jars = @(); mods = @() }
    'diggus' = @{ jars = @($DiggusJar); mods = @('diggusmaximus') }
    'carry' = @{ jars = @($CarryOnJar); mods = @('carryon') }
    'wthit' = @{ jars = @($WthitJar, $BadPacketsJar); mods = @('wthit', 'badpackets') }
    'diggus-carry' = @{ jars = @($DiggusJar, $CarryOnJar); mods = @('diggusmaximus', 'carryon') }
    'diggus-wthit' = @{ jars = @($DiggusJar, $WthitJar, $BadPacketsJar); mods = @('diggusmaximus', 'wthit', 'badpackets') }
    'carry-wthit' = @{ jars = @($CarryOnJar, $WthitJar, $BadPacketsJar); mods = @('carryon', 'wthit', 'badpackets') }
    'all' = @{ jars = @($DiggusJar, $CarryOnJar, $WthitJar, $BadPacketsJar); mods = @('diggusmaximus', 'carryon', 'wthit', 'badpackets') }
    'jade-wthit' = @{ jars = @($JadeJar, $WthitJar, $BadPacketsJar); mods = @('jade', 'wthit', 'badpackets') }
}

function Assert-SafeMatrixPath([string] $Path) {
    $fullPath = [IO.Path]::GetFullPath($Path)
    $fullBuildRoot = [IO.Path]::GetFullPath($buildRoot) + [IO.Path]::DirectorySeparatorChar
    if (-not $fullPath.StartsWith($fullBuildRoot, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Runtime matrix path escapes the build directory: $fullPath"
    }
}

function Get-SelectedScenarios {
    $selected = [Collections.Generic.List[string]]::new()
    foreach ($value in $Scenarios) {
        foreach ($name in $value.Split(',', [StringSplitOptions]::RemoveEmptyEntries)) {
            $trimmed = $name.Trim()
            if (-not $scenarioDefinitions.Contains($trimmed)) {
                throw "Unknown runtime scenario '$trimmed'"
            }
            if (-not $selected.Contains($trimmed)) {
                $selected.Add($trimmed)
            }
        }
    }
    return $selected
}

function Resolve-ScenarioJars([string] $Name) {
    $resolved = [Collections.Generic.List[string]]::new()
    foreach ($candidate in $scenarioDefinitions[$Name].jars) {
        if ([string]::IsNullOrWhiteSpace($candidate)) {
            throw "Scenario '$Name' requires an optional-mod jar path that was not supplied"
        }
        $path = (Resolve-Path -LiteralPath $candidate).Path
        if (-not $path.EndsWith('.jar', [StringComparison]::OrdinalIgnoreCase)) {
            throw "Scenario '$Name' input is not a jar: $path"
        }
        $resolved.Add($path)
    }
    return $resolved
}

function Write-Utf8File([string] $Path, [string] $Content) {
    [IO.File]::WriteAllText($Path, $Content, [Text.UTF8Encoding]::new($false))
}

function Get-LatestLog([string] $RunDirectory) {
    $latest = Join-Path $RunDirectory 'logs\latest.log'
    if (Test-Path -LiteralPath $latest) {
        try {
            $stream = [IO.FileStream]::new($latest, [IO.FileMode]::Open, [IO.FileAccess]::Read, [IO.FileShare]::ReadWrite -bor [IO.FileShare]::Delete)
            try {
                $reader = [IO.StreamReader]::new($stream, [Text.Encoding]::UTF8, $true, 4096, $true)
                try {
                    return $reader.ReadToEnd()
                } finally {
                    $reader.Dispose()
                }
            } finally {
                $stream.Dispose()
            }
        } catch [IO.IOException] {
            return ''
        }
    }
    return ''
}

function Stop-ProcessTree([Diagnostics.Process] $Process) {
    if (-not $Process.HasExited) {
        $Process.Kill($true)
        $Process.WaitForExit()
    }
}

function Save-ClientThreadDumps([string] $Scenario, [string] $RunDirectory, [Diagnostics.Process] $RootProcess) {
    $jcmd = Join-Path $env:JAVA_HOME 'bin\jcmd.exe'
    if (-not (Test-Path -LiteralPath $jcmd)) {
        throw "jcmd was not found under JAVA_HOME: $jcmd"
    }
    $allProcesses = @(Get-CimInstance Win32_Process)
    $descendantIds = [Collections.Generic.HashSet[int]]::new()
    $pendingIds = [Collections.Generic.Queue[int]]::new()
    $pendingIds.Enqueue($RootProcess.Id)
    while ($pendingIds.Count -gt 0) {
        $parentId = $pendingIds.Dequeue()
        foreach ($child in $allProcesses | Where-Object ParentProcessId -eq $parentId) {
            if ($descendantIds.Add([int]$child.ProcessId)) {
                $pendingIds.Enqueue([int]$child.ProcessId)
            }
        }
    }
    $gameProcess = $allProcesses | Where-Object {
        $descendantIds.Contains([int]$_.ProcessId) -and
        $_.Name -eq 'java.exe' -and
        $_.CommandLine -match 'devlaunchinjector\.Main'
    } | Select-Object -First 1
    if ($null -eq $gameProcess) {
        throw "Could not locate the Minecraft client JVM for thread dumps in scenario '$Scenario'"
    }

    $relativeDumps = [Collections.Generic.List[string]]::new()
    foreach ($number in 1..2) {
        $dumpPath = Join-Path $RunDirectory ("matrix-client-thread-dump-{0}.txt" -f $number)
        $dump = @(& $jcmd $gameProcess.ProcessId Thread.print -l 2>&1)
        if ($LASTEXITCODE -ne 0) {
            throw "jcmd Thread.print failed for client scenario '$Scenario' dump $number"
        }
        Write-Utf8File $dumpPath (($dump | ForEach-Object ToString) -join [Environment]::NewLine)
        $relativeDumps.Add([IO.Path]::GetRelativePath($repoRoot, $dumpPath).Replace('\', '/'))
        if ($number -eq 1) {
            Start-Sleep -Seconds 5
        }
    }
    return @($relativeDumps)
}

function Start-GradleRun(
    [string] $Scenario,
    [string] $RunDirectory,
    [ValidateSet('server', 'client')] [string] $Kind,
    [int] $Pass,
    [int] $TimeoutSeconds,
    [string[]] $ExpectedMods
) {
    $relativeRunDirectory = [IO.Path]::GetRelativePath($repoRoot, $RunDirectory).Replace('\', '/')
    $previousLatestLog = Join-Path $RunDirectory 'logs\latest.log'
    if (Test-Path -LiteralPath $previousLatestLog) {
        Remove-Item -LiteralPath $previousLatestLog -Force
    }
    $expected = $ExpectedMods -join ','
    $serverProbe = if ($Kind -eq 'server') { 'true' } else { 'false' }
    $task = if ($Kind -eq 'server') { 'runServer' } else { 'runClient' }
    $quickPlay = if ($Kind -eq 'client') { ' --args="--quickPlaySingleplayer compat-world"' } else { '' }
    $arguments = @(
        '-PtfRunDir=' + $relativeRunDirectory,
        '-PtfRuntimeProbe=true',
        '-PtfRuntimeServerProbe=' + $serverProbe,
        '-PtfRuntimeScenario=' + $Scenario,
        '-PtfRuntimePass=' + $Pass,
        '-PtfRuntimeExpectedMods=' + $expected,
        $task,
        '--console=plain',
        '--no-daemon'
    ) -join ' '

    $startInfo = [Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $env:ComSpec
    $startInfo.Arguments = "/d /c .\gradlew.bat $arguments$quickPlay"
    $startInfo.WorkingDirectory = $repoRoot
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardInput = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.CreateNoWindow = $true

    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    $startedAt = [DateTimeOffset]::UtcNow
    if (-not $process.Start()) {
        throw "Could not start Gradle $Kind run for $Scenario"
    }
    $stdoutTask = $process.StandardOutput.ReadToEndAsync()
    $stderrTask = $process.StandardError.ReadToEndAsync()
    $deadline = [DateTimeOffset]::UtcNow.AddSeconds($TimeoutSeconds)
    $stopSent = $false
    $clientProbeStartedAt = $null
    $experimentalWarningShownAt = $null
    $onboardingClickReported = $false
    $automaticClickReported = $false
    $threadDumps = @()

    try {
        while (-not $process.HasExited) {
            if ([DateTimeOffset]::UtcNow -gt $deadline) {
                throw "$Kind run timed out after $TimeoutSeconds seconds for scenario '$Scenario' pass $Pass"
            }
            $latestLog = Get-LatestLog $RunDirectory
            if ($Kind -eq 'client') {
                if ($null -eq $clientProbeStartedAt -and $latestLog -match '\[TF-RUNTIME-PROBE\] START client') {
                    $clientProbeStartedAt = [DateTimeOffset]::UtcNow
                }
                if (-not $onboardingClickReported -and $latestLog -match '\[TF-RUNTIME-PROBE\] ACCEPT accessibility_onboarding') {
                    Write-Host "[$Scenario] automatically clicked the accessibility onboarding continue button"
                    $onboardingClickReported = $true
                }
                if ($null -eq $experimentalWarningShownAt -and
                    $latestLog -match '\[TF-RUNTIME-PROBE\] SCREEN .*type=.*(?:BackupConfirmScreen|ConfirmExperimentalFeaturesScreen)') {
                    $experimentalWarningShownAt = [DateTimeOffset]::UtcNow
                }
                if (-not $automaticClickReported -and $latestLog -match '\[TF-RUNTIME-PROBE\] ACCEPT experimental_features') {
                    Write-Host "[$Scenario] automatically clicked the experimental-features proceed button"
                    $automaticClickReported = $true
                    if ($Scenario -in @('base', 'all')) {
                        $threadDumps = @(Save-ClientThreadDumps $Scenario $RunDirectory $process)
                    }
                }
                if ($null -ne $experimentalWarningShownAt -and -not $automaticClickReported -and
                    [DateTimeOffset]::UtcNow -gt $experimentalWarningShownAt.AddSeconds(10)) {
                    throw "Client did not automatically click the experimental-features proceed button within 10 seconds of it appearing for scenario '$Scenario'"
                }
                if ($null -ne $clientProbeStartedAt -and $null -eq $experimentalWarningShownAt -and
                    [DateTimeOffset]::UtcNow -gt $clientProbeStartedAt.AddSeconds(90)) {
                    throw "Client did not reach the experimental-features warning within 90 seconds for scenario '$Scenario'"
                }
            }
            if ($latestLog -match '\[TF-RUNTIME-PROBE\] FAIL') {
                if ($Kind -eq 'server' -and -not $stopSent) {
                    $process.StandardInput.WriteLine('stop')
                    $process.StandardInput.Flush()
                    $stopSent = $true
                }
            }
            if ($Kind -eq 'server' -and -not $stopSent -and
                $latestLog -match 'Done \(' -and
                $latestLog -match ("\[TF-RUNTIME-PROBE\] PASS server scenario=" + [regex]::Escape($Scenario) + " pass=" + $Pass)) {
                $process.StandardInput.WriteLine('stop')
                $process.StandardInput.Flush()
                $stopSent = $true
            }
            Start-Sleep -Milliseconds 250
        }
    } catch {
        Stop-ProcessTree $process
        throw
    } finally {
        if (-not $process.HasExited) {
            Stop-ProcessTree $process
        }
    }

    $stdout = $stdoutTask.GetAwaiter().GetResult()
    $stderr = $stderrTask.GetAwaiter().GetResult()
    $commandLog = Join-Path $RunDirectory ("matrix-{0}-pass-{1}-command.log" -f $Kind, $Pass)
    Write-Utf8File $commandLog ($stdout + [Environment]::NewLine + $stderr)
    $latestLog = Get-LatestLog $RunDirectory
    $gameLog = Join-Path $RunDirectory ("matrix-{0}-pass-{1}-latest.log" -f $Kind, $Pass)
    Write-Utf8File $gameLog $latestLog

    if ($process.ExitCode -ne 0) {
        throw "$Kind run exited with code $($process.ExitCode) for scenario '$Scenario' pass $Pass; see $commandLog"
    }
    $passPattern = if ($Kind -eq 'server') {
        "\[TF-RUNTIME-PROBE\] PASS server scenario=" + [regex]::Escape($Scenario) + " pass=" + $Pass
    } else {
        "\[TF-RUNTIME-PROBE\] PASS client scenario=" + [regex]::Escape($Scenario)
    }
    if ($latestLog -notmatch $passPattern) {
        throw "$kind run did not emit its PASS marker for scenario '$Scenario' pass $Pass"
    }
    if ($Kind -eq 'client' -and $latestLog -notmatch '\[TF-RUNTIME-PROBE\] ACCEPT experimental_features') {
        throw "Client run did not automatically accept the experimental-features warning for scenario '$Scenario'"
    }

    $forbiddenPatterns = @(
        'Missing block model: minecraft:builtin/entity',
        "Couldn't parse item model",
        'Missing (?:item|block) model',
        'Missing texture',
        "Can't keep up!",
        'OutOfMemoryError',
        'NoClassDefFoundError',
        'ClassNotFoundException',
        'Could not execute entrypoint',
        'Mixin apply failed',
        'Exception in server tick loop',
        '\[TF-RUNTIME-PROBE\] FAIL'
    )
    foreach ($pattern in $forbiddenPatterns) {
        if ($latestLog -match $pattern) {
            throw "$Kind run contains forbidden log pattern '$pattern' for scenario '$Scenario' pass $Pass"
        }
    }

    return [pscustomobject]@{
        scenario = $Scenario
        kind = $Kind
        pass = $Pass
        elapsed_ms = [long]([DateTimeOffset]::UtcNow - $startedAt).TotalMilliseconds
        game_log = [IO.Path]::GetRelativePath($repoRoot, $gameLog).Replace('\', '/')
        command_log = [IO.Path]::GetRelativePath($repoRoot, $commandLog).Replace('\', '/')
        thread_dumps = @($threadDumps)
    }
}

$selectedScenarios = Get-SelectedScenarios
$results = [Collections.Generic.List[object]]::new()
[IO.Directory]::CreateDirectory($matrixRoot) | Out-Null

foreach ($scenario in $selectedScenarios) {
    $definition = $scenarioDefinitions[$scenario]
    $jars = Resolve-ScenarioJars $scenario
    $runDirectory = Join-Path $matrixRoot $scenario
    Assert-SafeMatrixPath $runDirectory

    if ($Mode -in @('server', 'all')) {
        if (Test-Path -LiteralPath $runDirectory) {
            Remove-Item -LiteralPath $runDirectory -Recurse -Force
        }
        [IO.Directory]::CreateDirectory((Join-Path $runDirectory 'mods')) | Out-Null
        foreach ($jar in $jars) {
            Copy-Item -LiteralPath $jar -Destination (Join-Path $runDirectory 'mods')
        }
        Write-Utf8File (Join-Path $runDirectory 'eula.txt') "eula=true`n"
        Write-Utf8File (Join-Path $runDirectory 'server.properties') @"
allow-flight=true
difficulty=peaceful
enable-command-block=false
enable-query=false
enable-rcon=false
enable-status=false
enforce-secure-profile=false
gamemode=creative
generate-structures=false
level-name=compat-world
level-seed=8675309
max-tick-time=120000
motd=Twilight Forest runtime compatibility probe
online-mode=false
player-idle-timeout=0
server-port=25565
simulation-distance=4
spawn-protection=0
sync-chunk-writes=true
view-distance=4
white-list=false
"@
        $results.Add((Start-GradleRun $scenario $runDirectory server 1 $ServerTimeoutSeconds $definition.mods))
        $results.Add((Start-GradleRun $scenario $runDirectory server 2 $ServerTimeoutSeconds $definition.mods))

        $sourceWorld = Join-Path $runDirectory 'compat-world'
        $savesDirectory = Join-Path $runDirectory 'saves'
        if (-not (Test-Path -LiteralPath (Join-Path $sourceWorld 'level.dat'))) {
            throw "Server scenario '$scenario' did not save compat-world"
        }
        [IO.Directory]::CreateDirectory($savesDirectory) | Out-Null
        Copy-Item -LiteralPath $sourceWorld -Destination $savesDirectory -Recurse
    }

    if ($Mode -in @('client', 'all')) {
        if (-not (Test-Path -LiteralPath (Join-Path $runDirectory 'saves\compat-world\level.dat'))) {
            throw "Client scenario '$scenario' needs a server-created saves/compat-world; run server mode first"
        }
        $results.Add((Start-GradleRun $scenario $runDirectory client 3 $ClientTimeoutSeconds $definition.mods))
        $minimumScreenshots = if ($definition.mods -contains 'wthit') { 7 } else { 4 }
        $screenshots = @(Get-ChildItem -LiteralPath (Join-Path $runDirectory 'screenshots') -Filter "$scenario-*.png" -File)
        if ($screenshots.Count -lt $minimumScreenshots) {
            throw "Client scenario '$scenario' produced $($screenshots.Count) screenshots; expected at least $minimumScreenshots"
        }
    }
}

$artifactInputs = [ordered]@{}
foreach ($scenario in $selectedScenarios) {
    foreach ($jar in (Resolve-ScenarioJars $scenario)) {
        if (-not $artifactInputs.Contains($jar)) {
            $artifactInputs[$jar] = (Get-FileHash -LiteralPath $jar -Algorithm SHA256).Hash.ToLowerInvariant()
        }
    }
}
$summary = [ordered]@{
    generated_at_utc = [DateTimeOffset]::UtcNow.ToString('o')
    mode = $Mode
    scenarios = @($selectedScenarios)
    artifact_sha256 = $artifactInputs
    results = @($results)
}
$summaryPath = Join-Path $matrixRoot 'matrix-results.json'
Write-Utf8File $summaryPath ($summary | ConvertTo-Json -Depth 8)
Write-Host "Runtime compatibility matrix passed. Evidence: $summaryPath"
