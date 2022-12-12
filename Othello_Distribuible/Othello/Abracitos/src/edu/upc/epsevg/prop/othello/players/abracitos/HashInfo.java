/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.othello.players.abracitos;

import java.util.BitSet;

/**
 *
 * @author Dario
 */
public class HashInfo {
    
    private int heuristica;
    private int millorFill;
    private int profunditat;
    private BitSet tColor;
    private BitSet tOcupat;

    public HashInfo(int heuristica, int millorFill, BitSet tColor, BitSet tOcupat, int profunditat) {
        this.heuristica = heuristica;
        this.millorFill = millorFill;
        this.tColor = tColor;
        this.tOcupat = tOcupat;
        this.profunditat = profunditat;
    }

    public int getHeuristica() {
        return heuristica;
    }

    public int getMillorFill() {
        return millorFill;
    }

    public BitSet gettColor() {
        return tColor;
    }

    public BitSet gettOcupat() {
        return tOcupat;
    }

    public int getProfunditat() {
        return profunditat;
    }
}
