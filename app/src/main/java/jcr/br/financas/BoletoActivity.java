package jcr.br.financas;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jcr.br.financas.Adapter.BoletoAdapter;
import jcr.br.financas.WS.WebService;
import jcr.br.financas.funcoes.CDate;
import jcr.br.financas.funcoes.Conv;
import jcr.br.financas.model.Boleto;
import jcr.br.financas.model.DatePickerFragment;
import jcr.br.financas.model.FiltroData;
import jcr.br.financas.model.MyException;

import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BoletoActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private BoletoAdapter boletoAdapter;
    public static FiltroData filtroData;
    private Button editInicial, editFinal;
    private boolean inicial_click, final_click;
    private FloatingActionButton floatingActionButton;
    private static final int DIAS_FILTRO = -7;
    private RecyclerView listBoletos;
    private List<Boleto> boletos;
    private ProgressBar pb;

    private CarregarListAsync carregarListAsync;
    private PagarAsync pagarAsync;
    private ExcluirAsync excluirAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boleto);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setTitle(getString(R.string.title_boleto));
        inicial_click = false;
        final_click = false;
        pb = findViewById(R.id.pbBoletosList);
        editInicial = findViewById(R.id.editBoletoDataInicial);
        editInicial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inicial_click = true;
                final_click = false;
                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getSupportFragmentManager(), "date picker");
            }
        });
        editFinal = findViewById(R.id.editBoletoDataFinal);
        editFinal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inicial_click = false;
                final_click = true;
                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getSupportFragmentManager(), "date picker");
            }
        });

        listBoletos = findViewById(R.id.list_dados);
        listBoletos.setHasFixedSize(true);
        listBoletos.setLayoutManager(new LinearLayoutManager(this));
        boletos = new ArrayList<>();

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.RIGHT) {
                    //pagar
                    pagarBoletoSelecionado(viewHolder.getAdapterPosition());
                } else if (direction == ItemTouchHelper.LEFT) {
                    //excluir
                    excluirBoletoSelecionado(viewHolder.getAdapterPosition());
                }
            }
        }).attachToRecyclerView(listBoletos);

        iniciarDatasFiltroList(DIAS_FILTRO);

        floatingActionButton = findViewById(R.id.floatingActionLancarBoleto);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLancarBoleto(v);
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        if (inicial_click) {
            editInicial.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime()));
        } else if (final_click) {
            editFinal.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime()));
        }
        inicial_click = false;
        final_click = false;
    }

    private void excluirBoletoSelecionado(final int adapterPosition) {
        Boleto b = boletos.get(adapterPosition);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Confirmação")
                .setMessage("Deseja Excluir esse Boleto?\n" + b.getVencimento() + "   " + Conv.colocarPontoEmValor(Conv.validarValue(b.getValor())) + "\n" + b.getFornecedor_id().getNome())
                .setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletar(adapterPosition);
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelarAlteracao(adapterPosition);
                    }
                }).create();
        alertDialog.show();
    }

    private void deletar(int adapterPosition) {
        excluirAsync = new ExcluirAsync(adapterPosition, boletos.get(adapterPosition));
        excluirAsync.execute();
    }

    private void pagarBoletoSelecionado(final int adapterPosition) {
        Boleto b = boletos.get(adapterPosition);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Confirmação")
                .setMessage("Deseja dar Baixa nesse Boleto?\n" + b.getVencimento() + "   " + Conv.colocarPontoEmValor(Conv.validarValue(b.getValor())) + "\n" + b.getFornecedor_id().getNome())
                .setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pagar(adapterPosition);
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelarAlteracao(adapterPosition);
                    }
                }).create();
        alertDialog.show();
    }

    private void cancelarAlteracao(int adapterPosition) {
        preencherList();
        listBoletos.scrollToPosition(adapterPosition);
    }

    private void pagar(int adapterPosition) {
        pagarAsync = new PagarAsync(adapterPosition, boletos.get(adapterPosition));
        pagarAsync.execute();
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
            String ini_temp = editInicial.getText().toString();
            ini_temp = ini_temp.replaceAll("\\.", "/");
            ini_temp = ini_temp.replaceAll("-", "/");
            ini = new SimpleDateFormat("dd/MM/yyyy").parse(ini_temp);
            editInicial.setText(ini_temp);
            filtroData.inicio = ini_temp;
            try {
                String fim_temp = editFinal.getText().toString();
                fim_temp = fim_temp.replaceAll("\\.", "/");
                fim_temp = fim_temp.replaceAll("-", "/");
                fim = new SimpleDateFormat("dd/MM/yyyy").parse(fim_temp);
                if (fim.before(ini)) {
                    throw new Exception(getString(R.string.erro_data_inicial_maior_final));
                }
                filtroData.fim = fim_temp;
                editFinal.setText(fim_temp);
            } catch (ParseException e) {
                editFinal.setText(ini_temp);
                filtroData.fim = ini_temp;
            }
        } catch (ParseException e) {
            throw new Exception(getString(R.string.erro_data_inicial_invalida));
        }
    }

    private void preencherList() {
        TextView txt_valor_aberto = findViewById(R.id.txtBoletoValorListaAberto);
        boletoAdapter = new BoletoAdapter(boletos);
        listBoletos.setAdapter(boletoAdapter);
        double valor_aberto = 0;
        if (!boletos.isEmpty()) {
            for (Boleto b : boletos) {
                if (b.getPago() == null) {
                    valor_aberto += b.getValor();
                }
            }
        }
        txt_valor_aberto.setText(getString(R.string.lbl_valor_em_aberto) + " R$" + Conv.colocarPontoEmValor(Conv.validarValue(valor_aberto)));
    }

    public void initLancarBoleto(View view) {
        Intent lancarBoleto = new Intent(BoletoActivity.this, LancarBoletoActivity.class);
        startActivity(lancarBoleto);
    }

    public void btn_ok_action(View view) {
        try {
            definirDatasFiltroList();
            carregarListAsync = new CarregarListAsync();
            carregarListAsync.execute();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            definirDatasFiltroList();
            carregarListAsync = new CarregarListAsync();
            carregarListAsync.execute();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        carregarListAsync.cancel(true);
    }

    public class CarregarListAsync extends AsyncTask<Void, Void, String> {
        private List<Boleto> boletoList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            String url = "Boleto/get/periodo/";
            String param = filtroData.toString();
            String request = WebService.get(url + param);
            boletoList = new ArrayList<>();
            if (request == null) {
                switch (MyException.code) {
                    case 204:
                        Toast.makeText(getApplicationContext(), R.string.message_request_nulo_vazio, Toast.LENGTH_SHORT).show();
                        break;
                }
            } else {
                boletoList = Arrays.asList(new Gson().fromJson(request, Boleto[].class));
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            boletos = boletoList;
            preencherList();
            pb.setVisibility(View.GONE);
        }
    }

    public class PagarAsync extends AsyncTask<Boleto, Void, String> {

        private int position;
        private Boleto boleto;

        public PagarAsync(int position, Boleto boleto) {
            this.position = position;
            this.boleto = boleto;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Boleto... boletos) {
            String response = WebService.post("Boleto/pagar", new Gson().toJson(boleto), "POST");
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                if (response.equals("true")) {
                    boletos.get(position).setPago(CDate.getHojePTBR());
                    preencherList();
                } else if (response.equals("unmodified")) {
                    preencherList();
                } else {
                    Toast.makeText(getApplicationContext(), "ERRO: " + response, Toast.LENGTH_LONG).show();
                    preencherList();
                }
            } else {
                Toast.makeText(getApplicationContext(), "ERRO: " + String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
                preencherList();
            }
            if (position >= 0 && position < boletos.size() && boletos.size() > 0)
                listBoletos.scrollToPosition(position);

            pb.setVisibility(View.GONE);
        }
    }

    public class ExcluirAsync extends AsyncTask<Boleto, Void, String> {

        private int position;
        private Boleto boleto;

        public ExcluirAsync(int position, Boleto boleto) {
            this.position = position;
            this.boleto = boleto;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Boleto... boletos) {
            String response = WebService.post("Boleto/excluir", new Gson().toJson(boleto), "POST");
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                if (response.equals("true")) {
                    boletos.remove(position);
                    preencherList();
                } else {
                    Toast.makeText(getApplicationContext(), "ERRO: " + response, Toast.LENGTH_LONG).show();
                    preencherList();
                }
            } else {
                Toast.makeText(getApplicationContext(), "ERRO: " + String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
                preencherList();
            }
            if (position >= 0 && position < boletos.size() && boletos.size() > 0)
                listBoletos.scrollToPosition(position);
            pb.setVisibility(View.GONE);
        }
    }

}
