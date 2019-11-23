package jcr.br.financas;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jcr.br.financas.Adapter.BoletoAdapter;
import jcr.br.financas.WS.HTTPService;
import jcr.br.financas.funcoes.CDate;
import jcr.br.financas.model.Boleto;
import jcr.br.financas.model.FiltroData;
import jcr.br.financas.model.MyException;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BoletoActivity extends AppCompatActivity {
    private BoletoAdapter boletoAdapter;
    public static FiltroData filtroData;
    private EditText editInicial, editFinal;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boleto);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle(getString(R.string.title_boleto));
        editInicial = findViewById(R.id.editBoletoDataInicial);
        editFinal = findViewById(R.id.editBoletoDataFinal);

        iniciarDatasFiltroList(-10);
        try {
            definirDatasFiltroList();
            carregarList();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        floatingActionButton = findViewById(R.id.floatingActionLancarBoleto);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLancarBoleto(v);
            }
        });
        floatingActionButton.requestFocus();
    }

    private void iniciarDatasFiltroList(int diasInicial) {
        filtroData = new FiltroData();
        filtroData.inicio = CDate.incrementarDias(diasInicial, CDate.getHojePTBR());
        filtroData.fim = CDate.getHojePTBR();
        editInicial.setText(filtroData.inicio);
        editFinal.setText(filtroData.fim);
    }

    private void definirDatasFiltroList() throws Exception {
        try {
            Date ini, fim;
            ini = new SimpleDateFormat("dd/MM/yyyy").parse(editInicial.getText().toString());
            filtroData.inicio = editInicial.getText().toString();
            try {
                fim = new SimpleDateFormat("dd/MM/yyyy").parse(editFinal.getText().toString());
                if(fim.before(ini)){
                    throw new Exception(getString(R.string.erro_data_inicial_maior_final));
                }
                filtroData.fim = editFinal.getText().toString();
            } catch (ParseException e) {
                editFinal.setText(editInicial.getText());
                filtroData.fim = editInicial.getText().toString();
            }
        } catch (ParseException e) {
            throw new Exception(getString(R.string.erro_data_inicial_invalida));
        }
    }

    private void carregarList() {
        try {
            String url = "Boleto/get/periodo/";
            String param = filtroData.toString();
            HTTPService service = new HTTPService(url, param);
            String request = service.execute().get();
            List<Boleto> boletos = new ArrayList<>();
            if (request == null) {
                switch (MyException.code) {
                    case 204:
                        Toast.makeText(this, R.string.message_request_nulo_vazio, Toast.LENGTH_SHORT).show();
                        break;
                }
            } else {
                boletos = Arrays.asList(new Gson().fromJson(request, Boleto[].class));
            }
            RecyclerView listBoletos = findViewById(R.id.list_dados);
            listBoletos.setHasFixedSize(true);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

            listBoletos.setLayoutManager(linearLayoutManager);

            boletoAdapter = new BoletoAdapter(boletos);
            listBoletos.setAdapter(boletoAdapter);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void initLancarBoleto(View view) {
        Intent lancarBoleto = new Intent(BoletoActivity.this, LancarBoletoActivity.class);
        startActivity(lancarBoleto);
    }

    public void btn_ok_action(View view) {
        try {
            definirDatasFiltroList();
            carregarList();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        floatingActionButton.requestFocus();
    }
}
