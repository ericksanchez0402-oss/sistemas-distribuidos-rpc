package mx.ipn.esimecu.rpc;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BitacoraImpl extends UnicastRemoteObject implements BitacoraRemota {
    private static final long serialVersionUID = 1L;
    private static final String URL_DB = "jdbc:sqlite:bitacora.db";

    protected BitacoraImpl() throws RemoteException {
        super();
        inicializarBD();
    }

    private void inicializarBD() {
        try (Connection conn = DriverManager.getConnection(URL_DB);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS operaciones (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "operacion TEXT NOT NULL, " +
                         "resultado TEXT NOT NULL, " +
                         "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(sql);
        } catch (Exception e) {
            System.err.println("Error al inicializar BD: " + e.getMessage());
        }
    }

    @Override
    public List<String> obtenerHistorial() throws RemoteException {
        List<String> historial = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL_DB);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM operaciones ORDER BY id DESC LIMIT 10")) {
            while (rs.next()) {
                historial.add(rs.getString("timestamp") + " | " + 
                              rs.getString("operacion") + " = " + 
                              rs.getString("resultado"));
            }
        } catch (Exception e) {
            throw new RemoteException("Error al consultar la bitácora", e);
        }
        return historial;
    }
}
