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

    private final HashMap<Integer, HashInfo> zobrist;
    private final int[][][] tablero;
    
    private final HashInfo[] vector_auxiliar;

    private final int moviment_jugador1;
    
    public ZobristHashing() {
        this.zobrist = new HashMap<>();
        this.tablero = new int[8][8][3];
        this.vector_auxiliar = new HashInfo[1];
        
        Random rand = new Random();
        
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                for(int k = 0; k < 3; k++) {
                    this.tablero[i][j][k] = rand.nextInt();
                }
            }
        }
        
        this.moviment_jugador1 = rand.nextInt();
    }
    
    public void actualitza(GameStatus gs, HashInfo hash_info) {
        int index = this.getHash(gs);   // nos faltaria modulo x para que sea el indice, sino esto es incorrecto
        HashInfo map_info = this.zobrist.get(index);
        
        if(map_info.gettColor().toLongArray()[0] == hash_info.gettColor().toLongArray()[0])
        
        
        
        
        
        
        if(hash_info.gettColor() == gs.getBoard_color() && info.gettOcupat().toLongArray()[0] == gs.getBoard_occupied().toLongArray()[0]){
            return info.getHeuristica();
        }
        
        if (hash_info == null || hash_info.getProf() < hash_info.getProf()){
            this.zobrist.put(hash, hash_info);
        } 
        
        
        
        
        this.zobrist.put(index, hash_info);
    }
    
    public HashInfo getInfo(GameStatus gs) {
        int hash = this.getHash(gs);
        if (this.zobrist.containsKey(hash)) {
            HashInfo info = this.zobrist.get(hash);
//            if (info.profundidad >= profundidad) {
//                return info;
//            }
        }
        return null;
    }
    
    public int getHash (GameStatus gs) {
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
