package p2p;
import java.io.*;
import java.net.Socket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;



/**
 * Clase destinada a recibir peticiones por parte de los peer,
 * ya sea para actualizar mi bitmap con su informacion,
 * para pedirme mi bitmap y actualizar el su informazion,
 * o para pedirme una parte del archivo, y yo mandarsela.
 */
@SuppressWarnings("JavaDoc")
class traspaso implements Runnable{






    private String str ="";
    private final RandomAccessFile archivo1;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private DataOutputStream dos;
    private DataInputStream dis;
    private InetAddress ipaux;
    private int portaux;
    private final List <matrix> mimatrix= peer.getmimatrix();

    traspaso(Socket s, RandomAccessFile archivo1){

        this.archivo1 = archivo1;
        try {
            InputStream is = s.getInputStream();
        ois = new ObjectInputStream(is);
            OutputStream os = s.getOutputStream();
        oos = new ObjectOutputStream(os);
        dos = new DataOutputStream(os);
        dis = new DataInputStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    @Override
    public void run() {

        try {
             while(true){

                try{

                    ipaux = (InetAddress) ois.readObject();
                    portaux = (int) ois.readObject();
                    String name = (String) ois.readObject();
                    int vari = 0;
                    if(mimatrix.isEmpty()){
                        matrix matrixauxaa = new matrix(name,ipaux,portaux,null);
                        mimatrix.add(matrixauxaa);
                        vari = 1;
                    }
                    for (matrix aux : mimatrix) {
                        if (aux.getIp().equals(ipaux) & aux.getport() == portaux) {
                            vari = 2;
                            break;
                        }
                    }
                    if(vari == 0){
                        AtomicIntegerArray a = new AtomicIntegerArray(peer.dimesize());
                        matrix matrixauxaa = new matrix(name, ipaux, portaux, a);
                        mimatrix.add(matrixauxaa);
                    }
                    System.out.println("matriz size : "+mimatrix.size());
                    str= (String) ois.readObject();
                    System.out.println(str);
                    } catch (ClassNotFoundException cnf) {
                    cnf.printStackTrace();
                } catch (IOException io) {
                    throw new RuntimeException(io);

                }

                switch(str)
                {       case "paso1":
                            int nparte =  dis.readInt();
                            System.out.println("Me pide la parte "+nparte+"");
                            enviarparte2(nparte);
                            peer.actualizarmimatrix(mimatrix);
                            System.out.println("Finalizo");

                            return;

                        case "update":

                            System.out.println("Un cliente quiere actualizar mimatrix");
                            System.out.println("Ahora recibo el suyo");
                            int size = mimatrix.size();
                            for (int i = 0; i < size; i++){
                                matrix aux = mimatrix.get(i);
                                if(aux.getIp().equals(ipaux) & aux.getport()== portaux){
                                    matrix axx = givemebitmap(  ois, mimatrix, i);
                                    mimatrix.set(i,axx);
                                    break;
                                }
                            }
                            System.out.println("Tamaño de la matriz: "+mimatrix.size());
                            System.out.println("Esta todo cerrado y voy a cerrar y actualizar");
                            peer.actualizarmimatrix(mimatrix);

                            return;

                        case "info":
                            System.out.println("Un cliente me pide mi bitmap");
                            oos.writeObject("si");
                            oos.writeObject(peer.mibitmap);
                            System.out.println("Ahora recibo el suyo");
                            size = mimatrix.size();
                            for (int i = 0; i < size; i++){
                                matrix aux = mimatrix.get(i);
                                if(aux.getIp().equals(ipaux) & aux.getport()== portaux){
                                    matrix axx = givemebitmap(  ois, mimatrix, i);
                                    mimatrix.set(i,axx);
                                    break;
                                }
                            }
                            System.out.println("Esta todo cerrado y voy a cerrar y actualizar");
                            peer.actualizarmimatrix(mimatrix);
                            return;

                }

                }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }


    }


    /**
     * El objetivo es dividir el file que tenemos en partes más pequeñas y enviar la parte que nos pide solo.
     *
     * @param parte
     */
    private void enviarparte2(int parte) {

        try{

            FileInputStream fis = new FileInputStream( archivo1.getFD());
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] Buffer = new byte[1024*512];
            archivo1.seek(1024*512*parte);
            int nread;
            //noinspection LoopStatementThatDoesntLoop
            while ((nread = bis.read(Buffer)) > 0) {
                    System.out.println("El mensaje contiene "+ nread +"");
                    dos.writeInt(nread);
                    dos.write(Buffer);
                    System.out.println("Todo enviado");
            break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Recibe el bitmap por el socket selecionado, lo imprime  y lo devuelve en una matrix nueva.
     *
     * @param oisaux
     * @param mimatrixaa
     * @param iaux
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private matrix givemebitmap(ObjectInputStream oisaux, List<matrix> mimatrixaa, int iaux) throws IOException, ClassNotFoundException {

        System.out.println("Me va a llegar su bitmap");
        AtomicIntegerArray intax = (AtomicIntegerArray)  oisaux.readObject();
        imprimirbitmap2(intax);
        return new matrix(mimatrixaa.get(iaux).getname(),mimatrixaa.get(iaux).getIp(),mimatrixaa.get(iaux).getport(),intax);
    }


    /**
     * Imprime por consola el bitmap dado por el AtomicIntegerArray
     *
     * @param ausx
     */
    private void imprimirbitmap2(AtomicIntegerArray  ausx){

        System.out.print("[");
        for (int i = 0; i<ausx.length();i++) {
            System.out.print(Integer.toBinaryString(ausx.get(i)));
        }
        System.out.println( "]");
    }
}
