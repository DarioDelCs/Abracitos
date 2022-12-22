package edu.upc.epsevg.prop.othello.players.abracitos;

import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import edu.upc.epsevg.prop.othello.IAuto;
import edu.upc.epsevg.prop.othello.IPlayer;
import edu.upc.epsevg.prop.othello.Move;
import edu.upc.epsevg.prop.othello.SearchType;
import java.awt.Point;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * @author Dario, Xavi
 */
public class Abracitos_Thread implements IPlayer, IAuto {

    private String name;
    
    private CellType jugador = null;
    private CellType jugador_enemic = null;
    private boolean timeout = false;
    private int maxima_profunditat;
    private int profunditat_IDS;
    private long nodes;
    private int dimensio_taula;
    
    private ZobristHashing taula_hash = null;
    private byte num_threads;
    
    
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

    public Abracitos_Thread(String name, int mode) {
        this.name = name;
        if(mode == 1){
            this.dimensio_taula = 150001;//40949;
            this.num_threads = 2;
        }else if(mode == 2){
            this.dimensio_taula = 199999;
            this.num_threads = 4;
        }
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
        this.profunditat_IDS = 2;
        
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
        taula_hash = new ZobristHashing(dimensio_taula);
        int millor_posicio = 0;
        ArrayList<Point> moves = gs.getMoves();
        
        do{
            boolean firstLoop = true;
            int millor_heur = Integer.MIN_VALUE;
            int millor_posicio_prof = 0;
            List<Callable<AbracitosInfo>> tasques = new ArrayList<>();
            
            for (int i : getMovimientos(gs, moves, true)) {
                AbracitosGame game_aux = new AbracitosGame(gs);
                game_aux.movePiece(moves.get(i));

                if (game_aux.isGameOver()) {
                    if(game_aux.GetWinner() == jugador){
                        return new Move(moves.get(i), nodes, maxima_profunditat,  SearchType.MINIMAX);
                    }

                } else {
                    if(firstLoop){
                        int alpha = minimitza(game_aux, profunditat_IDS - 1, 1, millor_heur, Integer.MAX_VALUE);

                        if (alpha > millor_heur) {
                            millor_posicio_prof = i;
                            millor_heur = alpha;
                        }
                        firstLoop = false;
                    }else{
//                System.out.println("Millor Heuristica -> " + millor_heur);
                        Task task = new Task(game_aux, millor_heur, i);
                        //executor.execute(task);
                        tasques.add(task);
                    }
                }
            }
          
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(this.num_threads);
            List<Future<AbracitosInfo>> resultats;
            try {
                resultats = executor.invokeAll(tasques);
                while(executor.getCompletedTaskCount() != executor.getTaskCount()){}
                
                for (Future<AbracitosInfo> resultat : resultats) {
                    AbracitosInfo info_final = resultat.get();
                    
//System.out.println("heu -> " + resultat.get().getMillor_heur() + " _ pos -> " + resultat.get().getPosicio());
                    
                    if(info_final.getMillor_heur() > millor_heur){
                        millor_heur = info_final.getMillor_heur();
                        millor_posicio_prof = info_final.getPosicio();
                    }
                }
                
            } catch (InterruptedException ex) {
                System.out.println("InterruptedException -> ");
                ex.printStackTrace();
                return null;
            } catch (ExecutionException ex) {
                System.out.println("ExecutionException -> ");
                ex.printStackTrace();
                return null;
            }catch (Exception e){
                System.out.println("ERROR -> " + e.getMessage());
                e.printStackTrace();
                return null;
            }
            
            
            if(!timeout){
                taula_hash.actualitza(gs, new HashInfo(millor_heur, millor_posicio_prof, profunditat_IDS, gs.getBoard_color(), gs.getBoard_occupied(), 0));
                millor_posicio = millor_posicio_prof;
            }
            if(gs.getEmptyCellsCount() < 20){
                profunditat_IDS ++;
            }else{
                profunditat_IDS += 2;
            }
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
            taula_hash.actualitza(ag, new HashInfo(heur, -1, profunditat_IDS - profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 0));
            return heur;
        }
        
        int nova_alpha = Integer.MIN_VALUE;
        int vella_alpha = Integer.MIN_VALUE;
        int millor_posicio = 0;
        
        HashInfo info = taula_hash.getInfo(ag);
        if(info != null && info.gettColor() == ag.getBoard_color().toLongArray()[0] && info.gettOcupat() == ag.getBoard_occupied().toLongArray()[0] && profunditat_IDS - profunditat > info.getProfunditat()){
            if (info.getTipusPoda() == 1){
                alpha = info.getHeuristica();
            }else if (info.getTipusPoda() == 0){
                taula_hash.actualitza(ag, new HashInfo(info.getHeuristica(), info.getMillorFill(), profunditat_IDS - profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 0));
                return info.getHeuristica();
            }
        }
        
        for (int i : getMovimientos(ag, moves, false)) {
            
            AbracitosGame game_aux = new AbracitosGame(ag);
            game_aux.movePiece(moves.get(i));
            
            
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador){
                    taula_hash.actualitza(game_aux, new HashInfo(Integer.MAX_VALUE, i, profunditat_IDS - profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 0));
                    return Integer.MAX_VALUE;
                }
                
            } else {
                vella_alpha = nova_alpha;
                nova_alpha = Math.max(nova_alpha, minimitza(game_aux, profunditat - 1, max_profunditat + 1, alpha, beta));
                if(vella_alpha != nova_alpha){
                    millor_posicio = i;
                }
                
                alpha = Math.max(nova_alpha, alpha);
                if (alpha >= beta) {
                    taula_hash.actualitza(ag, new HashInfo(alpha, -1, profunditat_IDS - profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 1));
                    return alpha;
                }
            }
        }
        
        taula_hash.actualitza(ag, new HashInfo(alpha, millor_posicio, profunditat_IDS - profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 0));
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
            taula_hash.actualitza(ag, new HashInfo(heur, -1, profunditat_IDS - profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 0));
            return heur;
        }
        
        int nova_beta = Integer.MAX_VALUE;
        int vella_beta = Integer.MAX_VALUE;
        int millor_tirada = 0;
        
        HashInfo info = taula_hash.getInfo(ag);
        if(info != null && info.gettColor() == ag.getBoard_color().toLongArray()[0] && info.gettOcupat() == ag.getBoard_occupied().toLongArray()[0] && profunditat_IDS - profunditat > info.getProfunditat()){
            if (info.getTipusPoda() == 2){
                beta = info.getHeuristica();
            }else if (info.getTipusPoda() == 0){
                taula_hash.actualitza(ag, new HashInfo(info.getHeuristica(), info.getMillorFill(), profunditat_IDS - profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 0));
                return info.getHeuristica();
            }
        }
        
        for (int i : getMovimientos(ag, moves, false)) {
            
            AbracitosGame game_aux = new AbracitosGame(ag);
            game_aux.movePiece(moves.get(i));
            
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador_enemic){
                    taula_hash.actualitza(ag, new HashInfo(Integer.MIN_VALUE, i, profunditat_IDS - profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 0));
                    return Integer.MIN_VALUE;
                }
                
            } else {
                vella_beta = nova_beta;
                nova_beta = Math.min(nova_beta, maximitza(game_aux, profunditat - 1, max_profunditat + 1, alpha, beta));
                if(vella_beta != nova_beta){
                    millor_tirada = i;
                }
                
                beta = Math.min(nova_beta, beta);
                if (alpha >= beta) {
                    taula_hash.actualitza(ag, new HashInfo(beta, -1, profunditat_IDS - profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 2));
                    return beta;
                }
            }
        }
        
        taula_hash.actualitza(ag, new HashInfo(beta, millor_tirada, profunditat_IDS - profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 0));
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
        
        int x,y,my_front_tiles = 0, opp_front_tiles = 0;
        int X1[] = {-1, -1, 0, 1, 1, 1, 0, -1};
	int Y1[] = {0, 1, 1, 1, 0, -1, -1, -1};
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (ag.getPos(i, j) == this.jugador) {
                    puntuacio += taula_heur[i][j];
                } else if (ag.getPos(i, j) == this.jugador_enemic) {
                    puntuacio -= taula_heur[i][j];
                }
                if(ag.getPos(i, j) != CellType.EMPTY)   {
                        for(int k=0; k<8; k++)  {
                                x = i + X1[k]; y = j + Y1[k];
                                if(x >= 0 && x < 8 && y >= 0 && y < 8 && ag.getPos(x, y) == CellType.EMPTY) {
                            if(ag.getPos(i, j) == this.jugador)  my_front_tiles++;
                            else opp_front_tiles++;
                            break;
                        }
                    }
                }
            }
        }
        
        //Heuristica dels anells
        if(my_front_tiles > opp_front_tiles)
		puntuacio = -(200 * my_front_tiles)/(my_front_tiles + opp_front_tiles);
	else if(my_front_tiles < opp_front_tiles)
		puntuacio = (200 * opp_front_tiles)/(my_front_tiles + opp_front_tiles);
        
        //heuristica de corners
        if(player_corners + enemy_corners != 0){
            puntuacio += 500 * (player_corners - enemy_corners) / (player_corners + enemy_corners);
        }
        
        //heuristica mobility
        int player_moves = ag.getMoves().size();
        ag.changePlayer(jugador_enemic);
        int enemic_moves = ag.getMoves().size();
        ag.changePlayer(jugador);
        if(player_moves + enemic_moves != 0){
            puntuacio += 100 * (player_moves - enemic_moves) / (player_moves + enemic_moves);
        }
        
        //heuristica de coin party
        puntuacio += 50 * (ag.getScore(jugador) - ag.getScore(jugador_enemic)) / (ag.getScore(jugador) + ag.getScore(jugador_enemic));
        
        return puntuacio;
    }
    //https://play-othello.appspot.com/files/Othello.pdf
    //si el enemigo tiene menos movimientos deberia aumentar la heuristica
    
    private int[] getMovimientos(AbracitosGame ag, ArrayList<Point> moves, boolean a){
        
        int[] movimientos_disponibles = new int[moves.size()];
        HashInfo hi = this.taula_hash.getInfo(ag);
        
        for (int i = 0; i < moves.size(); i++) {
            movimientos_disponibles[i] = i;
        }
        
//        if(a){
//            System.out.println("----------------");
//            if(hi != null){
//                System.out.println(hi.getMillorFill());
//                System.out.println(hi.gettColor());
//                System.out.println(ag.getBoard_color().toLongArray()[0]);
//                System.out.println(hi.gettOcupat());
//                System.out.println(ag.getBoard_occupied().toLongArray()[0]);
//            }else{
//                System.out.println("es null");
//            }
//        }
        
        if(hi != null && hi.getMillorFill() > 0 && hi.gettColor() == ag.getBoard_color().toLongArray()[0] && hi.gettOcupat() == ag.getBoard_occupied().toLongArray()[0]){
            movimientos_disponibles[hi.getMillorFill()] = 0;
            movimientos_disponibles[0] = hi.getMillorFill();
        }
        
        return movimientos_disponibles;
    }

    private class Task implements Callable<AbracitosInfo>{

        private AbracitosGame game_aux;
        private int millor_heur;
        private int posicio;

        public Task(AbracitosGame game_aux, int millor_heur, int millor_posicio_prof) {
            this.game_aux = game_aux;
            this.millor_heur = millor_heur;
            this.posicio = millor_posicio_prof;
        }

        @Override
        public AbracitosInfo call() throws Exception {
            int alpha = minimitza(game_aux, profunditat_IDS - 1, 1, millor_heur, Integer.MAX_VALUE);

//                System.out.println("1alpha -> " + alpha + " _ heur -> " + millor_heur);
            if (alpha > millor_heur) {
//                System.out.println("2alpha -> " + alpha + " _ heur -> " + millor_heur);
                millor_heur = alpha;
            }
            
            return new AbracitosInfo(millor_heur, posicio);
        }
        
    }

}