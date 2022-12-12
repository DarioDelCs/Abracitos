package edu.upc.epsevg.prop.othello.players.abracitos;

import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import edu.upc.epsevg.prop.othello.IAuto;
import edu.upc.epsevg.prop.othello.IPlayer;
import edu.upc.epsevg.prop.othello.Move;
import edu.upc.epsevg.prop.othello.SearchType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Dario, Xavi
 */
public class Abracitos_IDS_Hash implements IPlayer, IAuto {

    private String name;
    
    private CellType jugador = null;
    private CellType jugador_enemic = null;
    private boolean timeout = false;
    private int maxima_profunditat;
    private int profunditat_IDS;
    private long nodes;
    private long dimensio_taula;
    
    private ZobristHashing taula_hash = null;
    
    
    /*private final int[][] taula_heur = {
        {100, -20, 10, 5, 5, 10, -20, 100},
        {-20, -50, -2,-2,-2, -2, -50, -20},
        {10 , -2 , -1,-1,-1, -1, -2 ,  10},
        {5  , -2 , -1,-1,-1, -1, -2 ,   5},
        {5  , -2 , -1,-1,-1, -1, -2 ,   5},
        {10 , -2 , -1,-1,-1, -1, -2 ,  10},
        {-20, -50, -2,-2,-2, -2, -50, -20},
        {100, -20, 10, 5, 5, 10, -20, 100}
    };*/
    /*private final int[][] taula_heur = {
        {100, 0, 0, 0, 0, 0, 0, 100},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {100, 0, 0, 0, 0, 0, 0, 100}
    };*/

    public Abracitos_IDS_Hash(String name, int dimensio_taula_GB) {
        this.name = name;
        this.dimensio_taula = 40949;
    }

    @Override
    public void timeout() {
        System.out.println("TIMEOUT");
        this.timeout = true;
    }

    /**
     * Decideix el moviment del jugador donat un tauler i un color de peça que
     * ha de posar.
     *
     * @param s Tauler i estat actual de joc.
     * @return el moviment que fa el jugador.
     */
    @Override
    public Move move(GameStatus gs) {
        if(this.jugador == null){
            jugador = gs.getCurrentPlayer();
            jugador_enemic = (jugador == CellType.PLAYER1 ? CellType.PLAYER2 : CellType.PLAYER1);
        }
        this.nodes = 0;
        this.timeout = false;
        this.maxima_profunditat = 0;
        this.profunditat_IDS = 1;
        
        ArrayList<Point> moves = gs.getMoves();
        if(moves.isEmpty())
        {
            // no podem moure, el moviment (de tipus Point) es passa null.
            return new Move(null, 0L, 0,  SearchType.MINIMAX);
        } else {
            return novaTirada(new AbracitosGame(gs));
        }
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public String getName() {
        return name;
    }
    
    public Move novaTirada(AbracitosGame gs) {
        taula_hash = new ZobristHashing();
        int millor_posicio = 0;
        ArrayList<Point> moves = gs.getMoves();
        
        do{
            int millor_heur = Integer.MIN_VALUE;
            int millor_posicio_prof = 0;
            
            for (int i : getMovimientos(gs, moves)) {
                int alpha = Integer.MIN_VALUE;
                
                AbracitosGame game_aux = new AbracitosGame(gs);
                game_aux.movePiece(moves.get(i));
                
                if (game_aux.isGameOver()) {
                    if(game_aux.GetWinner() == jugador){
                        return new Move(moves.get(i), nodes, maxima_profunditat,  SearchType.MINIMAX);
                    }

                } else {
                    alpha = minimitza(game_aux, profunditat_IDS - 1, 1, millor_heur, Integer.MAX_VALUE);

                    if (alpha > millor_heur) {
                        millor_posicio_prof = i;
                        millor_heur = alpha;
                    }
                }
            }
            
            if(!timeout && profunditat_IDS%2 != 0){//TALLAR AL MINIMITZAR
                taula_hash.actualitza(gs, new HashInfo(millor_heur, millor_posicio_prof, gs.getBoard_color(), gs.getBoard_occupied(), maxima_profunditat-1));
                millor_posicio = millor_posicio_prof;
            }
            
//            if(!timeout && profunditat_IDS%2 != 0){//TALLAR AL MINIMITZAR
//                taula_hash.update((int)(millor_heur%dimensio_taula), new HashInfo(millor_heur, millor_posicio_prof, gs.getBoard_color(), gs.getBoard_occupied()));
//            }
            profunditat_IDS++;
        }while(!timeout);
        
        return new Move(moves.get(millor_posicio), nodes, maxima_profunditat,  SearchType.MINIMAX);
    }
    
    /**
     * Funcion que ens indica l'heuristica mes gran trobada per totes les tirades analitzades.
     * @param t taulell sobre el que s'esta jugant
     * @param profunditat profunditat fins la que s'explorara l'arbre de posibilitats
     * @param alpha valor heuristic mes alt trobat fins al moment per fer la poda
     * @param beta valor heuristic mes baix trobat fins al moment per fer la poda
     * @return retorna la heuristica mes alta de totes les tirades analitzades
     */
    public int maximitza (AbracitosGame ag, int profunditat, int max_profunditat, int alpha, int beta){
        ArrayList<Point> moves =  ag.getMoves();
        
        if(max_profunditat > maxima_profunditat){
            maxima_profunditat = max_profunditat;
        }
        if (timeout || moves.isEmpty() || profunditat == 0) {
            int heur = heur(ag);
            taula_hash.actualitza(ag, new HashInfo(heur, -1, ag.getBoard_color(), ag.getBoard_occupied(), maxima_profunditat - profunditat));
            return heur;//if timeout return 0;
        }
        
        int nova_alpha = Integer.MIN_VALUE;
        int vella_alpha = Integer.MIN_VALUE;
        int millor_posicio = 0;
        
        for (int i : getMovimientos(ag, moves)) {
            
            AbracitosGame game_aux = new AbracitosGame(ag);
            game_aux.movePiece(moves.get(i));
            
            HashInfo info = taula_hash.getInfo(ag);
            if(info.gettColor() == ag.getBoard_color() && info.gettOcupat() == ag.getBoard_occupied()){
                return info.getHeuristica();
            }
            
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador){
                    taula_hash.actualitza(ag, new HashInfo(Integer.MAX_VALUE, -1, ag.getBoard_color(), ag.getBoard_occupied(), maxima_profunditat - profunditat));
                    return Integer.MAX_VALUE;
                }
                
            } else {
                nova_alpha = Math.max(nova_alpha, minimitza(game_aux, profunditat - 1, max_profunditat + 1, alpha, beta));
                alpha = Math.max(nova_alpha, alpha);
                if (alpha >= beta) {
                    taula_hash.actualitza(ag, new HashInfo(alpha, -1, ag.getBoard_color(), ag.getBoard_occupied(), maxima_profunditat - profunditat));
                    return alpha;
                }
                if(vella_alpha != nova_alpha){
                    vella_alpha = nova_alpha;
                    millor_posicio = i;
                }
            }
        }
        
        taula_hash.actualitza(ag, new HashInfo(vella_alpha, millor_posicio, ag.getBoard_color(), ag.getBoard_occupied(), maxima_profunditat - profunditat));
        
        return nova_alpha;
    }
    
    
    /**
     * Funcion que ens indica l'heuristica mes petita trobada per totes les tirades analitzades.
     * @param t taulell sobre el que s'esta jugant
     * @param profunditat profunditat fins la que s'explorara l'arbre de posibilitats
     * @param alpha valor heuristic mes alt trobat fins al moment per fer la poda
     * @param beta valor heuristic mes baix trobat fins al moment per fer la poda
     * @return retorna la heuristica mes baixa de totes les tirades analitzades
     */
    public int minimitza (AbracitosGame ag, int profunditat, int max_profunditat, int alpha, int beta){
        ArrayList<Point> moves =  ag.getMoves();
        
        if(max_profunditat > maxima_profunditat){
            maxima_profunditat = max_profunditat;
        }
        if (timeout || moves.isEmpty() || profunditat == 0) {
            int heur = heur(ag);
            taula_hash.actualitza(ag, new HashInfo(heur, -1, ag.getBoard_color(), ag.getBoard_occupied(), maxima_profunditat - profunditat));
            return heur;
        }
        
        int nova_beta = Integer.MAX_VALUE;
        int vella_beta = Integer.MAX_VALUE;
        int millor_tirada = 0;
        
        for (int i : getMovimientos(moves, profunditat)) {
            
            AbracitosGame game_aux = new AbracitosGame(ag);
            game_aux.movePiece(moves.get(i));
            
            HashInfo info = taula_hash.getInfo(ag, profunditat_IDS);
            if(info.gettColor() == ag.getBoard_color() && info.gettOcupat() == ag.getBoard_occupied()){
                return info.getHeuristica();
            }
            
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador_enemic){
                    taula_hash.actualitza(ag, new HashInfo(Integer.MIN_VALUE, -1, ag.getBoard_color(), ag.getBoard_occupied(), maxima_profunditat - profunditat));
                    return Integer.MIN_VALUE;
                }
                
            } else {
                nova_beta = Math.min(nova_beta, maximitza(game_aux, profunditat - 1, max_profunditat + 1, alpha, beta));
                beta = Math.min(nova_beta, beta);
                if (alpha >= beta) {
                    taula_hash.actualitza(ag, new HashInfo(beta, -1, ag.getBoard_color(), ag.getBoard_occupied(), maxima_profunditat - profunditat));
                    return beta;
                }
                if(vella_beta != nova_beta){
                    vella_beta = nova_beta;
                    millor_tirada = i;
                }
            }
        }
        
        taula_hash.actualitza(ag, new HashInfo(vella_beta, millor_tirada, ag.getBoard_color(), ag.getBoard_occupied(), maxima_profunditat - profunditat));
        return nova_beta;
    }
    
    public int heur(AbracitosGame ag) {
        nodes++;
        int puntuacio = 0;
        int size = ag.getSize();
        int[][] taula_heur = {
            { 4, -3, 2, 2, 2, 2, -3, 4},
            {-3, -4,-1,-1,-1,-1, -4,-3},
            { 2, -1, 1, 0, 0, 1, -1, 2},
            { 2, -1, 0, 1, 1, 0, -1, 2},
            { 2, -1, 0, 1, 1, 0, -1, 2},
            { 2, -1, 1, 0, 0, 1, -1, 2},
            {-3, -4,-1,-1,-1,-1, -4,-3},
            { 4, -3, 2, 2, 2, 2, -3, 4}
        };
        
        int player_corners = 0;
        int enemy_corners = 0;
        if (ag.getPos(0, 0) == this.jugador) {
            player_corners++;
            taula_heur[0][1] = 4;
            taula_heur[1][0] = 4;
        } else if (ag.getPos(0, 0) == this.jugador_enemic) {
            enemy_corners++;
            taula_heur[0][1] = 4;
            taula_heur[1][0] = 4;
        }
        if (ag.getPos(0, 7) == this.jugador) {
            player_corners++;
            taula_heur[0][6] = 4;
            taula_heur[1][7] = 4;
        } else if (ag.getPos(0, 7) == this.jugador_enemic) {
            enemy_corners++;
            taula_heur[0][6] = 4;
            taula_heur[1][7] = 4;
        }
        if (ag.getPos(7, 0) == this.jugador) {
            player_corners++;
            taula_heur[7][1] = 4;
            taula_heur[6][0] = 4;
        } else if (ag.getPos(7, 0) == this.jugador_enemic) {
            enemy_corners++;
            taula_heur[7][1] = 4;
            taula_heur[6][0] = 4;
        }
        if (ag.getPos(7, 7) == this.jugador) {
            player_corners++;
            taula_heur[6][7] = 4;
            taula_heur[7][6] = 4;
        } else if (ag.getPos(7, 7) == this.jugador_enemic) {
            enemy_corners++;
            taula_heur[6][7] = 4;
            taula_heur[7][6] = 4;
        }
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (gs.getPos(i, j) == this.jugador) {
                    puntuacio += taula_heur[i][j];
                } else if (gs.getPos(i, j) == this.jugador_enemic) {
                    puntuacio -= taula_heur[i][j];
                }
            }
        }
        
        //heuristica de corners
        if(player_corners + enemy_corners != 0){
            puntuacio += 100 * (player_corners - enemy_corners) / (player_corners + enemy_corners);
        }
        
        //heuristica mobility
        int player_moves = gs.getMoves().size();
        gs.changePlayer(jugador_enemic);
        int enemic_moves = gs.getMoves().size();
        gs.changePlayer(jugador);
        if(player_moves + enemic_moves != 0){
            puntuacio += 100 * (player_moves - enemic_moves) / (player_moves + enemic_moves);
        }
        
        //heuristica de coin party
        puntuacio += 100 * (gs.getScore(jugador) - gs.getScore(jugador_enemic)) / (gs.getScore(jugador) + gs.getScore(jugador_enemic));
        
        return puntuacio;
    }
    //https://play-othello.appspot.com/files/Othello.pdf
    //si el enemigo tiene menos movimientos deberia aumentar la heuristica
    
    private int[] getMovimientos(AbracitosGame ag, ArrayList<Point> moves){
        
        this.taula_hash.getInfo(ag);
        
        
        
        
        int[] movimientos_disponibles = new int[moves.size()];
//        int j = millor_posicio.get(profunditat_IDS - profunditat);
        
        for (int i = 0; i < moves.size(); i++) {
            movimientos_disponibles[i] = i;
        }
        
//        if(millor_pares){
//            movimientos_disponibles[j] = 0;
//            movimientos_disponibles[0] = j;
//        }
        
        return movimientos_disponibles;
    }
}