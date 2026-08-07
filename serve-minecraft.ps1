# Simple HTTP server to serve Minecraft jar at the S3 URL path that ForgeGradle expects
$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add("http://localhost:8888/")
$listener.Start()
Write-Host "Serving on http://localhost:8888/"

$jarPath = "$env:USERPROFILE\.gradle\caches\minecraft\versions\1.7.10\1.7.10.jar"
$serverJarPath = "$env:USERPROFILE\.gradle\caches\minecraft\versions\1.7.10\1.7.10-server.jar"
$jsonPath = "$env:USERPROFILE\.gradle\caches\minecraft\assets\indexes\1.7.10.json"

while ($listener.IsListening) {
    try {
        $context = $listener.GetContext()
        $request = $context.Request
        $response = $context.Response

        $path = $request.Url.AbsolutePath
        Write-Host "Request: $path"

        if ($path -match "1\.7\.10\.jar$") {
            $file = $jarPath
        } elseif ($path -match "minecraft_server\.1\.7\.10\.jar$") {
            $file = $serverJarPath
        } elseif ($path -match "1\.7\.10\.json$") {
            $file = $jsonPath
        } else {
            $response.StatusCode = 404
            $response.Close()
            continue
        }

        if (Test-Path $file) {
            $buf = [System.IO.File]::ReadAllBytes($file)
            $response.ContentLength64 = $buf.Length
            $response.OutputStream.Write($buf, 0, $buf.Length)
            Write-Host "Served: $file"
        } else {
            $response.StatusCode = 404
        }
        $response.Close()
    } catch {
        Write-Host "Error: $_"
    }
}
