FROM openjdk:24-ea-9-jdk-oraclelinux9
ARG DEPENDENCY=build/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
EXPOSE 8080 8080
ENTRYPOINT ["java","-cp","app:app/lib/*","com.coltsclub.tusa.TusaApplicationKt"]