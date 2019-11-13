package jcr.br.financas;

import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import jcr.br.financas.WS.HTTPService;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

public class LancarBoletoActivity extends AppCompatActivity {

    private EditText codigo_barras;
    private Spinner spinnerFornecedor;

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
        try {
            HTTPService service = new HTTPService("/Boleto/get/testeFornecedor/", codigo);
            String request = service.execute().get();
            if (request != null && !request.equals("[]")) {
                preecherComboBox(request);
            } else {
                System.out.println("REQUEST NULO/VAZIO");
                Toast.makeText(this.getApplicationContext(), (R.string.message_request_nulo_vazio), Toast.LENGTH_LONG).show();
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

    private void preecherComboBox(String request) {
        Fornecedor[] fornecedores = new Gson().fromJson(request, Fornecedor[].class);
        ArrayAdapter adapter = new ArrayAdapter<Fornecedor>(getApplicationContext(), android.R.layout.simple_spinner_item, fornecedores);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFornecedor.setAdapter(adapter);
        for (int x=0; x < fornecedores.length; x++) {
            TextView spinnerText = (TextView) spinnerFornecedor.getChildAt(x);
            spinnerText.setTextColor(Color.BLACK);
        }
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
}
