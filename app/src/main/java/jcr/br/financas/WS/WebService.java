package jcr.br.financas.WS;

import android.os.StrictMode;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import jcr.br.financas.model.MyException;

public class WebService {
    //private static final String base = "http://181.221.135.100:9999/mercadows/webresources/ws2/";
    private static final String base = "http://192.168.0.158:9999/mercadows/webresources/ws2/";

    public static String get(String urlString) {
        try {
            URL url = new URL(base + urlString);
            StringBuilder resposta = new StringBuilder();
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod("GET");
            request.setRequestProperty("Accept", "application/json");
            request.setConnectTimeout(3000);
            request.connect();
            MyException.code = request.getResponseCode();
            if (request.getResponseCode() / 100 == 2) {
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    resposta.append(inputLine);
                }
                in.close();
                return resposta.toString();
            }
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String post(String url, String json, String metodo) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            // Cria um objeto HttpURLConnection:
            HttpURLConnection request = (HttpURLConnection) new URL(base + url).openConnection();

            try {
                // Define que a conexão pode enviar informações e obtê-las de volta:
                request.setDoOutput(true);
                request.setDoInput(true);

                // Define o content-type:
                request.setRequestProperty("Content-Type", "application/json");
                // Define o tempo máximo
                request.setConnectTimeout(3000);

                // Define o método da requisição:
                request.setRequestMethod(metodo);

                // Conecta na URL:
                request.connect();

                // Escreve o objeto JSON usando o OutputStream da requisição:
                try (OutputStream outputStream = request.getOutputStream()) {
                    outputStream.write(json.getBytes("UTF-8"));
                }

                MyException.code = request.getResponseCode();

                if (request.getResponseCode() / 100 == 2) {
                    return readResponse(request);
                } else {
                    return null;
                }
            } finally {
                request.disconnect();
            }
        } catch (IOException ex) {
            System.err.println(ex);
            return null;
        }
    }

    private static String readResponse(HttpURLConnection request) throws IOException {
        ByteArrayOutputStream os;
        try (InputStream is = request.getInputStream()) {
            os = new ByteArrayOutputStream();
            int b;
            while ((b = is.read()) != -1) {
                os.write(b);
            }
        }
        return new String(os.toByteArray());
    }
}
