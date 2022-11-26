/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.othello.players.abracitos;

/**
 *
 * @author Dario
 */
public class Pair {

    private int heur;
    private boolean win;

    //en caso de que encuentre dos opciones ganadoras o perdedoras, le dara igual por que rama ir, cuando alomejor si la heuristica anterior era superior en una rama, habra mas opciones de que llegamos a ganar por esa rama
    //tabla heuristica deberia ser dinamica? porque sino al tener una esquina, justo el lado lo tenemos con numeros muy bajos cuando deberian ser muy altos
    //la produndidad es 1, es normal?
    public Pair(int heur, boolean win) {
        this.heur = heur;
        this.win = win;
    }

    public int getHeur() {
        return heur;
    }

    public boolean isWin() {
        return win;
    }
}
