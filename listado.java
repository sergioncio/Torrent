package p2p;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Clase usada por el tracker para guardar la informacion
 * de los distintos pares.
 */
class listado implements Serializable{


	private final String archivo;
	  private final ArrayList<byte[]> listadohash;
	  private final int puerto;
	  private final InetAddress ip;
	  private final long fullsize;
	  private final String fullhash;


	  listado(String archivo, ArrayList<byte[]> listadohash, int puerto, InetAddress ip, long fullsize, String fullhash) {
	  	this.archivo = archivo;
	    this.listadohash = listadohash;
	    this.puerto = puerto;
	    this.ip = ip;
	    this.fullsize = fullsize;
	    this.fullhash = fullhash;
	  }

	/**
	 * Devuelve el nombre del archivo
	 */
	String getArchivo() { return archivo; }

	/**
	 * Devuelve listado de hashs.
	 * @return
	 */
	ArrayList<byte[]> getlistadohash() { return listadohash;}

	/**
	 * Devuelve el puerto.
	 *
	 * @return
	 */
	int getPuerto() {return puerto;}

	/**
	 * Devuelve la ip.
	 *
	 * @return
	 */
	InetAddress getIp(){return ip;}

	/**
	 * Devuelve el hash del archivo completo.
	 *
	 * @return
	 */
	String getFullhash(){return fullhash;}

	/**
	 * Devuelve el tamaño del archivo.
	 *
	 * @return
	 */
	long getSize() {return fullsize;}



}
