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
    private byte millorFill;
    private byte profunditat;
    private long tColor;
    private long tOcupat;
    
    public HashInfo(int heuristica, int millorFill, int profunditat, BitSet tColor, BitSet tOcupat) {
        this.heuristica = heuristica;
        this.millorFill = (byte)millorFill;
        this.profunditat = (byte)profunditat;
        this.tColor = tColor.toLongArray()[0];
        this.tOcupat = tOcupat.toLongArray()[0];
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

    
}
