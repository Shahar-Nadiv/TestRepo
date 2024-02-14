import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String rootDirectory;

    public ClientHandler(Socket clientSocket, String rootDirectory) {
        this.clientSocket = clientSocket;
        this.rootDirectory = rootDirectory;
    }

    @Override
    public void run() {
        try {
            handleRequest();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSocket();
        }
    }

    private void handleRequest() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = clientSocket.getOutputStream();
        boolean isChunked = false;
    
        try {
            HTTPRequest httpRequest = new HTTPRequest(in);
            switch (httpRequest.getType()) {
                case "GET":
                    handleGetRequest(out, httpRequest, isChunked);
                    break;
                case "POST":
                    handlePostRequest(out, httpRequest, isChunked);
                    break;
                case "HEAD":
                    handleHeadRequest(out, httpRequest, isChunked);
                    break;
                case "TRACE":
                    handleTraceRequest(out, httpRequest);
                    break;
                default:
                    sendErrorResponse(out, "HTTP/1.1 501 Not Implemented\r\n\r\n", isChunked);
                    break;
            }
        } catch (IOException e) {
            sendErrorResponse(out, "HTTP/1.1 400 Bad Request\r\n\r\n", isChunked);
        }
    }

    private void handleGetRequest(OutputStream out, HTTPRequest httpRequest, boolean isChunked) throws IOException {
        String fileName = httpRequest.getRequestedPage();
        Map<String, String> getParams = httpRequest.getParameters();

        // Print the request details
        System.out.println("Type: " + httpRequest.getType());
        System.out.println("Requested Page: " + httpRequest.getRequestedPage());
        System.out.println("Is Image: " + httpRequest.isImage());
        System.out.println("Content Length: " + httpRequest.getContentLength());
        System.out.println("Referer: " + httpRequest.getReferer());
        System.out.println("User Agent: " + httpRequest.getUserAgent());
        System.out.println("Parameters: " + httpRequest.getParameters());

        Path filePath = Paths.get(rootDirectory, fileName).normalize();
        if (!Files.exists(filePath) || !isPathInsideRoot(Paths.get(rootDirectory), filePath)) {
            sendErrorResponse(out, "HTTP/1.1 404 Not Found", isChunked);
            return;
        }

        String contentType = getContentType(filePath.toString());
        byte[] content = Files.readAllBytes(filePath);

        // Print the response details
        System.out.println();
        System.out.println("Response: HTTP/1.1 200 OK");
        System.out.println("Content-Type: " + contentType);
        System.out.println("Content-Length: " + content.length);

        String header = buildHeader("HTTP/1.1 200 OK", contentType, content.length, isChunked);
        sendResponse(out, header, content, isChunked);
    }

    private void handlePostRequest(OutputStream out, HTTPRequest httpRequest, boolean isChunked) throws IOException {
        String fileName = httpRequest.getRequestedPage();
        System.out.println("Handling POST request for: " + fileName);

        // Print request headers and data
        System.out.println("Type: " + httpRequest.getType());
        System.out.println("Requested Page: " + httpRequest.getRequestedPage());
        System.out.println("Content Length: " + httpRequest.getContentLength());
        System.out.println("Referer: " + httpRequest.getReferer());
        System.out.println("User-Agent: " + httpRequest.getUserAgent());

        int contentLength = httpRequest.getContentLength();
        if (contentLength <= 0) {
            System.out.println("No content to read for POST request");
            sendErrorResponse(out, "HTTP/1.1 400 Bad Request", isChunked);
            return;
        }

        Map<String, String> postParams = httpRequest.getParameters();
        System.out.println("POST Parameters: " + postParams);

        String postData = postParams.toString(); // Convert parameters map to String for logging
        System.out.println("POST Data: " + postData);

        // Prepare response content
        String responseMessage = "Form submitted successfully. Data received: " + postData;
        byte[] responseContent = responseMessage.getBytes(StandardCharsets.UTF_8);

        // Print response details
        System.out.println();
        System.out.println("Response: HTTP/1.1 200 OK");
        System.out.println("Content-Type: text/plain");
        System.out.println("Content-Length: " + responseContent.length);
        System.out.println();
        // Prepare and send the response
        String header = buildHeader("HTTP/1.1 200 OK", "text/plain", responseContent.length, isChunked);
        sendResponse(out, header, responseContent, isChunked);
    }

    private void handleHeadRequest(OutputStream out, HTTPRequest httpRequest, boolean isChunked) throws IOException {
        String fileName = httpRequest.getRequestedPage();
        System.out.println("Handling HEAD request for: " + fileName);

        Path filePath = Paths.get(rootDirectory, fileName).normalize();
        if (!Files.exists(filePath) || !isPathInsideRoot(Paths.get(rootDirectory), filePath)) {
            System.out.println("File not found: " + fileName);
            sendErrorResponse(out, "HTTP/1.1 404 Not Found", isChunked);
            return;
        }

        long contentLength = Files.size(filePath); // Get content length without reading the content
        if(contentLength >= 0){
            String contentType = getContentType(filePath.toString());

            // Print the response headers

            System.out.println("Response: HTTP/1.1 200 OK");
            System.out.println("Content-Type: " + contentType);
            System.out.println("Content-Length: " + contentLength);
            System.out.println("Type: " + httpRequest.getType());
            System.out.println("Requested Page: " + httpRequest.getRequestedPage());
            System.out.println("Is Image: " + httpRequest.isImage());
            System.out.println("Content Length: " + httpRequest.getContentLength());
            System.out.println("Referer: " + httpRequest.getReferer());
            System.out.println("User Agent: " + httpRequest.getUserAgent());
            System.out.println("Parameters: " + httpRequest.getParameters());
            System.out.println();

            String header = buildHeader("HTTP/1.1 200 OK", contentType, (int) contentLength, isChunked);
            sendResponse(out, header, new byte[0], isChunked); // Send headers only
        }

    }

    private void handleTraceRequest(OutputStream out, HTTPRequest httpRequest) throws IOException {
        String requestLine = httpRequest.getRawRequestLine();
        System.out.println("Handling TRACE request");
        // System.out.println("Request Line: " + requestLine);
        System.out.println("Server Port: " + ConfigReader.getPort("config.ini"));

        String header = buildHeader("HTTP/1.1 200 OK", "message/http", requestLine.length(), false);

        // Print the echo response
        System.out.println("Response: ");
        System.out.println(header);
        System.out.println(requestLine);

        sendResponse(out, header, requestLine.getBytes(StandardCharsets.UTF_8), false);
    }

    private boolean isPathInsideRoot(Path rootPath, Path filePath) {
        // Normalize the paths to remove any ".." or "." that can lead to directory
        // traversal
        Path normalizedRootPath = rootPath.normalize();
        Path normalizedFilePath = filePath.normalize();

        // Check if the file path starts with the root path
        return normalizedFilePath.startsWith(normalizedRootPath);
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".html")) {
            return "text/html";
        } else if (fileName.matches(".*\\.(bmp|gif|png|jpg|jpeg)$")) {
            return "image";
        } else if (fileName.endsWith(".ico")) {
            return "icon";
        } else {
            return "application/octet-stream";
        }
    }

    private void sendResponse(OutputStream out, String header, byte[] content, boolean isChunked) throws IOException {
        out.write(header.getBytes(StandardCharsets.UTF_8));

        if (isChunked) {
            int offset = 0;
            int chunkSize = 1024; // You can adjust the chunk size as needed
            while (offset < content.length && content.length > 0) {
                int length = Math.min(chunkSize, content.length - offset);
                String chunkHeader = Integer.toHexString(length) + "\r\n";
                out.write(chunkHeader.getBytes(StandardCharsets.UTF_8));
                out.write(content, offset, length);
                out.write("\r\n".getBytes(StandardCharsets.UTF_8));
                offset += length;
            }
            out.write("0\r\n\r\n".getBytes(StandardCharsets.UTF_8)); // End of chunks
        } else {
            out.write(content); // For regular content-length response
        }
        
        out.flush();
        out.close();
    }

    private void sendErrorResponse(OutputStream out, String statusLine, boolean isChunked) throws IOException {
        // Print the error response details based on the status line
        if (statusLine.contains("404")) {
            System.out.println("404 Not Found – the file was not found.");
        } else if (statusLine.contains("501")) {
            System.out.println("501 Not Implemented – the method used is unknown.");
        } else if (statusLine.contains("400")) {
            System.out.println(statusLine);
            System.out.println("400 Bad Request – the request’s format is invalid.");
        } else if (statusLine.contains("500")) {
            System.out.println("500 Internal Server Error – some kind of an error occurred.");
        }

        String responseHeader = buildHeader(statusLine, "text/plain", 0, isChunked);
        sendResponse(out, responseHeader, new byte[0], isChunked);
    }

    private String buildHeader(String statusLine, String contentType, int contentLength, boolean isChunked) {
        StringBuilder header = new StringBuilder();
        header.append(statusLine).append("\r\n");
        header.append("Content-Type: ").append(contentType).append("\r\n");

        if (!isChunked) {
            header.append("Content-Length: ").append(contentLength).append("\r\n");
        } else {
            header.append("Transfer-Encoding: chunked\r\n");
        }

        header.append("\r\n");
        return header.toString();
    }

    private void closeSocket() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
