package service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import exception.AiReservationException;
import model.CategoriaRecurso;

import java.time.LocalDate;

public class AiReservationService {

    private final Gson gson = new Gson();

    public AiReservationResponse interpretarSolicitud(
            String solicitud,
            java.util.List<CategoriaRecurso> categorias
    ) throws AiReservationException {

        if (solicitud == null || solicitud.trim().isEmpty()) {
            throw new AiReservationException("La solicitud no puede estar vacía.");
        }

        if (categorias == null || categorias.isEmpty()) {
            throw new AiReservationException("No hay categorías de recursos registradas en el sistema.");
        }

        try {
            String respuestaIA = consultarIA(solicitud, categorias);

            if (respuestaIA.trim().isEmpty()) {
                throw new AiReservationException("La IA no devolvió una respuesta.");
            }

            return convertirRespuesta(respuestaIA);

        } catch (AiReservationException e) {
            throw e;
        } catch (Exception e) {
            throw new AiReservationException(
                    "Ocurrió un error al procesar la solicitud.", e
            );
        }
    }

    private String consultarIA(
            String solicitud,
            java.util.List<CategoriaRecurso> categorias
    ) throws AiReservationException {

        try {
            Client client = Client.builder()
                    .apiKey(System.getenv("GEMINI_API_KEY"))
                    .build();

            GenerateContentResponse respuesta = client.models.generateContent(
                    "gemini-3.6-flash",
                    construirPrompt(solicitud, categorias),
                    null
            );

            String texto = respuesta.text();

            if (texto == null || texto.isBlank()) {
                throw new AiReservationException("La IA no devolvió ningún resultado.");
            }

            texto = texto.strip();

            if (texto.startsWith("```")) {
                texto = texto.replaceAll("^```[a-zA-Z]*\\n?", "")
                        .replaceAll("```$", "")
                        .strip();
            }

            return texto;

        } catch (AiReservationException e) {
            throw e;
        } catch (Exception e) {
            throw new AiReservationException(
                    "No se pudo conectar con la IA: " + e.getMessage(), e
            );
        }
    }

    private String construirPrompt(
            String solicitud,
            java.util.List<CategoriaRecurso> categorias
    ) {

        LocalDate hoy = LocalDate.now();

        StringBuilder listaCats = new StringBuilder();

        for (CategoriaRecurso categoria : categorias) {
            listaCats.append("- ")
                    .append(categoria.getDescripcion())
                    .append("\n");
        }

        return String.format("""
        Eres un asistente para un sistema de reservas de recursos.

        La fecha de hoy es: %s

        Analiza la solicitud del usuario y extrae:

        - actividad
        - fecha
        - horaInicio
        - horaFin
        - categorias

        Estas son las categorías reales disponibles en el sistema:

        %s

        INSTRUCCIONES IMPORTANTES SOBRE LAS CATEGORÍAS:

        1. Identifica qué tipo de recurso necesita el usuario
           según lo que escribe en su solicitud.

        2. Puedes interpretar sinónimos o expresiones similares.
           Por ejemplo, si el usuario pide un aula, salón o sala
           para una reunión, debes identificar la categoría
           disponible que mejor corresponda.

        3. Debes seleccionar únicamente categorías de la lista
           proporcionada anteriormente.

        4. Escribe el nombre de la categoría exactamente como
           aparece en la lista. No inventes categorías.

        5. Si el usuario solicita varios tipos de recursos,
           incluye todas las categorías correspondientes.

        6. No confundas la actividad que realizará el usuario
           con la categoría del recurso que necesita.

        Devuelve únicamente un JSON con este formato:

        {
          "actividad": "string",
          "fecha": "YYYY-MM-DD",
          "horaInicio": "HH:mm",
          "horaFin": "HH:mm",
          "categorias": ["Nombre exacto de la categoría"]
        }

        Para expresiones como "mañana", "pasado mañana",
        "el próximo martes", etc., utiliza la fecha de hoy
        como referencia.

        No agregues explicaciones ni texto fuera del JSON.

        Solicitud del usuario: %s
        """, hoy, listaCats.toString(), solicitud);
    }


    private AiReservationResponse convertirRespuesta(String respuestaIA) throws AiReservationException {
        try {
            RawIA raw = gson.fromJson(respuestaIA, RawIA.class);

            if (raw == null) {
                throw new AiReservationException("La respuesta de la IA no tiene el formato esperado.");
            }

            AiReservationResponse respuesta = new AiReservationResponse(
                    raw.actividad,
                    raw.fecha != null ? java.time.LocalDate.parse(raw.fecha) : null,
                    raw.horaInicio != null ? java.time.LocalTime.parse(raw.horaInicio) : null,
                    raw.horaFin != null ? java.time.LocalTime.parse(raw.horaFin) : null,
                    raw.categorias != null
                            ? raw.categorias.stream()
                                .map(CategoriaRecurso::valueOf)
                                .collect(java.util.stream.Collectors.toList())
                            : null
            );

            validarRespuesta(respuesta);
            return respuesta;
        } catch (JsonSyntaxException e) {
            throw new AiReservationException("La respuesta de la IA tiene un formato inválido.", e);
        } catch (AiReservationException e) {
            throw e;
        } catch (Exception e) {
            throw new AiReservationException("Ocurrió un error inesperado al convertir la respuesta de la IA.", e);
        }
    }

    private static class RawIA {
        String actividad;
        String fecha;
        String horaInicio;
        String horaFin;
        java.util.List<String> categorias;
    }

    private void validarRespuesta(AiReservationResponse respuesta) throws AiReservationException {
        if (respuesta.getActividad() == null || respuesta.getActividad().trim().isEmpty()) {
            throw new AiReservationException("La actividad no puede estar vacía.");
        }

        if (respuesta.getFecha() == null) {
            throw new AiReservationException("La fecha no pudo ser interpretada.");
        }
        if (respuesta.getHoraInicio() == null || respuesta.getHoraFin() == null) {
            throw new AiReservationException("La hora no pudo ser interpretada.");
        }

        if(!respuesta.getHoraInicio().isBefore(respuesta.getHoraFin())) {
            throw new AiReservationException("La hora de inicio debe ser anterior a la hora de fin.");
        }

        if(respuesta.getCategorias() == null || respuesta.getCategorias().isEmpty()) {
            throw new AiReservationException("Debe especificar al menos una categoría de recurso.");
        }
    }
}