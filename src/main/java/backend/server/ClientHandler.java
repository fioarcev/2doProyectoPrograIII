
package backend.server;

import backend.database.UsuarioBDRepository;
import model.Usuario;
import shared.network.MensajeUtil;
import shared.network.Solicitud;
import shared.network.Respuesta;

import backend.database.FuncionarioBDRepository;
import backend.database.CategoriaBDRepository;
import model.Funcionario;
import model.CategoriaRecurso;
import com.google.gson.Gson;

import backend.database.RecursoBDRepository;
import model.Recurso;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import backend.database.ReservaBDRepository;
import model.Reserva;
import model.DetalleReserva;
import model.EstadoReserva;
import com.google.gson.JsonElement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler implements Runnable {

    private final Socket cliente;
    private final RecursoBDRepository recursoRepository = new RecursoBDRepository();
    private final Gson gson = new Gson();
    private final ReservaBDRepository reservaRepository =
            new ReservaBDRepository();

    public ClientHandler(Socket cliente) {
        this.cliente = cliente;
    }

    @Override
    public void run() {

        try (
                BufferedReader entrada = new BufferedReader(
                        new InputStreamReader(
                                cliente.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );

                PrintWriter salida = new PrintWriter(
                        cliente.getOutputStream(),
                        true,
                        StandardCharsets.UTF_8
                )
        ) {
            ClientesConectados.registrar(this, salida);

            String mensaje;

            while ((mensaje = entrada.readLine()) != null) {

                Solicitud solicitud =
                        MensajeUtil.convertirDesdeJson(
                                mensaje,
                                Solicitud.class
                        );

                Respuesta respuesta =
                        procesarSolicitud(solicitud);

                String respuestaJson =
                        MensajeUtil.convertirAJson(respuesta);

                synchronized (salida) {
                    salida.println(respuestaJson);
                    salida.flush();
                }
            }

        } catch (Exception e) {

            System.out.println("ERROR AL ATENDER AL CLIENTE:");
            e.printStackTrace();

        } finally {

            try {
                cliente.close();
                ClientesConectados.desconectar(this);

            } catch (IOException e) {
                System.out.println(
                        "Error cerrando conexión."
                );
            }
        }
    }

    private Respuesta procesarSolicitud(Solicitud solicitud) {

        if (solicitud == null
                || solicitud.getOperacion() == null) {

            return new Respuesta(
                    false,
                    "Solicitud inválida.",
                    new JsonObject()
            );
        }
        JsonObject datos = solicitud.getDatos();

        switch (solicitud.getOperacion()) {

            case "PING":

                return new Respuesta(
                        true,
                        "Conexión con el Backend exitosa.",
                        new JsonObject()
                );

            case "LOGIN":

                return iniciarSesion(solicitud.getDatos());

            case "USUARIO_LISTAR":
                return listarUsuarios();

            case "USUARIO_BUSCAR":
                return buscarUsuario(solicitud.getDatos());

            case "USUARIO_GUARDAR":
                return guardarUsuario(solicitud.getDatos());

            case "USUARIO_ELIMINAR":
                return eliminarUsuario(solicitud.getDatos());

            case "FUNCIONARIO_LISTAR":
                return listarFuncionarios();

            case "FUNCIONARIO_BUSCAR":
                return buscarFuncionario(solicitud.getDatos());

            case "FUNCIONARIO_GUARDAR":
                return guardarFuncionario(solicitud.getDatos());

            case "FUNCIONARIO_ELIMINAR":
                return eliminarFuncionario(solicitud.getDatos());

            case "CATEGORIA_LISTAR":
                return listarCategorias();

            case "CATEGORIA_BUSCAR":
                return buscarCategoria(solicitud.getDatos());

            case "CATEGORIA_GUARDAR":
                return guardarCategoria(solicitud.getDatos());

            case "CATEGORIA_ELIMINAR":
                return eliminarCategoria(solicitud.getDatos());

            case "RECURSO_GUARDAR":
                return guardarRecurso(datos);

            case "RECURSO_LISTAR":
                return listarRecursos();

            case "RECURSO_BUSCAR":
                return buscarRecurso(datos);

            case "RECURSO_ACTUALIZAR":
                return actualizarRecurso(datos);

            case "RECURSO_ELIMINAR":
                return eliminarRecurso(datos);

            case "RECURSO_BUSCAR_CATEGORIA":
                return buscarRecursosPorCategoria(datos);

            case "RESERVA_GUARDAR":
                return guardarReserva(datos);

            case "RESERVA_LISTAR":
                return listarReservas();

            case "RESERVA_BUSCAR":
                return buscarReserva(datos);

            case "RESERVA_ACTUALIZAR":
                return actualizarReserva(datos);

            case "RESERVA_ELIMINAR":
                return eliminarReserva(datos);

            default:

                return new Respuesta(
                        false,
                        "Operación no reconocida: "
                                + solicitud.getOperacion(),
                        new JsonObject()
                );
        }
    }

    private Respuesta iniciarSesion(JsonObject datos) {

        if (datos == null
                || !datos.has("id")
                || !datos.has("clave")) {

            return new Respuesta(
                    false,
                    "Faltan datos para iniciar sesión.",
                    new JsonObject()
            );
        }

        String id = datos.get("id").getAsString();
        String clave = datos.get("clave").getAsString();

        if (id.isBlank() || clave.isBlank()) {

            return new Respuesta(
                    false,
                    "El ID y la clave son obligatorios.",
                    new JsonObject()
            );
        }

        UsuarioBDRepository repositorio =
                new UsuarioBDRepository();

        Usuario usuario = repositorio.buscarPorId(id);

        if (usuario == null) {

            return new Respuesta(
                    false,
                    "No existe un usuario con ese ID.",
                    new JsonObject()
            );
        }

        if (!usuario.getClave().equals(clave)) {

            return new Respuesta(
                    false,
                    "La clave es incorrecta.",
                    new JsonObject()
            );
        }

        JsonObject datosRespuesta = new JsonObject();

        datosRespuesta.addProperty(
                "id",
                usuario.getId()
        );

        datosRespuesta.addProperty(
                "rol",
                usuario.getRol().name()
        );

        return new Respuesta(
                true,
                "Inicio de sesión exitoso.",
                datosRespuesta
        );
    }

    private Respuesta listarUsuarios() {

        UsuarioBDRepository repositorio =
                new UsuarioBDRepository();

        JsonObject datos = new JsonObject();

        datos.add(
                "usuarios",
                new com.google.gson.Gson().toJsonTree(
                        repositorio.listarTodos()
                )
        );

        return new Respuesta(
                true,
                "Usuarios consultados correctamente.",
                datos
        );
    }

    private Respuesta buscarUsuario(JsonObject datos) {

        if (datos == null || !datos.has("id")) {

            return new Respuesta(
                    false,
                    "Debe indicar el ID del usuario.",
                    new JsonObject()
            );
        }

        String id = datos.get("id").getAsString();

        UsuarioBDRepository repositorio =
                new UsuarioBDRepository();

        Usuario usuario = repositorio.buscarPorId(id);

        JsonObject resultado = new JsonObject();

        if (usuario == null) {

            resultado.add("usuario", null);

            return new Respuesta(
                    true,
                    "Usuario no encontrado.",
                    resultado
            );
        }

        resultado.add(
                "usuario",
                new com.google.gson.Gson().toJsonTree(usuario)
        );

        return new Respuesta(
                true,
                "Usuario encontrado.",
                resultado
        );
    }


    private Respuesta guardarUsuario(JsonObject datos) {

        try {
            if (datos == null
                    || !datos.has("id")
                    || !datos.has("clave")
                    || !datos.has("rol")) {

                return new Respuesta(
                        false,
                        "Faltan los datos del usuario.",
                        new JsonObject()
                );
            }

            Usuario usuario = new Gson().fromJson(
                    datos,
                    Usuario.class
            );

            if (usuario == null
                    || usuario.getId() == null
                    || usuario.getClave() == null
                    || usuario.getRol() == null) {

                return new Respuesta(
                        false,
                        "Los datos del usuario son inválidos.",
                        new JsonObject()
                );
            }

            new UsuarioBDRepository().guardar(usuario);

            return new Respuesta(
                    true,
                    "Usuario guardado correctamente.",
                    new JsonObject()
            );

        } catch (Exception e) {

            return new Respuesta(
                    false,
                    "No se pudo guardar el usuario: " + e.getMessage(),
                    new JsonObject()
            );
        }
    }


    private Respuesta eliminarUsuario(JsonObject datos) {

        if (datos == null || !datos.has("id")) {

            return new Respuesta(
                    false,
                    "Debe indicar el ID del usuario.",
                    new JsonObject()
            );
        }

        try {

            String id = datos.get("id").getAsString();

            UsuarioBDRepository repositorio =
                    new UsuarioBDRepository();

            repositorio.eliminar(id);

            return new Respuesta(
                    true,
                    "Usuario eliminado correctamente.",
                    new JsonObject()
            );

        } catch (Exception e) {

            return new Respuesta(
                    false,
                    "No se pudo eliminar el usuario: "
                            + e.getMessage(),
                    new JsonObject()
            );
        }
    }


    private Respuesta listarFuncionarios() {
        JsonObject datos = new JsonObject();

        datos.add("funcionarios",
                new Gson().toJsonTree(
                        new FuncionarioBDRepository().listarTodos()
                ));

        return new Respuesta(true, "Funcionarios consultados.", datos);
    }

    private Respuesta buscarFuncionario(JsonObject datos) {
        if (datos == null || !datos.has("id")) {
            return new Respuesta(false, "Debe indicar el ID.", new JsonObject());
        }

        Funcionario funcionario =
                new FuncionarioBDRepository().buscarPorId(
                        datos.get("id").getAsString()
                );

        JsonObject resultado = new JsonObject();
        resultado.add("funcionario", new Gson().toJsonTree(funcionario));

        return new Respuesta(true, "Consulta realizada.", resultado);
    }

    private Respuesta guardarFuncionario(JsonObject datos) {
        try {
            if (datos == null || !datos.has("funcionario")
                    || datos.get("funcionario") == null
                    || datos.get("funcionario").isJsonNull()) {

                return new Respuesta(
                        false,
                        "No se recibieron los datos del funcionario.",
                        new JsonObject()
                );
            }

            Funcionario funcionario = new Gson().fromJson(
                    datos.get("funcionario"),
                    Funcionario.class
            );

            if (funcionario == null) {
                return new Respuesta(
                        false,
                        "No fue posible convertir los datos del funcionario.",
                        new JsonObject()
                );
            }

            new FuncionarioBDRepository().guardar(funcionario);

            return new Respuesta(
                    true,
                    "Funcionario guardado.",
                    new JsonObject()
            );

        } catch (Exception e) {
            e.printStackTrace();

            return new Respuesta(
                    false,
                    "Error al guardar funcionario: " + e.getMessage(),
                    new JsonObject()
            );
        }
    }
    private Respuesta eliminarFuncionario(JsonObject datos) {
        try {
            new FuncionarioBDRepository().eliminar(
                    datos.get("id").getAsString()
            );

            return new Respuesta(true, "Funcionario eliminado.", new JsonObject());

        } catch (Exception e) {
            return new Respuesta(false, e.getMessage(), new JsonObject());
        }
    }

    private Respuesta listarCategorias() {
        JsonObject datos = new JsonObject();

        datos.add("categorias",
                new Gson().toJsonTree(
                        new CategoriaBDRepository().listarTodos()
                ));

        return new Respuesta(true, "Categorías consultadas.", datos);
    }

    private Respuesta buscarCategoria(JsonObject datos) {
        if (datos == null || !datos.has("id")) {
            return new Respuesta(false, "Debe indicar el ID.", new JsonObject());
        }

        CategoriaRecurso categoria =
                new CategoriaBDRepository().buscarPorId(
                        datos.get("id").getAsString()
                );

        JsonObject resultado = new JsonObject();
        resultado.add("categoria", new Gson().toJsonTree(categoria));

        return new Respuesta(true, "Consulta realizada.", resultado);
    }

    private Respuesta guardarCategoria(JsonObject datos) {
        try {
            CategoriaRecurso categoria = new Gson().fromJson(
                    datos.get("categoria"),
                    CategoriaRecurso.class
            );

            new CategoriaBDRepository().guardar(categoria);

            return new Respuesta(true, "Categoría guardada.", new JsonObject());

        } catch (Exception e) {
            return new Respuesta(false, e.getMessage(), new JsonObject());
        }
    }

    private Respuesta eliminarCategoria(JsonObject datos) {
        try {
            new CategoriaBDRepository().eliminar(
                    datos.get("id").getAsString()
            );

            return new Respuesta(true, "Categoría eliminada.", new JsonObject());

        } catch (Exception e) {
            return new Respuesta(false, e.getMessage(), new JsonObject());
        }
    }

    private Respuesta guardarRecurso(JsonObject datos) {
        try {
            String id = datos.get("id").getAsString();
            String categoriaId = datos.get("categoriaId").getAsString();
            String descripcion = datos.get("descripcion").getAsString();

            CategoriaRecurso categoria = new CategoriaBDRepository().buscarPorId(categoriaId);

            if (categoria == null) {
                return new Respuesta(false, "La categoría indicada no existe.", null);
            }

            recursoRepository.guardar(new Recurso(id, categoria, descripcion));
            return new Respuesta(true, "Recurso guardado correctamente.", null);

        } catch (Exception e) {
            return new Respuesta(false, e.getMessage(), null);
        }
    }

    private Respuesta listarRecursos() {
        try {
            JsonObject datos = new JsonObject();
            JsonArray arreglo = gson.toJsonTree(recursoRepository.listar()).getAsJsonArray();
            datos.add("recursos", arreglo);

            return new Respuesta(true, "Recursos obtenidos correctamente.", datos);

        } catch (Exception e) {
            return new Respuesta(false, e.getMessage(), null);
        }
    }

    private Respuesta buscarRecurso(JsonObject datos) {
        try {
            Recurso recurso = recursoRepository.buscarPorId(datos.get("id").getAsString());

            JsonObject resultado = new JsonObject();
            resultado.add("recurso", gson.toJsonTree(recurso));

            return new Respuesta(true, "Búsqueda realizada.", resultado);

        } catch (Exception e) {
            return new Respuesta(false, e.getMessage(), null);
        }
    }

    private Respuesta actualizarRecurso(JsonObject datos) {
        try {
            String id = datos.get("id").getAsString();
            String categoriaId = datos.get("categoriaId").getAsString();
            String descripcion = datos.get("descripcion").getAsString();

            CategoriaRecurso categoria = new CategoriaBDRepository().buscarPorId(categoriaId);

            if (categoria == null) {
                return new Respuesta(false, "La categoría indicada no existe.", null);
            }

            recursoRepository.actualizar(new Recurso(id, categoria, descripcion));
            return new Respuesta(true, "Recurso actualizado correctamente.", null);

        } catch (Exception e) {
            return new Respuesta(false, e.getMessage(), null);
        }
    }

    private Respuesta eliminarRecurso(JsonObject datos) {
        try {
            String id = datos.get("id").getAsString();
            Recurso recurso = recursoRepository.buscarPorId(id);

            if (recurso == null) {
                return new Respuesta(false, "No existe el recurso indicado.", null);
            }

            recursoRepository.eliminar(recurso);
            return new Respuesta(true, "Recurso eliminado correctamente.", null);

        } catch (Exception e) {
            return new Respuesta(false, e.getMessage(), null);
        }
    }

    private Respuesta buscarRecursosPorCategoria(JsonObject datos) {
        try {
            String categoriaId = datos.get("categoriaId").getAsString();

            JsonObject resultado = new JsonObject();
            resultado.add("recursos",
                    gson.toJsonTree(recursoRepository.buscarPorCategoria(categoriaId)));

            return new Respuesta(true, "Búsqueda realizada.", resultado);

        } catch (Exception e) {
            return new Respuesta(false, e.getMessage(), null);
        }
    }


    private Respuesta guardarReserva(JsonObject datos) {

        try {
            Reserva reserva = convertirReserva(datos);

            reservaRepository.guardar(reserva);

            JsonObject datosNotificacion = new JsonObject();

            datosNotificacion.addProperty("id", reserva.getId());
            datosNotificacion.addProperty("actividad", reserva.getActividad());
            datosNotificacion.addProperty("fecha", reserva.getFecha().toString());
            datosNotificacion.addProperty(
                    "horaInicio",
                    reserva.getHoraInicio().toString()
            );
            datosNotificacion.addProperty(
                    "horaFin",
                    reserva.getHoraFin().toString()
            );
            datosNotificacion.addProperty(
                    "funcionarioId",
                    reserva.getFuncionarioId()
            );

            Respuesta notificacion = Respuesta.crearNotificacion(
                    "Se ha registrado una nueva reserva.",
                    datosNotificacion
            );

            ClientesConectados.notificarOtros(this, notificacion);

            return new Respuesta(
                    true,
                    "Reserva guardada correctamente.",
                    null
            );

        } catch (Exception e) {

            return new Respuesta(
                    false,
                    e.getMessage(),
                    null
            );
        }
    }


    private Respuesta listarReservas() {
        try {
            JsonObject resultado = new JsonObject();

            resultado.add(
                    "reservas",
                    convertirReservasAJson(reservaRepository.listar())
            );

            return new Respuesta(
                    true,
                    "Reservas obtenidas correctamente.",
                    resultado
            );

        } catch (Exception e) {
            return new Respuesta(false, e.getMessage(), null);
        }
    }

    private Respuesta buscarReserva(JsonObject datos) {
        try {
            Reserva reserva = reservaRepository.buscarPorId(
                    datos.get("id").getAsString()
            );

            JsonObject resultado = new JsonObject();

            if (reserva == null) {
                resultado.add("reserva", null);
            } else {
                resultado.add("reserva", convertirReservaAJson(reserva));
            }

            return new Respuesta(true, "Búsqueda realizada.", resultado);

        } catch (Exception e) {
            return new Respuesta(false, e.getMessage(), null);
        }
    }

    private Respuesta actualizarReserva(JsonObject datos) {
        try {
            Reserva reserva = convertirReserva(datos);
            reservaRepository.actualizar(reserva);

            return new Respuesta(
                    true,
                    "Reserva actualizada correctamente.",
                    null
            );

        } catch (Exception e) {
            return new Respuesta(false, e.getMessage(), null);
        }
    }

    private Respuesta eliminarReserva(JsonObject datos) {
        try {
            String id = datos.get("id").getAsString();

            Reserva reserva = reservaRepository.buscarPorId(id);

            if (reserva == null) {
                return new Respuesta(false, "No existe esa reserva.", null);
            }

            reservaRepository.eliminar(reserva);

            return new Respuesta(
                    true,
                    "Reserva eliminada correctamente.",
                    null
            );

        } catch (Exception e) {
            return new Respuesta(false, e.getMessage(), null);
        }
    }

    private Reserva convertirReserva(JsonObject datos) {
        Reserva reserva = new Reserva(
                datos.get("id").getAsString(),
                datos.get("actividad").getAsString(),
                LocalDate.parse(datos.get("fecha").getAsString()),
                LocalTime.parse(datos.get("horaInicio").getAsString()),
                LocalTime.parse(datos.get("horaFin").getAsString()),
                datos.get("funcionarioId").getAsString(),
                EstadoReserva.valueOf(datos.get("estado").getAsString())
        );

        JsonArray detalles = datos.getAsJsonArray("detalles");

        if (detalles != null) {
            for (JsonElement elemento : detalles) {
                JsonObject detalleJson = elemento.getAsJsonObject();

                String categoriaId =
                        detalleJson.get("categoriaId").getAsString();

                String recursoId =
                        detalleJson.get("recursoId").getAsString();

                CategoriaRecurso categoria =
                        new CategoriaBDRepository().buscarPorId(categoriaId);

                Recurso recurso =
                        new RecursoBDRepository().buscarPorId(recursoId);

                if (categoria == null || recurso == null) {
                    throw new IllegalArgumentException(
                            "No se encontró la categoría o el recurso de la reserva."
                    );
                }

                reserva.getDetalles().add(
                        new DetalleReserva(categoria, recurso)
                );
            }
        }

        return reserva;
    }

    private JsonArray convertirReservasAJson(List<Reserva> reservas) {
        JsonArray arreglo = new JsonArray();

        for (Reserva reserva : reservas) {
            arreglo.add(convertirReservaAJson(reserva));
        }

        return arreglo;
    }

    private JsonObject convertirReservaAJson(Reserva reserva) {
        JsonObject datos = new JsonObject();

        datos.addProperty("id", reserva.getId());
        datos.addProperty("actividad", reserva.getActividad());
        datos.addProperty("fecha", reserva.getFecha().toString());
        datos.addProperty("horaInicio", reserva.getHoraInicio().toString());
        datos.addProperty("horaFin", reserva.getHoraFin().toString());
        datos.addProperty("funcionarioId", reserva.getFuncionarioId());
        datos.addProperty("estado", reserva.getEstado().name());

        JsonArray detalles = new JsonArray();

        for (DetalleReserva detalle : reserva.getDetalles()) {
            JsonObject elemento = new JsonObject();

            elemento.addProperty(
                    "categoriaId",
                    detalle.getCategoria().getId()
            );

            elemento.addProperty(
                    "categoriaDescripcion",
                    detalle.getCategoria().getDescripcion()
            );

            elemento.addProperty(
                    "recursoId",
                    detalle.getRecurso().getId()
            );

            elemento.addProperty(
                    "recursoDescripcion",
                    detalle.getRecurso().getDescripcion()
            );

            detalles.add(elemento);
        }

        datos.add("detalles", detalles);

        return datos;
    }

}
