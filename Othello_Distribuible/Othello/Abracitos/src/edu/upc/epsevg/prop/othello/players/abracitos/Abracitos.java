package edu.upc.epsevg.prop.othello.players.abracitos;

import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import edu.upc.epsevg.prop.othello.IAuto;
import edu.upc.epsevg.prop.othello.IPlayer;
import edu.upc.epsevg.prop.othello.Move;
import edu.upc.epsevg.prop.othello.SearchType;
import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author Dario, Xavi
 */
public class Abracitos implements IPlayer, IAuto {

    private String name;
    private int heur;
    
    private CellType jugador = null;
    private CellType jugador_enemic = null;
    private boolean timeout = false;
    private int profunditat;
    private long nodes;
    
    private final int[][] taula_heur = {
        {100, -20, 10, 5, 5, 10, -20, 100},
        {-20, -50, -2,-2,-2, -2, -50, -20},
        {10 , -2 , -1,-1,-1, -1, -2 ,  10},
        {5  , -2 , -1,-1,-1, -1, -2 ,   5},
        {5  , -2 , -1,-1,-1, -1, -2 ,   5},
        {10 , -2 , -1,-1,-1, -1, -2 ,  10},
        {-20, -50, -2,-2,-2, -2, -50, -20},
        {100, -20, 10, 5, 5, 10, -20, 100}
    };

    public Abracitos() {
        this.name = "Abracitos";
    }

    @Override
    public void timeout() {
        timeout = true;
    }

    /**
     * Decideix el moviment del jugador donat un tauler i un color de peça que
     * ha de posar.
     *
     * @param s Tauler i estat actual de joc.
     * @return el moviment que fa el jugador.
     */
    @Override
    public Move move(GameStatus s) {
        if(this.jugador == null){
            jugador = s.getCurrentPlayer();
            jugador_enemic = (jugador == CellType.PLAYER1 ? CellType.PLAYER2 : CellType.PLAYER1);
        }
        
        ArrayList<Point> moves =  s.getMoves();
        if(moves.isEmpty())
        {
            // no podem moure, el moviment (de tipus Point) es passa null.
            return new Move(null, 0L, 0,  SearchType.MINIMAX);
        } else {
            return novaTirada(s, moves);
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
    
    public Move novaTirada(GameStatus gs, ArrayList<Point> moves) {
        int millor_heur = Integer.MIN_VALUE;
        Move millor_tirada = null;
        this.heur = 0;
        this.profunditat = 0;
        this.nodes = 0;
        
        for (int i = 0; i < moves.size(); i++) {
            int alpha = Integer.MIN_VALUE;
            
            GameStatus game_aux = new GameStatus(gs);
            game_aux.movePiece(moves.get(i));
            
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador){
                    
                    System.out.println("pos win: " + moves.get(i));
                    return new Move(moves.get(i), nodes, profunditat,  SearchType.MINIMAX);
                }
                
            } else {
                alpha = minimitza(game_aux, 1, millor_heur, Integer.MAX_VALUE);

                if (alpha > millor_heur || millor_tirada == null) {
                    millor_tirada = new Move(moves.get(i), nodes, profunditat,  SearchType.MINIMAX);
                    millor_heur = alpha;
                }
            }
        }
        
        System.out.println("pos fin: " + millor_tirada.getTo());
        return millor_tirada;
    }
    
    /**
     * Funcion que ens indica l'heuristica mes gran trobada per totes les tirades analitzades.
     * @param t taulell sobre el que s'esta jugant
     * @param profunditat profunditat fins la que s'explorara l'arbre de posibilitats
     * @param alpha valor heuristic mes alt trobat fins al moment per fer la poda
     * @param beta valor heuristic mes baix trobat fins al moment per fer la poda
     * @return retorna la heuristica mes alta de totes les tirades analitzades
     */
    public int maximitza (GameStatus gs, int profunditat, int alpha, int beta){
        ArrayList<Point> moves =  gs.getMoves();
        if (timeout || moves.isEmpty()) {
            if(this.profunditat < profunditat){
                this.profunditat = profunditat;
            }
            return heur(gs);
        }
        
        int nova_alpha = Integer.MIN_VALUE;
        for (int i = 0; i < moves.size(); i++) {
            
            GameStatus game_aux = new GameStatus(gs);
            game_aux.movePiece(moves.get(i));
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador){
                    return Integer.MAX_VALUE;
                }
                
            } else {
                nova_alpha = Math.max(nova_alpha, minimitza(game_aux, profunditat + 1, alpha, beta));
                alpha = Math.max(nova_alpha, alpha);
                if (alpha >= beta) {
                    return alpha;
                }
            }
        }
        
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
    public int minimitza (GameStatus gs, int profunditat, int alpha, int beta){
        ArrayList<Point> moves =  gs.getMoves();
        if (timeout || moves.isEmpty()) {
            if(this.profunditat < profunditat){
                this.profunditat = profunditat;
            }
            return heur(gs);
        }
        
        int nova_beta = Integer.MAX_VALUE;
        for (int i = 0; i < moves.size(); i++) {
            
            GameStatus game_aux = new GameStatus(gs);
            game_aux.movePiece(moves.get(i));
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador_enemic){
                    return Integer.MIN_VALUE;
                }
                
            } else {
                nova_beta = Math.min(nova_beta, maximitza(game_aux, profunditat + 1, alpha, beta));
                beta = Math.min(nova_beta, beta);
                if (alpha >= beta) {
                    return beta;
                }
            }
        }
        
        return nova_beta;
    }
    
    public int heur(GameStatus gs) {
        nodes++;
        int puntuacio = 0;
        int size = gs.getSize();
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (gs.getPos(i, j) == this.jugador) {
                    puntuacio += taula_heur[i][j];
                } else if (gs.getPos(i, j) == this.jugador_enemic) {
                    puntuacio -= taula_heur[i][j];
                }
            }
        }
        
        return puntuacio + gs.getScore(jugador)*5 - gs.getScore(jugador_enemic)*5;
    }
    //https://play-othello.appspot.com/files/Othello.pdf
    //si el enemigo tiene menos movimientos deberia aumentar la heuristica
}
