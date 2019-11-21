/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcr.br.financas.funcoes;

import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 *
 * @author User
 */
public class Conv {

    /**
     *
     * @param precoDouble
     * @return
     */
    public static double CDblDuasCasas(double precoDouble) {
        String string = new DecimalFormat("0.00").format(precoDouble);
        return Double.parseDouble(string.replaceAll(",", "."));
    }

    /**
     *
     * @param precoDouble
     * @return
     */
    public static String CDblDuasCasasString(double precoDouble) {
        String string = new DecimalFormat("0.00").format(precoDouble);
        return string.replaceAll(",", ".");
    }

    public static final String validarValue(double valor) {
        String v = Conv.CDblDuasCasasString(valor).replaceAll("\\.", ",");
        String sub = v.substring(v.indexOf(","));
        if (sub.length() == 1) {
            return v + "0";
        } else {
            return v;
        }
    }

    /**
     *
     * @param s
     * @return
     */
    public static final String colocarPontoEmValor(String s) {
        int index = s.indexOf(",");
        if (index > 3) {
            String sub = s.substring(0, index);
            int controle = 0;
            String temp = "";
            for (int x = 0; x < index + 1; x++) {
                if (x == index - 3) {
                    temp += '.';
                } else {
                    temp += sub.charAt(controle);
                    controle++;
                }
            }
            temp += s.substring(index);
            s = temp;
        }
        return s;
    }

    /*
        normaliza strings com acento e retorna a string sem acentuação
    */
    public static final String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

}
