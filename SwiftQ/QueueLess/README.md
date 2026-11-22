# QueueLess – Smart Campus Queue Management System

A console-based Core Java project for managing student queues at college service counters. This upgraded version keeps the original Student, Staff and Admin modules and adds persistent file storage, staff account creation, and beginner-friendly multithreading demonstrations.

## Requirements
- JDK 17 or later
- VS Code with the Java Extension Pack (recommended)
- No MySQL, JDBC, external libraries, or internet connection required

## Run in VS Code
1. Extract the ZIP file.
2. Open the `QueueLess` folder in VS Code.
3. Open **Terminal → New Terminal**.
4. Run the commands for your terminal.

### Windows PowerShell
```powershell
javac -d out (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName })
java -cp out Main
```

### Windows Command Prompt
```cmd
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
java -cp out Main
```

### macOS / Linux
```bash
mkdir -p out
find src -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out Main
```

## Login and registration

### Main menu
1. Student Registration
2. Student Login
3. Staff Login
4. Admin Login
5. Exit

### Default Admin
- Username: `admin`
- Password: `admin123`

### Demo Staff
| Staff ID | Password | Assigned counter |
|---|---|---:|
| S101 | staff123 | 1 |
| S102 | staff123 | 2 |
| S103 | staff123 | 3 |

Students register through the main menu before logging in. Admin can create more staff accounts from **Admin Portal → Add staff account**. The application generates the next staff ID (for example, `S104`), creates a counter for the selected service, and displays the login details.

## Main features

### Student
- Register and log in
- View college services
- Generate a unique token (normal or priority)
- View queue position and estimated waiting time
- Cancel a waiting token
- View token history

### Staff
- Log in with an account created by the admin or a demo account
- View assigned counter queue
- Call next token, start serving, complete service, or skip an absent student
- Only one token per service can be in Called/Serving state at a time

### Admin
- View, add, and update services
- View and activate/deactivate counters
- View students, staff, and all tokens
- Add staff accounts and see generated staff IDs
- View queue statistics
- Change the admin password
- Run a small multithreading demonstration

## Data storage
The application automatically saves records to `queueless-data.ser` in the project folder using Java object serialization (`ObjectOutputStream` / `ObjectInputStream`). Students, staff, services, counters, tokens, token statuses, and the token number are restored when the application starts again.

**Important:** Keep `queueless-data.ser` in the project folder if you want to retain your data. Delete this file only if you want to reset the application to its original demo accounts and sample services. Passwords are stored in this educational local demo file as plain text; this is not suitable for production use.

## Multithreading – intentionally kept simple
Choose **Admin Portal → Run simple multithreading demo**. The demo is separate from real queue records, so it does not change students' actual tokens.

| Syllabus topic | Where it is demonstrated |
|---|---|
| Creating threads / Runnable | Two producer threads and one consumer thread |
| Thread lifecycle and control | `start()`, `getState()`, `sleep()`, and `join()` |
| Daemon thread | A small background monitor thread |
| Synchronization / race-condition protection | `synchronized` queue methods and synchronized producer-consumer methods |
| `wait()` | Consumer waits when the shared buffer is empty |
| `notify()` / `notifyAll()` | Producers wake one or all waiting consumers as appropriate |
| ExecutorService / thread pool | A small fixed thread pool runs a background task |
| Callable / Future | Callable returns the token count; Future retrieves the result |
| ScheduledExecutorService | Runs one delayed demonstration report |

The actual token-generation method is also synchronized to protect token creation from simultaneous calls. The concurrency demonstration uses only sample messages and a read-only token count; it does not insert demo tokens into the real queue.

## Other Java concepts covered
- OOP: abstraction, encapsulation, inheritance, and method overriding
- Collections: ArrayList, HashSet, HashMap, PriorityQueue, Comparator
- Exception handling: custom exceptions and input validation
- File handling: file streams, object streams, serialization, and deserialization
- Queue logic: priority tokens first; FIFO order for tokens with the same priority

## Suggested evaluation demonstration
1. Register a student and log in.
2. Generate a token and show its queue position.
3. Log out and log in as staff (`S101` / `staff123`). Call, start, and complete a token.
4. Log in as admin (`admin` / `admin123`), add a staff account, and show the generated Staff ID.
5. Run the multithreading demonstration.
6. Exit and reopen the program to show that saved records remain available.

## Project limitation
This is a local console-based academic project. It does not use a database, network server, graphical interface, or real SMS/mobile notifications.
