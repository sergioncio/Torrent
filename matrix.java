package p2p;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Clase donde se guarda la informacion de los demás pares
 * incluyendo el puerto, la ip, el bitmap de las partes que tiene
 * y el nombre del archivo.
 */
class matrix implements Serializable {


    private final int puerto;
    private InetAddress ip;
    private  AtomicIntegerArray bitomap ;
    private String name;


    matrix(String name, InetAddress ip, int puerto, AtomicIntegerArray bitomap){
        this.ip = ip;
        this.puerto = puerto;
        this.bitomap = bitomap;
        this.name = name;
    }

    /**
     * Devuelve el puerto.
     *
     * @return
     */
    int getport(){return puerto;}

    /**
     * Devuelve la ip.
     *
     * @return
     */
    InetAddress getIp(){return ip;}

    /**
     * Devuelve el bitmap.
     *
     * @return
     */
    AtomicIntegerArray  getbitmap(){return bitomap;}

    /**
     * Devuelve el nombre.
     *
     * @return
     */
    String getname(){return name;}


}
