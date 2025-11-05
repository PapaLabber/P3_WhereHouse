FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# copy maven wrapper + pom
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# download dependencies
RUN ./mvnw -q -DskipTests dependency:go-offline

# now copy source
COPY src ./src

# build jar
RUN ./mvnw clean package -DskipTests

# runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]