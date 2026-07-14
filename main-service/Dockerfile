FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Сначала копируем только pom.xml — этот слой кешируется
COPY pom.xml .
COPY main-service/pom.xml main-service/
COPY stat/pom.xml stat/
COPY stat/stat-dto/pom.xml stat/stat-dto/
COPY stat/stat-client/pom.xml stat/stat-client/
COPY stat/stat-server/pom.xml stat/stat-server/
# Скачиваем зависимости отдельным слоем
RUN mvn dependency:go-offline -pl main-service -am -q
# Теперь копируем исходники
COPY . .
RUN mvn -pl main-service -am clean package -DskipTests -q

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/main-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]