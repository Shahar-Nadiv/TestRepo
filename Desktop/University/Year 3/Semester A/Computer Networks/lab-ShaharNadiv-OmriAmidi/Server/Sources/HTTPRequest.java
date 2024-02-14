import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HTTPRequest {
    private String type;
    private String requestedPage;
    private boolean isImage;
    private int contentLength;
    private String referer;
    private String userAgent;
    private Map<String, String> parameters;
    private String rawRequestLine;

    public HTTPRequest(BufferedReader in) throws IOException {
        parameters = new HashMap<>();
        if (in != null) {
            parseRequest(in);
        } 
        else {
            throw new IOException("BufferedReader is null");
        }
    }

    private void parseRequest(BufferedReader in) throws IOException {
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Request line is null or empty");
        }
        System.out.println();
        System.out.println("Request Line: " + requestLine);
        this.rawRequestLine = requestLine; // Store the raw request line here

        // Parse the request line
        String[] tokens = requestLine.split(" ");
        if (tokens.length >= 2) {
            this.type = tokens[0];
            this.requestedPage = tokens[1];
            this.isImage = requestedPage.matches(".*\\.(bmp|gif|png|jpg|jpeg)$");
        } else {
            throw new IOException("Invalid request line format");
        }

        // Parse headers
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (line.toLowerCase().startsWith("content-length:")) {
                this.contentLength = Integer.parseInt(line.substring(15).trim());
                
            } else if (line.toLowerCase().startsWith("referer:")) {
                this.referer = line.substring(8).trim();
            } else if (line.toLowerCase().startsWith("user-agent:")) {
                this.userAgent = line.substring(11).trim();
            }
        }

        // Parse parameters from URL or body
        if ("GET".equals(this.type)) {
            parseGetParameters(this.requestedPage);
        } else if ("POST".equals(this.type)) {
            parsePostParameters(in);
        }
    }

    private void parseGetParameters(String url) throws IOException {
        URL aURL = new URL("http://" + url);
        String query = aURL.getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    this.parameters.put(pair[0], pair[1]);
                } else {
                    this.parameters.put(pair[0], "");
                }
            }
        }
    }

    private void parsePostParameters(BufferedReader in) throws IOException {
        if (this.contentLength > 0) {
            char[] bodyChars = new char[this.contentLength];
            in.read(bodyChars);
            String body = new String(bodyChars);
            for (String param : body.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    this.parameters.put(pair[0], pair[1]);
                } else {
                    this.parameters.put(pair[0], "");
                }
            }
        }
    }

    // Getters for instance variables...

    public String getType() {
        return type;
    }

    public String getRequestedPage() {
        return requestedPage;
    }

    public boolean isImage() {
        return isImage;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getReferer() {
        return referer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getRawRequestLine() {
        return rawRequestLine;
    }
}
