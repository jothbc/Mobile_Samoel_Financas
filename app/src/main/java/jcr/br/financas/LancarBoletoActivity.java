package jcr.br.financas;

import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import jcr.br.financas.WS.HTTPService;
import jcr.br.financas.WS.HTTPServicePost;
import jcr.br.financas.funcoes.BoletoFuncoes;
import jcr.br.financas.funcoes.CDate;

import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;

public class LancarBoletoActivity extends AppCompatActivity {

    private EditText codigo_barras;
    private Spinner spinnerFornecedor;
    private Fornecedor[] fornecedores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lancar_boleto);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    }

    private void buscarFornecedores(String codigo) {
        if (codigo.trim().isEmpty()) {
            return;
        }
        codigo = BoletoFuncoes.linhaDigitavelEmCodigoDeBarras(codigo.trim());
        if (codigo == null) {
            Toast.makeText(this.getApplicationContext(), (R.string.message_tamanho_codigo_invalido), Toast.LENGTH_LONG).show();
            return;
        }
        try {
            HTTPService service = new HTTPService("/Boleto/get/testeFornecedor/", codigo,this.getApplicationContext());
            String request = service.execute().get();
            if (request != null && !request.equals("[]")) {
                fornecedores = new Gson().fromJson(request, Fornecedor[].class);
                preecherComboBox();
                preencherVenciment(codigo);
                preencherValor(codigo);
            } else {
                System.out.println("REQUEST NULO/VAZIO");
                //Toast.makeText(this.getApplicationContext(), (R.string.message_request_nulo_vazio), Toast.LENGTH_LONG).show();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println(e);
        } catch (InterruptedException e) {
            System.out.println(e);
            e.printStackTrace();
        } catch (ExecutionException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    private void preencherValor(String codigo) {
        EditText valorEdit = findViewById(R.id.editBoletoValor);
        double valor = BoletoFuncoes.getValorBoleto(codigo);
        valorEdit.setText(new DecimalFormat("0.00").format(valor));
    }

    private void preencherVenciment(String codigo) {
        EditText vencimentoEdit = findViewById(R.id.editBoletoVencimento);
        String venc_temp = BoletoFuncoes.getVencimento(codigo);
        if(venc_temp.equals(CDate.getDataInicialBanco())){
            return;
        }
        vencimentoEdit.setText(venc_temp);
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
        EditText vencimentoEdit = findViewById(R.id.editBoletoVencimento);
        EditText valorEdit = findViewById(R.id.editBoletoValor);
        EditText codigoEdit = findViewById(R.id.editBoletoCodigoBarras);
        String codigo_barras = BoletoFuncoes.linhaDigitavelEmCodigoDeBarras(codigoEdit.getText().toString().trim());
        if (codigo_barras == null) {
            Toast.makeText(this, (R.string.message_tamanho_codigo_invalido), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (vencimentoEdit.getText().equals(CDate.getDataInicialBanco())) {
                Toast.makeText(this, (R.string.message_erro_vencimento_invalido), Toast.LENGTH_SHORT).show();
                throw new Exception(getString(R.string.message_erro_vencimento_invalido));
            }
            Boleto boleto = new Boleto();
            boleto.setFornecedor_id((Fornecedor) spinnerFornecedor.getSelectedItem());

            boleto.setCd_barras(codigo_barras);

            String value = valorEdit.getText().toString().replaceAll(",", ".");
            boleto.setValor(Double.parseDouble(value));

            boleto.setVencimento(vencimentoEdit.getText().toString());

            HTTPServicePost httpServicePost = new HTTPServicePost(boleto, "Boleto/post/", "POST");
            String response = httpServicePost.execute().get();
            if (response.equals("falha")) {
                Toast.makeText(this, (R.string.message_erro_salvar), Toast.LENGTH_SHORT).show();
                return;
            } else if (response.equals("existe")) {
                Toast.makeText(this, (R.string.message_duplicacao_boleto), Toast.LENGTH_SHORT).show();
                return;
            }if(response.equals("concluido")){
                Toast.makeText(this, (R.string.message_concluido), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, (R.string.message_erro_salvar) + "\n" + ex, Toast.LENGTH_SHORT).show();
        } finally {
            codigoEdit.setText("");
            vencimentoEdit.setText("");
            valorEdit.setText("");
            fornecedores = new Fornecedor[0];
            preecherComboBox();
            codigoEdit.requestFocus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
