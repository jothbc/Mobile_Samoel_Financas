package jcr.br.financas;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
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
import jcr.br.financas.WS.HTTPService;
import jcr.br.financas.WS.HTTPServicePost;
import jcr.br.financas.WS.WebService;
import jcr.br.financas.funcoes.Conv;
import jcr.br.financas.model.Imposto;
import jcr.br.financas.model.MyException;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ImpostoActivity extends AppCompatActivity {
    private List<Imposto> impostoList;
    private RecyclerView recyclerView;
    private ImpostoAdapter impostoAdapter;
    private TextView lbTotalAberto;
    private RadioGroup radioGroup;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imposto);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        impostoList = new ArrayList<>();
        pb =findViewById(R.id.pbImpostoList);
        recyclerView = findViewById(R.id.rvImpostos);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        lbTotalAberto = findViewById(R.id.textImpostoTotalAberto);
        radioGroup = findViewById(R.id.rgImpostos);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                new assyncCarregarList().execute();
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
        try {
            String response = new HTTPServicePost(new Gson().toJson(imposto), "Imposto/pagar/", "POST").execute().get();
            if (response != null) {
                if (response.equals("true")) {
                    impostoList.remove(adapterPosition);
                } else {
                    Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "ERRO: " + String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        refreshRV(adapterPosition);
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

    private void excluirConfirmado(int adapterPosition) {
        try {
            Imposto imposto = impostoList.get(adapterPosition);
            String response = new HTTPServicePost(new Gson().toJson(imposto), "Imposto/remover/", "POST").execute().get();
            if (response != null) {
                if (response.equals("true")) {
                    impostoList.remove(adapterPosition);
                } else {
                    Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "ERRO: " + String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        refreshRV(adapterPosition);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new assyncCarregarList().execute();
    }

    public class assyncCarregarList extends AsyncTask<Void,Void,String>{
        List<Imposto> impostos;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            impostos = new ArrayList<>();
            String param = "";
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.rbImpostoAberto:
                    param = "aberto";
                    break;
                case R.id.rbImpostoTodos:
                    param = "todos";
                    break;
            }
            String request = WebService.get("Imposto/get/"+param);//new HTTPService("Imposto/get/", param, "GET").execute().get();
            if (request != null) {
                try {
                    Imposto[] imp = new Gson().fromJson(request, Imposto[].class);
                    impostos.addAll(Arrays.asList(imp));
                } catch (JsonSyntaxException e) {
                    Toast.makeText(getApplicationContext(), request, Toast.LENGTH_LONG).show();
                    impostos = new ArrayList<>();
                }
            } else {
                Toast.makeText(getApplicationContext(), "ERRO: " + String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
                impostos = new ArrayList<>();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            impostoList = impostos;
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
