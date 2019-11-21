package jcr.br.financas;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jcr.br.financas.WS.HTTPService;
import jcr.br.financas.WS.send;
import jcr.br.financas.funcoes.CDate;
import jcr.br.financas.funcoes.CDbl;
import jcr.br.financas.funcoes.Conv;
import jcr.br.financas.model.FiltroData;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    static EditText boleto;
    static EditText imposto;
    static EditText cheque;
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
        atualizarValores();

    }

    private void atualizarValores() {
        try {
            String boletoString = new HTTPService("Boleto/get/valor/aberto", "").execute().get();
            String chequeString = new HTTPService("Cheque/get/valor/aberto", "").execute().get();
            String impostoString = new HTTPService("Imposto/get/valor/aberto", "").execute().get();
            boleto.setText(boletoString != null ? Conv.colocarPontoEmValor(Conv.validarValue(new Gson().fromJson(boletoString, Double.class))) : "0,00");
            cheque.setText(chequeString != null ? Conv.colocarPontoEmValor(Conv.validarValue(new Gson().fromJson(chequeString, Double.class))) : "0,00");
            imposto.setText(impostoString != null ? Conv.colocarPontoEmValor(Conv.validarValue(new Gson().fromJson(impostoString, Double.class))) : "0,00");
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
