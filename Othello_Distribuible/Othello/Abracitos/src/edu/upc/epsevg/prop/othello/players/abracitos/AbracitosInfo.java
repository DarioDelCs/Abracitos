/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.othello.players.abracitos;

/**
 *
 * @author Dario
 */
public class AbracitosInfo {
        private int millor_heur;
        private int posicio;

    public AbracitosInfo(int millor_heur, int posicio) {
        this.millor_heur = millor_heur;
        this.posicio = posicio;
    }

    public int getMillor_heur() {
        return millor_heur;
    }

    public int getPosicio() {
        return posicio;
    }

    @Override
    public String toString() {
        return "AbracitosInfo{" + "millor_heur=" + millor_heur + ", posicio=" + posicio + '}';
    }
}
