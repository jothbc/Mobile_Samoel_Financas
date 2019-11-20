package jcr.br.financas;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

    public void concluirCadastro(View view){
        if(banco.getText().toString().trim().isEmpty()){
            Toast.makeText(this,"Preencha o campo -> Banco",Toast.LENGTH_LONG).show();
            return;
        }
        try {
            Fornecedor fornecedor = new Fornecedor(nome.getText().toString().trim().toUpperCase(), Integer.parseInt(banco.getText().toString()), numero.getText().toString().trim());

        }catch (Exception e){

        }


    }
}