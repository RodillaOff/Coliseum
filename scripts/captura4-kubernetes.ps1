# Captura 4 (Nivel 3): minikube + ConfigMap/Secret + kubectl apply + get pods,svc + /health.
# Uso: doble clic en scripts\captura4-kubernetes.bat (o: powershell -ExecutionPolicy Bypass -File scripts\captura4-kubernetes.ps1)
# Guarda los resultados reales en docs\evidencias\ . La contrasena del Secret se genera al azar y NO se guarda.
$ErrorActionPreference = 'Continue'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Set-Location (Split-Path $PSScriptRoot -Parent)
$ev = Join-Path (Get-Location) 'docs\evidencias'
New-Item -ItemType Directory -Force $ev | Out-Null
$resumen = New-Object System.Collections.Generic.List[string]
$imagen = 'ghcr.io/rodillaoff/coliseum:latest'

function Run($cmd, $file) {
    Write-Host ">> $cmd"
    $out = (cmd /c "$cmd 2>&1") | Out-String
    $code = $LASTEXITCODE
    Set-Content -Path (Join-Path $ev $file) -Value $out -Encoding UTF8
    Write-Host $out
    return $code
}
function Paso($texto, $ok, $archivo) {
    $estado = if ($ok) { 'OK' } else { 'FALLO' }
    $resumen.Add("| $texto | $estado | $archivo |")
}

# 1) Docker Desktop
cmd /c "docker info >NUL 2>&1"
if ($LASTEXITCODE -ne 0) {
    Write-Host 'Iniciando Docker Desktop...'
    $dd = "$env:ProgramFiles\Docker\Docker\Docker Desktop.exe"
    if (Test-Path $dd) { Start-Process $dd }
    for ($i = 0; $i -lt 40; $i++) { Start-Sleep 5; cmd /c "docker info >NUL 2>&1"; if ($LASTEXITCODE -eq 0) { break } }
}

# 2) Imagen local (la misma que publica el workflow de ghcr.io)
$c = Run "docker build -t $imagen ." 'n3-docker-build.txt'
Paso 'docker build de la imagen' ($c -eq 0) 'n3-docker-build.txt'

# 3) minikube
$c = Run 'minikube start --driver=docker' 'n3-minikube-start.txt'
Paso 'minikube start' ($c -eq 0) 'n3-minikube-start.txt'
[void](Run "minikube image load $imagen" 'n3-image-load.txt')

# 4) Limpiar restos de ejecuciones anteriores (por ejemplo la version vieja con MySQL)
[void](Run 'kubectl delete deployment,service,pvc coliseum-app coliseum-db coliseum-db-datos --ignore-not-found' 'n3-limpieza.txt')
cmd /c "kubectl delete deployment coliseum-app coliseum-db --ignore-not-found >NUL 2>&1"
cmd /c "kubectl delete service coliseum-app coliseum-db --ignore-not-found >NUL 2>&1"
cmd /c "kubectl delete pvc coliseum-db-datos --ignore-not-found >NUL 2>&1"
cmd /c "kubectl delete secret coliseum-secret --ignore-not-found >NUL 2>&1"

# 5) ConfigMap con los scripts SQL y Secret con las credenciales
$c = Run 'kubectl create configmap coliseum-sql --from-file=sql/ --dry-run=client -o yaml | kubectl apply -f -' 'n3-configmap-sql.txt'
Paso 'ConfigMap coliseum-sql desde sql/' ($c -eq 0) 'n3-configmap-sql.txt'
$pw = -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 24 | ForEach-Object { [char]$_ })
kubectl create secret generic coliseum-secret "--from-literal=DB_USER=coliseum" "--from-literal=DB_PASSWORD=$pw" *> $null
$pw = $null

# 6) Manifiestos
$c = Run 'kubectl apply -f k8s/configmap.yaml -f k8s/db-deployment.yaml -f k8s/db-service.yaml -f k8s/app-deployment.yaml -f k8s/app-service.yaml' 'n3-apply.txt'
Paso 'kubectl apply de k8s/' ($c -eq 0) 'n3-apply.txt'
[void](Run 'kubectl rollout status deployment/coliseum-db --timeout=300s' 'n3-rollout-db.txt')
$c = Run 'kubectl rollout status deployment/coliseum-app --timeout=300s' 'n3-rollout-app.txt'
Paso 'rollout de coliseum-app' ($c -eq 0) 'n3-rollout-app.txt'
Start-Sleep 20

# 7) Captura 4: pods y servicios
$c = Run 'kubectl get pods,svc' 'n3-get-pods-svc.txt'
$txt = Get-Content (Join-Path $ev 'n3-get-pods-svc.txt') -Raw
$running = ([regex]::Matches($txt, '1/1\s+Running')).Count
Paso "kubectl get pods,svc (pods Running 1/1: $running de 2)" ($running -ge 2) 'n3-get-pods-svc.txt'

# 8) /health dentro del cluster y SELECT directo a la base del cluster
$c = Run 'kubectl exec deploy/coliseum-app -- java -cp "/app/classes:/app/lib/*" consola.ConsolaMain --health' 'n3-health-en-cluster.txt'
Paso '/health dentro del cluster (ConsolaMain --health)' ($c -eq 0) 'n3-health-en-cluster.txt'
[void](Run 'kubectl exec deploy/coliseum-db -- sh -c "psql -U coliseum -d coliseum_db -c \"SELECT COUNT(*) AS usuarios FROM usuarios\""' 'n3-select-db.txt')
[void](Run 'kubectl logs deploy/coliseum-app --tail=20' 'n3-logs-app.txt')

# 9) Probes, limits, ConfigMap y Secret (nombres, nunca valores)
[void](Run 'kubectl describe deployment coliseum-app coliseum-db | findstr /i /c:"Name:" /c:"Image:" /c:"Limits" /c:"Requests" /c:"cpu" /c:"memory" /c:"Liveness" /c:"Readiness" /c:"Environment Variables from" /c:"coliseum-config" /c:"coliseum-secret"' 'n3-probes-limits.txt')
[void](Run 'kubectl get configmap,secret' 'n3-configmap-secret.txt')

$md = @('# Captura 4 - Kubernetes (resultado real)', '', ('Ejecutado: ' + (Get-Date -Format 'yyyy-MM-dd HH:mm')), '', '| Paso | Resultado | Archivo |', '|---|---|---|') + $resumen
Set-Content -Path (Join-Path $ev 'captura4-resumen.md') -Value $md -Encoding UTF8
Write-Host ''
Write-Host 'LISTO. Resultados en docs\evidencias\ (captura4-resumen.md).'
Write-Host 'Para apagar: minikube stop'
