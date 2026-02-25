# --- Етап 1: Збірка ---
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# --- ВИПРАВЛЕНО ТУТ ---
# 1. Копіюємо ВЕСЬ проєкт
COPY . .

# 2. Переходимо в кореневу папку
WORKDIR /app

# 3. Запускаємо збірку ТІЛЬКИ для auth-service
#    -pl auth-service = build only auth-service
#    -am = also make dependencies (тобто, він збере common-grpc спочатку)
RUN ./mvnw clean package -pl auth-service -am -DskipTests

# --- Етап 2: Запуск ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 4. Копіюємо згенерований JAR з правильного шляху
COPY --from=builder /app/auth-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]