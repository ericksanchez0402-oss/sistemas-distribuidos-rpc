package mx.ipn.esimecu.rpc;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BitacoraRemota extends Remote {
    String consultarHistorial() throws RemoteException;
}
