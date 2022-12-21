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
        private int millor_posicio_prof;

    public AbracitosInfo(int millor_heur, int millor_posicio_prof) {
        this.millor_heur = millor_heur;
        this.millor_posicio_prof = millor_posicio_prof;
    }

    public int getMillor_heur() {
        return millor_heur;
    }

    public int getMillor_posicio_prof() {
        return millor_posicio_prof;
    }
    
}
