# Quick Start Script for Lemonview App (PowerShell)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "    LEMONVIEW APP - QUICK START" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

cd "C:\Users\Akshay\AndroidStudioProjects\Lemonview"

Write-Host "Checking for connected devices..." -ForegroundColor Yellow
& "C:\Users\Akshay\AppData\Local\Android\Sdk\platform-tools\adb.exe" devices

Write-Host ""
Write-Host "Waiting for device to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "Installing app..." -ForegroundColor Yellow
& "C:\Users\Akshay\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✓ Installation successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Launching app..." -ForegroundColor Yellow
    & "C:\Users\Akshay\AppData\Local\Android\Sdk\platform-tools\adb.exe" shell am start -n "com.lemonview.ai/.IntroActivity"
    Write-Host ""
    Write-Host "✓ App launched! Check your emulator/device..." -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "✗ Installation failed. Make sure emulator is running." -ForegroundColor Red
    Write-Host ""
    Write-Host "Try:" -ForegroundColor Yellow
    Write-Host "  1. Start emulator manually from Android Studio" -ForegroundColor White
    Write-Host "  2. Wait 60 seconds for it to fully boot" -ForegroundColor White
    Write-Host "  3. Run this script again" -ForegroundColor White
}

Read-Host "Press Enter to exit"
