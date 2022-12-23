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
    private byte tipus_poda;
    
    /**
     * Constructor de la clase
     * @param heuristica l'heuristica del node
     * @param millorFill el fill amb la millor heuristica del node
     * @param profunditat la profunditat que per sota aquest node
     * @param tColor tauler amb el color per saber quin jugador ocupa x posicio
     * @param tOcupat tauler amb la posicio per saber si x posicio esta agafada per algun jugador o esta buida
     * @param tipus_poda valor per saber d'on prove l'heuristica:
     * 0 -> sense poda, la heuristica = la heuristica
     * 1 -> poda alpha, la heuristica = alpha
     * 2 -> poda beta,  la heuristica = beta 
     */
    public HashInfo(int heuristica, int millorFill, int profunditat, BitSet tColor, BitSet tOcupat, int tipus_poda) {
        this.heuristica = heuristica;
        this.millorFill = (byte)millorFill;
        this.profunditat = (byte)profunditat;
        this.tColor = tColor.toLongArray()[0];
        this.tOcupat = tOcupat.toLongArray()[0];
        this.tipus_poda = (byte)tipus_poda;
    }

    /**
     * Funcio per retornar l'heuristica
     * @return l'heuristica
     */
    public int getHeuristica() {
        return heuristica;
    }

    /**
     * Funcio per retornar el millor fill
     * @return el millor fill
     */
    public short getMillorFill() {
        return millorFill;
    }

    /**
     * Funcio per retornar la profunditat
     * @return la profunditat
     */
    public short getProfunditat() {
        return profunditat;
    }

    /**
     * Funcio per retornar el color del tauler
     * @return el color del tauler
     */
    public long gettColor() {
        return tColor;
    }

    /**
     * Funcio per retornar l'ocupacio del tauler
     * @return l'ocupacio del tauler
     */
    public long gettOcupat() {
        return tOcupat;
    }
    
    /**
     * Funcio per retornar el tipus de poda de l'heuristica
     * @return el tipus de poda de l'heuristica
     */
    public byte getTipusPoda(){
        return tipus_poda;
    }
    
}
