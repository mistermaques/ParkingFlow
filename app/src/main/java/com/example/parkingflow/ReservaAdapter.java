package com.example.parkingflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// Adapter da lista de "Minhas reservas": guarda as reservas e monta um card (item_reserva.xml) para cada uma.
// Quem preenche os textos do card é a MainActivity, pelo "preenchedor" recebido no construtor.
public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.CardViewHolder> {

    // Quem sabe preencher um card com os dados de uma reserva (a MainActivity)
    public interface Preenchedor {
        void preencher(View card, Reserva reserva);
    }

    private final List<Reserva> reservas = new ArrayList<>();
    private final Preenchedor preenchedor;

    public ReservaAdapter(Preenchedor preenchedor) {
        this.preenchedor = preenchedor;
    }

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

    // Troca uma reserva por outra na mesma posição (ex: depois de cancelar)
    public void substituir(int posicao, Reserva reserva) {
        reservas.set(posicao, reserva);
        notifyItemChanged(posicao);
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
        preenchedor.preencher(holder.itemView, reservas.get(position));
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    // "Suporte" de um card na lista (a RecyclerView reaproveita os cards ao rolar)
    static class CardViewHolder extends RecyclerView.ViewHolder {
        CardViewHolder(@NonNull View card) {
            super(card);
        }
    }
}