FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew --no-daemon :webstore-view:jar stageDistribution

FROM eclipse-temurin:25-jre
RUN useradd --system --uid 10001 spring \
    && mkdir -p /srv/webstore/media /srv/webstore/theme /app \
    && chown -R spring:spring /srv/webstore /app
WORKDIR /app
COPY --from=build /workspace/distribution/storefront/ /app/
ENV SERVER_PORT=8080
EXPOSE 8080
USER spring
ENTRYPOINT ["java", "-cp", "/app/application.jar:/app/libs/*", "org.tavall.webstore.WebstoreApplication"]
