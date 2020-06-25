package p2p;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 *  Se trata de un hilo creado por trackerthread, el cual recibe peticiones de los peer, ya sea para pedir informacion de los archivos disponibles de descarga, para
 *  informar de que se pone un archivo para la descarga de los demás (seed), o para salir del enjambre.
 */
@SuppressWarnings("ALL")
class tracker implements Runnable{

	
	
	 private final Socket servicio;
	 private final ArrayList<String> arraystring = new ArrayList<>();
	 private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	 private String str="";
	 private final ArrayList<String> listaarchivos = new ArrayList<>();
	 private int auxiliar;
	 private final InetAddress local = InetAddress.getByName("127.0.0.1");
	 private final ArrayList<Integer> puertobueno = new ArrayList<>();
     private final ArrayList<InetAddress> ippeers = new ArrayList<>();

	tracker(Socket s) throws UnknownHostException {
		servicio = s;
	}


	private void leer(ArrayList<byte[]> aux){
		for (byte[] anAux : aux) {
			arraystring.add(bytesToHex(anAux));
		}
		System.out.println(arraystring);
	}
	
	private static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}


@SuppressWarnings("unchecked")
    public void run(){
        ObjectInputStream in;
        ObjectOutputStream out;
        try
        {
            in = new ObjectInputStream(servicio.getInputStream());
            out = new ObjectOutputStream(servicio.getOutputStream());

        while(true){

            try {
                Object obj1 = in.readObject();
                str =(String) obj1;
                System.out.println(str);
            } catch (ClassNotFoundException cnf) {
                cnf.printStackTrace();
            } catch (IOException io) {
                throw new RuntimeException(io);
            }
            String nombrearchivo;
            int puertoapasar;

            switch(str) {
                case "registrar":
                    try {
                        puertoapasar = (int)in.readObject();
                        System.out.println("Se va a registrar un peer con puerto: "+ puertoapasar);
                        nombrearchivo = (String) in.readObject();
                        System.out.println("Nombre del archivo :"+ nombrearchivo);
                        long fullsize = (long) in.readObject();
                        System.out.println("Tama�o del archivo :"+ fullsize);
                        String hasharchivo = (String) in.readObject();
                        System.out.println("Hash del archivo completo: "+ hasharchivo);
                        System.out.println("Listado de los hash de los paquetes :");
                        ArrayList<byte[]> listahash = (ArrayList<byte[]>) in.readObject();
                        leer(listahash);
                        listado listin = new listado(nombrearchivo, listahash, puertoapasar,local, fullsize, hasharchivo);
                        trackerthread.list.add(listin);
                        System.out.println("Se ha registrado con exito");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                        Thread.interrupted();

                    break;

                    case "obtener":

                        System.out.println("Me pide la lista de objetos");
                        actualizarlistadoarchivos();
                        out.writeObject(listaarchivos.size());
                        for (String listaarchivo : listaarchivos) {
                            out.writeObject(listaarchivo);
                        }
                        System.out.println(listaarchivos.size());
                        System.out.println(listaarchivos);
                        System.out.println("Se ha enviado la lista");

                        break;

                    case "pedir":

                        System.out.println("El peer ha pedido el objeto: ");
                        String nombrearch = (String) in.readObject();
                        System.out.println("Vamos ha obtener el listado de personas con el archivo: "+ nombrearch);
                        listadopeer(nombrearch);
                        imprimirlistadopeers();
                        out.writeObject(ippeers);
                        out.writeObject(puertobueno);
                        out.writeObject(trackerthread.list.get(auxiliar).getlistadohash());
                        System.out.println("Le envio el listado de los hashes de los paquetes "+trackerthread.list.get(auxiliar).getlistadohash() );
                        out.writeObject(trackerthread.list.get(auxiliar).getSize());
                        System.out.println("Le envio el tama�o del archivo");
                        System.out.println("Le envio el hash del archivo final");
                        out.writeObject(trackerthread.list.get(auxiliar).getFullhash());
                        String asd = (String) in.readObject();
                        System.out.println(asd);
                        InetAddress ipaux = (InetAddress) in.readObject();
                        int portaux = (int) in.readObject();
                        listado listin = new listado(trackerthread.list.get(auxiliar).getArchivo(),trackerthread.list.get(auxiliar).getlistadohash(), portaux,ipaux,trackerthread.list.get(auxiliar).getSize(),trackerthread.list.get(auxiliar).getFullhash());//nombre de archivo y fuillsize estan mal
                        trackerthread.list.add(listin);
                        System.out.println("Ya se ha enviado");

                        break;

                    case "salir":
                        System.out.println("Alguien quiere salir del enjambre");
                        InetAddress aux ;
                        puertoapasar = (int)in.readObject();
                        aux = (InetAddress)in.readObject();
                        nombrearchivo = (String) in.readObject();
                        System.out.println(trackerthread.list.size());
                        Iterator<listado> iterador = trackerthread.list.iterator();
                        //noinspection LoopStatementThatDoesntLoop
                        while(iterador.hasNext()){

                            if(iterador.next().getIp().equals(aux) &&  iterador.next().getPuerto() == puertoapasar)
                                iterador.next();
                                iterador.remove();
                                System.out.println("Se ha eliminado");
                                System.out.println(trackerthread.list.size());
                            break;
                        }
                        Thread.interrupted();

                        break;

                    default:

                        break;
                }
            }
	    }
        catch(IOException e)
        {
            System.out.println("Error (ServerThread): " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
      
    }

    /**
     *  Primero borra el listado de archivos actual, y copia el listado de archivos del manejador de threads
     */
    private void actualizarlistadoarchivos(){

        listaarchivos.clear();
        String temp;
        for (int i=0;i<trackerthread.list.size();i++){
            temp = trackerthread.list.get(i).getArchivo();
            if(listaarchivos.isEmpty()){
                listaarchivos.add(temp);
            }else {
                if(!listaarchivos.contains(temp)){
                    listaarchivos.add(temp);
                }
            }
		}
	}


    /**
     *Dado el nombre del archivo, creado un listado con los peer que tiene dicho archivo
     *
     * @param nombrearch
     */
    private void listadopeer(String nombrearch){

        int tempa= 0;
        String aux;
        for(int i = 0; i<trackerthread.list.size();i++){
            String bc = trackerthread.list.get(i).getArchivo();
            if(nombrearch.equals(bc)){
                tempa =trackerthread.list.get(i).getPuerto();
                puertobueno.add(tempa);
                System.out.println(tempa);
                ippeers.add(trackerthread.list.get(i).getIp());
                auxiliar = i;
                System.out.println(trackerthread.list.get(i).getIp().toString());
            }
        }
	}


    /**
     * Muestra por pantalla toda la información del listado de peers
     *
     * @param a
     */
	public void mostrartodo(List<listado> a){
		System.out.println(a.size());
		for (int i=0;i< a.size();i++){
			System.out.println("El numero: "+i+"");
			System.out.println(a.get(i).getArchivo());
		}
	}


    /**
     *  Muestra por pantalla el listado de peer
     *
     *
     */
    private void imprimirlistadopeers(){

        System.out.println(puertobueno.size());
		System.out.println(ippeers.size());
		for (int i = 0; i<puertobueno.size();i++){
			System.out.println("El cliente "+i+" es: "+ippeers.get(i).toString()+" - Puerto: "+puertobueno.get(i)+"");
		}
	}
}