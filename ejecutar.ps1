param([switch]$Pruebas)

$ErrorActionPreference = 'Stop'
$codificacionAnterior = [Console]::OutputEncoding
try {
    [Console]::OutputEncoding = [Text.UTF8Encoding]::new($false)
    $directorioSalida = Join-Path $PSScriptRoot 'salida/clases'
    New-Item -ItemType Directory -Path $directorioSalida -Force | Out-Null
    $directoriosFuente = @(Join-Path $PSScriptRoot 'src')
    if ($Pruebas) {
        $directoriosFuente += Join-Path $PSScriptRoot 'pruebas'
    }
    $archivosFuente = @(Get-ChildItem -LiteralPath $directoriosFuente -Filter '*.java' -Recurse |
        ForEach-Object { $_.FullName })
    & javac -encoding UTF-8 -Xlint:all -Werror -d $directorioSalida $archivosFuente
    if ($LASTEXITCODE -ne 0) { throw 'La compilacion fallo.' }

    if ($Pruebas) {
        $clasesPrueba = @(
            'shopmaster.catalogo.PruebaComposite',
            'shopmaster.pedido.PruebaDecorator',
            'shopmaster.pagos.PruebaAdapter',
            'shopmaster.pagos.PruebaProxy',
            'shopmaster.compra.PruebaFacade',
            'PruebaMain'
        )
        foreach ($clase in $clasesPrueba) {
            Write-Output "Ejecutando $clase"
            & java '-Dstdout.encoding=UTF-8' -cp $directorioSalida $clase
            if ($LASTEXITCODE -ne 0) { throw "Fallo la prueba $clase." }
        }
        Write-Output 'Las seis suites de pruebas finalizaron correctamente.'
    } else {
        & java '-Dstdout.encoding=UTF-8' -cp $directorioSalida Main
        if ($LASTEXITCODE -ne 0) { throw 'La ejecucion de Main fallo.' }
    }
} finally {
    [Console]::OutputEncoding = $codificacionAnterior
}
