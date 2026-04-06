FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -pl webstore-view -am -DskipTests package

FROM eclipse-temurin:21-jre-jammy
RUN useradd --system --uid 10001 spring \
    && mkdir -p /srv/webstore/media /srv/webstore/theme /app \
    && chown -R spring:spring /srv/webstore /app
WORKDIR /app
COPY --from=build /workspace/webstore-view/target/webstore-view-1.0.0-SNAPSHOT.jar /app/app.jar
ENV SERVER_PORT=8080
EXPOSE 8080
USER spring
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
