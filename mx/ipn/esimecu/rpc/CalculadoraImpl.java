package mx.ipn.esimecu.rpc;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class CalculadoraImpl extends UnicastRemoteObject implements Calculadora {

    private static final long serialVersionUID = 1L;
    private Connection conn;

    protected CalculadoraImpl() throws RemoteException { 
        super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory()); 
        conectarBD();
    }

    private void conectarBD() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:historial.db");
            String sql = "CREATE TABLE IF NOT EXISTS bitacora ("
                       + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                       + "fecha TEXT NOT NULL,"
                       + "operacion TEXT NOT NULL"
                       + ");";
            conn.createStatement().execute(sql);
        } catch (Exception e) {
            System.out.println("Error de BD: " + e.getMessage());
        }
    }

    private void registrarOperacion(String operacion) {
        try {
            String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String sql = "INSERT INTO bitacora(fecha, operacion) VALUES(?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fecha);
            pstmt.setString(2, operacion);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("No se pudo registrar: " + e.getMessage());
        }
    }

    @Override public double sumar(double a, double b) throws RemoteException { 
        double res = a + b;
        registrarOperacion(a + " + " + b + " = " + res);
        return res; 
    }
    
    @Override public double restar(double a, double b) throws RemoteException { 
        double res = a - b;
        registrarOperacion(a + " - " + b + " = " + res);
        return res; 
    }
    
    @Override public double multiplicar(double a, double b) throws RemoteException { 
        double res = a * b;
        registrarOperacion(a + " * " + b + " = " + res);
        return res; 
    }

    @Override
    public double dividir(double a, double b) throws RemoteException {
        if (b == 0.0) throw new RemoteException("División entre cero");
        double res = a / b;
        registrarOperacion(a + " / " + b + " = " + res);
        return res;
    }
    
    @Override
    public String quienSoy() throws RemoteException {
        return "Calculadora remota segura en " + System.getProperty("os.name");
    }
}
