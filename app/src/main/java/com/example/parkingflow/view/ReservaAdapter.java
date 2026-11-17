package com.example.parkingflow.view;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkingflow.R;
import com.example.parkingflow.model.Reserva;
import com.example.parkingflow.util.Formatos;

import java.util.ArrayList;
import java.util.List;

// View: lista de "Minhas reservas". Guarda as reservas e monta um card (item_reserva.xml) para cada uma,
// com a etiqueta colorida do status. (Antes quem preenchia o card era a MainActivity.)
public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.CardViewHolder> {

    private final List<Reserva> reservas = new ArrayList<>();

    // Troca a lista inteira (usado ao carregar da API)
    public void atualizar(List<Reserva> novas) {
        reservas.clear();
        reservas.addAll(novas);
        notifyDataSetChanged();
    }

    // Reserva que está em uma posição da lista
    public Reserva getReserva(int posicao) {
        return reservas.get(posicao);
    }

    // Redesenha um card, devolvendo ele ao lugar depois de um arraste desistido
    public void restaurar(int posicao) {
        notifyItemChanged(posicao);
    }

    // Cria um card novo a partir do item_reserva.xml
    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reserva, parent, false);
        return new CardViewHolder(card);
    }

    // Preenche um card com a reserva da posição indicada
    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);
        View card = holder.itemView;

        ((TextView) card.findViewById(R.id.txtItemVaga)).setText("Vaga " + reserva.getNomeVaga());
        ((TextView) card.findViewById(R.id.txtItemId)).setText("Reserva #" + reserva.getId());
        ((TextView) card.findViewById(R.id.txtItemPlaca)).setText(reserva.getPlacaVeiculo());
        ((TextView) card.findViewById(R.id.txtItemEntrada)).setText(Formatos.dataDaApi(reserva.getDataInicio()));
        ((TextView) card.findViewById(R.id.txtItemSaida)).setText(Formatos.dataDaApi(reserva.getDataFim()));
        ((TextView) card.findViewById(R.id.txtItemTotal)).setText(Formatos.dinheiroOuTraco(reserva.getPrecoReserva()));

        pintarStatus(card.findViewById(R.id.txtItemStatus), reserva.getStatus());
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    // Texto e cores da etiqueta conforme o status
    private void pintarStatus(TextView etiqueta, String status) {
        String texto;
        String corFundo;
        String corTexto;

        if ("PENDENTE".equals(status)) {
            texto = "Pendente";
            corFundo = "#FEF3C7";
            corTexto = "#92400E";
        } else if ("EM_USO".equals(status)) {
            texto = "Em uso";
            corFundo = "#DCFCE7";
            corTexto = "#166534";
        } else if ("FINALIZADA".equals(status)) {
            texto = "Finalizada";
            corFundo = "#E5E7EB";
            corTexto = "#374151";
        } else if ("CANCELADA".equals(status)) {
            texto = "Cancelada";
            corFundo = "#FEE2E2";
            corTexto = "#991B1B";
        } else {
            texto = (status == null) ? "-" : status;
            corFundo = "#E5E7EB";
            corTexto = "#374151";
        }

        etiqueta.setText(texto);
        etiqueta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(corFundo)));
        etiqueta.setTextColor(Color.parseColor(corTexto));
    }

    // "Suporte" de um card na lista (a RecyclerView reaproveita os cards ao rolar)
    static class CardViewHolder extends RecyclerView.ViewHolder {
        CardViewHolder(@NonNull View card) {
            super(card);
        }
    }
}
