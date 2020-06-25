package p2p;

/**
 * Inicia el peer que quiere seedear un archivo
 */
class launcher  {

    /**
     * Sirve para lanzar la clase peer como seed y con la dirección de salida de los archivos.
     *
     * @param args
     */
    @SuppressWarnings("ParameterCanBeLocal")
    public static void main(String[] args) {


        args= new String[2];
        //funcion
        args[0]="registrar";
        //direccion de salida
        args[1]="/home/alumnos/ssd27/entregaTrabajo/resultado/";
        peer.main(args);



    }
}
