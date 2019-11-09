package jcr.br.financas.WS;

import android.os.StrictMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class wsGET {

    public static String sendGet(String url_) throws IOException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        StringBuilder resposta = new StringBuilder();
        URL url = new URL(url_);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("GET");
        request.setRequestProperty("Accept", "application/json");
        request.setConnectTimeout(10000);
        request.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            resposta.append(inputLine);
        }
        in.close();
        return resposta.toString();
    }
}
