package p2p;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase encargada de manejar los threads que atienden los peers
 * que quieren con el tracker creando un thread,
 * ademas aqui se guarda el listado con los peer del enjambre.
 */

class trackerthread {

	/**
	 * Listado donde se va guardando la informacion de los peer y seed.
	 */
	static final List<listado> list = new ArrayList<>();

	/**
	 * Metodo principal encargado de lanzar los treads cuando llegan peticiones.
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		ExecutorService Handler = null;
		ServerSocketChannel trckerchannel;
		Socket client;
		int puerto= 5006;
		System.out.println("--Tracker Server--");

		try (ServerSocket trackerser = new ServerSocket(puerto)) {
			trckerchannel = ServerSocketChannel.open();
			trckerchannel.socket().bind(new InetSocketAddress(puerto + 1));
			Handler = Executors.newCachedThreadPool();

			//noinspection InfiniteLoopStatement
			while (true) {
				try {
					client = trackerser.accept();
					System.out.println("Ha llegado un cliente con el siguiente puerto:"+ client.getPort());
					Handler.execute(new tracker(client));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			assert Handler != null;
			Objects.requireNonNull(Handler).shutdown();
		}
	}
}
