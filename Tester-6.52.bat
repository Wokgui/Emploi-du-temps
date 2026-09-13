@echo off
setlocal EnableExtensions
chcp 65001 >nul
set "BRANCH=test/652-smoke-navigation-ack"
set "WORK=%USERPROFILE%\Emploi-du-temps-652-test"
set "ZIP=%TEMP%\emploi-du-temps-652.zip"

echo [1/4] Preparation du dossier local...
if not exist "%WORK%" mkdir "%WORK%"

echo [2/4] Telechargement de la branche 6.52 de test...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Invoke-WebRequest -UseBasicParsing 'https://github.com/Wokgui/Emploi-du-temps/archive/refs/heads/test/652-smoke-navigation-ack.zip' -OutFile '%ZIP%'"
if errorlevel 1 goto :fail

echo [3/4] Extraction du depot...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Get-ChildItem -LiteralPath '%WORK%' -Force | Remove-Item -Recurse -Force; Expand-Archive -LiteralPath '%ZIP%' -DestinationPath '%WORK%' -Force"
if errorlevel 1 goto :fail

echo [4/4] Depot pret.
for /d %%D in ("%WORK%\Emploi-du-temps-*") do set "REPO=%%~fD"
if not defined REPO goto :fail

echo.
echo Branche preparee : %BRANCH%
echo Dossier local      : %REPO%
echo.
echo L'etape 4 est terminee. Aucun Git n'est necessaire.
echo Garde cette fenetre ouverte ou note le dossier ci-dessus pour l'etape suivante.
pause
exit /b 0

:fail
echo.
echo ECHEC : impossible de preparer automatiquement le depot.
echo Rien n'a ete modifie sur le telephone.
pause
exit /b 1
