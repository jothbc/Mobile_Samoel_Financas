/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcr.br.financas.funcoes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author User
 */
public class CDate {

    /**
     * Converte String data formato MYSQL (yyyy-MM-dd) e retorna uma String data
     * formato BR (dd/MM/yyyy)
     *
     * @param dataMYSQL yyyy-MM-dd String
     * @return dd/MM/yyyy String
     */
    public static String MYSQLtoPTBR(String dataMYSQL) {
        Date data = null;
        try {
            data = new SimpleDateFormat("yyyy-MM-dd").parse(dataMYSQL);
        } catch (ParseException ex) {
            System.err.println("Erro ao converter data :" + ex);
        }
        return new SimpleDateFormat("dd/MM/yyyy").format(data);
    }

    /**
     * Converte Date formato MYSQL (yyyy-MM-dd) e retorna uma String data
     * formato BR (dd/MM/yyyy)
     *
     * @param dataMYSQL Date formato MYSQL yyyy-MM-dd
     * @return dd/MM/yyyy String
     */
    public static String MYSQLtoPTBR(Date dataMYSQL) {
        return new SimpleDateFormat("dd/MM/yyyy").format(dataMYSQL);
    }

    /**
     * Obtem a data atual em formato PTBR String
     *
     * @return data de hoje em PTBR
     */
    public static String getHojePTBR() {
        return new SimpleDateFormat("dd/MM/yyyy").format(new Date());
    }

    /**
     * Converte String de data PTBR em String data MYSQL
     *
     * @param dataPTBR data formato dd/MM/yyyy
     * @return data String em formato yyyy-MM-dd
     */
    public static String PTBRtoMYSQL(String dataPTBR) {
        Date data = null;
        try {
            data = new SimpleDateFormat("dd/MM/yyyy").parse(dataPTBR);
        } catch (ParseException ex) {
            System.err.println("Erro ao converter data :" + ex);
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(data);
    }

    /**
     * Adiciona dias a data informada e retorna com uma nova data ja somado.
     *
     * @param dias quantidades de dias a ser incrementado
     * @param dataPTBR_inicial data inicial em formato PTBR
     * @return uma nova data PTBR com a soma dos dias
     */
    public static String incrementarDias(int dias, String dataPTBR_inicial) {
        try {
            Date dataInicial = new SimpleDateFormat("dd/MM/yyyy").parse(dataPTBR_inicial);
            Calendar calendarData = Calendar.getInstance();
            calendarData.setTime(dataInicial);
            calendarData.add(Calendar.DATE, dias);
            return new SimpleDateFormat("dd/MM/yyyy").format(calendarData.getTime());
        } catch (ParseException ex) {
            System.err.println("Erro no CDate.incrementarDias:" + ex.getMessage());
            return dataPTBR_inicial;
        }
    }

    /**
     * Incrementa o numero de meses informado a data informada.
     *
     * @param mes quantidade de meses a ser incrementado.
     * @param dataPTBR_inicial data inicial.
     * @return uma nova data PTBR com o incremento da quantia dos meses
     * informado.
     */
    public static String incrementarMes(int mes, String dataPTBR_inicial) {
        try {
            Date dataInicial = new SimpleDateFormat("dd/MM/yyyy").parse(dataPTBR_inicial);
            Calendar calendarData = Calendar.getInstance();
            calendarData.setTime(dataInicial);
            calendarData.add(Calendar.MONTH, mes);
            return new SimpleDateFormat("dd/MM/yyyy").format(calendarData.getTime());
        } catch (ParseException ex) {
            System.err.println("Erro no CDate.incrementarMes:" + ex.getMessage());
            return dataPTBR_inicial;
        }
    }

    /**
     * Função que calcula a quantidade de dias restantes até a o dia de hoje a
     * partir de uma data informada.
     *
     * @param dataInicio data inicial para contagem de dias restantes
     * @return retorna um tipo Long do calculo de dias restantes, caso a data
     * informada gere erro, é retornado 0 para não dar erro de compilação.
     */
    public static long diasRestantes(String dataInicio) {
        int dias = 0;
        Calendar data1 = Calendar.getInstance(), data2 = Calendar.getInstance();
        try {
            data2.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(dataInicio));
        } catch (java.text.ParseException e) {
            System.err.println("Data informada deve estar em formato incorreto.\n" + e);
            return dias;
        }
        if (data1.get(Calendar.YEAR) == data2.get(Calendar.YEAR)) {
            dias = data2.get(Calendar.DAY_OF_YEAR) - data1.get(Calendar.DAY_OF_YEAR);
        } else if (data1.get(Calendar.YEAR) < data2.get(Calendar.YEAR)) {
            int bix = 0;
            if (data1.get(Calendar.YEAR) % 4 == 0 && data1.get(Calendar.YEAR) % 100 != 0 && data1.get(Calendar.YEAR) % 400 == 0) {
                bix = 366;
            } else {
                bix = 365;
            }
            dias = data2.get(Calendar.DAY_OF_YEAR) + bix - data1.get(Calendar.DAY_OF_YEAR);
        }
        return dias;
    }

    /**
     * Converte data string PTBR em formato Date
     *
     * @param dataPTBR dataPTBR em formato dd/MM/yyyy
     * @return retorna data em formato Date
     * @throws Exception
     */
    public static Date PTBRtoDate(String dataPTBR) throws Exception {
        try {
            return new SimpleDateFormat("dd/MM/yyyy").parse(dataPTBR);
        } catch (ParseException ex) {
            throw new Exception("Erro ao converter data. " + ex);
        }
    }

    /**
     * Converte data string MYSQL em formato Date
     *
     * @param dataMYSQL dataMYSQL em formato yyyy-MM-dd
     * @return retorna data em formato Date
     * @throws Exception
     */
    public static Date MYSQLtoDate(String dataMYSQL) throws Exception {
        try {
            return new SimpleDateFormat("dd/MM/yyyy").parse(MYSQLtoPTBR(dataMYSQL));
        } catch (ParseException ex) {
            throw new Exception("Erro ao converter data. " + ex);
        }
    }

    /**
     * Retorna uma string da data inicial em que é feito o calculo para obter o
     * vencimento dos boletos gerados pelos bancos
     *
     * @return
     */
    public static String getDataInicialBanco() {
        return "07/10/1997";
    }

    /**
     * obtem a hora atual em String formato HH:MM:SS
     *
     * @return
     */
    public static String getHoraAtualPTBR() {
        Calendar calendario = Calendar.getInstance();
        int hora, minuto, segundo;
        hora = calendario.get(Calendar.HOUR_OF_DAY);
        minuto = calendario.get(Calendar.MINUTE);
        segundo = calendario.get(Calendar.SECOND);
        String h, m, s;
        if (hora < 10) {
            h = "0" + hora;
        } else {
            h = "" + hora;
        }
        if (minuto < 10) {
            m = "0" + minuto;
        } else {
            m = "" + minuto;
        }
        if (segundo < 10) {
            s = "0" + segundo;
        } else {
            s = "" + segundo;
        }
        return (h + ":" + m + ":" + s);
    }

    public static String decimalPHora(double decimal, boolean incluirSegundos) {
        double takttime = decimal;
        int t = (int) (takttime * 60 * 60);
        int hour = t / 3600;
        t %= 3600;
        int min = t / 60;
        t %= 60;
        int sec = t;

        String hora, minuto, segundo;
        hora = Integer.toString(hour);
        minuto = Integer.toString(min);
        segundo = Integer.toString(sec);

        /*
            ajustes da hora
         */
        if (hour == 0) {
            hora = "00";
        } else if (hour < 10) {
            hora = "0" + hora;
        }
        /*
            ajustes do minuto
         */
        if (min == 0) {
            minuto = "00";
        } else if (min < 10) {
            minuto = "0" + minuto;
        }
        /*
            ajustes do segundo
         */
        if (sec == 0) {
            segundo = "00";
        } else if (sec < 10) {
            segundo = "0" + segundo;
        }

        if (incluirSegundos) {
            return hora + ":" + minuto + ":" + segundo;
        } else {
            return hora + ":" + minuto;
        }
    }

    public static double horaPDecimal(String hora) {
        String[] temp = hora.split(":");
        int hour = Integer.parseInt(temp[0]);
        double minute = Integer.parseInt(temp[1]);

        minute = (minute * 100) / 60;
        minute /= 100;
        minute += hour;
        return minute;
    }
}
