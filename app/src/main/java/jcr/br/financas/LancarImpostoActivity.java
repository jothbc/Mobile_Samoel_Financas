package jcr.br.financas;

import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import jcr.br.financas.WS.HTTPService;
import jcr.br.financas.WS.HTTPServicePost;
import jcr.br.financas.WS.WebService;
import jcr.br.financas.funcoes.CDate;
import jcr.br.financas.model.DatePickerFragment;
import jcr.br.financas.model.Imposto;
import jcr.br.financas.model.MyException;

import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class LancarImpostoActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private EditText desc, valor;
    private Button venc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lancar_imposto);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        desc = findViewById(R.id.editImpostoDesc);
        venc = findViewById(R.id.editImpostoVencimento);
        venc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getSupportFragmentManager(), "date picker");
            }
        });
        valor = findViewById(R.id.editImpostoValor);
        FloatingActionButton fab = findViewById(R.id.fabImpostoAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                concluir(view);
            }
        });
    }

    private void concluir(View view) {
        if (desc.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.message_informe_descricao, Toast.LENGTH_LONG).show();
            return;
        } else if (venc.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.message_informe_vencimento, Toast.LENGTH_LONG).show();
            return;
        } else if (valor.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.message_informe_valor, Toast.LENGTH_LONG).show();
            return;
        }
        String vencimento_temp = venc.getText().toString().replaceAll("\\.", "/");
        vencimento_temp = vencimento_temp.replaceAll("-", "/");

        try {
            Date data = new SimpleDateFormat("dd/MM/yyyy").parse(vencimento_temp);
        } catch (ParseException e) {
            Toast.makeText(this, R.string.message_informe_vencimento_valido, Toast.LENGTH_LONG).show();
            return;
        }
        Imposto imposto = new Imposto();
        imposto.setDescricao(desc.getText().toString().trim().toUpperCase());
        imposto.setValor(Double.parseDouble(valor.getText().toString().trim().replaceAll(",", "\\.")));
        imposto.setVencimento(vencimento_temp);
        new LancarImpostoAsync(imposto).execute();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        venc.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime()));
    }

    public class LancarImpostoAsync extends AsyncTask<Void, Void, String> {

        private Imposto imposto;

        public LancarImpostoAsync(Imposto imposto) {
            this.imposto = imposto;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String response = WebService.post("Imposto/add", new Gson().toJson(imposto), "POST");
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            if (response != null) {
                if (response.equals("true")) {
                    Toast.makeText(getApplicationContext(), R.string.message_concluido, Toast.LENGTH_SHORT).show();
                    desc.setText("");
                    venc.setText("");
                    valor.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), R.string.message_erro_salvar, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.message_erro_salvar + "\n" + String.valueOf(MyException.code), Toast.LENGTH_LONG).show();
            }
        }
    }
}
