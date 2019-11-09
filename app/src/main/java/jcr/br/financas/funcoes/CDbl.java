/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcr.br.financas.funcoes;

import java.text.DecimalFormat;

/**
 *
 * @author User
 */
public class CDbl {

    public static double CDblDuasCasas(double precoDouble) {
        DecimalFormat fmt = new DecimalFormat("0.00");
        String string = fmt.format(precoDouble);
        string = string.replaceAll(",", ".");
        double preco = Double.parseDouble(string);
        return preco;
    }

    public static String CDblDuasCasasString(double precoDouble) {
        DecimalFormat fmt = new DecimalFormat("0.00");
        String string = fmt.format(precoDouble);
        string = string.replaceAll(",", ".");
        return string;
    }

    public static double CDblTresCasas(double d) {
        DecimalFormat fmt = new DecimalFormat("0.000");
        String string = fmt.format(d);
        string = string.replaceAll(",", ".");
        double preco = Double.parseDouble(string);
        return preco;
    }
}
