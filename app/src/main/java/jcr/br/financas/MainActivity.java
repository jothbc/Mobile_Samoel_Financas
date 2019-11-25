package jcr.br.financas;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import jcr.br.financas.WS.HTTPService;
import jcr.br.financas.WS.send;
import jcr.br.financas.funcoes.Conv;
import jcr.br.financas.model.MyException;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    static EditText boleto;
    static EditText imposto;
    static EditText cheque;
    static EditText total;
    static String url_base = "http://187.4.229.36:9999/mercadows/webresources/ws/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        boleto = findViewById(R.id.editBoletoValorAberto);
        imposto = findViewById(R.id.editImpostoValorAberto);
        cheque = findViewById(R.id.editChequeValorAberto);
        total = findViewById(R.id.editTotalValorAberto);
        atualizarValores();

    }

    private void atualizarValores() {
        try {
            String boletoString = new HTTPService("Boleto/get/valor/aberto", "").execute().get();
            String chequeString = new HTTPService("Cheque/get/valor/aberto", "").execute().get();
            String impostoString = new HTTPService("Imposto/get/valor/aberto", "").execute().get();
            double valor_aberto = 0;
            if (boletoString != null) {
                valor_aberto += new Gson().fromJson(boletoString, Double.class);
                boleto.setText(Conv.colocarPontoEmValor(Conv.validarValue(new Gson().fromJson(boletoString, Double.class))));
            } else {
                boleto.setText("0,00");
            }
            if (chequeString != null) {
                valor_aberto += new Gson().fromJson(chequeString, Double.class);
                cheque.setText(Conv.colocarPontoEmValor(Conv.validarValue(new Gson().fromJson(chequeString, Double.class))));
            } else {
                cheque.setText("0,00");
            }
            if (impostoString != null) {
                valor_aberto += new Gson().fromJson(chequeString, Double.class);
                imposto.setText(Conv.colocarPontoEmValor(Conv.validarValue(new Gson().fromJson(chequeString, Double.class))));
            } else {
                imposto.setText("0,00");
            }
            total.setText(Conv.colocarPontoEmValor(Conv.validarValue(valor_aberto)));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_Boleto:
                Intent actBoleto = new Intent(MainActivity.this, BoletoActivity.class);
                startActivity(actBoleto);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

}
