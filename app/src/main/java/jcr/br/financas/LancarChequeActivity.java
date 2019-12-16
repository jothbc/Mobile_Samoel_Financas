package jcr.br.financas;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import jcr.br.financas.WS.HTTPService;
import jcr.br.financas.WS.HTTPServicePost;
import jcr.br.financas.model.Cheque;
import jcr.br.financas.model.Fornecedor;
import jcr.br.financas.model.MyException;

import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LancarChequeActivity extends AppCompatActivity {

    private EditText sequencia, valor;
    private Button emissao, vencimento;
    private Spinner spinnerFornecederes;
    private CalendarView calendarView;
    private List<Fornecedor> fornecedorList;
    private boolean isEmissao, isVencimento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lancar_cheque);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        isEmissao = false;
        isVencimento = false;
        emissao = findViewById(R.id.buttonChequeEmissao);
        emissao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    DatePickerDialog alertDialog = new DatePickerDialog(getApplicationContext());
                    alertDialog.show();
                }*/
                calendarView.setVisibility(View.VISIBLE);
                isEmissao = true;
                isVencimento = false;
            }
        });
        vencimento = findViewById(R.id.buttonChequeVencimento);
        vencimento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.setVisibility(View.VISIBLE);
                isEmissao = false;
                isVencimento = true;
            }
        });
        sequencia = findViewById(R.id.editChequeSequencia);
        valor = findViewById(R.id.editChequeValor);
        spinnerFornecederes = findViewById(R.id.spinnerChequeFornecedor);
        calendarView = findViewById(R.id.calendarViewCheque);
        calendarView.setVisibility(View.GONE);

        popularFornecedores();
        obterProximoCheque();

        FloatingActionButton fabChequeConcluir = findViewById(R.id.FloatingActionButtonChequeConcluir);
        fabChequeConcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                concluir();
            }
        });
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                preencherDatas(view, year, month + 1, dayOfMonth);
                calendarView.setVisibility(View.GONE);
            }
        });
    }

    private void obterProximoCheque() {
        try {
            String request = new HTTPService("Cheque/get/proximo", "", "GET").execute().get();
            if (request != null) {
                sequencia.setText(request);
            } else {
                sequencia.setText(String.valueOf(-1));
            }
        } catch (ExecutionException | MalformedURLException | InterruptedException e) {
            e.printStackTrace();
            sequencia.setText(String.valueOf(-1));
            Toast.makeText(this, String.valueOf(MyException.code), Toast.LENGTH_SHORT).show();
        }

    }

    private void preencherDatas(CalendarView view, int year, int month, int dayOfMonth) {
        String dia, mes;
        dia = String.valueOf(dayOfMonth).length() == 1 ? "0" + String.valueOf(dayOfMonth) : String.valueOf(dayOfMonth);
        mes = String.valueOf(month).length() == 1 ? "0" + String.valueOf(month) : String.valueOf(month);
        String data = dia + "/" + mes + "/" + String.valueOf(year);
        if (isEmissao) {
            emissao.setText(data);
            vencimento.setText(data);
        } else if (isVencimento) {
            vencimento.setText(data);
        }
    }

    private void popularFornecedores() {
        try {
            String request = new HTTPService("Fornecedor/get/all/cheques", "", "GET").execute().get();
            if (request != null) {
                fornecedorList = new ArrayList<>();
                fornecedorList.addAll(Arrays.asList(new Gson().fromJson(request, Fornecedor[].class)));
                Fornecedor fornecedor = new Fornecedor("OUTRO...", -1);
                fornecedorList.add(0, fornecedor);
            }
        } catch (MalformedURLException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            fornecedorList = new ArrayList<>();
            Toast.makeText(this, String.valueOf(MyException.code), Toast.LENGTH_SHORT).show();
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter<Fornecedor>(getApplicationContext(), R.layout.spinner_selected_line, fornecedorList);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinnerFornecederes.setAdapter(arrayAdapter);
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
            try {
                String response = new HTTPServicePost(new Gson().toJson(cheque), "Cheque/post/add", "POST").execute().get();
                if (response != null) {
                    if (response.equals("true")) {
                        Toast.makeText(this, R.string.message_concluido, Toast.LENGTH_SHORT).show();
                        obterProximoCheque();
                        emissao.setText("");
                        vencimento.setText("");
                        valor.setText("");
                    } else if (response.equals("false")) {
                        Toast.makeText(this, R.string.message_erro_salvar, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.message_erro_salvar + "\n" + response, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "ADD Cheque erro:" + String.valueOf(MyException.code), Toast.LENGTH_SHORT).show();
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.message_informe_valor, Toast.LENGTH_SHORT).show();
            return;
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
                if (nome_temp == null) {
                    Toast.makeText(getApplicationContext(), "Fornecedor NULL", Toast.LENGTH_LONG).show();
                    return;
                }
                if (nome_temp.trim().isEmpty()) {
                    return;
                }

                Fornecedor fornecedor = new Fornecedor(nome_temp.trim().toUpperCase(), -1);
                try {
                    String response = new HTTPServicePost(new Gson().toJson(fornecedor), "Fornecedor/post", "POST").execute().get();
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
                            Toast.makeText(getApplicationContext(), "Erro ao cadastrar novo fornecedor.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Erro " + String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
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


    private void lancarChequeNulo(Fornecedor fornecedor) {
        Cheque cheque = new Cheque();
        cheque.setSeq(Integer.parseInt(sequencia.getText().toString().trim()));
        cheque.setFornecedor(fornecedor);
        try {
            String response = new HTTPServicePost(new Gson().toJson(cheque), "Cheque/post/add", "POST").execute().get();
            if (response != null) {
                if (response.equals("true")) {
                    Toast.makeText(this, R.string.message_concluido, Toast.LENGTH_SHORT).show();
                    obterProximoCheque();
                    emissao.setText("");
                    vencimento.setText("");
                    valor.setText("");
                } else if (response.equals("false")) {
                    Toast.makeText(this, R.string.message_erro_salvar, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.message_erro_salvar + "\n" + response, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "ADD Cheque NULL erro: " + String.valueOf(MyException.code), Toast.LENGTH_SHORT).show();
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
