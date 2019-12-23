package jcr.br.financas;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import jcr.br.financas.WS.HTTPServicePost;
import jcr.br.financas.WS.WebService;
import jcr.br.financas.model.Fornecedor;
import jcr.br.financas.model.MyException;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

public class CadFornecedorBoletoActivity extends AppCompatActivity {

    EditText nome, banco, numero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cad_fornecedor_boleto);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setTitle("Cadastrar Fornecedor");
        init();
    }

    private void init() {
        nome = findViewById(R.id.editCadNomeFornecedor);
        banco = findViewById(R.id.editCadBancoFornecedor);
        numero = findViewById(R.id.editCadNumeroFornecedor);
        if (LancarBoletoActivity.cd_barras != null) {
            numero.setText(LancarBoletoActivity.cd_barras.substring(25, 29));
            banco.setText(LancarBoletoActivity.cd_barras.substring(0, 3));
        }
    }

    public void concluirCadastro(View view) {
        if (nome.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Preencha o campo Fornecedor", Toast.LENGTH_LONG).show();
            return;
        }
        if (banco.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Preencha o campo Banco", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            int banco_temp = Integer.parseInt(banco.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "O campo Banco deve ser apenas números", Toast.LENGTH_LONG).show();
            return;
        }

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome(nome.getText().toString().trim().toUpperCase());
        fornecedor.setBanco(Integer.parseInt(banco.getText().toString().trim()));
        if (!numero.getText().toString().trim().isEmpty()) {
            fornecedor.setNumero(numero.getText().toString().trim());
        }
        new SalvarNovoFornecedorAsync(fornecedor).execute();
    }

    public class SalvarNovoFornecedorAsync extends AsyncTask<Void, Void, String> {

        Fornecedor fornecedor;

        public SalvarNovoFornecedorAsync(Fornecedor f) {
            this.fornecedor = f;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String response = WebService.post("Fornecedor/post/", new Gson().toJson(fornecedor), "POST");
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                if (response.equals("true")) {
                    Toast.makeText(getApplicationContext(), R.string.message_concluido, Toast.LENGTH_SHORT).show();
                    nome.setText("");
                    banco.setText("");
                    numero.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), R.string.message_erro_salvar + "\n" + response, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.message_erro_salvar + "\n" + String.valueOf(MyException.code), Toast.LENGTH_SHORT).show();
            }
        }
    }
}