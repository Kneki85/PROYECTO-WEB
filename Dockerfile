# ---------------------------------------------------------------
# GestPersonal - imagen para desplegar en Render (Tomcat 10.1 + JDK 17)
# Compila el proyecto dentro de Docker: no hace falta subir ningún .war.
# ---------------------------------------------------------------

# ---- Etapa 1: compilar ----
FROM tomcat:10.1-jdk17-temurin AS build
WORKDIR /app
COPY src ./src
COPY web ./web

# Copia la parte web tal cual y compila las clases Java contra las librerías de Tomcat
RUN mkdir -p /app/war \
 && cp -r web/. /app/war/ \
 && mkdir -p /app/war/WEB-INF/classes \
 && javac -encoding UTF-8 --release 17 \
      -cp "/usr/local/tomcat/lib/*:/app/war/WEB-INF/lib/*" \
      -d /app/war/WEB-INF/classes \
      $(find src/java -name '*.java')

# ---- Etapa 2: imagen final ----
FROM tomcat:10.1-jdk17-temurin

# La app se publica en la raíz (https://tu-app.onrender.com/)
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/war /usr/local/tomcat/webapps/ROOT

# Configuración solo para la nube (el proyecto local queda intacto)
COPY docker/context.xml /usr/local/tomcat/webapps/ROOT/META-INF/context.xml
COPY docker/entrypoint.sh /entrypoint.sh

# Cookie de sesión "secure" (Render sirve la app por HTTPS); falla el build si no se aplicó
RUN sed -i 's#<http-only>true</http-only>#<http-only>true</http-only><secure>true</secure>#' \
      /usr/local/tomcat/webapps/ROOT/WEB-INF/web.xml \
 && grep -q '<secure>true</secure>' /usr/local/tomcat/webapps/ROOT/WEB-INF/web.xml

# Usuario sin privilegios (no se ejecuta como root)
RUN useradd --system --no-create-home tomcatapp \
 && chmod +x /entrypoint.sh \
 && chown -R tomcatapp:tomcatapp /usr/local/tomcat
USER tomcatapp

# Memoria ajustada al plan gratuito de Render (512 MB)
ENV JAVA_OPTS="-Xmx256m -XX:MaxMetaspaceSize=96m -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"

EXPOSE 10000
ENTRYPOINT ["/entrypoint.sh"]
