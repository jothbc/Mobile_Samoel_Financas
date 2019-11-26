package jcr.br.financas;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.RadioButton;

public class ChequeActivity extends AppCompatActivity {
    private RadioButton todos, aberto, nulo, pago;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheque);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        todos = findViewById(R.id.rb_cheque_todos);
        aberto = findViewById(R.id.rb_cheque_aberto);
        nulo = findViewById(R.id.rb_cheque_nulo);
        pago = findViewById(R.id.rb_cheque_pago);
        aberto.setSelected(true);
    }

}
