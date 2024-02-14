# HTTP multithreaded web server - Final Lab project - Omri Amidi, Shahar Nadiv.

* Shahar Nadiv(International School) - ID: 208811505

* Omri Amidi(Israeli School) - ID: 208802751



Project Overview:

This Java Web Server application is designed to handle basic HTTP requests. It is capable of processing GET, POST, HEAD, and TRACE requests.
The server uses a multi-threaded approach to handle multiple client connections simultaneously.



Classes:

### WebServer

- The main class that starts the server.
- Initializes the server socket and listens for incoming connections.
- Utilizes a thread pool to manage client connections
- Reads server configuration from a properties file (`config.ini`).

### ClientHandler

- Implements the Runnable interface, making it suitable for execution in a thread pool.
- Handles client requests by reading the HTTP request, processing it, and sending back an appropriate response.
- Supports different types of requests: GET, POST, HEAD, and TRACE.
- Uses the HTTPRequest class to parse incoming requests.

### HTTPRequest

- Parses the raw HTTP request from the client.
- Extracts important information such as request type, requested page, headers, and parameters.
- Stores the raw request line for TRACE requests.

### ConfigReader

- Utility class to read server configuration from a properties file (`config.ini`).
- Provides methods to retrieve the root directory, server port, and maximum number of threads for the server.


Summary: 

1. `WebServer`: The entry point of the application, responsible for setting up the server and managing client connections.
2. `ClientHandler`: Handles the client requests. It is designed to be executed in a separate thread for each client.
3. `HTTPRequest`: Dedicated to parsing the HTTP request, it encapsulates all the logic required to interpret different elements of the request.
4. `ConfigReader`: A utility class to manage server configurations, promoting ease of changes and scalability.

