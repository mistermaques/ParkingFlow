package com.example.parkingflow.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkingflow.R;
import com.example.parkingflow.model.Reserva;

// View: gesto de arrastar um card PENDENTE para a esquerda em "Minhas reservas",
// desenhando o fundo vermelho com a lixeira. (Antes ficava dentro da MainActivity.)
public class ArrasteParaCancelar extends ItemTouchHelper.SimpleCallback {

    // Quem usa o gesto diz o que fazer quando o card for solto (abrir a confirmação)
    public interface AoSoltar {
        void aoSoltar(int posicao);
    }

    private final ReservaAdapter adaptador;
    private final AoSoltar aoSoltar;
    private final float dp;
    private final Paint pinturaFundo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Drawable lixeira;

    public ArrasteParaCancelar(Context contexto, ReservaAdapter adaptador, AoSoltar aoSoltar) {
        super(0, ItemTouchHelper.LEFT);
        this.adaptador = adaptador;
        this.aoSoltar = aoSoltar;
        this.dp = contexto.getResources().getDisplayMetrics().density;
        this.pinturaFundo.setColor(Color.parseColor("#DC2626"));
        this.lixeira = ContextCompat.getDrawable(contexto, R.drawable.ic_lixeira);
    }

    // Liga o gesto a uma lista
    public void ligarNa(RecyclerView lista) {
        new ItemTouchHelper(this).attachToRecyclerView(lista);
    }

    // Só reservas PENDENTE podem ser arrastadas (a API só cancela essas)
    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder holder) {
        int posicao = holder.getBindingAdapterPosition();
        if (posicao == RecyclerView.NO_POSITION) return 0;
        Reserva reserva = adaptador.getReserva(posicao);
        return "PENDENTE".equals(reserva.getStatus()) ? ItemTouchHelper.LEFT : 0;
    }

    // Não usamos arrastar para reordenar a lista
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder origem,
                          @NonNull RecyclerView.ViewHolder destino) {
        return false;
    }

    // Soltou o card depois de arrastar o suficiente
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder holder, int direcao) {
        aoSoltar.aoSoltar(holder.getBindingAdapterPosition());
    }

    // Desenha o fundo vermelho com a lixeira por trás do card enquanto ele é arrastado
    @Override
    public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder holder, float dX, float dY,
                            int estado, boolean arrastando) {
        View card = holder.itemView;
        if (dX < 0) {
            RectF fundo = new RectF(card.getRight() + dX, card.getTop(), card.getRight(), card.getBottom());
            canvas.drawRoundRect(fundo, 12 * dp, 12 * dp, pinturaFundo);

            if (lixeira != null) {
                int tamanho = (int) (24 * dp);
                int direita = card.getRight() - (int) (24 * dp);
                int topo = card.getTop() + (card.getHeight() - tamanho) / 2;
                lixeira.setBounds(direita - tamanho, topo, direita, topo + tamanho);
                lixeira.draw(canvas);
            }
        }
        super.onChildDraw(canvas, recyclerView, holder, dX, dY, estado, arrastando);
    }
}
