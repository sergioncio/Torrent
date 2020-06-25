package p2p;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import java.util.concurrent.atomic.AtomicIntegerArray;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

/**
 * Clase principal que puede funcionar como peer o como seed y se encarga de la mayoria del programa, llamando a otras clase cuando hace falta.
 */
@SuppressWarnings("JavaDoc")
class peer implements Runnable{
     private File selectedFile = null;
	 private ArrayList<byte[]> listita = new ArrayList<>(); //arraylist con los hashes de los archivos
	 private ArrayList<byte[]> listitafromtracker = new ArrayList<>(); //arraylist con los hashes de los archivos
	 private Socket clientSocket;
	 private final ArrayList<String> arraystring = new ArrayList<>();
	 private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	 private ObjectOutputStream out;
	 private ObjectInputStream in;
	 private InputStream is;
	 private BufferedInputStream bis;
	 private OutputStream os;
     private DataInputStream dis;
	 private final ArrayList<String> archivos2 = new ArrayList<>();
	 private String str="aa";
	 private String nombrearch ="";
	 private String totalhash ="";
	 private long fullsize;
     private final List<InetAddress> avaibleIP = new ArrayList<>();
	 private final List<Integer> avaibleports = new ArrayList<>();
	 private InetAddress local;
	 private RandomAccessFile temporal;
	 private String nameaux;
	 private List<matrix> qwe = null;
	 private String hashobjetivo = null;


	{
		try {
			local = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	 private static List<matrix> mimatrix = new ArrayList<>();
	 private final Random r = new Random();
	 private int rndpuerto = 0;

    /**
     * Es un array en el que se encuentra la información de las  partes que tengo del archivo con el que trabajo(seedeando/descargando), un 1 si tengo la parte, un 0 si no la tengo.
     */
     static AtomicIntegerArray mibitmap = null;

    private int nread = 0;
    private String txt;
    private String direccion;
   	 private peer(String txt, String direccion) {
		this.txt = txt;
		this.direccion = direccion;
	}


	public static void main(String[] args) {

		String txt = args[0];
		String direccion = args[1];
		(new Thread(new peer(txt,direccion))).start();
	}


	public void run() {
			System.out.println("Se ha iniciado el peer");
			rndpuerto = r.nextInt(40000 - 7000) + 7000;
			System.out.println("El puerto para hacer de server es: "+rndpuerto);
			Scanner cc;
			cc = new Scanner(System.in);

		//noinspection InfiniteLoopStatement
		while(true) switch (txt) {
            case "registrar":
                conectartracker();
                System.out.println("Seed Inicial");
                registrararchivo();
                desconectartracker();
                esperarpeticion();
                System.out.println("Escribe la opcion: \n\"registrar\" (Registrar archivo y hacer de servidor)  \n\"salir\" (para dejar el enjambre) \n\"obtener\" (Obtener listado de archivos y descargar archivo)   ");
                txt = cc.nextLine();
                cc.reset();
                break;

            case "obtener":
                conectartracker();
                System.out.println("Obteniendo listado de archivos");
                obtener();
                System.out.println("Indica el numero del archivo que quieres");
                pedir();
                System.out.println("Ya no necesito al tracker");
                System.out.println("Preparo el servidor de respuesta");
                prepserv();
                System.out.println("envio las peticiones");
                selectparte();
                System.out.println("Escribe la opcion: \n\"registrar\" (Registrar archivo y hacer de servidor)  \n\"salir\" (para dejar el enjambre) \n\"obtener\" (Obtener listado de archivos y descargar archivo)   ");
                txt = cc.nextLine();
                cc.reset();
                break;

            case "salir":
                System.out.println(txt);

                    salir();
                    System.out.println("Escribe la opcion: \n\"registrar\" (Registrar archivo y hacer de servidor)  \n\"salir\" (para dejar el enjambre) \n\"obtener\" (Obtener listado de archivos y descargar archivo)   ");
                    txt = cc.nextLine();
                    cc.reset();

                break;

            default:
                System.out.println("Invalid choice");
                System.out.println("Escribe la opcion: \n\"registrar\" (Registrar archivo y hacer de servidor)  \n\"salir\" (para dejar el enjambre) \n\"obtener\" (Obtener listado de archivos y descargar archivo)   ");
                txt = cc.nextLine();
                cc.reset();
        }
	}






    /**
     * Metodo para conectarse al tracker
     */
	private void conectartracker()  {

		try {
			//Creo un socket y me conecto a la ip y direccion siguiente:
            // puerto del tracker
            int puerto = 5006;
            clientSocket = new Socket("localhost", puerto);
			System.out.println("Conexion con el tracker establecida");
			os = clientSocket.getOutputStream();
			out = new ObjectOutputStream(os);
			is = clientSocket.getInputStream() ;
			bis = new BufferedInputStream (is);
			in = new ObjectInputStream(bis);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


    /**
     * Se encarga de los pasos necesarios para
     * seedear un archivo
     */
	private void registrararchivo(){
        str ="registrar";
        //elijo un archivo
        readfile();
        System.out.println("Este es el Hash del archivo: ");
        //obtenemos el hash del archivo completo
        gettotalhash(selectedFile);
        //Tamaño del archivo
        fullsize = selectedFile.length();
        //crea arraylist de hashes de el archivo elegido
        System.out.println("Este es el listado con los Hash de las partes: ");
        listita= gethash(selectedFile);
        //imprimo el hash
        leer(listita);
        //envio hash
        enviarhash();
        //relleno mi bitmap de unos
            mibitmap = new AtomicIntegerArray(listita.size());
        rellenarunos();

	}


    /**
     *  Metodo para desconectarse del tracker
     */
    private void desconectartracker(){

        try {

            clientSocket.close();
            os.close();
            out.close();
            is.close();
            bis.close();
            in.close();
            System.out.println("Cerrada la conexion con el tracker");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    /**
     *  Activa el thread que recoge peticiones de otros peer
     */
    private void esperarpeticion(){
        try {
            RandomAccessFile completo;
            completo = new RandomAccessFile(selectedFile,"rw");
            PeerAsServer peerasserver = new PeerAsServer(rndpuerto,completo);
            new Thread(peerasserver).start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    /**
     *  Metodo por el que se pide el listado de archivos al tracker
     */
	@SuppressWarnings("unchecked")
	private void obtener(){

    	try{
			str ="obtener";
			System.out.println("listado de archivos");
			out.writeObject(str);
			int tamn =(int) in.readObject();
			archivos2.clear();
			for (int i=0;i<tamn;i++){
				archivos2.add((String) in.readObject());
			}
			for(int i=0;i<archivos2.size();i++){
				System.out.println("Archivo "+i+":");
				System.out.println(archivos2.get(i));
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}




    /**
     *  Metodo en el que se elige un archivo y se pide su informacion al tracker,
     *  luego se registra el peer en el tracker
     *
      */
	@SuppressWarnings("unchecked")//No se coprueba el traspaso de objeto a arraylist<socket>
	private void pedir(){

	 	try {
            str ="pedir";
            out.writeObject(str);

                //Se ha cambiado el scaner, donde elegias el archivo que querias por un random para facilitar la demostracion.
            int nobj = r.nextInt(archivos2.size());

            out.writeObject(archivos2.get(nobj));
            System.out.println("Quien tiene el archivo: "+ nobj);
            List<InetAddress> listippeers = (ArrayList<InetAddress>) in.readObject();
            List<Integer> listpuertosbuenos = (ArrayList<Integer>) in.readObject();
            nameaux = archivos2.get(nobj);
            listitafromtracker = (ArrayList<byte[]>) in.readObject();
            fullsize = (long) in.readObject();
            hashobjetivo = (String) in.readObject();
            AtomicIntegerArray aux2  = new AtomicIntegerArray(listitafromtracker.size());
            for (int i = 0; i < aux2.length(); i++){
                aux2.set(i,0);
            }
            for (int i = 0; i< listippeers.size(); i++){
                matrix aux = new matrix(nameaux, listippeers.get(i), listpuertosbuenos.get(i),aux2);
                mimatrix.add(aux);
            }
            System.out.println("Ya he recibido la informacion del archivo");
            System.out.println("Ahora me registro yo en el tracker");
            out.writeObject(local);
            out.writeObject(rndpuerto);
            out.writeObject(nameaux);
            desconectartracker();
    	} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}






    /**
     *  Activa el thread que recoge peticiones de otros peer
     *  Configura el RandomAccesFile con la dirreccion, y tamño deseado.
     *  Configura el bitmap propio y lo rellena de ceros
     *  Refresca la matriz con la informacion de los peer.
     */
	private void prepserv(){

		mibitmap = new AtomicIntegerArray(mimatrix.get(0).getbitmap().length());
		rellenarceros(mibitmap);
		try {
		temporal = new RandomAccessFile(direccion+rndpuerto+"-"+nameaux,"rw");
		temporal.setLength(fullsize);
		PeerAsServer peerasserver = new PeerAsServer(rndpuerto, temporal);
		new Thread(peerasserver).start();
        System.out.println("Voy a pedir el listado de bitmaps");
		List<matrix> ss = getmimatrix();
		refreshmatrix(ss);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}


    /**
     *  Comprueba el tampaño de mibitmap, si es mayor a 5 partes:
     *  empieza descargando 5 partes aleatorias , luego utiliza un metodo para buscar las partes menos frecuentes y descargarlas
     *  Si es menor de 5 partes las descarga de forma aleatoria
     *
     *  Al finalizar comprueba que el archivo se ha descargado correctamente,y actua en consecuencia.
     */
    private void selectparte() {

        try {
            if (mibitmap.length()>5 ){
                if(partestengo(mibitmap)<5) {
                    int aleatorio[] = new int[mibitmap.length()];

                    for (int i = 0; i < mibitmap.length(); i++) {
                        aleatorio[i] = i;

                    }
                    aleatorio = RandomizeArray(aleatorio);
                    for (int i = 0; i < 5; i++) {
                        if (mibitmap.get(aleatorio[i]) == 0) {
                            System.out.println("Se a elegido la parte : " + aleatorio[i]);
                            //qwe = getmimatrix();
                            selectserver(aleatorio[i]);
                            //	sendupdate();

                        }
                    }
                }
                int partestengo = partestengo(mibitmap);
                int[] partes_descargar = new int[2];
                while(partestengo != mibitmap.length()) {
                    System.out.println("Tengo "+partestengo+" de "+mibitmap.length()+" partes");
                    partes_descargar= partemenosfrecuente();
                    selectserver(partes_descargar[0]);
                    //sendupdate();
                    partestengo = partestengo(mibitmap);
                    if(partestengo == mibitmap.length()){break;}
                    selectserver(partes_descargar[1]);
                    //sendupdate();
                    partestengo = partestengo(mibitmap);
                }
                //Compruebo el hash completo
                temporal.seek(0);
                byte[] finaltest = new byte[(int) temporal.length()];

                temporal.readFully(finaltest);
                String emergenciafile = gettotalhash2(finaltest);
                if(hashobjetivo.equals(emergenciafile)){
                    System.out.println("FIN");
                }else{
                    System.out.println("Error al final");
                    ArrayList<byte[]> listaemergencia = gethash2(finaltest);
                    mibitmap = emergencia(listitafromtracker,listaemergencia);
                    selectparte();
                }




            }else{
                System.out.println("Menos de 5 partes");
                int aleatorio[] = new int [mibitmap.length()];

                for (int i = 0; i < mibitmap.length(); i++){
                    aleatorio[i] = i;

                }
                aleatorio = RandomizeArray(aleatorio);
                for (int i = 0; i < mibitmap.length(); i++){
                    if(mibitmap.get(aleatorio[i]) == 0) {

                        selectserver(aleatorio[i]);


                    }
                }

                temporal.seek(0);
                byte[] finaltest = new byte[(int) temporal.length()];

                temporal.readFully(finaltest);
                String emergenciafile = gettotalhash2(finaltest);
                System.out.println("Son iguales??");
                System.out.println(emergenciafile);
                System.out.println(hashobjetivo);
                if(hashobjetivo.equals(emergenciafile)){
                    System.out.println("FIN");
                }else{
                    System.out.println("Error al final");
                    ArrayList<byte[]> listaemergencia = gethash2(finaltest);
                    mibitmap = emergencia(listitafromtracker,listaemergencia);
                    selectparte();
                }

                System.out.println("FIN?");




            }
        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    /**
     *
     */
    private void salir()  {

        conectartracker();

        try {
            str ="salir";
            out.writeObject(str);
            out.writeObject(rndpuerto);
            out.writeObject(local);
            out.writeObject(nombrearch);
        } catch (IOException e) {
            e.printStackTrace();
        }

        desconectartracker();


    }



    /**
     * Devuelve mimatrix
     * @return mimatrix
     */
    static    List<matrix> getmimatrix(){return mimatrix;}


    /**
     * Toma una lista de mimatrix auxiliar y actualiza la lista de mimatrix buena
     *
     * @param ss
     */
    @SuppressWarnings("JavaDoc")
    static   void actualizarmimatrix(List<matrix> ss){ mimatrix = ss;}


    /**
     *  Devuelve la longitud del bitmap
     *
     * @return
     */
    static  int dimesize(){return mibitmap.length();}


    /**
     *  Con este metodo se elige el archivo a seedear.
     *
     *  Se utiliza en registrararchivo()
     */
    private void readfile(){
        //Elegimos el archivo para seedear


        JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        int returnValue = fc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            this.selectedFile = fc.getSelectedFile();
            System.out.println("Ruta del archivo: "+selectedFile.getAbsolutePath());
            nombrearch= selectedFile.getName();
            System.out.println("Nombre del archivo: "+nombrearch);
        }
    }


    /**
     * Crea el hash del archivo completo a seedear dado un File
     *
     * Se utiliza en registrararchivo()
     * @param sf
     */
    private void gettotalhash(File sf){

        try {
            MessageDigest md1 = MessageDigest.getInstance("MD5");
            FileInputStream fis1 = new FileInputStream(sf);//selectedfile
            BufferedInputStream bis1 = new BufferedInputStream(fis1);
            byte[] buffer = new byte[1024];
            while (true) {
                int c = bis1.read(buffer);
                if (c > 0)
                    md1.update(buffer, 0, c);
                else if (c < 0)
                    break;
            }
            bis1.close();
            byte[] result = md1.digest();
            totalhash = bytesToHex(result);
            System.out.println(totalhash);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *  Imprime por pantalla el listado de hashes en hexadecimal
     *
     *  Se utiliza en registrararchivo()
     * @param aux
     */
    private void leer(ArrayList<byte[]> aux){

        for (byte[] anAux : aux) {
            arraystring.add(bytesToHex(anAux));
        }
        System.out.println(arraystring);
    }


    /**
     *  Metodo para enviar mi informacion y la del archivo
     *  al tracker para que me registre como seeder.
     *
     *  Se utiliza en registrararchivo()
     */
    private void enviarhash(){

        try {
            System.out.println("Envio la informacion al tracker");
            out.writeObject(str);
            out.writeObject(rndpuerto);
            out.writeObject(nombrearch);
            out.writeObject(fullsize);
            out.writeObject(totalhash);
            out.writeObject(listita);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *  Metodo que rellena el bitmap propio con unos.
     *  Util para los seeder.
     *
     * Se utiliza en registrararchivo()
     */
    private void rellenarunos(){
        //


        for(int i = 0; i < mibitmap.length(); i++) {

            mibitmap.set(i,1);
        }
    }


    /**
     * Metodo para rellenar con ceros un AtomicIntegerArray
     * @param ax
     */
    private void rellenarceros(AtomicIntegerArray ax){
        for(int i = 0; i < ax.length(); i++) {
            ax.set(i,0);//quizas cambiar a mi bitmap
        }
    }


    /**
     *  Metodo que toma un listado de matrix y lo actualiza pidiendole informacion a los peers
     *
     * @param mimatrix
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void refreshmatrix(List<matrix> mimatrix) throws IOException, ClassNotFoundException {
        int size = mimatrix.size();
        System.out.println("Size de mimatrix 1 refresh: "+mimatrix.size());
        for (int i = 0; i < size; i++) {
            System.out.println("Posicion : "+i);
            Socket socketaux = new Socket(mimatrix.get(i).getIp(),mimatrix.get(i).getport());
            ObjectOutputStream oosaux = new ObjectOutputStream(socketaux.getOutputStream());
            ObjectInputStream oisaux = new ObjectInputStream(socketaux.getInputStream());
            System.out.println("Nos conectamos al puerto :"+mimatrix.get(i).getport() );
            oosaux.writeObject(local);
            oosaux.writeObject(rndpuerto);
            oosaux.writeObject(nameaux);
            String info = "info";
            String vuelta;
            oosaux.writeObject(info);
            vuelta = (String) oisaux.readObject();
            if(vuelta.equals("si")) {
                System.out.println("Responde que si");
                matrix aux = givemebitmap( oosaux, oisaux, mimatrix, i);
                mimatrix.set(i,aux);
                socketaux.close();
            }else{
                socketaux.close();
            }

        }
        System.out.println("Size de mimatrix 2 refresh: "+mimatrix.size());
        actualizarmimatrix(mimatrix);
    }


    /**
     * Dado un AtomicIntegerArray te cuenta cuantos partes tiene
     * (cuenta el numero de 1) y lo devuelve en forma de integer
     *
     * @param aux
     * @return
     */
    private Integer partestengo(AtomicIntegerArray aux){
        int salida = 0;
        for(int i = 0;i<aux.length();i++){
            if (aux.get(i)== 1)
                salida++;
        }
        return salida;
    }


    /**
     * Dado un array de integer lo devuelve desordenado
     *
     * @param array
     * @return
     */
    private int[] RandomizeArray(int[] array){
        Random rgen = new Random();

        for (int i=0; i<array.length; i++) {
            int randomPosition = rgen.nextInt(array.length);
            int temp = array[i];
            array[i] = array[randomPosition];
            array[randomPosition] = temp;
        }

        return array;
    }


    /**
     *  Crea una matriz con los peer que tienen la parte deseada
     *  despues selecciona un peer de forma aleatoria y le pedimos la parte deseada
     * @param parte
     */
    private void selectserver(int parte) {
        qwe= getmimatrix();
        for (matrix aQwe : qwe) {
            AtomicIntegerArray auxilis = aQwe.getbitmap();
            int a = auxilis.get(parte);
            if (a == 1) {
                avaibleIP.add(aQwe.getIp());
                avaibleports.add(aQwe.getport());
            }
        }
            if(avaibleports.isEmpty()){
            selectparte();
        }
        int next = r.nextInt(avaibleports.size());
        pedir_parte(avaibleIP.get(next),avaibleports.get(next),parte);

    }


    /**
     * Comprueba la matriz con la informacion de los bitmaps de los demás peers
     * y obtiene las 2 partes menos frecuentes  que aun no tenemos
     * y las devuelve
     * @return
     */
    private int[] partemenosfrecuente(){
        int[] resultados = new int [mibitmap.length()];
        rellenarceros2(resultados);
        for (matrix aMimatrix : mimatrix) {
            AtomicIntegerArray auxili;
            if (aMimatrix.getbitmap().length() == mibitmap.length()) {
                auxili = aMimatrix.getbitmap();
                for (int ia = 0; ia < auxili.length(); ia++) {
                    if (auxili.get(ia) == 1) {
                        resultados[ia]++;
                    }

                }
            }

        }
        int ali[] = new int[2];
        int index = 0;
        int index2=0;
        int min = Integer.MAX_VALUE;
        int min2 =Integer.MAX_VALUE;
        for (int i=1; i<resultados.length; i++){

            if (resultados[i] < min  && mibitmap.get(i)==0){
                min2 = min;
                index2 = index;
                min = resultados[i];
                index = i;

            }else if(resultados[i] <= min2 && mibitmap.get(i)==0){
                min2 = resultados[i];
                index2 = i;

            }
            ali[0] = index;
            ali[1] = index2;

        }
        return ali ;
    }


    /**
     * Dado un array de byte
     * Devuelve el hash del archivo completo a seedear
     * @param message
     * @return
     */
    private String gettotalhash2(byte[] message){
        String totalhash= null;
        try {
            MessageDigest md1 = MessageDigest.getInstance("MD5");
            byte[] result  = md1.digest(message);
            totalhash = bytesToHex(result);
            System.out.println(totalhash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return totalhash;
    }




    /**
     * Dado un array de hash devuelve un string con los byte convertido a hexadecimal
     *
     * @param bytes
     * @return
     */
    private  String bytesToHex(byte[] bytes) {
        //


        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    /**
     * Dada una conexion con un peer recibe e imprime su bitmap y envia el bitmap propio
     * Devuelve su bitmap
     *
     * @param oosaux
     * @param oisaux
     * @param mimatrixaa
     * @param iaux
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private matrix givemebitmap(ObjectOutputStream oosaux, ObjectInputStream oisaux, List<matrix> mimatrixaa, int iaux) throws IOException, ClassNotFoundException {
        AtomicIntegerArray intax = (AtomicIntegerArray)   oisaux.readObject();
        System.out.println("Refresh matrix , su bitmap es:");
        imprimirbitmap(intax);
        oosaux.writeObject(mibitmap);

        return new matrix(mimatrixaa.get(iaux).getname(),mimatrixaa.get(iaux).getIp(),mimatrixaa.get(iaux).getport(),intax);
    }


    /**
     * Dado una ip, un puerto y una parte
     * Se conecta con la direccion y le pide la parte elegida
     *
     * @param ip
     * @param puerto
     * @param parte
     */
    private void pedir_parte(InetAddress ip, int puerto, int parte){

		try {
            clientSocket = new Socket(ip, puerto);
            os = clientSocket.getOutputStream();
            out = new ObjectOutputStream(os);
            is = clientSocket.getInputStream();
            bis = new BufferedInputStream(is);
            in = new ObjectInputStream(bis);
            DataOutputStream dos = new DataOutputStream(os);
            dis = new DataInputStream(is);
            System.out.println("Conectados con el peer servidor: "+ puerto);
            out.writeObject(local);
            out.writeObject(rndpuerto);
            out.writeObject(nameaux);
            String texto = "paso1";
            out.writeObject(texto);
            dos.writeInt(parte);
            System.out.println("Pido parte: "+parte+"");

            descargarparte(parte);

            clientSocket.close();
            os.close();
            out.close();
            is.close();
            bis.close();
            in.close();
            dos.close();
            dos.close();
            dis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
	}


    /**
     * Dado un array de int lo devuelve con valores de 0
     * @param ax
     */
    private void rellenarceros2(int [] ax){
        for(int i = 0; i < ax.length; i++) {
            ax[i]=0;
        }
    }




    /**
     * Imprime por consola el bitmap dado por el AtomicIntegerArray
     *
     * @param aux
     */
    private void imprimirbitmap(AtomicIntegerArray aux){

        System.out.print("[");
        for ( int i =0;i<aux.length();i++) {
            System.out.print(Integer.toBinaryString(aux.get(i)));
        }
        System.out.println( "]");
    }


    /**
     * Funcion encargada de obtener la parte del servidor y de comprobar que es la parte adecuada,
     * en caso de haber algún problema no se actualizaria el  mapa con los partes que se tiene,
     * por lo que se tendría que volver a descargar.
     *
     * @param parte
     * @throws IOException
     */
    private void descargarparte(int parte)throws IOException{

        byte[] message = null;
        int length;
        length = dis.readInt();
        if(length >0) {
            message = new byte[length];
            dis.readFully(message, 0, message.length);
        }


        totalhash= gettotalhash2(message);

        String prueba = new String(bytesToHex(listitafromtracker.get(parte)));

        if(prueba.equals(totalhash)){
            temporal.seek(1024*512*parte);
            temporal.write(Objects.requireNonNull(message));
            mibitmap.set(parte,1);
            System.out.println("Descarga del paquete completada");
            imprimirbitmap(mibitmap);
            sendupdate();
        }else {
            System.out.println("Error");
            System.out.println("No se ha podido descargar el paquete");


        }

    }


    /**
     *  Funcion encargada de enviar el mapa de bits con la nueva informafion a el enjambre de peers.
     */
    private void sendupdate()throws IOException {

        System.out.println("Sendupdate");
        qwe= getmimatrix();
        int size = qwe.size();
        for (int i = 0; i < size; i++) {
            System.out.println("Posicion : " + i);
            Socket socketaux = new Socket(qwe.get(i).getIp(), qwe.get(i).getport());
            ObjectOutputStream oosaux = new ObjectOutputStream(socketaux.getOutputStream());
            oosaux.writeObject(local);
            oosaux.writeObject(rndpuerto);
            oosaux.writeObject(nameaux);
            String update = "update";
            oosaux.writeObject(update);
            System.out.println("le envio mi bitmap");
            oosaux.writeObject(mibitmap);
            socketaux.close();
        }
    }


    /**
     * Copia de forma segura un bitmap elegido y lo devuelve
     *
     * @param bueno
     * @param malo
     * @return
     */
    private AtomicIntegerArray emergencia (ArrayList<byte[]> bueno, ArrayList<byte[]> malo){

        AtomicIntegerArray mibitmapemergencia =  new AtomicIntegerArray(bueno.size());
        rellenarunos2(mibitmapemergencia);
        for (int i = 0; i < bueno.size();i++){
            if(!bueno.get(i).equals(malo.get(i))){
                mibitmapemergencia.set(i,0);
            }
        }
        return mibitmapemergencia;
    }


    /**
     *  Dado un AtomicIntegerArray lo rellena con 1 de forma segura
     *
     * @param ax
     */
    private void rellenarunos2(AtomicIntegerArray ax){

        for(int i = 0; i < ax.length(); i++) {
            ax.set(i,1);
        }
    }


    /**
     * Dado un file devuelve un listado con los hashs de los bloques
     *
     * @param aux
     * @return
     */
    private ArrayList<byte[]> gethash(File aux){

        ArrayList<byte[]> listauxiliar = new ArrayList<>();

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(aux);
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] dataBytes = new byte[1024*512];
            while ((nread = bis.read(dataBytes)) > 0) {
                md.update(dataBytes,0,nread);
                listauxiliar.add(md.digest());
                md.reset();
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return listauxiliar;
    }


    /**
     * Dado un array de byte devuelve un listado con los hashs de los bloques
     *
     * @param aux
     * @return
     */
    private ArrayList<byte[]> gethash2(byte[] aux){

        ArrayList<byte[]> listauxiliar = new ArrayList<>();

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            ByteArrayInputStream bais = new ByteArrayInputStream(aux);
            byte[] dataBytes = new byte[1024*512];
            while ((nread = bais.read(dataBytes)) > 0) {
                md.update(dataBytes,0,nread);
                listauxiliar.add(md.digest());
                md.reset();
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return listauxiliar;
    }

}
