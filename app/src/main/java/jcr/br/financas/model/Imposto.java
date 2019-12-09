/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcr.br.financas.model;

/**
 * @author User
 */
public class Imposto {
    private int seq;
    private String descricao;
    private String vencimento;
    private String pago;
    private double valor;

    public Imposto() {
    }

    public Imposto(String descricao, String vencimento, double valor) {
        this.descricao = descricao;
        this.vencimento = vencimento;
        this.valor = valor;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }


    /**
     * @return the descricao
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * @param descricao the descricao to set
     */
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    /**
     * @return the vencimento
     */
    public String getVencimento() {
        return vencimento;
    }

    /**
     * @param vencimento the vencimento to set
     */
    public void setVencimento(String vencimento) {
        this.vencimento = vencimento;
    }

    /**
     * @return the pago
     */
    public String getPago() {
        return pago;
    }

    /**
     * @param pago the pago to set
     */
    public void setPago(String pago) {
        this.pago = pago;
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

}
