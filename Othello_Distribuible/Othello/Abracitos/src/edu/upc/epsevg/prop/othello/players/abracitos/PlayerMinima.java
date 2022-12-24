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
 * Clase amb profunditat maxima on s'implamenta el robot per guanyar al othello
 * @author Dario, Xavi
 */
public class PlayerMinima implements IPlayer, IAuto {

    private String name;
    
    private CellType jugador = null;
    private CellType jugador_enemic = null;
    private int maxima_profunditat;
    private int profunditat;
    private long nodes;

    /**
     * Constructor de la clase
     * @param profunditat profunditat a la que arribara el minimax
     */
    public PlayerMinima(int profunditat) {
        this.name = "Abracitos";
        this.profunditat = profunditat;
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps de joc.
     */
    @Override
    public void timeout() {
    }

    /**
     * Decideix el moviment del jugador donat un tauler i un color de peça que ha de posar.
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
        this.maxima_profunditat = 0;
        
        ArrayList<Point> moves =  s.getMoves();
        if(moves.isEmpty())
        {
            // no podem moure, el moviment (de tipus Point) es passa null.
            return new Move(null, 0L, 0,  SearchType.MINIMAX);
        } else {
            //començem l'algorisme de minimax
            return novaTirada(new AbracitosGame(s), moves);
        }
    }

    /**
     * Retorna el nom del jugador que s'utlilitza per visualització a la UI
     * @return Nom del jugador
     */
    @Override
    public String getName() {
        return name;
    }
    
    /**
     * Algorisme de MiniMax per un tauler especific
     * @param gs tauler actual
     * @param moves moviments disponibles del tauler
     * @return la millor tirada que ha trobat 
     */
    public Move novaTirada(AbracitosGame gs, ArrayList<Point> moves) {
        int millor_heur = Integer.MIN_VALUE;
        Move millor_tirada = new Move(moves.get(0), 0L, 0,  SearchType.MINIMAX);
        
        //fem un bucle dels moviments disponibles
        for (int i = 0; i < moves.size(); i++) {
            int alpha = Integer.MIN_VALUE;
            
            AbracitosGame game_aux = new AbracitosGame(gs);
            game_aux.movePiece(moves.get(i));
            
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador){
                    //si el joc sacaba i hem guanyat no fa falta calcular mes moviments
                    return new Move(moves.get(i), nodes, maxima_profunditat,  SearchType.MINIMAX);
                }
                
            } else {
                //començem l'algorisme minimax per aquesta tirada
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
     * Funcio que ens indica l'heuristica mes gran trobada per totes les tirades analitzades.
     * @param gs tauler sobre el cual estem calculant la nova heuristica
     * @param profunditat profunditat que falta per acabar l'exploracio de l'arbre
     * @param max_profunditat numero de profunditat a la cual hem arribat en aquesta branca
     * @param alpha valor heuristic del max per fer la poda
     * @param beta valor heuristic del min per fer la poda
     * @return si es fulla retorna l'heuristica d'aquest tauler, si encara es branca retorna la heuristica mes alta de tots els seus fills
     */
    public int maximitza (AbracitosGame gs, int profunditat, int max_profunditat, int alpha, int beta){
        ArrayList<Point> moves =  gs.getMoves();
        
        //si aquest node esta mes profund que un anterior analitzat actualitzarem la profunditat asolida en aquesta profunditat ids
        if(max_profunditat > maxima_profunditat){
            maxima_profunditat = max_profunditat;
        }
        //si no hi ha mes moviments, o hem arribat al final del arbre, retornem l'heuristica
        if (moves.isEmpty() || profunditat == 0) {
            return heur(gs);
        }
        
        int nova_alpha = Integer.MIN_VALUE;
        //fem un bucle dels moviments disponibles
        for (int i = 0; i < moves.size(); i++) {
            
            AbracitosGame game_aux = new AbracitosGame(gs);
            game_aux.movePiece(moves.get(i));
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador){
                    //si el joc sacaba i hem guanyat no fa falta calcular mes moviments
                    return Integer.MAX_VALUE;
                }
                
            } else {
                //si algun dels resultats es millor als guardats, i es major a la beta, fem la poda alpha-beta
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
     * Funcio que ens indica l'heuristica mes petita trobada per totes les tirades analitzades.
     * @param gs tauler sobre el cual estem calculant la nova heuristica
     * @param profunditat profunditat que falta per acabar l'exploracio de l'arbre
     * @param max_profunditat numero de profunditat a la cual hem arribat en aquesta branca
     * @param alpha valor heuristic del max per fer la poda
     * @param beta valor heuristic del min per fer la poda
     * @return si es fulla retorna l'heuristica del contrincant d'aquest tauler per la jugada, si encara es branca retorna la heuristica mes baixa de tots els seus fills
     */
    public int minimitza (AbracitosGame gs, int profunditat, int max_profunditat, int alpha, int beta){
        ArrayList<Point> moves =  gs.getMoves();
        
        //si aquest node esta mes profund que un anterior analitzat actualitzarem la profunditat asolida en aquesta profunditat ids
        if(max_profunditat > maxima_profunditat){
            maxima_profunditat = max_profunditat;
        }
        //si no hi ha mes moviments, o hem arribat al final del arbre, retornem l'heuristica
        if (moves.isEmpty() || profunditat == 0) {
            return heur(gs);
        }
        
        //fem un bucle dels moviments disponibles, on el primer moviment sera la millor jugada del contrincant que ha calculat en la profunditat anterior
        int nova_beta = Integer.MAX_VALUE;
        for (int i = 0; i < moves.size(); i++) {
            
            AbracitosGame game_aux = new AbracitosGame(gs);
            game_aux.movePiece(moves.get(i));
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador_enemic){
                    //si el joc sacaba i ha guanyat el contrincant no fa falta calcular mes moviments
                    return Integer.MIN_VALUE;
                }
                
            } else {
                //si algun dels resultats es millor als guardats, i es major a la alpha, fem la poda alpha-beta
                nova_beta = Math.min(nova_beta, maximitza(game_aux, profunditat - 1, max_profunditat + 1, alpha, beta));
                beta = Math.min(nova_beta, beta);
                if (alpha >= beta) {
                    return beta;
                }
            }
        }
        
        return nova_beta;
    }
    
    /**
     * Funcio on calculem l'heuristica a partir d'un taulell
     * @param ag el taulell al cual volem calcular l'heuristica
     * @return el valor de l'heuristica
     */
    public int heur(AbracitosGame ag) {
        nodes++;
        int puntuacio = 0;
        int size = ag.getSize();
        
        //heuristica de la taula
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
        
        //heuristica dels corners, en cas de que tinguem una cantonada, els nodes anexes aumenten el seu valor heuristic
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
        
        int x,y,my_front_tiles = 0, opp_front_tiles = 0;
        int X1[] = {-1, -1, 0, 1, 1, 1, 0, -1};
	int Y1[] = {0, 1, 1, 1, 0, -1, -1, -1};
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                //puntuacio de l'heuristica de la taula
                if (ag.getPos(i, j) == this.jugador) {
                    puntuacio += taula_heur[i][j];
                } else if (ag.getPos(i, j) == this.jugador_enemic) {
                    puntuacio -= taula_heur[i][j];
                }
                //calcul de l'heuristica dels anells
                if(ag.getPos(i, j) != CellType.EMPTY)   {
                    for(int k=0; k<8; k++)  {
                        x = i + X1[k];
                        y = j + Y1[k];
                        if(x >= 0 && x < 8 && y >= 0 && y < 8 && ag.getPos(x, y) == CellType.EMPTY) {
                            if(ag.getPos(i, j) == this.jugador)  my_front_tiles++;
                            else opp_front_tiles++;
                            break;
                        }
                    }
                }
            }
        }
        
        //puntuacio de l'heuristica dels anells
        if(my_front_tiles > opp_front_tiles)
		puntuacio -= (200 * my_front_tiles)/(my_front_tiles + opp_front_tiles);
	else if(my_front_tiles < opp_front_tiles)
		puntuacio += (200 * opp_front_tiles)/(my_front_tiles + opp_front_tiles);
        
        //puntuacio de l'heuristica de corners
        if(player_corners + enemy_corners != 0){
            puntuacio += 500 * (player_corners - enemy_corners) / (player_corners + enemy_corners);
        }
        
        //puntuacio de l'heuristica mobility
        int player_moves = ag.getMoves().size();
        ag.changePlayer(jugador_enemic);
        int enemic_moves = ag.getMoves().size();
        ag.changePlayer(jugador);
        if(player_moves + enemic_moves != 0){
            puntuacio += 100 * (player_moves - enemic_moves) / (player_moves + enemic_moves);
        }
        
        //puntuacio de l'heuristica de coin party
        puntuacio += 50 * (ag.getScore(jugador) - ag.getScore(jugador_enemic)) / (ag.getScore(jugador) + ag.getScore(jugador_enemic));
        
        return puntuacio;
    }
    //varies heuristiques les hem trobat en aquest document: https://play-othello.appspot.com/files/Othello.pdf
    
}