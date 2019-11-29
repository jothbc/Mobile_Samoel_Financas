package jcr.br.financas;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import jcr.br.financas.WS.HTTPService;
import jcr.br.financas.WS.HTTPServicePost;
import jcr.br.financas.model.Cheque;
import jcr.br.financas.model.Fornecedor;
import jcr.br.financas.model.MyException;

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
                preencherDatas(view, year, month, dayOfMonth);
                calendarView.setVisibility(View.GONE);
            }
        });
    }

    private void obterProximoCheque() {
        try {
            String request = new HTTPService("Cheque/get/proximo", "").execute().get();
            if (request != null) {
                sequencia.setText(request);
            }else{
                sequencia.setText(String.valueOf(-1));
            }
        } catch (ExecutionException | MalformedURLException | InterruptedException e) {
            e.printStackTrace();
            sequencia.setText(String.valueOf(-1));
            Toast.makeText(this, String.valueOf(MyException.code), Toast.LENGTH_SHORT).show();
        }

    }

    private void preencherDatas(CalendarView view, int year, int month, int dayOfMonth) {
        String data = String.valueOf(dayOfMonth) + "/" + String.valueOf(month) + "/" + String.valueOf(year);
        if (isEmissao) {
            emissao.setText(data);
            vencimento.setText(data);
        } else if (isVencimento) {
            vencimento.setText(data);
        }
    }

    private void popularFornecedores() {
        try {
            String request = new HTTPService("Fornecedor/get/all/cheques", "").execute().get();
            if (request != null) {
                fornecedorList = new ArrayList<>();
                fornecedorList.addAll(Arrays.asList(new Gson().fromJson(request, Fornecedor[].class)));
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
                    }
                } else {
                    Toast.makeText(this, String.valueOf(MyException.code), Toast.LENGTH_SHORT).show();
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }else{
            if (emissao.getText().toString().equals("")) {
                Toast.makeText(this, R.string.message_informe_emissao, Toast.LENGTH_SHORT).show();
                return;
            }
            if (valor.getText().toString().trim().equals("")) {
                Toast.makeText(this, R.string.message_informe_valor, Toast.LENGTH_SHORT).show();
                return;
            }
        }
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
                    }
                } else {
                    Toast.makeText(this, String.valueOf(MyException.code), Toast.LENGTH_SHORT).show();
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.message_informe_valor, Toast.LENGTH_SHORT).show();
            return;
        }
    }

}
