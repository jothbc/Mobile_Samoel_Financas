package jcr.br.financas.WS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import jcr.br.financas.LancarBoletoActivity;

public class send {
    public static String GET(String url_, String metodo) throws Exception {
        URL url = new URL(url_);
        StringBuilder resposta = new StringBuilder();
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("GET");
        request.setRequestProperty("Accept", "application/json");
        request.setConnectTimeout(3000);
        request.connect();
        if (request.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                resposta.append(inputLine);
            }
            in.close();
            return resposta.toString();
        } else {
            throw new Exception(String.valueOf(request.getResponseCode()));
        }
    }
}
