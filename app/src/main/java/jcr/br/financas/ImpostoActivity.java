package jcr.br.financas;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jcr.br.financas.Adapter.ImpostoAdapter;
import jcr.br.financas.WS.WebService;
import jcr.br.financas.funcoes.Conv;
import jcr.br.financas.model.Imposto;
import jcr.br.financas.model.MyException;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImpostoActivity extends AppCompatActivity {
    private List<Imposto> impostoList;
    private RecyclerView recyclerView;
    private ImpostoAdapter impostoAdapter;
    private TextView lbTotalAberto;
    private RadioGroup radioGroup;
    private ProgressBar pb;
    private CarregarListAsync carregarListAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imposto);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        impostoList = new ArrayList<>();
        pb = findViewById(R.id.pbImpostoList);
        recyclerView = findViewById(R.id.rvImpostos);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        lbTotalAberto = findViewById(R.id.textImpostoTotalAberto);
        radioGroup = findViewById(R.id.rgImpostos);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                new CarregarListAsync().execute();
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (ItemTouchHelper.LEFT == direction) {
                    //excluir
                    excluirSelecionado(viewHolder.getAdapterPosition());
                } else if (ItemTouchHelper.RIGHT == direction) {
                    //pagar
                    pagarSelecionado(viewHolder.getAdapterPosition());
                }
            }
        }).attachToRecyclerView(recyclerView);

        FloatingActionButton floatingActionButton = findViewById(R.id.fabImpostoAddNovo);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cadImposto = new Intent(v.getContext(), LancarImpostoActivity.class);
                startActivity(cadImposto);
            }
        });
    }

    private void pagarSelecionado(final int adapterPosition) {
        Imposto imposto = impostoList.get(adapterPosition);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Confirmação")
                .setMessage("Deseja dar Baixa nesse Imposto?\n" + imposto.getDescricao() + "\n" +
                        Conv.colocarPontoEmValor(Conv.validarValue(imposto.getValor())))
                .setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pagarConfirmado(adapterPosition);
                    }
                }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        refreshRV(adapterPosition);
                    }
                }).create();
        alertDialog.show();
    }

    private void refreshRV(int adapterPosition) {
        preencherRV();
        try {
            recyclerView.scrollToPosition(adapterPosition);
        } catch (Exception e) {
            System.err.println("Posição não existe no recyclerView " + String.valueOf(adapterPosition));
        }
    }

    private void pagarConfirmado(int adapterPosition) {
        Imposto imposto = impostoList.get(adapterPosition);
        new AlterarImpostoAsync("Imposto/pagar/", imposto, adapterPosition).execute();
    }

    private void excluirConfirmado(int adapterPosition) {
        Imposto imposto = impostoList.get(adapterPosition);
        new AlterarImpostoAsync("Imposto/remover/", imposto, adapterPosition).execute();
    }

    public class AlterarImpostoAsync extends AsyncTask<Void, Void, String> {

        private String caminho;
        private Imposto imposto;
        private int adapterPosition;

        public AlterarImpostoAsync(String caminho, Imposto imposto, int adapterPosition) {
            this.caminho = caminho;
            this.imposto = imposto;
            this.adapterPosition = adapterPosition;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String response = WebService.post(caminho, new Gson().toJson(imposto), "POST");
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                if (response.equals("true")) {
                    impostoList.remove(adapterPosition);
                } else {
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "ERRO: " + String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
            }
            refreshRV(adapterPosition);
        }
    }

    private void excluirSelecionado(final int adapterPosition) {
        Imposto imposto = impostoList.get(adapterPosition);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Confirmação")
                .setMessage("Deseja Excluir esse Imposto?\n" + imposto.getDescricao() + "\n" +
                        Conv.colocarPontoEmValor(Conv.validarValue(imposto.getValor())))
                .setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        excluirConfirmado(adapterPosition);
                    }
                }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        refreshRV(adapterPosition);
                    }
                }).create();
        alertDialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        carregarListAsync = new CarregarListAsync();
        carregarListAsync.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        carregarListAsync.cancel(true);
    }

    public class CarregarListAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            String param = "";
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.rbImpostoAberto:
                    param = "aberto";
                    break;
                case R.id.rbImpostoTodos:
                    param = "todos";
                    break;
            }
            String request = WebService.get("Imposto/get/" + param);

            return request;
        }

        @Override
        protected void onPostExecute(String request) {
            super.onPostExecute(request);
            impostoList = new ArrayList<>();
            if (request != null) {
                try {
                    Imposto[] imp = new Gson().fromJson(request, Imposto[].class);
                    impostoList.addAll(Arrays.asList(imp));
                } catch (JsonSyntaxException e) {
                    Toast.makeText(getApplicationContext(), request, Toast.LENGTH_LONG).show();
                    impostoList = new ArrayList<>();
                }
            } else {
                Toast.makeText(getApplicationContext(), "ERRO: " + String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
                impostoList = new ArrayList<>();
            }
            preencherRV();
            pb.setVisibility(View.GONE);
        }
    }

    private void preencherRV() {
        impostoAdapter = new ImpostoAdapter(impostoList);
        recyclerView.setAdapter(impostoAdapter);
        double valor = 0;
        for (Imposto i : impostoList) {
            if (i.getPago() == null) {
                valor += i.getValor();
            }
        }
        lbTotalAberto.setText(getString(R.string.lbl_valor_em_aberto) + " R$" + Conv.colocarPontoEmValor(Conv.validarValue(valor)));
    }
}
