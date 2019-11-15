package jcr.br.financas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import jcr.br.financas.funcoes.CDate;

public class BoletoAdapter extends RecyclerView.Adapter<BoletoAdapter.BoletoViewHolder> {
    private List<Boleto> boletos;

    public BoletoAdapter(List<Boleto> boletos) {
        this.boletos = boletos;
    }

    @NonNull
    @Override
    public BoletoAdapter.BoletoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.linha_boleto, parent, false);
        BoletoViewHolder boletoViewHolder = new BoletoViewHolder(view);
        return boletoViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BoletoAdapter.BoletoViewHolder holder, int position) {
        if (boletos != null && !boletos.isEmpty()) {
            Boleto boleto = boletos.get(position);

            holder.data.setText(CDate.MYSQLtoPTBR(boleto.getVencimento()));
            if (boleto.getPago() != null) {
                holder.pago.setText(CDate.MYSQLtoPTBR(boleto.getPago()));
            } else {
                holder.pago.setText("");
            }
            holder.valor.setText(new DecimalFormat("0.00").format(boleto.getValor()));
            holder.fornecedor.setText(boleto.getFornecedor_id().getNome());
        }
    }

    @Override
    public int getItemCount() {
        return boletos.size();
    }

    public class BoletoViewHolder extends RecyclerView.ViewHolder {
        public TextView data, pago, valor, fornecedor;

        public BoletoViewHolder(View itemView) {
            super(itemView);
            data = itemView.findViewById(R.id.txt_data);
            pago = itemView.findViewById(R.id.txt_pago);
            valor = itemView.findViewById(R.id.txt_valor);
            fornecedor = itemView.findViewById(R.id.txt_fornecedor);
        }
    }
}
