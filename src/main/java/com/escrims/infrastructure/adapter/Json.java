package com.escrims.infrastructure.adapter;

/** Utilidad minima para armar los cuerpos JSON de las APIs externas */
final class Json {

    private Json() {
    }

    /** Devuelve el texto como literal JSON, con comillas y caracteres escapados */
    static String texto(String valor) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : (valor == null ? "" : valor).toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}
