package com.example.parkingflow.model;

// Model: um usuário do app (por enquanto, os logins fixos do UsuarioRepository)
public class Usuario {

    private final String login;
    private final String senha;
    private final String nome;
    private final long id;      // ID usado nas APIs

    public Usuario(String login, String senha, String nome, long id) {
        this.login = login;
        this.senha = senha;
        this.nome = nome;
        this.id = id;
    }

    // Confere se o login e a senha digitados são deste usuário
    public boolean confere(String login, String senha) {
        return this.login.equals(login) && this.senha.equals(senha);
    }

    public String getNome() { return nome; }
    public long getId() { return id; }
}
