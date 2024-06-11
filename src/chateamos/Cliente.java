package chateamos;





import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Cliente extends JFrame {

    private String nombreUsuario;
    private JTextArea areaMensajes;
    private JTextField campoMensaje;
    private PrintWriter escritorServidor;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Cliente::new);
    }

    public Cliente() {
        super("Whatsapp 2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);

        areaMensajes = new JTextArea();
        areaMensajes.setEditable(false);

        campoMensaje = new JTextField();

        JButton botonEnviar = new JButton("Enviar");
        botonEnviar.addActionListener(e -> enviarMensaje());

        JButton botonSalir = new JButton("Salir");
        botonSalir.addActionListener(e -> desconectar());

        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout());
        panelBotones.add(botonEnviar);
        panelBotones.add(botonSalir);

        add(new JScrollPane(areaMensajes), BorderLayout.CENTER);
        add(campoMensaje, BorderLayout.NORTH);
        add(panelBotones, BorderLayout.SOUTH);

        setVisible(true);

        conectarAlServidor();
    }

    private void conectarAlServidor() {
        try {
            Socket socket = new Socket("localhost", 5555);
            BufferedReader lectorServidor = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            escritorServidor = new PrintWriter(socket.getOutputStream(), true);

            nombreUsuario = JOptionPane.showInputDialog("Por favor, ingresa tu  nombre:");
            escritorServidor.println(nombreUsuario);

            new Thread(() -> recibirMensajesDelServidor(lectorServidor)).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarMensaje() {
        String mensaje = campoMensaje.getText();
        if (!mensaje.isEmpty()) {
            escritorServidor.println("#CLIENTE_ENVIO_MENSAJE#");
            escritorServidor.println(mensaje);
            campoMensaje.setText("");
        }
    }

    private void recibirMensajesDelServidor(BufferedReader lectorServidor) {
        try {
            String mensaje;
            while ((mensaje = lectorServidor.readLine()) != null) {
                if (mensaje.equals("#LISTA_CLIENTES#")) {
                    SwingUtilities.invokeLater(() -> areaMensajes.setText(""));
                    while (!(mensaje = lectorServidor.readLine()).isEmpty()) {
                        String mensajeFinal = mensaje;
                        SwingUtilities.invokeLater(() -> areaMensajes.append(mensajeFinal + "\n"));
                    }
                } else {
                    agregarMensaje(mensaje);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void desconectar() {
        escritorServidor.println("#CLIENTE_DESCONECTAR#");
        System.exit(0);
    }

    private void agregarMensaje(String mensaje) {
        SwingUtilities.invokeLater(() -> areaMensajes.append(mensaje + "\n"));
    }
}

