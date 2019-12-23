package jcr.br.financas;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import jcr.br.financas.WS.HTTPService;
import jcr.br.financas.WS.HTTPServicePost;
import jcr.br.financas.WS.WebService;
import jcr.br.financas.model.Cheque;
import jcr.br.financas.model.DatePickerFragment;
import jcr.br.financas.model.Fornecedor;
import jcr.br.financas.model.MyException;

import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LancarChequeActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private EditText sequencia, valor;
    private Button emissao, vencimento;
    private Spinner spinnerFornecederes;
    private List<Fornecedor> fornecedorList;
    private boolean isEmissao, isVencimento;

    private ProgressBar pbFornecedor, pbSequencia;
    private FornecedoresListAsync fornecedoresListAsync;
    private ProximoChequeAsync proximoChequeAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lancar_cheque);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        pbFornecedor = findViewById(R.id.pbChequeFornecedor);
        pbSequencia = findViewById(R.id.pbChequeSequencia);

        isEmissao = false;
        isVencimento = false;
        emissao = findViewById(R.id.buttonChequeEmissao);
        emissao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getSupportFragmentManager(), "date picker");
                isEmissao = true;
                isVencimento = false;
            }
        });
        vencimento = findViewById(R.id.buttonChequeVencimento);
        vencimento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getSupportFragmentManager(), "date picker");
                isEmissao = false;
                isVencimento = true;
            }
        });
        sequencia = findViewById(R.id.editChequeSequencia);
        valor = findViewById(R.id.editChequeValor);
        spinnerFornecederes = findViewById(R.id.spinnerChequeFornecedor);

        FloatingActionButton fabChequeConcluir = findViewById(R.id.FloatingActionButtonChequeConcluir);
        fabChequeConcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                concluir();
            }
        });
    }

    private void obterProximoCheque() {
        proximoChequeAsync = new ProximoChequeAsync();
        proximoChequeAsync.execute();
    }

    public class ProximoChequeAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbSequencia.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            String request = WebService.get("Cheque/get/proximo");
            return request;
        }

        @Override
        protected void onPostExecute(String request) {
            super.onPostExecute(request);
            if (request != null) {
                sequencia.setText(request);
            } else {
                sequencia.setText(String.valueOf(-1));
                Toast.makeText(getApplicationContext(), "ERRO:" + String.valueOf(MyException.code), Toast.LENGTH_SHORT).show();
            }
            pbSequencia.setVisibility(View.GONE);
        }
    }

    private void popularFornecedores() {
        fornecedoresListAsync = new FornecedoresListAsync();
        fornecedoresListAsync.execute();
    }

    public class FornecedoresListAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbFornecedor.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            String request = WebService.get("Fornecedor/get/all/cheques");
            return request;
        }

        @Override
        protected void onPostExecute(String request) {
            super.onPostExecute(request);
            if (request != null) {
                fornecedorList = new ArrayList<>();
                fornecedorList.addAll(Arrays.asList(new Gson().fromJson(request, Fornecedor[].class)));
                Fornecedor fornecedor = new Fornecedor("OUTRO...", -1);
                fornecedorList.add(0, fornecedor);
            } else {
                fornecedorList = new ArrayList<>();
                Toast.makeText(getApplicationContext(), "ERRO: " + String.valueOf(MyException.code), Toast.LENGTH_SHORT).show();
            }
            ArrayAdapter arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_selected_line, fornecedorList);
            arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
            spinnerFornecederes.setAdapter(arrayAdapter);
            pbFornecedor.setVisibility(View.GONE);
        }
    }

    private void concluir() {
        if (sequencia.getText().toString().trim().equals("")) {
            Toast.makeText(this, R.string.message_informe_sequencia, Toast.LENGTH_SHORT).show();
            return;
        }
        Fornecedor fornecedor = (Fornecedor) spinnerFornecederes.getSelectedItem();
        if (fornecedor == null) {
            Toast.makeText(this, R.string.message_informe_fornecedor, Toast.LENGTH_SHORT).show();
            return;
        }
        if (fornecedor.getNome().equals("NULO")) {
            lancarChequeNulo(fornecedor);
            return;
        } else if (fornecedor.getNome().equals("OUTRO...")) {
            cadastrarFornecedor();
            return;
        } else {
            if (emissao.getText().toString().equals("")) {
                Toast.makeText(this, R.string.message_informe_emissao, Toast.LENGTH_SHORT).show();
                return;
            }
            if (valor.getText().toString().trim().equals("")) {
                Toast.makeText(this, R.string.message_informe_valor, Toast.LENGTH_SHORT).show();
                return;
            }
            lancarChequeNormal(fornecedor);
        }
    }

    private void lancarChequeNormal(Fornecedor fornecedor) {
        try {
            double val = Double.parseDouble(valor.getText().toString().replaceAll(",", "\\."));
            Cheque cheque = new Cheque();
            cheque.setSeq(Integer.parseInt(sequencia.getText().toString().trim()));
            cheque.setEmissao(emissao.getText().toString());
            cheque.setPredatado(vencimento.getText().toString());
            cheque.setFornecedor(fornecedor);
            cheque.setValor(val);
            new LancarChequeAsync(cheque, "Cheque/post/add");
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.message_informe_valor, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public class LancarChequeAsync extends AsyncTask<Void, Void, String> {
        private Cheque cheque;
        private String caminho;

        public LancarChequeAsync(Cheque cheque, String caminho) {
            this.cheque = cheque;
            this.caminho = caminho;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String response = WebService.post(caminho, new Gson().toJson(cheque), "POST");
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                if (response.equals("true")) {
                    Toast.makeText(getApplicationContext(), R.string.message_concluido, Toast.LENGTH_SHORT).show();
                    obterProximoCheque();
                    emissao.setText("");
                    vencimento.setText("");
                    valor.setText("");
                } else if (response.equals("false")) {
                    Toast.makeText(getApplicationContext(), R.string.message_erro_salvar, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.message_erro_salvar + "\n" + response, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "ADD Cheque erro:" + String.valueOf(MyException.code), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void cadastrarFornecedor() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(LancarChequeActivity.this);
        final Context context = builder.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.cad_fornecedor_simples, null, false);
        final EditText nome = view.findViewById(R.id.editChequeCadFornecedor);

        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    dialog.cancel();
                    return;
                }
                String nome_temp = nome.getText().toString();
                if (nome_temp == null || nome_temp.trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Fornecedor VAZIO", Toast.LENGTH_LONG).show();
                    return;
                }
                Fornecedor fornecedor = new Fornecedor(nome_temp.trim().toUpperCase(), -1);
                new CadastrarFornecedorAsync(fornecedor).execute();
            }
        };

        builder.setTitle("Cadastro");
        builder
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(R.string.lbl_cadastrar, listener)
                .setNegativeButton(R.string.lbl_cancelar, listener);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public class CadastrarFornecedorAsync extends AsyncTask<Void, Void, String> {

        private Fornecedor fornecedor;

        public CadastrarFornecedorAsync(Fornecedor fornecedor) {
            this.fornecedor = fornecedor;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String response = WebService.post("Fornecedor/post", new Gson().toJson(fornecedor), "POST");
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                if (response.equals("true")) {
                    popularFornecedores();
                    for (int x = 0; x < fornecedorList.size(); x++) {
                        if (fornecedor.getNome().equals(fornecedorList.get(x).getNome())) {
                            spinnerFornecederes.setSelection(x);
                            break;
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Erro ao cadastrar novo fornecedor.\n" + String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Erro " + String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void lancarChequeNulo(Fornecedor fornecedor) {
        Cheque cheque = new Cheque();
        cheque.setSeq(Integer.parseInt(sequencia.getText().toString().trim()));
        cheque.setFornecedor(fornecedor);
        new LancarChequeAsync(cheque, "Cheque/post/add");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        if (isEmissao) {
            emissao.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime()));
            vencimento.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime()));
        } else if (isVencimento) {
            vencimento.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime()));
        }
        isVencimento = false;
        isEmissao = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        popularFornecedores();
        obterProximoCheque();
    }

    @Override
    protected void onStop() {
        super.onStop();
        proximoChequeAsync.cancel(true);
        fornecedoresListAsync.cancel(true);
    }
}
