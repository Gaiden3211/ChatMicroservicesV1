FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Копіюємо весь проєкт, щоб Maven бачив батьківський pom.xml і модулі
COPY . .

# Переходимо в корінь (хоча ми вже там, але для надійності)
WORKDIR /app

# Збираємо ТІЛЬКИ guild-service, пропускаючи тести
# -am (also make) гарантує, що common-grpc теж збереться
RUN ./mvnw clean package -pl guild-service -am -DskipTests

# Етап 2: Запуск
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Копіюємо готовий JAR
COPY --from=builder /app/guild-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]