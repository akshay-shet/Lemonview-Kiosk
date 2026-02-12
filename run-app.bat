@echo off
REM Quick Start Script for Lemonview App

echo.
echo ==========================================
echo    LEMONVIEW APP - QUICK START
echo ==========================================
echo.

cd /d "C:\Users\Akshay\AndroidStudioProjects\Lemonview"

echo Checking for connected devices...
adb devices

echo.
echo Waiting for device to be ready (this may take a moment)...
timeout /t 5 /nobreak

echo.
echo Installing app...
adb install -r app\build\outputs\apk\debug\app-debug.apk

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ Installation successful!
    echo.
    echo Launching app...
    adb shell am start -n com.lemonview.ai/.IntroActivity
    echo.
    echo ✓ App launched! Check your emulator/device...
) else (
    echo.
    echo ✗ Installation failed. Make sure emulator is running.
    echo Try:
    echo   1. Start emulator manually from Android Studio
    echo   2. Wait 60 seconds for it to fully boot
    echo   3. Run this script again
)

pause
