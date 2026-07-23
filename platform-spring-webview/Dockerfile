FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew --no-daemon :platform-spring-webview:jar stageDistribution

FROM eclipse-temurin:25-jre
RUN useradd --system --uid 10001 spring
WORKDIR /app
COPY --from=build /workspace/distribution/platform/ /app/
ENV SERVER_PORT=8080
EXPOSE 8080
USER spring
ENTRYPOINT ["java", "-cp", "/app/application.jar:/app/libs/*", "org.tavall.platform.PlatformSpringWebviewApplication"]
