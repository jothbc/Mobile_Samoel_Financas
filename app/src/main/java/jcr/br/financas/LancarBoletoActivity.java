package jcr.br.financas;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.DialogFragment;
import jcr.br.financas.WS.HTTPServicePost;
import jcr.br.financas.WS.WebService;
import jcr.br.financas.funcoes.BoletoFuncoes;
import jcr.br.financas.funcoes.CDate;
import jcr.br.financas.model.Boleto;
import jcr.br.financas.model.DatePickerFragment;
import jcr.br.financas.model.Fornecedor;
import jcr.br.financas.model.MyException;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LancarBoletoActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private EditText codigo_barras;
    private Spinner spinnerFornecedor;
    private Fornecedor[] fornecedores;

    public static String cd_barras = null;

    private ProgressBar pbFornecedor;
    private GetFornecedorAsync getFornecedorAsync;
    private LancarBoletoAsync lancarBoletoAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lancar_boleto);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pbFornecedor = findViewById(R.id.pbBoletoFornecedor);
        pbFornecedor.setVisibility(View.GONE);

        codigo_barras = findViewById(R.id.editBoletoCodigoBarras);
        codigo_barras.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    buscarFornecedores(codigo_barras.getText().toString().trim());
                }
            }
        });
        spinnerFornecedor = findViewById(R.id.spinnerBoletoFornecedor);

        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionScanCodBoleto);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCodigoBarras(v);
            }
        });

        Button vencimentoEdit = findViewById(R.id.editBoletoVencimento);
        vencimentoEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getSupportFragmentManager(), "date picker");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_lancar_boleto, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_cad_fornecedor:
                cd_barras = BoletoFuncoes.linhaDigitavelEmCodigoDeBarras(codigo_barras.getText().toString().trim());
                Intent cadFornecedor = new Intent(LancarBoletoActivity.this, CadFornecedorBoletoActivity.class);
                startActivity(cadFornecedor);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void buscarFornecedores(String codigo) {
        getFornecedorAsync = new GetFornecedorAsync(codigo);
        getFornecedorAsync.execute();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Button vencimentoEdit = findViewById(R.id.editBoletoVencimento);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        vencimentoEdit.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime()));
    }


    public class GetFornecedorAsync extends AsyncTask<Void, Void, String> {

        private String codigo_temp;

        public GetFornecedorAsync(String codigo) {
            this.codigo_temp = codigo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbFornecedor.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (codigo_temp.trim().isEmpty()) {
                return null;
            }
            codigo_temp = BoletoFuncoes.linhaDigitavelEmCodigoDeBarras(codigo_temp.trim());
            if (codigo_temp == null) {
                Toast.makeText(getApplicationContext(), (R.string.message_tamanho_codigo_invalido), Toast.LENGTH_LONG).show();
                return null;
            }
            String request = WebService.get("Boleto/get/testeFornecedor/" + codigo_temp);
            return request;
        }

        @Override
        protected void onPostExecute(String request) {
            super.onPostExecute(request);
            if (request != null) {
                try {
                    fornecedores = new Gson().fromJson(request, Fornecedor[].class);
                    preecherComboBox();
                    if (preencherVencimento(codigo_temp)) {
                        preencherValor(codigo_temp);
                    }
                } catch (JsonSyntaxException e) {
                    Toast.makeText(getApplicationContext(), request, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "ERRO: " + String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
            }
            pbFornecedor.setVisibility(View.GONE);
        }
    }

    private void preencherValor(String codigo) {
        EditText valorEdit = findViewById(R.id.editBoletoValor);
        double valor = BoletoFuncoes.getValorBoleto(codigo);
        valorEdit.setText(new DecimalFormat("0.00").format(valor));
    }

    private boolean preencherVencimento(String codigo) {
        Button vencimentoEdit = findViewById(R.id.editBoletoVencimento);
        String venc_temp = BoletoFuncoes.getVencimento(codigo);
        if (venc_temp.equals(CDate.getDataInicialBanco())) {
            return false;
        }
        vencimentoEdit.setText(venc_temp);
        return true;
    }

    private void preecherComboBox() {
        ArrayAdapter adapter = new ArrayAdapter<Fornecedor>(getApplicationContext(), R.layout.spinner_selected_line, fornecedores);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinnerFornecedor.setAdapter(adapter);
    }

    public void scanCodigoBarras(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan Boleto");
        integrator.setCameraId(0);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                codigo_barras.setText(result.getContents());
                buscarFornecedores(codigo_barras.getText().toString());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void lancarBoletoBtn(View view) {
        Button vencimentoEdit = findViewById(R.id.editBoletoVencimento);
        EditText valorEdit = findViewById(R.id.editBoletoValor);
        EditText codigoEdit = findViewById(R.id.editBoletoCodigoBarras);
        String codigo_barras = BoletoFuncoes.linhaDigitavelEmCodigoDeBarras(codigoEdit.getText().toString().trim());
        if (codigo_barras == null) {
            Toast.makeText(this, (R.string.message_tamanho_codigo_invalido), Toast.LENGTH_SHORT).show();
            return;
        }
        if (vencimentoEdit.getText().equals(CDate.getDataInicialBanco())) {
            Toast.makeText(this, (R.string.message_erro_vencimento_invalido), Toast.LENGTH_SHORT).show();
            return;
        }
        Boleto boleto = new Boleto();
        boleto.setFornecedor_id((Fornecedor) spinnerFornecedor.getSelectedItem());

        boleto.setCd_barras(codigo_barras);

        String value = valorEdit.getText().toString().replaceAll(",", ".");
        boleto.setValor(Double.parseDouble(value));

        String vencimento_temp = vencimentoEdit.getText().toString();
        vencimento_temp = vencimento_temp.replaceAll("\\.", "/");
        vencimento_temp = vencimento_temp.replaceAll("-.", "/");
        boleto.setVencimento(vencimento_temp);

        lancarBoletoAsync = new LancarBoletoAsync(boleto);
        lancarBoletoAsync.execute();

    }

    public class LancarBoletoAsync extends AsyncTask<Void, Void, String> {

        private Boleto boleto;

        public LancarBoletoAsync(Boleto boleto) {
            this.boleto = boleto;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... voids) {

            String response = WebService.post("Boleto/post/", new Gson().toJson(boleto), "POST");
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response == null) {
                if (MyException.code == 304) {
                    Toast.makeText(getApplicationContext(), R.string.message_duplicacao_boleto, Toast.LENGTH_SHORT).show();
                } else if (MyException.code == 204) {
                    Toast.makeText(getApplicationContext(), R.string.message_erro_salvar, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "ERRO: " + String.valueOf(MyException.code), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                if (MyException.code == 200) {
                    Button vencimentoEdit = findViewById(R.id.editBoletoVencimento);
                    EditText valorEdit = findViewById(R.id.editBoletoValor);
                    EditText codigoEdit = findViewById(R.id.editBoletoCodigoBarras);

                    codigoEdit.setText("");
                    vencimentoEdit.setText("");
                    valorEdit.setText("");
                    fornecedores = new Fornecedor[0];
                    preecherComboBox();
                    codigoEdit.requestFocus();
                }
            }
        }
    }


}
