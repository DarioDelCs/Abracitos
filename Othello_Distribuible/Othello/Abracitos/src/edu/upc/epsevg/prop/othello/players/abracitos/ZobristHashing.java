/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.othello.players.abracitos;

import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author Dario
 */
public class ZobristHashing {

    private final int mida_hash;
    private final HashInfo[] zobrist;
    private final int[][][] tablero;
    
    private final HashInfo[] vector_auxiliar;

    private final int moviment_jugador1;
    
    public ZobristHashing(int mida_hash) {
        this.mida_hash = mida_hash;
        this.zobrist = new HashInfo[mida_hash];
        this.tablero = new int[8][8][3];
        this.vector_auxiliar = new HashInfo[1];
        
        Random rand = new Random();
        
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                for(int k = 0; k < 3; k++) {
                    this.tablero[i][j][k] = Math.abs(rand.nextInt());
                }
            }
        }
        
        this.moviment_jugador1 = Math.abs(rand.nextInt());
    }
    
    public void actualitza(GameStatus gs, HashInfo hash_info) {
        int index = this.getHash(gs) % mida_hash;
        HashInfo map_info = this.zobrist[index];
        
        if (map_info == null || map_info.getProfunditat() <= hash_info.getProfunditat()){
            this.zobrist[index] = hash_info;
        }
    }
    
    public HashInfo getInfo(GameStatus gs) {
        int index = this.getHash(gs) % mida_hash;
        return this.zobrist[index];
    }
    
    private int getHash (GameStatus gs) {
        int mida = gs.getSize();
        int hash = 0;
                
        for(int i = 0; i < mida; i++) {
            for (int j = 0; j < mida; j++) {
                hash ^= this.tablero[i][j][(gs.getPos(i, j) == CellType.EMPTY ? 0 : gs.getPos(i, j) == CellType.PLAYER1 ? 1 : 2 )];
            }
        }
        
        return hash ^ (gs.getCurrentPlayer() == CellType.PLAYER1 ? this.moviment_jugador1 : 0);
    }
}
