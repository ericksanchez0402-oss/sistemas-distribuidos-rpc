package mx.ipn.esimecu.rpc;
 
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
 
public class ClienteIntegrador {
    private static final HttpClient HC = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2)).build();
 
    public static String cotizar(String host, String divisa, double monto) throws Exception {
        String idem = UUID.randomUUID().toString();
        String url  = String.format("http://%s:8080/api/v1/cotizar?divisa=%s&monto=%s",
                                    host, divisa, monto);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(3))
                .header("Accept", "application/json")
                .header("Idempotency-Key", idem)
                .GET().build();
 
        int intentos = 0; long espera = 250;
        while (true) {
            try {
                HttpResponse<String> r = HC.send(req, HttpResponse.BodyHandlers.ofString());
                if (r.statusCode() >= 500 && intentos < 3) throw new RuntimeException("5xx");
                return r.body();
            } catch (Exception e) {
                if (++intentos >= 4) throw e;
                Thread.sleep(espera);  espera *= 2;   // backoff exponencial
            }
        }
    }
 
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        System.out.println(cotizar(host, "USD", 100));
        System.out.println(cotizar(host, "EUR", 250));
        System.out.println(cotizar(host, "MXN", 10));   // debe devolver 422
    }
}
