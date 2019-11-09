package jcr.br.financas;

public class Boleto {           //deixar datas com formato MySQL
    private int id;
    private Fornecedor fornecedor_id;
    private String cd_barras;
    private double valor;
    private String vencimento;
    private String pago;


    public Boleto() {
    }

    public Boleto(Fornecedor fornecedor_id, String cd_barras, double valor, String vencimento) {
        this.fornecedor_id = fornecedor_id;
        this.cd_barras = cd_barras;
        this.valor = valor;
        this.vencimento = vencimento;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the fornecedor_id
     */
    public Fornecedor getFornecedor_id() {
        return fornecedor_id;
    }

    /**
     * @param fornecedor_id the fornecedor_id to set
     */
    public void setFornecedor_id(Fornecedor fornecedor_id) {
        this.fornecedor_id = fornecedor_id;
    }

    /**
     * @return the cd_barras
     */
    public String getCd_barras() {
        return cd_barras;
    }

    /**
     * @param cd_barras the cd_barras to set
     */
    public void setCd_barras(String cd_barras) {
        this.cd_barras = cd_barras;
    }

    /**
     * @return the valor
     */
    public double getValor() {
        return valor;
    }

    /**
     * @param valor the valor to set
     */
    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getVencimento() {
        return vencimento;
    }

    public void setVencimento(String vencimento) {
        this.vencimento = vencimento;
    }

    public String getPago() {
        return pago;
    }

    public void setPago(String pago) {
        this.pago = pago;
    }
}