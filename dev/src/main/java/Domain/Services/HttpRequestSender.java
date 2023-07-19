package Domain.Services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import com.google.gson.Gson;
public class HttpRequestSender {
    public static String sendPostRequest(String url, Map<String, String> requestBody) throws IOException {
        // Convert the request body map to a URL-encoded string
        StringBuilder encodedBody = new StringBuilder();
        for (Map.Entry<String, String> entry : requestBody.entrySet()) {
            if (encodedBody.length() > 0) {
                encodedBody.append("&");
            }
            encodedBody.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        // Create a URL object with the specified URL
        URL requestUrl = new URL(url);

        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();

        // Set the request method to POST
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // Set the content type header
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // Write the request body
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(encodedBody.toString().getBytes());
        outputStream.flush();
        outputStream.close();

        // Get the response code
        int responseCode = connection.getResponseCode();

        // Check if the request was successful (response code 200)
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Create a BufferedReader to read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            // Read the response line by line
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Close the reader
            reader.close();

            // Return the response as a string
            return response.toString();
        } else {
            // Request failed, return an empty string or throw an exception
            return "Error Occurred";
        }
    }

    //curl -X POST -H "Content-Type: application/x-www-form-urlencoded"
    // -d "action_type=handshake" https://php-server-try.000webhostapp.com/
}


