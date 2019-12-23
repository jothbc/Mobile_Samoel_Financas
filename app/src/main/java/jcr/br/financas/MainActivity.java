package jcr.br.financas;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import jcr.br.financas.WS.WebService;
import jcr.br.financas.funcoes.CDate;
import jcr.br.financas.funcoes.Conv;
import jcr.br.financas.model.Boleto;
import jcr.br.financas.model.Cheque;
import jcr.br.financas.model.DatePickerFragment;
import jcr.br.financas.model.Imposto;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private EditText boleto;
    private EditText imposto;
    private EditText cheque;
    private EditText total;

    private ProgressBar pb;
    private GraphView graph;

    private GraphAsync graphAsync;
    private ValoresAsync valoresAsync;
    private CarregarListAsync carregarListAsync;

    private static final int DIA = 1;
    private static final int MES = 2;

    private Button dia_btn, mes_btn;
    private Button inicio_btn, fim_btn;
    private boolean inicio_btn_select, fim_btn_select;

    private List<Boleto> boletos;
    private List<Cheque> cheques;
    private List<Imposto> impostos;

    private static TextView txtPb;

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
        txtPb = findViewById(R.id.txtMainPb);
        graph = findViewById(R.id.graph);
        dia_btn = findViewById(R.id.btn_dia_graph);
        dia_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GraphAsync(DIA).execute();
            }
        });
        mes_btn = findViewById(R.id.btn_mes_graph);
        mes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GraphAsync(MES).execute();
            }
        });
        inicio_btn = findViewById(R.id.btn_main_data_inicio);
        inicio_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inicio_btn_select = true;
                fim_btn_select = false;
                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getSupportFragmentManager(), "date picker");
            }
        });
        fim_btn = findViewById(R.id.btn_main_data_fim);
        fim_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fim_btn_select = true;
                inicio_btn_select = false;
                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getSupportFragmentManager(), "date picker");
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        if (inicio_btn_select) {
            inicio_btn.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime()));
        } else if (fim_btn_select) {
            fim_btn.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime()));
        }
        inicio_btn_select = false;
        fim_btn_select = false;
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
        carregarListAsync = new CarregarListAsync();
        carregarListAsync.execute();
    }

    public class CarregarListAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
            txtPb.setVisibility(View.VISIBLE);
            txtPb.setText("Carregando...");
            graph.setVisibility(View.GONE);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -3);
            inicio_btn.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime()));
            calendar.add(Calendar.MONTH, 6);
            fim_btn.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime()));
        }

        @Override
        protected String doInBackground(Void... voids) {
            boletos = new ArrayList<>();
            cheques = new ArrayList<>();
            impostos = new ArrayList<>();
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
            return "concluido";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            graphAsync = new GraphAsync(DIA);
            graphAsync.execute();
        }
    }

    public class GraphAsync extends AsyncTask<Void, Void, String> {

        int op;

        public GraphAsync(int op) {
            this.op = op;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
            graph.setVisibility(View.GONE);
            txtPb.setVisibility(View.VISIBLE);
            txtPb.setText("Carregando...");
        }

        @Override
        protected String doInBackground(Void... voids) {
            atualizarGraficoDia();
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pb.setVisibility(View.GONE);
            txtPb.setVisibility(View.GONE);
            graph.setVisibility(View.VISIBLE);
        }

        public void atualizarGraficoDia() {
            List<DataPoint> dataPoints = new ArrayList<>();
            Calendar inicio = Calendar.getInstance();
            Calendar fim = Calendar.getInstance();
            try {
                inicio.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(inicio_btn.getText().toString()));
                fim.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(fim_btn.getText().toString()));
            } catch (ParseException e) {
            }
            int count = 0;
            if (op == DIA) {
                while (!new SimpleDateFormat("dd/MM/yyyy").format(inicio.getTime()).equals(new SimpleDateFormat("dd/MM/yyyy").format(fim.getTime()))) {
                    Date temp = inicio.getTime();
                    String comparacao_data = new SimpleDateFormat("dd/MM/yyyy").format(temp);
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
            } else if (op == MES) {
                Calendar hoje = Calendar.getInstance();
                while (inicio.before(fim)) {
                    double valor = 0;
                    Calendar atual = Calendar.getInstance();
                    boolean proximo = false;
                    for (Boleto b : boletos) {
                        try {
                            atual.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(b.getVencimento()));
                            if (atual.get(Calendar.MONTH) == inicio.get(Calendar.MONTH) && atual.get(Calendar.YEAR) == inicio.get(Calendar.YEAR)) {
                                valor += b.getValor();
                                proximo = true;
                            } else if (proximo) {
                                break;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    proximo = false;
                    for (Cheque c : cheques) {
                        try {
                            atual.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(c.getPredatado()));
                            if (atual.get(Calendar.MONTH) == inicio.get(Calendar.MONTH) && atual.get(Calendar.YEAR) == inicio.get(Calendar.YEAR)) {
                                valor += c.getValor();
                                proximo = true;
                            } else if (proximo) {
                                break;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    proximo = false;
                    for (Imposto i : impostos) {
                        try {
                            atual.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(i.getVencimento()));
                            if (atual.get(Calendar.MONTH) == inicio.get(Calendar.MONTH) && atual.get(Calendar.YEAR) == inicio.get(Calendar.YEAR)) {
                                valor += i.getValor();
                                proximo = true;
                            } else if (proximo) {
                                break;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    int y = inicio.get(Calendar.MONTH) - hoje.get(Calendar.MONTH);
                    if (inicio.get(Calendar.YEAR) > hoje.get(Calendar.YEAR)) {
                        y += 12;
                    }
                    dataPoints.add(new DataPoint(y, (int) valor));
                    inicio.add(Calendar.DAY_OF_YEAR, 1);
                    count++;
                }
            }

            DataPoint[] dataPointsArray = new DataPoint[count];
            for (int x = 0; x < count; x++) {
                dataPointsArray[x] = dataPoints.get(x);
            }
            //LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPointsArray);
            BarGraphSeries series = new BarGraphSeries(dataPointsArray);
            series.setColor(Color.BLUE);
            series.setTitle(op == DIA ? "Valor por Dia" : "Valor por Mês");


            graph.removeAllSeries();
            graph.addSeries(series);
            if (op == MES) {
                graph.getViewport().setMinX(-4);
                graph.getViewport().setMaxX(4);
            }
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
        carregarListAsync.cancel(true);
    }
}
