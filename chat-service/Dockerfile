FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Копіюємо весь проект
COPY . .

# Збираємо chat-service (і його залежності, такі як common-grpc)
RUN ./mvnw clean package -pl chat-service -am -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/chat-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]