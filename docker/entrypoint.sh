#!/bin/sh
set -e
# Render indica el puerto en la variable PORT (por defecto 10000)
PORT="${PORT:-10000}"
# Tomcat escucha en ese puerto y sabe que, delante, Render ya puso HTTPS
sed -i "s|<Connector port=\"8080\" protocol=\"HTTP/1.1\"|<Connector port=\"${PORT}\" protocol=\"HTTP/1.1\" scheme=\"https\" secure=\"true\" proxyPort=\"443\"|" /usr/local/tomcat/conf/server.xml
grep -q "port=\"${PORT}\"" /usr/local/tomcat/conf/server.xml
exec /usr/local/tomcat/bin/catalina.sh run
