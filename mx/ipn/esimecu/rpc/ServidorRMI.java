package mx.ipn.esimecu.rpc;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class ServidorRMI {
    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.keyStore", "keystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");

        // Activar el gestor de seguridad para que lea el archivo server.policy
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        Registry registry = LocateRegistry.createRegistry(1099, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
        
        // Instanciamos ambos objetos
        Calculadora calc = new CalculadoraImpl();
        BitacoraRemota bitacora = new BitacoraRemotaImpl();
        
        // Publicamos ambos en el registro seguro
        registry.rebind("CalculadoraIPN", calc);
        registry.rebind("BitacoraIPN", bitacora);
        
        System.out.println("Servidor RMI SEGURO (SSL/TLS) con Bitácora listo en el puerto 1099");
    }
}
