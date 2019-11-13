package jcr.br.financas.WS;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import jcr.br.financas.Boleto;

public class HTTPServicePost extends AsyncTask<Boleto, Void, String> {
    private final Boleto boleto;

    public HTTPServicePost(Boleto boleto) {
        this.boleto = boleto;
    }

    @Override
    protected String doInBackground(Boleto... boletos) {
         return sendPost("http://192.168.1.158:9999/mercadows/webresources/ws/Produto/post", new Gson().toJson(boleto), "POST");
    }

    private String sendPost(String url, String json,String metodo) {
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
                return readResponse(request);
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