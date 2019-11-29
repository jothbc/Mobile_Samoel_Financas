package jcr.br.financas;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jcr.br.financas.Adapter.ChequeAdapter;
import jcr.br.financas.WS.HTTPService;
import jcr.br.financas.WS.HTTPServicePost;
import jcr.br.financas.model.Cheque;
import jcr.br.financas.model.MyException;

import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ChequeActivity extends AppCompatActivity {
    private RadioGroup radioGroup;
    private static List<Cheque> chequeList;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheque);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.list_cheques_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        chequeList = new ArrayList<>();
        radioGroup = findViewById(R.id.radioGroupCheque);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                atualizarChequeList();
                preencherRecycleView();
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                baixarChequeSelecionado(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

        FloatingActionButton btnAddCheque = findViewById(R.id.floatingActionButtonChequeAdd);
        btnAddCheque.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarLancarProduto();
            }
        });

    }

    private void iniciarLancarProduto() {
        Intent actLancarCheque = new Intent(this, LancarChequeActivity.class);
        startActivity(actLancarCheque);
    }

    private void baixarChequeSelecionado(int adapterPosition) {
        Cheque cheque = chequeList.get(adapterPosition);
        if (cheque.getSaque() != null || cheque.getEmissao() == null) {
            Toast.makeText(this, (R.string.message_erro_nao_possivel_baixar_cheque), Toast.LENGTH_LONG).show();
            preencherRecycleView();
            return;
        }
        String response = null;
        try {
            response = new HTTPServicePost(String.valueOf(cheque.getSeq()), "Cheque/post", "POST").execute().get();
            if (response != null) {
                if (response.equals("true")) {
                    chequeList.remove(adapterPosition);
                    preencherRecycleView();
                }
                if (response.equals("false")) {
                    Toast.makeText(this, (R.string.message_erro_nao_possivel_baixar_cheque), Toast.LENGTH_LONG).show();
                    preencherRecycleView();
                }
            } else {
                Toast.makeText(this, String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
                preencherRecycleView();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void preencherRecycleView() {
        ChequeAdapter chequeAdapter = new ChequeAdapter(chequeList);
        recyclerView.setAdapter(chequeAdapter);
    }

    private void atualizarChequeList() {
        String request = null;
        try {
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.rb_cheque_todos:
                    request = new HTTPService("Cheque/get/", "tudo").execute().get();
                    break;
                case R.id.rb_cheque_aberto:
                    request = new HTTPService("Cheque/get/", "aberto").execute().get();
                    break;
                case R.id.rb_cheque_pago:
                    request = new HTTPService("Cheque/get/", "pago").execute().get();
                    break;
                case R.id.rb_cheque_nulo:
                    request = new HTTPService("Cheque/get/", "nulo").execute().get();
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (request != null) {
            try {
                /*
                    Fazer dessa forma para poder remover, adicionar e tudo mais...
                    Se for direto da erro ao tentar remover (testado)
                 */
                chequeList = new ArrayList<>();
                chequeList.addAll(Arrays.asList(new Gson().fromJson(request, Cheque[].class)));
            } catch (Exception e) {
                System.err.println("CODIGO EXEPTION " + MyException.code);
                chequeList = new ArrayList<>();
                Toast.makeText(this, request, Toast.LENGTH_SHORT).show();
            }
        } else {
            System.err.println("CODIGO EXEPTION " + MyException.code);
            Toast.makeText(this, String.valueOf(MyException.code), Toast.LENGTH_SHORT).show();
            chequeList = new ArrayList<>();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarChequeList();
        preencherRecycleView();
    }
}
