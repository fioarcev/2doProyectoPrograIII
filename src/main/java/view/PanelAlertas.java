package view;

import shared.network.Respuesta;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelAlertas extends JPanel {

    private final DefaultListModel<String> modeloLista;
    private final JList<String> listaAlertas;
    private final List<Alerta> alertas;

    private final JLabel lblCantidad;

    public PanelAlertas() {

        alertas = new ArrayList<>();
        modeloLista = new DefaultListModel<>();

        setLayout(new BorderLayout(5, 10));
        setPreferredSize(new Dimension(270, 0));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Título
        JLabel lblTitulo = new JLabel("Notificaciones");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));

        lblCantidad = new JLabel("0 alertas");
        lblCantidad.setFont(new Font("Arial", Font.PLAIN, 12));

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(lblTitulo, BorderLayout.NORTH);
        panelSuperior.add(lblCantidad, BorderLayout.SOUTH);

        // Lista de alertas
        listaAlertas = new JList<>(modeloLista);
        listaAlertas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaAlertas.setFont(new Font("Arial", Font.PLAIN, 13));
        listaAlertas.setFixedCellHeight(45);

        JScrollPane scroll = new JScrollPane(listaAlertas);

        // Botones
        JButton btnVer = new JButton("Ver detalle");
        JButton btnDescartar = new JButton("Descartar");

        JPanel panelBotones = new JPanel(new GridLayout(2, 1, 5, 5));
        panelBotones.add(btnVer);
        panelBotones.add(btnDescartar);

        add(panelSuperior, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        // Eventos
        btnVer.addActionListener(e -> mostrarDetalle());

        btnDescartar.addActionListener(e -> descartarAlerta());

        listaAlertas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean seleccionada = listaAlertas.getSelectedIndex() != -1;

                btnVer.setEnabled(seleccionada);
                btnDescartar.setEnabled(seleccionada);
            }
        });

        btnVer.setEnabled(false);
        btnDescartar.setEnabled(false);
    }

    /**
     * Recibe una notificación enviada por el Backend.
     */
    public void agregarNotificacion(Respuesta notificacion) {

        SwingUtilities.invokeLater(() -> {

            if (notificacion == null || !notificacion.isNotificacion()) {
                return;
            }

            JsonObject datos = notificacion.getDatos();

            String id = obtenerDato(datos, "id");
            String actividad = obtenerDato(datos, "actividad");
            String fecha = obtenerDato(datos, "fecha");
            String horaInicio = obtenerDato(datos, "horaInicio");
            String horaFin = obtenerDato(datos, "horaFin");
            String funcionarioId = obtenerDato(datos, "funcionarioId");

            Alerta alerta = new Alerta(
                    notificacion.getMensaje(),
                    id,
                    actividad,
                    fecha,
                    horaInicio,
                    horaFin,
                    funcionarioId
            );

            alertas.add(alerta);

            modeloLista.addElement(
                    "Nueva reserva: " + actividad
            );

            actualizarCantidad();
        });
    }

    /**
     * Obtiene un dato del JSON sin provocar errores
     * cuando el campo no existe.
     */
    private String obtenerDato(JsonObject datos, String campo) {

        if (datos == null || !datos.has(campo) || datos.get(campo).isJsonNull()) {
            return "No disponible";
        }

        return datos.get(campo).getAsString();
    }

    /**
     * Muestra la información completa de la alerta seleccionada.
     */
    private void mostrarDetalle() {

        int indice = listaAlertas.getSelectedIndex();

        if (indice == -1) {
            return;
        }

        Alerta alerta = alertas.get(indice);

        String mensaje =
                "Actividad: " + alerta.actividad + "\n"
                        + "Código de reserva: " + alerta.id + "\n"
                        + "Fecha: " + alerta.fecha + "\n"
                        + "Hora de inicio: " + alerta.horaInicio + "\n"
                        + "Hora de finalización: " + alerta.horaFin + "\n"
                        + "Funcionario: " + alerta.funcionarioId;

        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Detalle de la reserva",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Descarta la alerta únicamente en este Frontend.
     * No realiza ninguna solicitud al Backend.
     */
    private void descartarAlerta() {

        int indice = listaAlertas.getSelectedIndex();

        if (indice == -1) {
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de que desea descartar esta alerta?",
                "Confirmar descarte",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {

            alertas.remove(indice);
            modeloLista.remove(indice);

            actualizarCantidad();
        }
    }

    /**
     * Actualiza el número de alertas pendientes.
     */
    private void actualizarCantidad() {

        int cantidad = alertas.size();

        lblCantidad.setText(
                cantidad + (cantidad == 1 ? " alerta" : " alertas")
        );
    }

    /**
     * Representa una alerta recibida.
     */
    private static class Alerta {

        private final String mensaje;
        private final String id;
        private final String actividad;
        private final String fecha;
        private final String horaInicio;
        private final String horaFin;
        private final String funcionarioId;

        public Alerta(
                String mensaje,
                String id,
                String actividad,
                String fecha,
                String horaInicio,
                String horaFin,
                String funcionarioId
        ) {

            this.mensaje = mensaje;
            this.id = id;
            this.actividad = actividad;
            this.fecha = fecha;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.funcionarioId = funcionarioId;
        }
    }
}