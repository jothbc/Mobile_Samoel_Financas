package jcr.br.financas.WS;

import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import jcr.br.financas.model.MyException;

public class HTTPServicePost extends AsyncTask<String, Void, String> {
    private final String base = "http://192.168.1.158:9999/mercadows/webresources/ws/";
    private String caminho;
    private String metodo;
    private String json;

    public HTTPServicePost(String json, String caminho, String metodo) {
        this.json = json;
        this.caminho = caminho;
        this.metodo = metodo;
    }

    @Override
    protected String doInBackground(String... params) {
        return sendPost(base + caminho, json, metodo);
    }

    private String sendPost(String url, String json, String metodo) {
        try {
            // Cria um objeto HttpURLConnection:
            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();

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

                // Caso você queira usar o código HTTP para fazer alguma coisa, descomente esta linha.
                //int response = request.getResponseCode();
                MyException.code = request.getResponseCode();
                if (request.getResponseCode() / 100 == 2) {
                    return readResponse(request);
                }else{
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

    private String readResponse(HttpURLConnection request) throws IOException {
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

    @Deprecated
    public static class MinhaException extends Exception {

        private static final long serialVersionUID = 1L;

        public MinhaException(Throwable cause) {
            super(cause);
        }
    }


}
