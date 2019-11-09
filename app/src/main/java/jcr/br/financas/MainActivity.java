package jcr.br.financas;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jcr.br.financas.WS.HTTPService;
import jcr.br.financas.WS.wsGET;
import jcr.br.financas.funcoes.CDate;
import jcr.br.financas.model.FiltroData;

import android.os.StrictMode;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private RecyclerView listBoletos;
    private BoletoAdapter boletoAdapter;
    private List<Boleto> boletos;
    public static FiltroData filtroData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        definirDatasFiltroList();
        carregarList();

    }

    private void definirDatasFiltroList() {
        filtroData = new FiltroData();
        filtroData.inicio = CDate.incrementarMes(-3, CDate.getHojePTBR());
        filtroData.fim = CDate.getHojePTBR();
    }

    private void carregarList() {
        String url = "http://187.4.229.36:9999/mercadows/webresources/ws/Boleto/get/periodo/";
        String param = filtroData.toString();
        String request = null;
        try {
            //request = wsGET.sendGet(url);
            HTTPService service = new HTTPService(url, param);
            request = service.execute(url, param).get();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
