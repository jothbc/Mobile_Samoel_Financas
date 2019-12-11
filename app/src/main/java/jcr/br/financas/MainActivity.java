package jcr.br.financas;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import jcr.br.financas.WS.HTTPService;
import jcr.br.financas.WS.send;
import jcr.br.financas.funcoes.CDate;
import jcr.br.financas.funcoes.Conv;
import jcr.br.financas.model.Boleto;
import jcr.br.financas.model.MyException;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    }

    private void atualizarValores() {
        try {
            String boletoString = new HTTPService("Boleto/get/valor/aberto", "", "GET").execute().get();
            String chequeString = new HTTPService("Cheque/get/valor/aberto", "", "GET").execute().get();
            String impostoString = new HTTPService("Imposto/get/valor/aberto", "", "GET").execute().get();
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
                valor_aberto += new Gson().fromJson(impostoString, Double.class);
                imposto.setText(Conv.colocarPontoEmValor(Conv.validarValue(new Gson().fromJson(impostoString, Double.class))));
            } else {
                imposto.setText("0,00");
            }
            total.setText(Conv.colocarPontoEmValor(Conv.validarValue(valor_aberto)));
        } catch (MalformedURLException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                Intent actBoleto = new Intent(this, BoletoActivity.class);
                startActivity(actBoleto);
                break;
            case R.id.action_Cheque:
                Intent actCheque = new Intent(this, ChequeActivity.class);
                startActivity(actCheque);
                break;
            case R.id.action_Imposto:
                Intent actImposto = new Intent(this, ImpostoActivity.class);
                startActivity(actImposto);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarValores();
        atualizarGrafico();
    }

    private void atualizarGrafico() {
        GraphView graph = findViewById(R.id.graph);
        Boleto[] boletos_array = getBoletos().toArray(new Boleto[0]);
        DataPoint[] points = new DataPoint[boletos_array.length];
        double somaDia = 0;
        Date data = null;
        int count=0;
        for (int x = 0; x < boletos_array.length; x++) {
            try {
                if (x == 0) {
                    data = new SimpleDateFormat("yyyy-MM-dd").parse(boletos_array[x].getVencimento());
                }
                Date data2 = new SimpleDateFormat("yyyy-MM-dd").parse(boletos_array[x].getVencimento());
                if (x + 1 == boletos_array.length) {
                    points[count] = (new DataPoint(data, somaDia));
                    count++;
                } else if (data.equals(data2)) {
                    somaDia += boletos_array[x].getValor();
                } else {
                    points[count] = (new DataPoint(data, somaDia));
                    count++;
                    data = data2;
                    somaDia = 0;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        DataPoint[] dataPoints = new DataPoint[count];
        for (int i = 0; i < dataPoints.length; i++) {
            dataPoints[i] = points[i];
        }

        LineGraphSeries<DataPoint> seriesBoletos = new LineGraphSeries<>(dataPoints);
        seriesBoletos.setColor(Color.RED);
        seriesBoletos.setTitle("Boletos");

        graph.addSeries(seriesBoletos);

        graph.getViewport().setMinY(0);

        graph.getViewport().setScrollable(true); // enables horizontal scrolling
        graph.getViewport().setScrollableY(true); // enables vertical scrolling
        graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        graph.getViewport().setScalableY(true); // enables vertical zooming and scrolling


        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graph.getViewport().scrollToEnd();
    }


    private List<Boleto> getBoletos() {
        List<Boleto> boletos = new ArrayList<>();
        try {
            String request = new HTTPService("Boleto/get/historico", "", "GET").execute().get();
            if (request != null) {
                try {
                    Boleto[] barray = new Gson().fromJson(request, Boleto[].class);
                    boletos.addAll(Arrays.asList(barray));
                } catch (JsonSyntaxException e) {
                    System.err.println("ERRO AO TENTAR FAZER O PARSE DO BOLETO GET HISTORICO");
                }
            }
        } catch (InterruptedException | ExecutionException | MalformedURLException e) {
            e.printStackTrace();
        }
        return boletos;
    }
}
