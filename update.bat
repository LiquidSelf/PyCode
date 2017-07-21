@echo off
chcp 1251

echo Очистка кэша...
call gradlew cleanCache

echo Обновление рабочей среды...
call gradlew setupDecompWorkspace idea

pause