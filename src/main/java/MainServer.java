
import com.fasterxml.jackson.databind.ObjectMapper;
import static spark.Spark.*;

public class MainServer {
    // Data model for POST request
    public static class GreetingRequest {
        public String name;
    }

    public static void main(String[] args) {
        port(4567); // Default port is 4567

        // GET endpoint
        get("/hello", (req, res) -> {
            res.type("application/json");
            return "{\"message\": \"Hello World\"}";
        });

        // POST endpoint
        post("/echo", (req, res) -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                GreetingRequest request = mapper.readValue(req.body(), GreetingRequest.class);
                return "{\"echo\": \"" + request.name + "\"}";
            } catch (Exception e) {
                res.status(400); // Bad Request
                System.out.println(e);
                return "{\"error\": \"Invalid JSON format\"}";
            }
        });
    }
}