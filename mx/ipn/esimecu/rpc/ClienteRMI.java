package mx.ipn.esimecu.rpc;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.rmi.ssl.SslRMIClientSocketFactory;

public class ClienteRMI {
    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.trustStore", "keystore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        String host = args.length > 0 ? args[0] : "localhost";
        
        Registry registry = LocateRegistry.getRegistry(host, 1099, new SslRMIClientSocketFactory());
        
        // Buscamos ambos servicios
        Calculadora c = (Calculadora) registry.lookup("CalculadoraIPN");
        BitacoraRemota b = (BitacoraRemota) registry.lookup("BitacoraIPN");
 
        System.out.println("Conectado a " + c.quienSoy());
        System.out.println("3 + 4   = " + c.sumar(3, 4));
        System.out.println("10 - 6  = " + c.restar(10, 6));
        System.out.println("7 * 8   = " + c.multiplicar(7, 8));
        try {
            System.out.println("5 / 0   = " + c.dividir(5, 0));
        } catch (Exception e) {
            System.out.println("Error remoto controlado: " + e.getMessage());
        }
        
        // Consultamos la bitácora usando el segundo objeto remoto
        System.out.println(b.consultarHistorial());
    }
}
