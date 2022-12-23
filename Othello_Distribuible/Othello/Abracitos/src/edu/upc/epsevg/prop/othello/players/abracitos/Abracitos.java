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
    
    private CellType jugador = null;
    private CellType jugador_enemic = null;
    private boolean timeout = false;
    private int maxima_profunditat;
    private int profunditat;
    private long nodes;

    public Abracitos(int profunditat) {
        this.name = "Abracitos";
        this.profunditat = profunditat;
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
    public Move move(GameStatus s) {
        if(this.jugador == null){
            jugador = s.getCurrentPlayer();
            jugador_enemic = (jugador == CellType.PLAYER1 ? CellType.PLAYER2 : CellType.PLAYER1);
        }
        this.nodes = 0;
        this.timeout = false;
        this.maxima_profunditat = 0;
        
        ArrayList<Point> moves =  s.getMoves();
        if(moves.isEmpty())
        {
            // no podem moure, el moviment (de tipus Point) es passa null.
            return new Move(null, 0L, 0,  SearchType.MINIMAX);
        } else {
            return novaTirada(new AbracitosGame(s), moves);
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
    
    public Move novaTirada(AbracitosGame gs, ArrayList<Point> moves) {
        int millor_heur = Integer.MIN_VALUE;
        Move millor_tirada = new Move(moves.get(0), 0L, 0,  SearchType.MINIMAX);
        
        for (int i = 0; i < moves.size(); i++) {
            int alpha = Integer.MIN_VALUE;
            
            AbracitosGame game_aux = new AbracitosGame(gs);
            game_aux.movePiece(moves.get(i));
            
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador){
                    
                    System.out.println("pos win: " + moves.get(i));
                    return new Move(moves.get(i), nodes, maxima_profunditat,  SearchType.MINIMAX);
                }
                
            } else {
                alpha = minimitza(game_aux, profunditat - 1, 1, millor_heur, Integer.MAX_VALUE);

                if (alpha > millor_heur || millor_tirada == null) {
                    millor_tirada = new Move(moves.get(i), nodes, maxima_profunditat,  SearchType.MINIMAX);
                    millor_heur = alpha;
                }
            }
        }
        
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
    public int maximitza (AbracitosGame gs, int profunditat, int max_profunditat, int alpha, int beta){
        ArrayList<Point> moves =  gs.getMoves();
        if(max_profunditat > maxima_profunditat){
            maxima_profunditat = max_profunditat;
        }
        if (timeout || moves.isEmpty() || profunditat == 0) {
            return heur(gs);
        }
        
        int nova_alpha = Integer.MIN_VALUE;
        for (int i = 0; i < moves.size(); i++) {
            
            AbracitosGame game_aux = new AbracitosGame(gs);
            game_aux.movePiece(moves.get(i));
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador){
                    return Integer.MAX_VALUE;
                }
                
            } else {
                nova_alpha = Math.max(nova_alpha, minimitza(game_aux, profunditat - 1, max_profunditat + 1, alpha, beta));
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
    public int minimitza (AbracitosGame gs, int profunditat, int max_profunditat, int alpha, int beta){
        ArrayList<Point> moves =  gs.getMoves();
        if(max_profunditat > maxima_profunditat){
            maxima_profunditat = max_profunditat;
        }
        if (timeout || moves.isEmpty() || profunditat == 0) {
            return heur(gs);
        }
        
        int nova_beta = Integer.MAX_VALUE;
        for (int i = 0; i < moves.size(); i++) {
            
            AbracitosGame game_aux = new AbracitosGame(gs);
            game_aux.movePiece(moves.get(i));
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador_enemic){
                    return Integer.MIN_VALUE;
                }
                
            } else {
                nova_beta = Math.min(nova_beta, maximitza(game_aux, profunditat - 1, max_profunditat + 1, alpha, beta));
                beta = Math.min(nova_beta, beta);
                if (alpha >= beta) {
                    return beta;
                }
            }
        }
        
        return nova_beta;
    }
    
    public int heur(AbracitosGame gs) {
        nodes++;
        int puntuacio = 0;
        int size = gs.getSize();
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
        if (gs.getPos(0, 0) == this.jugador) {
            player_corners++;
            taula_heur[0][1] = 4;
            taula_heur[1][0] = 4;
        } else if (gs.getPos(0, 0) == this.jugador_enemic) {
            enemy_corners++;
            taula_heur[0][1] = 4;
            taula_heur[1][0] = 4;
        }
        if (gs.getPos(0, 7) == this.jugador) {
            player_corners++;
            taula_heur[0][6] = 4;
            taula_heur[1][7] = 4;
        } else if (gs.getPos(0, 7) == this.jugador_enemic) {
            enemy_corners++;
            taula_heur[0][6] = 4;
            taula_heur[1][7] = 4;
        }
        if (gs.getPos(7, 0) == this.jugador) {
            player_corners++;
            taula_heur[7][1] = 4;
            taula_heur[6][0] = 4;
        } else if (gs.getPos(7, 0) == this.jugador_enemic) {
            enemy_corners++;
            taula_heur[7][1] = 4;
            taula_heur[6][0] = 4;
        }
        if (gs.getPos(7, 7) == this.jugador) {
            player_corners++;
            taula_heur[6][7] = 4;
            taula_heur[7][6] = 4;
        } else if (gs.getPos(7, 7) == this.jugador_enemic) {
            enemy_corners++;
            taula_heur[6][7] = 4;
            taula_heur[7][6] = 4;
        }
        
        int x,y,my_front_tiles = 0, opp_front_tiles = 0;
        int X1[] = {-1, -1, 0, 1, 1, 1, 0, -1};
	int Y1[] = {0, 1, 1, 1, 0, -1, -1, -1};
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (gs.getPos(i, j) == this.jugador) {
                    puntuacio += taula_heur[i][j];
                } else if (gs.getPos(i, j) == this.jugador_enemic) {
                    puntuacio -= taula_heur[i][j];
                }
                if(gs.getPos(i, j) != CellType.EMPTY)   {
                    for(int k=0; k<8; k++)  {
                        x = i + X1[k];
                        y = j + Y1[k];
                        if(x >= 0 && x < 8 && y >= 0 && y < 8 && gs.getPos(x, y) == CellType.EMPTY) {
                            if(gs.getPos(i, j) == this.jugador)  my_front_tiles++;
                            else opp_front_tiles++;
                            break;
                        }
                    }
                }
            }
        }
        
        //Heuristica dels anells

        if(my_front_tiles > opp_front_tiles)
		puntuacio -= (200 * my_front_tiles)/(my_front_tiles + opp_front_tiles);
	else if(my_front_tiles < opp_front_tiles)
		puntuacio += (200 * opp_front_tiles)/(my_front_tiles + opp_front_tiles);
        
        
        //heuristica de corners
        if(player_corners + enemy_corners != 0){
            puntuacio += 500 * (player_corners - enemy_corners) / (player_corners + enemy_corners);
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
        puntuacio += 50 * (gs.getScore(jugador) - gs.getScore(jugador_enemic)) / (gs.getScore(jugador) + gs.getScore(jugador_enemic));
        
        return puntuacio;
    }
    //https://play-othello.appspot.com/files/Othello.pdf
    //si el enemigo tiene menos movimientos deberia aumentar la heuristica
}
