package jcr.br.financas.WS;

import android.os.AsyncTask;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HTTPService extends AsyncTask<String,Void, String> {
    private final String codigo;
    private final URL url;
    private final String base = "http://187.4.229.36:9999/mercadows/webresources/ws/";

    public HTTPService(String url, String parametro) throws MalformedURLException {
        this.codigo = parametro;
        this.url = new URL(base + url + parametro);
    }

    @Override
    protected String doInBackground(String... parametros) {
        StringBuilder resposta = new StringBuilder();

        try {
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod("GET");
            request.setRequestProperty("Accept", "application/json");
            request.setConnectTimeout(3000);
            request.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                resposta.append(inputLine);
            }
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return resposta.toString();
    }
}
