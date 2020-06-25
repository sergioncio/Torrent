package p2p;

/**
 * Inicia el peer que quiere descargar un archivo
 */

class launcherobtener {

    /**
     * Sirve para lanzar la clase peer como peer de descarga y con la dirección de salida de los archivos.
     *
     * @param args
     */
    @SuppressWarnings("ParameterCanBeLocal")
    public static void main(String[] args) {

        args = new String[2];
        //funcion
        args[0]="obtener";
        //direccion
        args[1]="/home/alumnos/ssd27/entregaTrabajo/resultado/";
        p2p.peer.main(args);
        }

    }
