package com.mycompany.mavenproject1.control;

import com.mycompany.mavenproject1.excepciones.DatoInvalidoException;
import java.time.Year;

/**
 * Validaciones de datos de entrada compartidas por la consola, las ventanas
 * y la persistencia. Antes esta lógica estaba copiada en tres clases distintas.
 */
public final class Validador {

    /** Año mínimo de ingreso aceptado. */
    public static final int ANIO_MINIMO = 1950;

    private Validador() {
        // clase utilitaria, no se instancia
    }

    /**
     * Valida un RUT chileno (formato y dígito verificador) y lo deja en formato
     * estándar 12.345.678-K, para que "12345678-k" y "12.345.678-K" sean el mismo RUT.
     *
     * @param rut RUT con o sin puntos
     * @return RUT normalizado
     * @throws DatoInvalidoException si el formato o el dígito verificador son incorrectos
     */
    public static String normalizarRut(String rut) throws DatoInvalidoException {
        if (rut == null || rut.trim().isEmpty()) {
            throw new DatoInvalidoException("Debe ingresar un RUT.");
        }
        String limpio = rut.trim().replace(".", "").toUpperCase();
        if (!limpio.matches("\\d{7,8}-[\\dK]")) {
            throw new DatoInvalidoException("El RUT \"" + rut + "\" no tiene formato válido (ej. 12.345.678-5).");
        }
        String cuerpo = limpio.substring(0, limpio.indexOf('-'));
        char dv = limpio.charAt(limpio.length() - 1);
        if (dv != calcularDigitoVerificador(cuerpo)) {
            throw new DatoInvalidoException("El dígito verificador del RUT \"" + rut + "\" no es correcto.");
        }
        StringBuilder conPuntos = new StringBuilder(cuerpo);
        for (int i = conPuntos.length() - 3; i > 0; i -= 3) {
            conPuntos.insert(i, '.');
        }
        return conPuntos + "-" + dv;
    }

    /**
     * Calcula el dígito verificador con el algoritmo módulo 11.
     *
     * @param cuerpo número del RUT sin dígito verificador
     * @return dígito verificador ('0'-'9' o 'K')
     */
    public static char calcularDigitoVerificador(String cuerpo) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) {
            return '0';
        }
        if (resto == 10) {
            return 'K';
        }
        return (char) ('0' + resto);
    }

    /**
     * Valida un texto obligatorio. No permite ';' ni saltos de línea porque
     * romperían el formato de los archivos CSV.
     *
     * @param valor texto ingresado
     * @param campo nombre del campo, para el mensaje de error
     * @return texto sin espacios al inicio ni al final
     * @throws DatoInvalidoException si está vacío o contiene caracteres no permitidos
     */
    public static String textoObligatorio(String valor, String campo) throws DatoInvalidoException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DatoInvalidoException("El campo \"" + campo + "\" es obligatorio.");
        }
        return textoOpcional(valor, campo);
    }

    /**
     * Igual que {@link #textoObligatorio} pero acepta texto vacío.
     *
     * @param valor texto ingresado (puede ser null)
     * @param campo nombre del campo, para el mensaje de error
     * @return texto limpio (nunca null)
     * @throws DatoInvalidoException si contiene ';' o saltos de línea
     */
    public static String textoOpcional(String valor, String campo) throws DatoInvalidoException {
        if (valor == null) {
            return "";
        }
        String limpio = valor.trim();
        if (limpio.contains(";") || limpio.contains("\n") || limpio.contains("\r")) {
            throw new DatoInvalidoException("El campo \"" + campo + "\" no puede contener ';' ni saltos de línea.");
        }
        return limpio;
    }

    /**
     * @param anio año de ingreso
     * @return el mismo año si es válido
     * @throws DatoInvalidoException si está fuera del rango permitido
     */
    public static int anioIngreso(int anio) throws DatoInvalidoException {
        int maximo = Year.now().getValue();
        if (anio < ANIO_MINIMO || anio > maximo) {
            throw new DatoInvalidoException("El año de ingreso debe estar entre " + ANIO_MINIMO + " y " + maximo + ".");
        }
        return anio;
    }

    /**
     * @param meses duración en meses
     * @return la misma duración si es válida
     * @throws DatoInvalidoException si no está entre 1 y 24 meses
     */
    public static int duracionMeses(int meses) throws DatoInvalidoException {
        if (meses < 1 || meses > 24) {
            throw new DatoInvalidoException("La duración debe estar entre 1 y 24 meses.");
        }
        return meses;
    }

    /**
     * Convierte un texto a entero.
     *
     * @param texto texto ingresado
     * @param campo nombre del campo, para el mensaje de error
     * @return el número
     * @throws DatoInvalidoException si el texto no es un número entero
     */
    public static int entero(String texto, String campo) throws DatoInvalidoException {
        try {
            return Integer.parseInt(texto == null ? "" : texto.trim());
        } catch (NumberFormatException e) {
            throw new DatoInvalidoException("El campo \"" + campo + "\" debe ser un número entero.");
        }
    }
}
