package jcr.br.financas;

import android.content.DialogInterface;
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
import jcr.br.financas.funcoes.Conv;
import jcr.br.financas.model.Imposto;
import jcr.br.financas.model.MyException;

import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imposto);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        impostoList = new ArrayList<>();
        recyclerView = findViewById(R.id.rvImpostos);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        lbTotalAberto = findViewById(R.id.textImpostoTotalAberto);
        radioGroup = findViewById(R.id.rgImpostos);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                carregarList();
                preencherRV();
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
            String response = new HTTPServicePost(new Gson().toJson(imposto), "Imposto/pagar", "POST").execute().get();
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
        Imposto imposto = impostoList.get(adapterPosition);
        try {
            String response = new HTTPServicePost(new Gson().toJson(imposto), "Imposto/remover", "POST").execute().get();
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
        carregarList();
        preencherRV();
    }

    private void carregarList() {
        impostoList = new ArrayList<>();
        String param = "";
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.rbImpostoAberto:
                param = "aberto";
                break;
            case R.id.rbImpostoTodos:
                param = "todos";
                break;
        }
        try {
            String request = new HTTPService("Imposto/get/", param, "GET").execute().get();
            if (request != null) {
                try {
                    Imposto[] imp = new Gson().fromJson(request, Imposto[].class);
                    impostoList.addAll(Arrays.asList(imp));
                } catch (JsonSyntaxException e) {
                    Toast.makeText(this, request, Toast.LENGTH_LONG).show();
                    impostoList = new ArrayList<>();
                }
            } else {
                Toast.makeText(this, "ERRO: " + String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
                impostoList = new ArrayList<>();
            }
        } catch (InterruptedException | ExecutionException | MalformedURLException e) {
            e.printStackTrace();
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
