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
        final Cheque cheque = chequeList.get(adapterPosition);
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //define o titulo
        builder.setTitle(getString(R.string.title_confirmacao));
        //define a mensagem
        builder.setMessage(getString(R.string.message_confirmar_exclusao_cheque) + "\n" + cheque.getSeq() + " R$ " + cheque.getValor() + "\n" + cheque.getFornecedor().getNome());
        //define um botão como positivo
        builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                new ChequeModifyAsync(cheque, adapterPosition, "Cheque/excluir/");
            }
        });
        //define um botão como negativo.
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                preencherRecycleView();
                try {
                    recyclerView.scrollToPosition(adapterPosition);
                } catch (Exception e) {
                }
            }
        });
        AlertDialog alerta = builder.create();
        alerta.show();
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
        builder.setMessage(getString(R.string.message_confirmar_baixa_cheque) + "\n" + cheque.getSeq() + " R$ " + cheque.getValor() + "\n" + cheque.getFornecedor().getNome());
        builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                new ChequeModifyAsync(cheque, adapterPosition, "Cheque/post/");
            }
        });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                preencherRecycleView();
                try {
                    recyclerView.scrollToPosition(adapterPosition);
                } catch (Exception e) {
                }
            }
        });
        AlertDialog alerta = builder.create();
        alerta.show();
    }

    public class ChequeModifyAsync extends AsyncTask<Void, Void, String> {
        private Cheque cheque;
        private int adapterPosition;
        private String caminho;

        public ChequeModifyAsync(Cheque cheque, int adapterPosition, String caminho) {
            this.cheque = cheque;
            this.adapterPosition = adapterPosition;
            this.caminho = caminho;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String response = WebService.post(caminho, String.valueOf(cheque.getSeq()), "POST");
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                if (response.equals("true")) {
                    chequeList.remove(adapterPosition);
                    preencherRecycleView();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.message_erro_cheque_alterar_estado, Toast.LENGTH_LONG).show();
                    preencherRecycleView();
                }
            } else {
                Toast.makeText(getApplicationContext(), String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
                preencherRecycleView();
            }
            try {
                recyclerView.scrollToPosition(adapterPosition);
            } catch (Exception e) {
            }
        }
    }

    private void iniciarLancarCheque() {
        Intent actLancarCheque = new Intent(this, LancarChequeActivity.class);
        startActivity(actLancarCheque);
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
