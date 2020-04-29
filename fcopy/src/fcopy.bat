@echo off
java %JAVA_ARGS% -Xnoclassgc -Xshare:off -noverify -cp "%~dp0\fcopy.jar";%CLASSPATH% id.fcopy.FCopyApp %*