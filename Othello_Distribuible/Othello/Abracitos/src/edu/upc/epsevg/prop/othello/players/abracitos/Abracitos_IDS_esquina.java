package edu.upc.epsevg.prop.othello.players.abracitos;

import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import edu.upc.epsevg.prop.othello.IAuto;
import edu.upc.epsevg.prop.othello.IPlayer;
import edu.upc.epsevg.prop.othello.Move;
import edu.upc.epsevg.prop.othello.SearchType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dario, Xavi
 */
public class Abracitos_IDS_esquina implements IPlayer, IAuto {

    private String name;
    
    private CellType jugador = null;
    private CellType jugador_enemic = null;
    private boolean timeout = false;
    private int maxima_profunditat;
    private int profunditat_IDS;
    private long nodes;
    
    private List<Integer> millor_posicio = null;
    
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

    public Abracitos_IDS_esquina() {
        this.name = "Abracitos";
    }

    @Override
    public void timeout() {
        test("TIMEOUT");
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
        this.profunditat_IDS = 1;
        
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
        millor_posicio = new ArrayList();
        millor_posicio.add(0);
        
        
        do{
            int millor_heur = Integer.MIN_VALUE;
            int millor_posicio_prof = 0;
            
            for (int i : getMovimientos(moves, profunditat_IDS, true)) {
                int alpha = Integer.MIN_VALUE;

                AbracitosGame game_aux = new AbracitosGame(gs);
                game_aux.movePiece(moves.get(i));

                if (game_aux.isGameOver()) {
                    if(game_aux.GetWinner() == jugador){
                        test("pos win: " + moves.get(i));
                        return new Move(moves.get(i), nodes, maxima_profunditat,  SearchType.MINIMAX);
                    }

                } else {
                    test("Entrando al primer min");
                    test("i -> " + i + " mp0 -> " + millor_posicio.get(0));
                    alpha = minimitza(game_aux, profunditat_IDS - 1, 1, millor_heur, Integer.MAX_VALUE, (i==millor_posicio.get(0)));

                    if (alpha > millor_heur) {
                        millor_posicio_prof = i;
                        millor_heur = alpha;
                    }
                }
            }
            if(!timeout){
                test("update -> 0 -> " + millor_posicio_prof);
                millor_posicio.set(0, millor_posicio_prof);
                //reiniciamos los hijos
                for (int j = 1; j < millor_posicio.size(); j++) {
                    millor_posicio.set(j, 0);
                }
            }
            profunditat_IDS++;
            millor_posicio.add(0);
        }while(!timeout);
        
        test("------------------------------------------");
        test("");
        test("");
        test("");
        test("------------------------------------------");
        
        return new Move(moves.get(millor_posicio.get(0)), nodes, maxima_profunditat,  SearchType.MINIMAX);
    }
    
    /**
     * Funcion que ens indica l'heuristica mes gran trobada per totes les tirades analitzades.
     * @param t taulell sobre el que s'esta jugant
     * @param profunditat profunditat fins la que s'explorara l'arbre de posibilitats
     * @param alpha valor heuristic mes alt trobat fins al moment per fer la poda
     * @param beta valor heuristic mes baix trobat fins al moment per fer la poda
     * @return retorna la heuristica mes alta de totes les tirades analitzades
     */
    public int maximitza (AbracitosGame gs, int profunditat, int max_profunditat, int alpha, int beta, boolean millor_pares){
        ArrayList<Point> moves =  gs.getMoves();
        
        
        test("Prof+ -> " + profunditat);
        test("Max   -> " + moves.size());
        
        
        if(max_profunditat > maxima_profunditat){
            maxima_profunditat = max_profunditat;
        }
        if (timeout || moves.isEmpty() || profunditat == 0) {
            return heur(gs);//if timeout return 0;
        }
        
        int nova_alpha = Integer.MIN_VALUE;
        int vella_alpha = Integer.MIN_VALUE;
        for (int i : getMovimientos(moves, profunditat, millor_pares)) {
            
            AbracitosGame game_aux = new AbracitosGame(gs);
            game_aux.movePiece(moves.get(i));
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador){
                test("a -> ");
                    millor_posicio.set(profunditat_IDS - profunditat, i);
                test("update -> " + (profunditat_IDS - profunditat) + " -> " + i);
        test("List+ -> " + millor_posicio.toString());
                    return Integer.MAX_VALUE;
                }
                
            } else {
                test("to min i -> " + i);
                nova_alpha = Math.max(nova_alpha, minimitza(game_aux, profunditat - 1, max_profunditat + 1, alpha, beta, (i==millor_posicio.get(profunditat_IDS - profunditat))));
                alpha = Math.max(nova_alpha, alpha);
                if (alpha >= beta) {
                test("b -> ");
                    millor_posicio.set(profunditat_IDS - profunditat, i);
                    //reiniciamos los hijos
                    for (int j = profunditat_IDS - profunditat; j < millor_posicio.size(); j++) {
                        millor_posicio.set(j, 0);
                    }
                test("update -> " + (profunditat_IDS - profunditat) + " -> " + i);
        test("List+ -> " + millor_posicio.toString());
                    return alpha;
                }
                if(vella_alpha != nova_alpha){
                    vella_alpha = nova_alpha;
                test("c -> ");
                    millor_posicio.set(profunditat_IDS - profunditat, i);
                    //reiniciamos los hijos
                    for (int j = profunditat_IDS - profunditat; j < millor_posicio.size(); j++) {
                        millor_posicio.set(j, 0);
                    }
                test("update -> " + (profunditat_IDS - profunditat) + " -> " + i);
        test("List+ -> " + millor_posicio.toString());
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
    public int minimitza (AbracitosGame gs, int profunditat, int max_profunditat, int alpha, int beta, boolean millor_pares){
        ArrayList<Point> moves =  gs.getMoves();
        
        
        test("Prof- -> " + profunditat);
        test("Min   -> " + moves.size());
        
        
        if(max_profunditat > maxima_profunditat){
            maxima_profunditat = max_profunditat;
        }
        if (timeout || moves.isEmpty() || profunditat == 0) {
            if(moves.isEmpty()){
                test("empty");
            }
            test("end T-> " + (timeout?"SI":"NO") + " ME -> " + (moves.isEmpty()?"SI":"NO") + " PROF -> " + (profunditat == 0?"SI":"NO"));
            return heur(gs);
        }
        
        int nova_beta = Integer.MAX_VALUE;
        int vella_beta = Integer.MAX_VALUE;
        for (int i : getMovimientos(moves, profunditat, millor_pares)) {
            
            AbracitosGame game_aux = new AbracitosGame(gs);
            game_aux.movePiece(moves.get(i));
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador_enemic){
                    millor_posicio.set(profunditat_IDS - profunditat, i);
                test("update -> " + (profunditat_IDS - profunditat) + " -> " + i);
        test("List- -> " + millor_posicio.toString());
                    return Integer.MIN_VALUE;
                }
                
            } else {
                test("to max i -> " + i);
                nova_beta = Math.min(nova_beta, maximitza(game_aux, profunditat - 1, max_profunditat + 1, alpha, beta, (i==millor_posicio.get(profunditat_IDS - profunditat))));
                beta = Math.min(nova_beta, beta);
                if (alpha >= beta) {
                test("b  ");
                    millor_posicio.set(profunditat_IDS - profunditat, i);
                    //reiniciamos los hijos
                    for (int j = profunditat_IDS - profunditat; j < millor_posicio.size(); j++) {
                        millor_posicio.set(j, 0);
                    }
                test("update -> " + (profunditat_IDS - profunditat) + " -> " + i);
        test("List- -> " + millor_posicio.toString());
                    return beta;
                }
                if(vella_beta != nova_beta){
                test("c ");
                    vella_beta = nova_beta;
                    millor_posicio.set(profunditat_IDS - profunditat, i);
                    //reiniciamos los hijos
                    for (int j = profunditat_IDS - profunditat; j < millor_posicio.size(); j++) {
                        millor_posicio.set(j, 0);
                    }
                test("update -> " + (profunditat_IDS - profunditat) + " -> " + i);
        test("List- -> " + millor_posicio.toString());
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
                
//                if(gs.getPos(i, j) != CellType.EMPTY)   {
//                        for(int k=0; k<8; k++)  {
//                                x = i + X1[k]; y = j + Y1[k];
//                                if(x >= 0 && x < 8 && y >= 0 && y < 8 && gs.getPos(x, y) == CellType.EMPTY) {
//                            if(gs.getPos(i, j) == this.jugador)  my_front_tiles++;
//                            else opp_front_tiles++;
//                            break;
//                        }
//                    }
//                }
            }
        }
        if(my_front_tiles > opp_front_tiles)
		puntuacio = -(100 * my_front_tiles)/(my_front_tiles + opp_front_tiles);
	else if(my_front_tiles < opp_front_tiles)
		puntuacio = (100 * opp_front_tiles)/(my_front_tiles + opp_front_tiles);
	//else puntuacio = 0;
        
        //heuristica de corners
        if(player_corners + enemy_corners != 0){
            puntuacio += 100 * (player_corners - enemy_corners) / (player_corners + enemy_corners);
        }
        
        //Heuristica propers a corners
        puntuacio += cerca_corners(gs);
        
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
    
    private int[] getMovimientos(ArrayList<Point> moves, int profunditat, boolean millor_pares){
        int[] movimientos_disponibles = new int[moves.size()];
        if(!millor_pares){
            test("not better father");
            for (int i = 0; i < moves.size(); i++) {
                movimientos_disponibles[i] = i;
            }
            return movimientos_disponibles;
        }
        movimientos_disponibles[0] = millor_posicio.get(profunditat_IDS - profunditat);
        int j = 0;
        for (int i = 0; i < moves.size()-1; i++) {
            if(i==movimientos_disponibles[0]){
                j++;
            }
            movimientos_disponibles[i+1] = j;
            j++;
        }
        
        test("ProfIDS-> " + profunditat_IDS);
        test("List -> " + millor_posicio.toString());
        test("Arr  -> [");
        for (int i = 0; i < movimientos_disponibles.length; i++) {
            test(movimientos_disponibles[i] + ", ");
        }
        test("]");
        
        return movimientos_disponibles;
    }
    
    
    private void test(String message){
        if(false){
            System.out.println(message);
        }
    }
    
    int cerca_corners(AbracitosGame gs){
        int puntuacion = 0;
        
	int my_tiles = 0,opp_tiles = 0;
	if(gs.getPos(0, 0) == CellType.EMPTY)   {
		if(gs.getPos(0, 1) == this.jugador) my_tiles++;
		else if(gs.getPos(0, 1) == this.jugador_enemic) opp_tiles++;
		if(gs.getPos(1, 1) == this.jugador) my_tiles++;
		else if(gs.getPos(1, 1) == this.jugador_enemic) opp_tiles++;
		if(gs.getPos(1, 0) == this.jugador) my_tiles++;
		else if(gs.getPos(1, 0) == this.jugador_enemic) opp_tiles++;
	}
	if(gs.getPos(0, 7) == CellType.EMPTY)   {
		if(gs.getPos(0, 6) == this.jugador) my_tiles++;
		else if(gs.getPos(0, 6) == this.jugador_enemic) opp_tiles++;
		if(gs.getPos(1, 6) == this.jugador) my_tiles++;
		else if(gs.getPos(1, 6) == this.jugador_enemic) opp_tiles++;
		if(gs.getPos(1, 7) == this.jugador) my_tiles++;
		else if(gs.getPos(1, 7) == this.jugador_enemic) opp_tiles++;
	}
	if(gs.getPos(7, 0) == CellType.EMPTY)   {
		if(gs.getPos(7, 1) == this.jugador) my_tiles++;
		else if(gs.getPos(7, 1) == this.jugador_enemic) opp_tiles++;
		if(gs.getPos(6, 1) == this.jugador) my_tiles++;
		else if(gs.getPos(6, 1) == this.jugador_enemic) opp_tiles++;
		if(gs.getPos(6, 0) == this.jugador) my_tiles++;
		else if(gs.getPos(6, 0) == this.jugador_enemic) opp_tiles++;
	}
	if(gs.getPos(7, 7) == CellType.EMPTY)   {
		if(gs.getPos(6, 7) == this.jugador) my_tiles++;
		else if(gs.getPos(6, 7) == this.jugador_enemic) opp_tiles++;
		if(gs.getPos(6, 6) == this.jugador) my_tiles++;
		else if(gs.getPos(6, 6) == this.jugador_enemic) opp_tiles++;
		if(gs.getPos(7, 6) == this.jugador) my_tiles++;
		else if(gs.getPos(7, 6) == this.jugador_enemic) opp_tiles++;
	}
        puntuacion = -10 * (my_tiles - opp_tiles);
        return puntuacion;
    }
}
