@echo off
chcp 1251

echo ������� ����...
call gradlew cleanCache

echo ���������� ������� �����...
call gradlew setupDecompWorkspace idea

pause