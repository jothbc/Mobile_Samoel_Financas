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
import jcr.br.financas.model.Cheque;

public class ChequeAdapter extends RecyclerView.Adapter<ChequeAdapter.ChequeViewHolder> {
    List<Cheque> cheques;

    public ChequeAdapter(List<Cheque> cheques){
        this.cheques = cheques;
    }
    @NonNull
    @Override
    public ChequeAdapter.ChequeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.linha_cheque, parent, false);
        ChequeViewHolder chequeViewHolder = new ChequeViewHolder(view);
        return chequeViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChequeAdapter.ChequeViewHolder holder, int position) {
        if(cheques!=null && !cheques.isEmpty()){
            Cheque cheque = cheques.get(position);
            holder.emissao.setText(cheque.getEmissao());
            holder.vencimento.setText(cheque.getPredatado());
            holder.pago.setText(cheque.getSaque());
            holder.valor.setText(Conv.colocarPontoEmValor(Conv.validarValue(cheque.getValor())));
            holder.fornecedor.setText(cheque.getFornecedor().getNome());
        }
    }

    @Override
    public int getItemCount() {
        return cheques.size();
    }

    public class ChequeViewHolder extends RecyclerView.ViewHolder{
        public TextView emissao,vencimento,pago,valor,fornecedor;

        public ChequeViewHolder(@NonNull View itemView) {
            super(itemView);
            emissao = itemView.findViewById(R.id.txt_cheque_data_emissao);
            vencimento = itemView.findViewById(R.id.txt_cheque_data_vencimento);
            pago = itemView.findViewById(R.id.txt_cheque_data_pago);
            valor = itemView.findViewById(R.id.txt_cheque_valor);
            fornecedor = itemView.findViewById(R.id.txt_cheque_fornecedor);
        }
    }
}
