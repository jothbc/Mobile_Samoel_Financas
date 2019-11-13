package jcr.br.financas;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jcr.br.financas.WS.HTTPService;
import jcr.br.financas.funcoes.CDate;
import jcr.br.financas.model.FiltroData;

import android.view.View;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BoletoActivity extends AppCompatActivity {
    private RecyclerView listBoletos;
    private BoletoAdapter boletoAdapter;
    private List<Boleto> boletos;
    public static FiltroData filtroData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boleto);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle(getString(R.string.title_boleto));

        definirDatasFiltroList();
        carregarList();

        FloatingActionButton floatingActionLancarBoleto = findViewById(R.id.floatingActionLancarBoleto);
        floatingActionLancarBoleto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent lancarBoleto= new Intent(BoletoActivity.this,LancarBoletoActivity.class);
                startActivity(lancarBoleto);
            }
        });
    }

    private void definirDatasFiltroList() {
        filtroData = new FiltroData();
        filtroData.inicio = CDate.incrementarMes(-3, CDate.getHojePTBR());
        filtroData.fim = CDate.getHojePTBR();
    }

    private void carregarList() {
        String url = "Boleto/get/periodo/";
        String param = filtroData.toString();
        String request = null;
        try {
            HTTPService service = new HTTPService(url, param);
            request = service.execute().get();
            boletos = Arrays.asList(new Gson().fromJson(request, Boleto[].class));

            listBoletos = findViewById(R.id.list_dados);
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

    public void scanCodigo(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setCameraId(0);
        integrator.setPrompt("Scan Boleto");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                //codigo esta aqui result.getContents();
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
