# ── Stage 1: Build ──────────────────────────────────────────────────────────
# Dùng eclipse-temurin (OpenJDK) hỗ trợ ARM64 (Raspberry Pi 5)
FROM eclipse-temurin:26-jdk-noble AS builder

WORKDIR /build

# Copy Maven wrapper & POM trước để cache dependency layer
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copy source và build JAR
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ── Stage 2: Run ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:26-jre-noble

# Non-root user cho bảo mật
RUN groupadd -r ezweb && useradd -r -g ezweb ezweb

WORKDIR /app

# Tạo thư mục storage
RUN mkdir -p /app/storage && chown -R ezweb:ezweb /app

# Copy JAR từ stage 1
COPY --from=builder /build/target/*.jar app.jar
RUN chown ezweb:ezweb app.jar

USER ezweb

EXPOSE 8080

# JVM tuning cho ARM64 / Raspberry Pi 5 (4GB RAM)
# -Xms128m: heap tối thiểu thấp để tiết kiệm RAM khi idle
# -Xmx512m: heap tối đa 512MB (để lại RAM cho MySQL và OS)
# -XX:+UseContainerSupport: nhận diện giới hạn container
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-Xms128m", \
  "-Xmx512m", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
