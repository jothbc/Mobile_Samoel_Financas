package jcr.br.financas.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import jcr.br.financas.R;
import jcr.br.financas.funcoes.Conv;
import jcr.br.financas.model.Imposto;

public class ImpostoAdapter extends RecyclerView.Adapter<ImpostoAdapter.ImpostoHolder> {
    private List<Imposto> impostoList;

    public ImpostoAdapter(List<Imposto> impostos) {
        this.impostoList = impostos;
    }

    @NonNull
    @Override
    public ImpostoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.linha_boleto_imposto, parent, false);
        ImpostoHolder impostoHolder = new ImpostoHolder(view);
        return impostoHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImpostoHolder holder, int position) {
        if (impostoList != null && !impostoList.isEmpty()) {
            Imposto imposto = impostoList.get(position);
            holder.vencimento.setText(imposto.getVencimento());
            holder.valor.setText(Conv.colocarPontoEmValor(Conv.validarValue(imposto.getValor())));
            holder.fornecedor.setText(imposto.getDescricao());
            if (imposto.getPago() != null)
                holder.pago.setText(imposto.getPago());
            else
                holder.pago.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return impostoList.size();
    }

    public class ImpostoHolder extends RecyclerView.ViewHolder {
        TextView vencimento, pago, fornecedor, valor;

        public ImpostoHolder(@NonNull View itemView) {
            super(itemView);
            vencimento = itemView.findViewById(R.id.txt_data);
            pago = itemView.findViewById(R.id.txt_pago);
            valor = itemView.findViewById(R.id.txt_valor);
            fornecedor = itemView.findViewById(R.id.txt_fornecedor);
        }
    }
}
