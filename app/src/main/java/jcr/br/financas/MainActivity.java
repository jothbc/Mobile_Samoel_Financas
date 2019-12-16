package jcr.br.financas;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import jcr.br.financas.WS.WebService;
import jcr.br.financas.funcoes.CDate;
import jcr.br.financas.funcoes.Conv;
import jcr.br.financas.model.Boleto;
import jcr.br.financas.model.Cheque;
import jcr.br.financas.model.Imposto;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static EditText boleto;
    static EditText imposto;
    static EditText cheque;
    static EditText total;

    static ProgressBar pb;
    static GraphView graph;

    GraphAsync graphAsync;
    ValoresAsync valoresAsync;

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
        pb = findViewById(R.id.pbMain);
        graph = findViewById(R.id.graph);
    }

    public class ValoresAsync extends AsyncTask<Void, Void, String> {
        private String b, c, i, t;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            boleto.setText(getString(R.string.message_carregando));
            cheque.setText(getString(R.string.message_carregando));
            imposto.setText(getString(R.string.message_carregando));
            total.setText(getString(R.string.message_carregando));
        }

        @Override
        protected String doInBackground(Void... voids) {
            String boletoString = WebService.get("Boleto/get/valor/aberto");
            String chequeString = WebService.get("Cheque/get/valor/aberto");
            String impostoString = WebService.get("Imposto/get/valor/aberto");
            double valor_aberto = 0;
            if (boletoString != null) {
                valor_aberto += new Gson().fromJson(boletoString, Double.class);
                b = (Conv.colocarPontoEmValor(Conv.validarValue(new Gson().fromJson(boletoString, Double.class))));
            } else {
                b = ("0,00");
            }
            if (chequeString != null) {
                valor_aberto += new Gson().fromJson(chequeString, Double.class);
                c = (Conv.colocarPontoEmValor(Conv.validarValue(new Gson().fromJson(chequeString, Double.class))));
            } else {
                c = ("0,00");
            }
            if (impostoString != null) {
                valor_aberto += new Gson().fromJson(impostoString, Double.class);
                i = (Conv.colocarPontoEmValor(Conv.validarValue(new Gson().fromJson(impostoString, Double.class))));
            } else {
                i = ("0,00");
            }
            t = (Conv.colocarPontoEmValor(Conv.validarValue(valor_aberto)));
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            boleto.setText(b);
            cheque.setText(c);
            imposto.setText(i);
            total.setText(t);
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
        valoresAsync = new ValoresAsync();
        valoresAsync.execute();
        graphAsync = new GraphAsync();
        graphAsync.execute();
    }

    public class GraphAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
            graph.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            atualizarGrafico();
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pb.setVisibility(View.GONE);
            graph.setVisibility(View.VISIBLE);
        }

        public void atualizarGrafico() {
            List<Boleto> boletos = new ArrayList<>();
            List<Cheque> cheques = new ArrayList<>();
            List<Imposto> impostos = new ArrayList<>();
            List<DataPoint> dataPoints = new ArrayList<>();
            String requestBoleto = WebService.get("Boleto/get/historico");
            if (requestBoleto != null) {
                boletos.addAll(Arrays.asList(new Gson().fromJson(requestBoleto, Boleto[].class)));
            }
            String requestCheque = WebService.get("Cheque/get/historico");
            if (requestCheque != null) {
                cheques.addAll(Arrays.asList(new Gson().fromJson(requestCheque, Cheque[].class)));
            }
            String requestImposto = WebService.get("Imposto/get/historico");
            if (requestImposto != null) {
                impostos.addAll(Arrays.asList(new Gson().fromJson(requestImposto, Imposto[].class)));
            }

            Calendar inicio = Calendar.getInstance();
            inicio.add(Calendar.DAY_OF_YEAR, -60);
            Calendar fim = Calendar.getInstance();
            fim.add(Calendar.DAY_OF_YEAR, 61);
            int count = 0;
            while (!new SimpleDateFormat("dd/MM/yyyy").format(inicio.getTime()).equals(new SimpleDateFormat("dd/MM/yyyy").format(fim.getTime()))) {
                Date temp = inicio.getTime();
                String comparacao_data = new SimpleDateFormat("dd/MM/yyyy").format(temp);

                //diferença em dias em relação a hoje
                long dias = CDate.diasRestantes(new SimpleDateFormat("dd/MM/yyyy").format(temp));

                double valor_dia = 0;

                for (Boleto b : boletos) {
                    if (b.getVencimento().equals(comparacao_data)) {
                        valor_dia += b.getValor();
                    }
                }
                for (Cheque c : cheques) {
                    if (c.getPredatado().equals(comparacao_data)) {
                        valor_dia += c.getValor();
                    }
                }
                for (Imposto i : impostos) {
                    if (i.getVencimento().equals(comparacao_data)) {
                        valor_dia += i.getValor();
                    }
                }
                dataPoints.add(new DataPoint((int) dias, (int) valor_dia));
                inicio.add(Calendar.DAY_OF_YEAR, 1);
                count++;
            }

            DataPoint[] dataPointsArray = new DataPoint[count];
            for (int x = 0; x < count; x++) {
                dataPointsArray[x] = dataPoints.get(x);
            }
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPointsArray);
            series.setColor(Color.BLUE);
            series.setTitle("Valor por Dia");
            graph.removeAllSeries();
            graph.addSeries(series);


            graph.getViewport().setMinY(0);

            graph.getViewport().setScrollable(true); // enables horizontal scrolling
            graph.getViewport().setScrollableY(true); // enables vertical scrolling
            graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
            graph.getViewport().setScalableY(true); // enables vertical zooming and scrolling

            graph.getLegendRenderer().setVisible(true);
            graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

            graph.getViewport().scrollToEnd();

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        graphAsync.cancel(true);
        valoresAsync.cancel(true);
    }
}
