package edu.upc.epsevg.prop.othello.players.abracitos;

import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import java.util.Random;

/**
 *
 * @author Dario
 */
public class ZobristHashing {

    private final int mida_hash;
    private final HashInfo[] zobrist;
    private final int[][][] tablero; //3 posicions, una per el jugador, una per el contrincant, i una per l'empty
    
    private final HashInfo[] vector_auxiliar;

    private final int moviment_jugador1;
    
    /**
     * Constructor de la clase del hash
     * @param mida_hash mida de la taula de hash
     */
    public ZobristHashing(int mida_hash) {
        this.mida_hash = mida_hash;
        this.zobrist = new HashInfo[mida_hash];
        this.tablero = new int[8][8][3];
        this.vector_auxiliar = new HashInfo[1];
        
        Random rand = new Random();
        
        //asignem valors aleatoris al tauler
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                for(int k = 0; k < 3; k++) {
                    this.tablero[i][j][k] = Math.abs(rand.nextInt());
                }
            }
        }
        
        this.moviment_jugador1 = Math.abs(rand.nextInt());
    }
    
    /**
     * Funcio per actualitzar la taula de hash
     * @param gs taulell del cual actualitzem
     * @param hash_info objecte amb l'informacio del node que actualitzem
     */
    public void actualitza(GameStatus gs, HashInfo hash_info) {
        int index = this.getHash(gs) % mida_hash;
        HashInfo map_info = this.zobrist[index];
        
        //actualitzarem sempre que en aquella posicio de la taula no hi hagui res o el node que hi havia tingues menys fills que l'actual
//        if(numero total fiches > que la que hi ha guardada actualitza)
        if (map_info == null || map_info.getProfunditat() <= hash_info.getProfunditat()){
            this.zobrist[index] = hash_info;
        }
    }
    
    /**
     * Funcio per obtenir l'informacio d'una posicio de la taula hash
     * @param gs tauler al cual volem obtenir l'informacio del hash
     * @return l'informacio del node que hem guardat a la taula hash, si esta buida retorna null
     */
    public HashInfo getInfo(GameStatus gs) {
        int index = this.getHash(gs) % mida_hash;
        return this.zobrist[index];
    }
    
    /**
     * Funcio privada per obtenir el hash que farem servir per obtenir el index de la taula hash
     * @param gs tauler per obtenir el hash
     * @return del hash a partir del tauler
     */
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
