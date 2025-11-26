FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# copy maven wrapper + root and module poms so parent POM can be resolved
COPY pom.xml .
COPY backend/mvnw .
COPY backend/.mvn .mvn
COPY backend/pom.xml backend/pom.xml

# fix line endings and permissions for mvnw
RUN chmod +x mvnw && \
    sed -i 's/\r$//' mvnw

# download dependencies for the backend module (parent POM is resolved via relativePath)
RUN ./mvnw -q -f backend/pom.xml -DskipTests dependency:go-offline

# copy the full project after dependencies are cached
COPY . .

# build the backend module
RUN ./mvnw -q -pl backend -am clean package -DskipTests

# runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# ensure output directory exists inside the container
RUN mkdir -p /app/outputFile

# copy the built backend jar (module build places artifacts under backend/target)
# use wildcard to tolerate versioned names; excludes *.original
COPY --from=build /app/backend/target/*.jar app.jar

ENTRYPOINT ["java", "-Xmx2g", "-XX:+UseG1GC", "-XX:+UseStringDeduplication", "-jar", "/app/app.jar"]
