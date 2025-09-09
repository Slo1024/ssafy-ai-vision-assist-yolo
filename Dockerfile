FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the JAR file from backend/build/libs
COPY backend/build/libs/lookey-*.jar app.jar

# Create .env file if it doesn't exist
COPY .env* ./

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]