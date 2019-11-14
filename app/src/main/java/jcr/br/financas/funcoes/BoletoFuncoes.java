/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcr.br.financas.funcoes;

/**
 *
 * @author User
 */
public class BoletoFuncoes {

    /*
    * Aceita somente boletos com 44 digitos
     */
    public static String codigoDeBarrasEmLinhaDigitavel(String barra, boolean incluirPontos) {
        // Remover caracteres não numéricos.
        String linha = barra.replaceAll("[^0-9]", "");

        if (linha.length() != 44) {
            return null; // 'A linha do Código de Barras está incompleta!'
        }
        String campo1, campo2, campo3, campo4, campo5;
        if (incluirPontos) {
            campo1 = linha.substring(0, 4) + linha.substring(19, 20) + '.' + linha.substring(20, 24);
            campo2 = linha.substring(24, 29) + '.' + linha.substring(29, 34);
            campo3 = linha.substring(34, 39) + '.' + linha.substring(39, 44);
            campo4 = linha.substring(4, 5); // Digito verificador
            campo5 = linha.substring(5, 19); // Vencimento + Valor
        } else {
            campo1 = linha.substring(0, 4) + linha.substring(19, 20) + linha.substring(20, 24);
            campo2 = linha.substring(24, 29) + linha.substring(29, 34);
            campo3 = linha.substring(34, 39) + linha.substring(39, 44);
            campo4 = linha.substring(4, 5); // Digito verificador
            campo5 = linha.substring(5, 19); // Vencimento + Valor   
        }

        if (modulo11Banco(linha.substring(0, 4) + linha.substring(5, 44)) != Integer.valueOf(campo4)) {
            return null; //'Digito verificador '+campo4+', o correto é '+modulo11_banco(  linha.substr(0,4)+linha.substr(5,99)  )+'\nO sistema não altera automaticamente o dígito correto na quinta casa!'
        }
        if (incluirPontos) {
            return campo1 + modulo10(campo1)
                    + ' '
                    + campo2 + modulo10(campo2)
                    + ' '
                    + campo3 + modulo10(campo3)
                    + ' '
                    + campo4
                    + ' '
                    + campo5;
        } else {
            return campo1 + modulo10(campo1)
                    + campo2 + modulo10(campo2)
                    + campo3 + modulo10(campo3)
                    + campo4
                    + campo5;
        }
    }

    public static int modulo10(String numero) {
        numero = numero.replaceAll("[^0-9]", "");
        int soma = 0;
        int peso = 2;
        int contador = numero.length() - 1;
        while (contador >= 0) {
            int multiplicacao = Integer.valueOf(numero.substring(contador, contador + 1)) * peso;
            if (multiplicacao >= 10) {
                multiplicacao = 1 + (multiplicacao - 10);
            }
            soma = soma + multiplicacao;
            if (peso == 2) {
                peso = 1;
            } else {
                peso = 2;
            }
            contador = contador - 1;
        }
        int digito = 10 - (soma % 10);
        if (digito == 10) {
            digito = 0;
        }

        return digito;
    }

    public static int modulo11Banco(String numero) {
        numero = numero.replaceAll("[^0-9]", "");

        int soma = 0;
        int peso = 2;
        int base = 9;
        int contador = numero.length() - 1;
        for (int i = contador; i >= 0; i--) {
            soma = soma + (Integer.valueOf(numero.substring(i, i + 1)) * peso);
            if (peso < base) {
                peso++;
            } else {
                peso = 2;
            }
        }
        int digito = 11 - (soma % 11);
        if (digito > 9) {
            digito = 0;
        }
        /* Utilizar o dígito 1(um) sempre que o resultado do cálculo padrão for igual a 0(zero), 1(um) ou 10(dez). */
        if (digito == 0) {
            digito = 1;
        }
        return digito;
    }

    public static String linhaDigitavelEmCodigoDeBarras(String cd_barras) {
        if (cd_barras.length() == 48) {
            cd_barras = arrumar48_44(cd_barras);
        } else {
            if (cd_barras.length() != 44 && cd_barras.length() != 47) {
                return null;
            }
            if (cd_barras.length() == 47) {
                String[] codigo = new String[6];
                codigo[0] = cd_barras.substring(0, 4);
                codigo[1] =  cd_barras.substring(32,33);
                codigo[2]= cd_barras.substring(33);
                codigo[3] =  cd_barras.substring(4, 9);
                codigo[4]= cd_barras.substring(10, 20);
                codigo[5]= cd_barras.substring(21, 31);
                cd_barras = codigo[0]+codigo[1]+codigo[2]+codigo[3]+codigo[4]+codigo[5];
            }
        }
        return cd_barras;
    }

    public static String arrumar48_44(String cd_barras) {
        //verificado
        String temp = "";
        char[] c = new char[48];
        for (int x = 0; x < 48; x++) {
            c[x] = cd_barras.charAt(x);
        }
        for (int x = 0; x < 47; x++) {
            if (x != 11 && x != 23 && x != 35) {
                temp += c[x];
            }
        }
        return temp;
    }

    public static Double getValorBoleto(String codigo) {
        if (codigo.length() != 44) {
            throw new IllegalArgumentException("Tamanho do código de barras informado está incorreto!");
        } else {
            return Double.parseDouble(codigo.substring(9, 19)) / 100;
        }
    }

    public static int getBanco(String codigo) {
        if (codigo.length() != 44) {
            throw new IllegalArgumentException("Tamanho do código de barras informado está incorreto!");
        } else {
            return Integer.parseInt(codigo.substring(0, 3));
        }
    }

    public static String getVencimento(String codigo) {
        if (codigo.length() != 44) {
            throw new IllegalArgumentException("Tamanho do código de barras informado está incorreto!");
        } else {
            return CDate.incrementarDias(Integer.parseInt(codigo.substring(5, 9)), CDate.getDataInicialBanco());
        }
    }
}
