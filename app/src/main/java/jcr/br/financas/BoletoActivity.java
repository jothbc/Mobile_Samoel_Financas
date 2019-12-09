package jcr.br.financas;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jcr.br.financas.Adapter.BoletoAdapter;
import jcr.br.financas.WS.HTTPService;
import jcr.br.financas.WS.HTTPServicePost;
import jcr.br.financas.funcoes.CDate;
import jcr.br.financas.funcoes.Conv;
import jcr.br.financas.model.Boleto;
import jcr.br.financas.model.FiltroData;
import jcr.br.financas.model.MyException;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
    private static final int DIAS_FILTRO = -7;
    private RecyclerView listBoletos;
    private List<Boleto> boletos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boleto);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle(getString(R.string.title_boleto));

        editInicial = findViewById(R.id.editBoletoDataInicial);
        editFinal = findViewById(R.id.editBoletoDataFinal);
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

    private void excluirBoletoSelecionado(final int adapterPosition) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Confirmação")
                .setMessage("Deseja Excluir esse Boleto?")
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
        Boleto boleto = boletos.get(adapterPosition);
        try {
            String response = new HTTPServicePost(new Gson().toJson(boleto), "Boleto/excluir", "POST").execute().get();
            if (response != null) {
                if (response.equals("true")) {
                    boletos.remove(adapterPosition);
                    preencherList();
                } else {
                    Toast.makeText(this, "ERRO: " + response, Toast.LENGTH_LONG).show();
                    preencherList();
                }
            } else {
                Toast.makeText(this, "ERRO: " + String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
                preencherList();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (adapterPosition >= 0 && adapterPosition < boletos.size() && boletos.size() > 0)
            listBoletos.scrollToPosition(adapterPosition);
    }

    private void pagarBoletoSelecionado(final int adapterPosition) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Confirmação")
                .setMessage("Deseja dar Baixa nesse Boleto?")
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
        Boleto boleto = boletos.get(adapterPosition);
        try {
            String response = new HTTPServicePost(new Gson().toJson(boleto), "Boleto/pagar", "POST").execute().get();
            if (response != null) {
                if (response.equals("true")) {
                    for (Boleto boleto1 : boletos) {
                        if (boleto1.equals(boleto)) {
                            boleto1.setPago(CDate.getHojePTBR());
                            break;
                        }
                    }
                    //boletos.remove(adapterPosition);
                    preencherList();
                } else {
                    Toast.makeText(this, "ERRO: " + response, Toast.LENGTH_LONG).show();
                    preencherList();
                }
            } else {
                Toast.makeText(this, "ERRO: " + String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
                preencherList();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (adapterPosition >= 0 && adapterPosition < boletos.size() && boletos.size() > 0)
            listBoletos.scrollToPosition(adapterPosition);
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

    private void carregarList() {
        try {
            String url = "Boleto/get/periodo/";
            String param = filtroData.toString();
            HTTPService service = new HTTPService(url, param, "GET");
            String request = service.execute().get();
            boletos = new ArrayList<>();
            if (request == null) {
                switch (MyException.code) {
                    case 204:
                        Toast.makeText(this, R.string.message_request_nulo_vazio, Toast.LENGTH_SHORT).show();
                        break;
                }
            } else {
                boletos = Arrays.asList(new Gson().fromJson(request, Boleto[].class));
            }
            preencherList();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
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
            carregarList();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            definirDatasFiltroList();
            carregarList();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
