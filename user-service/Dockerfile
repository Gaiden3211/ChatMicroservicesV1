# --- Етап 1: Збірка ---
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# --- ВИПРАВЛЕНО ТУТ ---
# 1. Копіюємо ВЕСЬ проєкт (всі pom.xml, всі src, mvnw)
COPY . .

# 2. Переходимо в кореневу папку (де лежить mvnw)
# (Ми вже в /app, цей крок не обов'язково потрібен, але для ясності)
WORKDIR /app

# 3. Запускаємо збірку ТІЛЬКИ для user-service
#    -pl user-service = build only user-service
#    -am = also make dependencies (тобто, він автоматично збере common-grpc спочатку)
RUN ./mvnw clean package -pl user-service -am -DskipTests

# --- Етап 2: Запуск ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 4. Копіюємо згенерований JAR з правильного шляху
COPY --from=builder /app/user-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]