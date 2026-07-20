package mx.ipn.esimecu.rpc;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

public class BitacoraRemotaImpl extends UnicastRemoteObject implements BitacoraRemota {

    private static final long serialVersionUID = 1L;

    protected BitacoraRemotaImpl() throws RemoteException {
        // Implementación con SSL/TLS en puerto dinámico
        super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
    }

    @Override
    public String consultarHistorial() throws RemoteException {
        StringBuilder sb = new StringBuilder("\n--- Historial en SQLite (Desde BitacoraRemota) ---\n");
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:historial.db");
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM bitacora");
            while (rs.next()) {
                sb.append(rs.getString("fecha")).append(" | ")
                  .append(rs.getString("operacion")).append("\n");
            }
            conn.close();
        } catch (Exception e) {
            throw new RemoteException("Error al consultar BD", e);
        }
        return sb.toString();
    }
}
