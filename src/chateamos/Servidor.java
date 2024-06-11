package chateamos;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Servidor extends JFrame {


    private List<PrintWriter> usuariosOnline = new ArrayList<>();
    private List<String> listaNombres = new ArrayList<>();
    private JTextArea mensajes;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Servidor servidor = new Servidor();
            servidor.iniciarServidor();
        });
    }

    private void iniciarServidor() {
        mensajes = new JTextArea();
        mensajes.setEditable(false);
        mensajes.append("Servidor\n");

        add(new JScrollPane(mensajes), BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setVisible(true);

        new Thread(this::conexionServidor).start();
    }

    private void conexionServidor() {
        try (ServerSocket serverSocket = new ServerSocket(5555)) {
            System.out.println("Servidor  iniciado, esperando que se conecten...");

            while (true) {
                Socket socketCliente = serverSocket.accept();
                System.out.println("Nuevo cliente en el servidor.");

                PrintWriter escribir = new PrintWriter(socketCliente.getOutputStream(), true);

                escribir.println(" Ingrese su nombre para continuar:");
                String nombreCliente = new BufferedReader(new InputStreamReader(socketCliente.getInputStream())).readLine();

                if (nombreduplicado(nombreCliente)) {
                    escribir.println("#NOMBRE_EN_USO#");
                    socketCliente.close();
                    continue;
                }

                listaNombres.add(nombreCliente);
                enviarMensaje(fechaActual() + " - " + nombreCliente + " se ha unido al chat exitosamente.");

                listaClientesConectados();

                new Thread(() -> manejoCliente(socketCliente, escribir, nombreCliente)).start();

                usuariosOnline.add(escribir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private boolean nombreduplicado(String nombre) {
        return listaNombres.contains(nombre);
    }

    //procedemos a comprobar  que clientes e la lista estan conectados
    private void listaClientesConectados() {
        for (PrintWriter cliente : usuariosOnline) {
            cliente.println("#LISTA_CLIENTES#");
            for (String nombre : listaNombres) {
                cliente.println(nombre);
            }
        }
    }



    private void manejoCliente(Socket socket, PrintWriter escritor, String nombreCliente) {
        try {
            BufferedReader leer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String mensaje;
            while ((mensaje = leer.readLine()) != null) {
                if (mensaje.equals("#CLIENTE_ENVIO_MENSAJE#")) {
                    mensaje = leer.readLine();
                    enviarMensaje(fechaActual() + " - " + nombreCliente + ": " + mensaje);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (escritor != null) {
                usuariosOnline.remove(escritor);
                listaNombres.remove(nombreCliente);
                enviarMensaje(fechaActual() + " - " + nombreCliente + " se ha desconectado del servidor.");
                listaClientesConectados();
            }
        }
    }

    private String fechaActual() {
        SimpleDateFormat formatoFechaHora = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return formatoFechaHora.format(new Date());
    }

    // con este metodo enviamos  un mensaje a todos los clientes conectados y se muestra en la interfaz del servidor.
    private void enviarMensaje(String mensaje) {
        System.out.println(mensaje);
        SwingUtilities.invokeLater(() -> {
            mensajes.append(mensaje + "\n");
            mensajes.setCaretPosition(mensajes.getDocument().getLength());
        });

        for (PrintWriter cliente : usuariosOnline) {
            try {
                cliente.println(mensaje);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
