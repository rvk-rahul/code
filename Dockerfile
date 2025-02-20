# Use Amazon Corretto 21 JDK for the build stage
FROM amazoncorretto:21 AS build

# Install necessary tools
RUN yum update -y && yum install -y tar gzip

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper and the pom.xml file to download dependencies first
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Grant execute permissions to the Maven wrapper
RUN chmod +x mvnw

# Install Maven dependencies
RUN ./mvnw dependency:go-offline -B

# Copy the rest of the application source code
COPY src ./src

# Build the application with debug output
RUN ./mvnw clean package -DskipTests -X

# Use Amazon Corretto 21 JRE for the runtime stage
FROM amazoncorretto:21 AS runtime

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port (if needed)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
