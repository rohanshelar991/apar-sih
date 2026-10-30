@echo off
echo ============================================================
echo   Setting Git Author: Abhishek Bagi (abhishekbagi262@gmail.com)
echo   Repository: https://github.com/abhishekbagi262/APAAR-ID---AuthenIQ.git
echo ============================================================
echo.

git config --global user.name "Abhishek Bagi"
git config --global user.email "abhishekbagi262@gmail.com"
git config user.name "Abhishek Bagi"
git config user.email "abhishekbagi262@gmail.com"

echo Git Author configured:
git config user.name
git config user.email
echo.
echo Pushing to GitHub as Abhishek Bagi...
echo.

git push -u origin main --force

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================
    echo   SUCCESS! All files pushed to GitHub under Abhishek Bagi.
    echo ============================================================
) else (
    echo.
    echo If GitHub asks to sign in, please sign in with abhishekbagi262@gmail.com.
)

pause
