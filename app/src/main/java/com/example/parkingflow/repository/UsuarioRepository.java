package com.example.parkingflow.repository;

import com.example.parkingflow.model.Usuario;

// Único lugar que sabe quais usuários existem.
// Hoje são dois logins fixos; quando existir a User API, só este arquivo muda.
public class UsuarioRepository {

    // ===== Logins fixos (até existir a User API) =====
    private static final Usuario[] USUARIOS = {
            new Usuario("sophia", "1234", "Sophia", 1L),
            new Usuario("gabriel", "1234", "Gabriel", 4L)
    };

    // Devolve o usuário com esse login e senha, ou null se não existir
    public Usuario autenticar(String login, String senha) {
        for (Usuario usuario : USUARIOS) {
            if (usuario.confere(login, senha)) {
                return usuario;
            }
        }
        return null;
    }
}
