# Use official Java image
FROM openjdk:17

# Set working directory
WORKDIR /app

# Copy all project files into the container
COPY . .

# Compile Java files
RUN javac -cp ".:mysql-connector-j-9.4.0.jar" *.java

# Expose the chat server port
EXPOSE 5000

# Run the server
CMD ["java", "-cp", ".:mysql-connector-j-9.4.0.jar", "ChatServer"]
