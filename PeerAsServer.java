package p2p;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Esta clase tiene como funcion princiapl manejar los threads que se van creando para responder a los demas peer cuando
 * quieren conectarse conmigo, ya sea para actualizar el listado de peer, pedir una parte etc..
 */
@SuppressWarnings("InfiniteLoopStatement")
class PeerAsServer implements Runnable{

    private final int mipuerto;
    private RandomAccessFile archivo;

    PeerAsServer(int puertodado, RandomAccessFile archivo){
       this.archivo = archivo;
       mipuerto = puertodado;
    }

    public void run(){
        ServerSocket clientser = null;
        ExecutorService manejador = null;
        ServerSocketChannel clientchannel;
        Socket client;

        try {
            System.out.println("PeerServer con el puerto: "+ mipuerto);
            clientser = new ServerSocket(mipuerto);
            clientchannel = ServerSocketChannel.open();
            clientchannel.socket().bind(new InetSocketAddress(mipuerto+1));
            manejador = Executors.newCachedThreadPool();
              while(true){
                try{
                    client = clientser.accept();
                    manejador.execute(new traspaso(client, archivo));
                }catch	(IOException e){
                    e.printStackTrace();
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }finally{
            assert manejador != null;
            manejador.shutdown();
            try {
                clientser.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
