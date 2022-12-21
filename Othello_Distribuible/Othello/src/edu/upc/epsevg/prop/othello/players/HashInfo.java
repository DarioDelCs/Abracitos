/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.othello.players;

import java.util.BitSet;

/**
 *
 * @author Dario
 */
public class HashInfo {
    
    private int heuristica;
    private byte millorFill;
    private byte profunditat;
    private long tColor;
    private long tOcupat;
    private byte tipus_poda;
    /*
    tipus_poda:
    0 -> sin poda, la heuristica = la heuristica
    1 -> poda alpha, la heuristica = alpha
    2 -> poda beta, la heuristica = beta
    */
    
    public HashInfo(int heuristica, int millorFill, int profunditat, BitSet tColor, BitSet tOcupat, int tipus_poda) {
        this.heuristica = heuristica;
        this.millorFill = (byte)millorFill;
        this.profunditat = (byte)profunditat;
        this.tColor = tColor.toLongArray()[0];
        this.tOcupat = tOcupat.toLongArray()[0];
        this.tipus_poda = (byte)tipus_poda;
    }

    public int getHeuristica() {
        return heuristica;
    }

    public short getMillorFill() {
        return millorFill;
    }

    public short getProfunditat() {
        return profunditat;
    }

    public long gettColor() {
        return tColor;
    }

    public long gettOcupat() {
        return tOcupat;
    }
    
    public byte getTipusPoda(){
        return tipus_poda;
    }
    
}
