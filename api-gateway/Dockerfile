# Етап 1: Збірка
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# 1. Копіюємо ВЕСЬ проєкт (у корінь /app)
COPY . .

# 2. ЗАЛИШАЄМОСЯ В КОРЕНІ (/app)
# Не переходимо в папку модуля!
# Запускаємо Maven з кореня, вказуючи, який модуль зібрати (-pl)
RUN ./mvnw clean package -pl api-gateway -am -DskipTests

# Етап 2: Запуск
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 3. Копіюємо JAR файл. Шлях тепер відносно кореня /app
COPY --from=builder /app/api-gateway/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]