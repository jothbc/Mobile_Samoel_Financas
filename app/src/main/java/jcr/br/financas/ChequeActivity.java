package jcr.br.financas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jcr.br.financas.Adapter.ChequeAdapter;
import jcr.br.financas.WS.HTTPService;
import jcr.br.financas.WS.HTTPServicePost;
import jcr.br.financas.WS.WebService;
import jcr.br.financas.funcoes.Conv;
import jcr.br.financas.model.Cheque;
import jcr.br.financas.model.MyException;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
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
    private TextView lbTotalAberto;
    private ProgressBar pb;
    ListAsync listAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheque);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = findViewById(R.id.list_cheques_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        lbTotalAberto = findViewById(R.id.textChequeTotalAberto);
        pb = findViewById(R.id.pbChequeList);

        chequeList = new ArrayList<>();
        radioGroup = findViewById(R.id.radioGroupCheque);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                new ListAsync().execute();
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.RIGHT)
                    baixarChequeSelecionado(viewHolder.getAdapterPosition());
                else if (direction == ItemTouchHelper.LEFT)
                    deletarCheque(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

        FloatingActionButton btnAddCheque = findViewById(R.id.floatingActionButtonChequeAdd);
        btnAddCheque.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarLancarCheque();
            }
        });

    }

    private void deletarCheque(final int adapterPosition) {
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //define o titulo
        builder.setTitle(getString(R.string.title_confirmacao));
        //define a mensagem
        builder.setMessage(getString(R.string.message_confirmar_exclusao_cheque));
        //define um botão como positivo
        builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Cheque cheque = chequeList.get(adapterPosition);
                try {
                    String respose = new HTTPService("Cheque/excluir/", Integer.toString(cheque.getSeq()), "GET").execute().get();
                    if (respose != null) {
                        if (respose.equals("true")) {
                            chequeList.remove(adapterPosition);
                            preencherRecycleView();
                        } else {
                            Toast.makeText(getApplicationContext(), "ERRO: " + String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
                            preencherRecycleView();
                        }
                    }
                } catch (ExecutionException | InterruptedException | MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });
        //define um botão como negativo.
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                preencherRecycleView();
            }
        });
        AlertDialog alerta = builder.create();
        alerta.show();
        recyclerView.scrollToPosition(adapterPosition);
    }

    private void iniciarLancarCheque() {
        Intent actLancarCheque = new Intent(this, LancarChequeActivity.class);
        startActivity(actLancarCheque);
    }

    private void baixarChequeSelecionado(final int adapterPosition) {
        final Cheque cheque = chequeList.get(adapterPosition);
        if (cheque.getSaque() != null || cheque.getEmissao() == null) {
            Toast.makeText(this, (R.string.message_erro_nao_possivel_baixar_cheque), Toast.LENGTH_LONG).show();
            preencherRecycleView();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_confirmacao));
        builder.setMessage(getString(R.string.message_confirmar_baixa_cheque));
        builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                try {
                    String response = new HTTPServicePost(String.valueOf(cheque.getSeq()), "Cheque/post", "POST").execute().get();
                    if (response != null) {
                        if (response.equals("true")) {
                            chequeList.remove(adapterPosition);
                            preencherRecycleView();
                        }
                        if (response.equals("false")) {
                            Toast.makeText(getApplicationContext(), (R.string.message_erro_nao_possivel_baixar_cheque), Toast.LENGTH_LONG).show();
                            preencherRecycleView();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
                        preencherRecycleView();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                preencherRecycleView();
            }
        });
        AlertDialog alerta = builder.create();
        alerta.show();
        recyclerView.scrollToPosition(adapterPosition);
    }

    public void preencherRecycleView() {
        ChequeAdapter chequeAdapter = new ChequeAdapter(chequeList);
        recyclerView.setAdapter(chequeAdapter);
        double valor = 0;
        for (Cheque i : chequeList) {
            if (i.getSaque() == null) {
                valor += i.getValor();
            }
        }
        lbTotalAberto.setText(getString(R.string.lbl_valor_em_aberto) + " R$" + Conv.colocarPontoEmValor(Conv.validarValue(valor)));
    }


    @Override
    protected void onResume() {
        super.onResume();
        listAsync = new ListAsync();
        listAsync.execute();
    }

    public class ListAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            String request = null;
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.rb_cheque_todos:
                    request = WebService.get("Cheque/get/tudo");//new HTTPService("Cheque/get/", "tudo", "GET").execute().get();
                    break;
                case R.id.rb_cheque_aberto:
                    request = WebService.get("Cheque/get/aberto");//new HTTPService("Cheque/get/", "aberto", "GET").execute().get();
                    break;
                case R.id.rb_cheque_pago:
                    request = WebService.get("Cheque/get/pago");//new HTTPService("Cheque/get/", "pago", "GET").execute().get();
                    break;
                case R.id.rb_cheque_nulo:
                    request = WebService.get("Cheque/get/nulo");//new HTTPService("Cheque/get/", "nulo", "GET").execute().get();
                    break;
            }
            return request;
        }

        @Override
        protected void onPostExecute(String request) {
            super.onPostExecute(request);
            if (request != null) {
                try {
                    chequeList = new ArrayList<>();
                    chequeList.addAll(Arrays.asList(new Gson().fromJson(request, Cheque[].class)));
                } catch (Exception e) {
                    System.err.println("CODIGO EXCEPTION " + MyException.code);
                    chequeList = new ArrayList<>();
                    Toast.makeText(getApplicationContext(), request + "\n" + String.valueOf(MyException.code), Toast.LENGTH_SHORT).show();
                }
            } else {
                System.err.println("CODIGO EXCEPTION " + MyException.code);
                Toast.makeText(getApplicationContext(), String.valueOf(MyException.code), Toast.LENGTH_SHORT).show();
                chequeList = new ArrayList<>();
            }
            preencherRecycleView();
            pb.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        listAsync.cancel(true);
    }
}
