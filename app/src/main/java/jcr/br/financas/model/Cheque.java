/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcr.br.financas.model;

/**
 *
 * @author User
 */
public class Cheque {
    private int seq;
    private Fornecedor fornecedor;
    private String emissao;
    private String predatado;
    private double valor;
    private String saque;

    public Cheque() {
    }

    public Cheque(int seq, Fornecedor fornecedor, String vencimento, String predatado, double valor) {
        this.seq = seq;
        this.fornecedor = fornecedor;
        this.emissao = vencimento;
        this.predatado = predatado;
        this.valor = valor;
    }


    /**
     * @return the seq
     */
    public int getSeq() {
        return seq;
    }

    /**
     * @param seq the seq to set
     */
    public void setSeq(int seq) {
        this.seq = seq;
    }

    /**
     * @return the fornecedor
     */
    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    /**
     * @param fornecedor the fornecedor to set
     */
    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    /**
     * @return the emissao
     */
    public String getEmissao() {
        return emissao;
    }

    /**
     * @param emissao the emissao to set
     */
    public void setEmissao(String emissao) {
        this.emissao = emissao;
    }

    /**
     * @return the predatado
     */
    public String getPredatado() {
        return predatado;
    }

    /**
     * @param predatado the predatado to set
     */
    public void setPredatado(String predatado) {
        this.predatado = predatado;
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

    public void setSaque(String saque) {
        this.saque = saque;
    }

    public String getSaque() {
        return saque;
    }

}
