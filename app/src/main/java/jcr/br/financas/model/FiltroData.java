package jcr.br.financas.model;

public class FiltroData {
    public String inicio;
    public String fim;

    public String toString() {
        return inicio.replaceAll("/", "-") + "/" + fim.replaceAll("/", "-");
    }
}
