package jcr.br.financas;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import jcr.br.financas.WS.HTTPServicePost;

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
        init();
    }

    private void init() {
        nome = findViewById(R.id.editCadNomeFornecedor);
        banco = findViewById(R.id.editCadBancoFornecedor);
        numero = findViewById(R.id.editCadNumeroFornecedor);
        if (LancarBoletoActivity.cd_barras != null) {
            numero.setText(LancarBoletoActivity.cd_barras.substring(25, 29));
            banco.setText(LancarBoletoActivity.cd_barras.substring(3));
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
        HTTPServicePost httpServicePost = new HTTPServicePost(new Gson().toJson(fornecedor), "Fornecedor/post/", "POST");
        try {
            boolean response = new Gson().fromJson(httpServicePost.execute().get(), boolean.class);
            if(response){
                Toast.makeText(this,R.string.message_concluido, Toast.LENGTH_SHORT).show();
                nome.setText("");
                banco.setText("");
                numero.setText("");
            }else{
                Toast.makeText(this,R.string.message_erro_salvar, Toast.LENGTH_SHORT).show();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}