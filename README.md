# Quiz Game Backend

This is the backend server for the Multiplayer Quiz Game. It handles admin connections for managing quiz questions stored in MongoDB.

## Prerequisites

- **JDK 21** (or compatible Java version matching `pom.xml`)
- **Maven** (for building and running)
- **MongoDB** (local instance or remote accessible via URI)

## Setup

### 1. Configure MongoDB Connection

The backend uses environment variables to connect to MongoDB. These are loaded from a `.env` file in the `.idea` directory.

1. Navigate to the `.idea` directory:
   ```powershell
   cd Backend\Network_Backend\.idea
   ```

2. Copy the sample environment file:
   ```powershell
   Copy-Item .env.sample .env
   ```

3. Edit `.env` and update with your MongoDB details:
   ```
   MONGO_URI=mongodb://localhost:27017
   MONGO_DB=quizgame
   ```

### 2. Build the Project

From the backend root directory:

```powershell
cd C:\Users\Sashini\NPAssignment\Backend\Network_Backend
mvn clean compile
```

## Running the Server

### Option 1: Using Maven Exec Plugin (Recommended)

The easiest way to run the server:

```powershell
mvn exec:java
```

This uses the `exec-maven-plugin` configured in `pom.xml` with `Main` as the main class.

### Option 2: Manual Java Command

If you prefer to run directly with `java`:

```powershell
# Copy dependencies to target/dependency
mvn dependency:copy-dependencies -DoutputDirectory=target/dependency

# Run the main class
java -cp "target/classes;target/dependency/*" Main
```

## Verification

When the server starts successfully, you should see:

```
=== Quiz Game Server ===
Connected to MongoDB database: quizgame
Question Manager initialized with X questions.
Admin Server started on port 8081
Waiting for admin connections...
```

### Test the Server

From PowerShell, verify port 8081 is listening:

```powershell
Test-NetConnection -ComputerName localhost -Port 8081
```

## Project Structure

```
Backend/Network_Backend/
├── src/main/java/
│   ├── Main.java                    # Server entry point
│   ├── admin/
│   │   ├── AdminAuthentication.java
│   │   └── AdminRequestHandler.java # Handles admin connections
│   ├── model/
│   │   └── Question.java            # Question data model
│   └── utils/
│       ├── QuestionManager.java     # CRUD operations for questions
│       ├── QuestionSeeder.java
│       └── QuickAddQuestion.java
├── src/main/resources/
│   └── questions.json               # Question data (if used)
├── .idea/
│   ├── .env.sample                  # Sample environment config
│   └── .env                         # Your environment config (create this)
├── pom.xml                          # Maven configuration
└── README.md                        # This file
```

## Ports

- **8081**: Admin server (for managing questions)

## Troubleshooting

### MongoDB Connection Failed

**Error**: `Could not connect to MongoDB` or similar connection errors

**Solution**: 
- Ensure MongoDB is running: `mongod` or check your MongoDB service
- Verify `MONGO_URI` in `.idea/.env` is correct
- Test connection: `mongosh "mongodb://localhost:27017"`

### NoClassDefFoundError

**Error**: Missing dependency classes at runtime

**Solution**: 
- Run `mvn clean compile` to rebuild
- If using manual java command, ensure you ran `mvn dependency:copy-dependencies`

### Port Already in Use

**Error**: `Address already in use: bind` on port 8081

**Solution**:
- Check if another process is using port 8081:
  ```powershell
  netstat -ano | findstr :8081
  ```
- Stop the conflicting process or change the port in `Main.java` (ADMIN_PORT constant)

### .env File Not Found

**Error**: `Dotenv not found` or environment variables are null

**Solution**:
- Ensure `.idea/.env` exists (copy from `.env.sample`)
- Run the server from `Backend/Network_Backend` directory so the relative path `.idea` is correct
- Check file location: the code looks for `.idea/.env` relative to the working directory

## Development

### Adding Questions

See [HOW_TO_ADD_QUESTIONS.md](HOW_TO_ADD_QUESTIONS.md) for instructions on adding quiz questions.

### Running in Development Mode

For development with auto-reload, consider using an IDE like IntelliJ IDEA or VS Code with Java extensions.

## Dependencies

- **MongoDB Driver** (5.5.1): For database operations
- **Jackson Databind** (2.16.0): JSON parsing and serialization
- **Dotenv Java** (3.0.0): Environment variable management

See `pom.xml` for complete dependency list.
