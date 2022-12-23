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
import java.util.concurrent.Callable;
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
    private int profunditat_anterior;
    private int profunditat_IDS;
    private long nodes;
    private int dimensio_taula;
    
    private ZobristHashing taula_hash = null;
    private int num_threads;
    
    /**
     * Constructor de la clase
     * @param mode mode de joc:
     * 1 - dimensio de la taula 150.001 (0.7 GB aprox), 2 threads
     * 2 - dimensio de la taula 500.009 (0.8 GB aprox), numero maxim de nuclis de la CPU
     */
    public Abracitos_Thread(int mode) {
        this.name = "Abracitos";
        if(mode == 1){
            this.dimensio_taula = 150001;
            this.num_threads = 2;
        }else if(mode == 2){
            this.dimensio_taula = 500009;
            this.num_threads = Runtime.getRuntime().availableProcessors()/2;
        }
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps de joc.
     */
    @Override
    public void timeout() {
        System.out.println("TIMEOUT");
        this.timeout = true;
    }

    /**
     * Decideix el moviment del jugador donat un tauler i un color de peça que ha de posar.
     *
     * @param gs Tauler i estat actual de joc.
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
        this.profunditat_anterior = 0;
        this.profunditat_IDS = 2;
        this.taula_hash = new ZobristHashing(dimensio_taula);
        
        ArrayList<Point> moves = gs.getMoves();
        if(moves.isEmpty())
        {
            // no podem moure, el moviment (de tipus Point) es passa null.
            return new Move(null, 0L, 0,  SearchType.MINIMAX);
        } else {
            //començem l'algorisme de minimax
            return novaTirada(new AbracitosGame(gs));
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
     * @return la millor tirada que ha trobat 
     */
    public Move novaTirada(AbracitosGame gs) {
        int millor_posicio = 0;
        ArrayList<Point> moves = gs.getMoves();
        
        do{
            //resetajem les variables que es necesiten per cada volta del IDS
            boolean firstLoop = true;
            int millor_heur = Integer.MIN_VALUE;
            int millor_posicio_prof = 0;
            List<Callable<AbracitosInfo>> tasques = new ArrayList<>();
            
            //fem un bucle dels moviments disponibles, on el primer moviment sera la millor jugada que ha calculat en la profunditat anterior
            for (int i : getMovimientos(gs, moves)) {
                AbracitosGame game_aux = new AbracitosGame(gs);
                game_aux.movePiece(moves.get(i));
                
                if (game_aux.isGameOver()) {
                    if(game_aux.GetWinner() == jugador){
                        //si el joc sacaba i hem guanyat no fa falta calcular mes moviments
                        return new Move(moves.get(i), nodes, maxima_profunditat,  SearchType.MINIMAX);
                    }

                } else {
                    //al fer threading la primera volta te que ser sequencial ja que es molt recomenable tenir de antema una bona alpha per millorar la poda paralela
                    if(firstLoop){
                        int alpha = minimitza(game_aux, profunditat_IDS - 1, 1, millor_heur, Integer.MAX_VALUE);

                        if (alpha > millor_heur) {
                            millor_posicio_prof = i;
                            millor_heur = alpha;
                        }
                        firstLoop = false;
                    }else{
                        //genarem una nueva taska per aquesta tirada
                        Task task = new Task(game_aux, millor_heur, i);
                        tasques.add(task);
                    }
                }
            }
            
            //creem treballadors amb el numero que hem especificat en el constructor de la clase
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(this.num_threads);
            List<Future<AbracitosInfo>> resultats;//aquesta es una llista de les dades que tindrem cuan acabem les execucions
            try {
                //fem que els threads executin les tasques (les obtenen como una cua)
                resultats = executor.invokeAll(tasques);
                
                //un cop haguin acabat les tasques tindrem els resultats actualizats, veurem quin te millor heuristica i agafarem aquesta tirada
                for (Future<AbracitosInfo> resultat : resultats) {
                    AbracitosInfo info_final = resultat.get();
                    if(info_final.getMillor_heur() > millor_heur){
                        //si te millor heuristica que els anteriors ens guardarem l'heuristica i quina tirada ha sigut
                        millor_heur = info_final.getMillor_heur();
                        millor_posicio_prof = info_final.getPosicio();
                    }
                }
            }catch (Exception e){
                System.out.println("ERROR -> " + e.getMessage());
                e.printStackTrace();
                return new Move(null, 0L, 0,  SearchType.MINIMAX);
            }
            
            //en cas de que hagui explorat tota la profunditat, actualitzarem les dades de la taula, asignarem la nova posicio de la millor tirada i actualitzem la profunditat maxima
            if(!timeout){
                taula_hash.actualitza(gs, new HashInfo(millor_heur, millor_posicio_prof, profunditat_IDS, gs.getBoard_color(), gs.getBoard_occupied(), 0));
                millor_posicio = millor_posicio_prof;
                maxima_profunditat = profunditat_anterior;
            }
            //al principi de la partida jugarem de forma mes defensiva acabant sempre en la fase de minim
            //en cas de que quedin 20 tirades en el joc, observarem totes les profunditats posibles i no donarem preferencia a si hem arribat a un max o a un min
            if(gs.getEmptyCellsCount() < 20){
                profunditat_IDS ++;
            }else{
                profunditat_IDS += 2;
            }
        }while(!timeout);//IDS fins que s'acabi el temps
        
        return new Move(moves.get(millor_posicio), nodes, maxima_profunditat,  SearchType.MINIMAX);
    }
    
    /**
     * Funcio que ens indica l'heuristica mes gran trobada per totes les tirades analitzades.
     * @param ag tauler sobre el cual estem calculant la nova heuristica
     * @param profunditat profunditat que falta per acabar l'exploracio de l'arbre
     * @param max_profunditat numero de profunditat a la cual hem arribat en aquesta branca
     * @param alpha valor heuristic del max per fer la poda
     * @param beta valor heuristic del min per fer la poda
     * @return si es fulla retorna l'heuristica d'aquest tauler, si encara es branca retorna la heuristica mes alta de tots els seus fills
     */
    public int maximitza (AbracitosGame ag, int profunditat, int max_profunditat, int alpha, int beta){
        ArrayList<Point> moves =  ag.getMoves();
        
        //si aquest node esta mes profund que un anterior analitzat actualitzarem la profunditat asolida en aquesta profunditat ids
        if(max_profunditat > profunditat_anterior){
            profunditat_anterior = max_profunditat;
        }
        //si s'ha acabat el temps, no hi ha mes moviments, o hem arribat al final del arbre, actualitzarem la taula hash i retornarem la heuristica de la fulla
        if (timeout){
            return 0;
        }else if(moves.isEmpty() || profunditat == 0){
            int heur = heur(ag);
            taula_hash.actualitza(ag, new HashInfo(heur, -1, profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 0));
            return heur;
        }
        
        int nova_alpha = Integer.MIN_VALUE;
        int vella_alpha;
        int millor_posicio = 0;
        
        HashInfo info = taula_hash.getInfo(ag);
        if(info != null && info.gettColor() == ag.getBoard_color().toLongArray()[0] && info.gettOcupat() == ag.getBoard_occupied().toLongArray()[0] && profunditat < info.getProfunditat()){
            System.out.println("a");
            //en cas de que aquest node ja l'haguem calculat en una profunditat anteior
            if (info.getTipusPoda() == 1){
                //si el calcul hauristic hagues sigut de tipus poda alpha, actualitzem el valor de alpha
                alpha = info.getHeuristica();
            }else if (info.getTipusPoda() == 0){
                
                // ??????
                
                return info.getHeuristica();
            }
        }
        
        //fem un bucle dels moviments disponibles, on el primer moviment sera la millor jugada que ha calculat en la profunditat anterior
        for (int i : getMovimientos(ag, moves)) {
            
            AbracitosGame game_aux = new AbracitosGame(ag);
            game_aux.movePiece(moves.get(i));
            
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador){
                    //si el joc sacaba i hem guanyat no fa falta calcular mes moviments
                    taula_hash.actualitza(game_aux, new HashInfo(Integer.MAX_VALUE, i, profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 0));
                    return Integer.MAX_VALUE;
                }
                
            } else {
                
                // ??????
                
                vella_alpha = nova_alpha;
                nova_alpha = Math.max(nova_alpha, minimitza(game_aux, profunditat - 1, max_profunditat + 1, alpha, beta));
                if(vella_alpha != nova_alpha){
                    millor_posicio = i;
                }
                
                alpha = Math.max(nova_alpha, alpha);
                if (alpha >= beta) {
                    taula_hash.actualitza(ag, new HashInfo(alpha, -1, profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 1));
                    return alpha;
                }
            }
        }
        
        //actualitzem la taula de hash i retornem l'alpha
        taula_hash.actualitza(ag, new HashInfo(alpha, millor_posicio, profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 0));
        return nova_alpha;
    }
    
    /**
     * Funcio que ens indica l'heuristica mes petita trobada per totes les tirades analitzades.
     * @param ag tauler sobre el cual estem calculant la nova heuristica
     * @param profunditat profunditat que falta per acabar l'exploracio de l'arbre
     * @param max_profunditat numero de profunditat a la cual hem arribat en aquesta branca
     * @param alpha valor heuristic del max per fer la poda
     * @param beta valor heuristic del min per fer la poda
     * @return si es fulla retorna l'heuristica del contrincant d'aquest tauler per la jugada, si encara es branca retorna la heuristica mes baixa de tots els seus fills
     */
    public int minimitza (AbracitosGame ag, int profunditat, int max_profunditat, int alpha, int beta){
        ArrayList<Point> moves =  ag.getMoves();
        
        //si aquest node esta mes profund que un anterior analitzat actualitzarem la profunditat asolida en aquesta profunditat ids
        if(max_profunditat > profunditat_anterior){
            profunditat_anterior = max_profunditat;
        }
        //si s'ha acabat el temps, no hi ha mes moviments, o hem arribat al final del arbre, actualitzarem la taula hash i retornarem la heuristica de la fulla
        if (timeout){
            return 0;
        }else if (moves.isEmpty() || profunditat == 0) {
            int heur = heur(ag);
            taula_hash.actualitza(ag, new HashInfo(heur, -1, profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 0));
            return heur;
        }
        
        int nova_beta = Integer.MAX_VALUE;
        int vella_beta;
        int millor_tirada = 0;
        
        HashInfo info = taula_hash.getInfo(ag);
        if(info != null && info.gettColor() == ag.getBoard_color().toLongArray()[0] && info.gettOcupat() == ag.getBoard_occupied().toLongArray()[0] && profunditat < info.getProfunditat()){
            System.out.println("b");
            //en cas de que aquest node ja l'haguem calculat en una profunditat anteior
            if (info.getTipusPoda() == 2){
                //si el calcul hauristic hagues sigut de tipus poda beta, actualitzem el valor de beta
                beta = info.getHeuristica();
            }else if (info.getTipusPoda() == 0){
                
                // ??????
                
                taula_hash.actualitza(ag, new HashInfo(info.getHeuristica(), info.getMillorFill(), profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 0));
                return info.getHeuristica();
            }
        }
        
        //fem un bucle dels moviments disponibles, on el primer moviment sera la millor jugada del contrincant que ha calculat en la profunditat anterior
        for (int i : getMovimientos(ag, moves)) {
            
            AbracitosGame game_aux = new AbracitosGame(ag);
            game_aux.movePiece(moves.get(i));
            
            if (game_aux.isGameOver()) {
                if(game_aux.GetWinner() == jugador_enemic){
                    //si el joc sacaba i ha guanyat el contrincant no fa falta calcular mes moviments
                    taula_hash.actualitza(ag, new HashInfo(Integer.MIN_VALUE, i, profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 0));
                    return Integer.MIN_VALUE;
                }
                
            } else {
                
                // ??????
                
                vella_beta = nova_beta;
                nova_beta = Math.min(nova_beta, maximitza(game_aux, profunditat - 1, max_profunditat + 1, alpha, beta));
                if(vella_beta != nova_beta){
                    millor_tirada = i;
                }
                
                beta = Math.min(nova_beta, beta);
                if (alpha >= beta) {
                    taula_hash.actualitza(ag, new HashInfo(beta, -1, profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 2));
                    return beta;
                }
            }
        }
        
        //actualitzem la taula de hash i retornem la beta
        taula_hash.actualitza(ag, new HashInfo(beta, millor_tirada, profunditat, ag.getBoard_color(), ag.getBoard_occupied(), 0));
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
    
    /**
     * Funcio per saber l'ordre dels moviments, si ja hem calculat una profunditat anterior el primer moviment sera la millor jugada anterior
     * @param ag tauler al cual volem saber els moviments
     * @param moves moviments d'aquest tauler
     * @return llista de moviments, si ja hem calculat anteriorment aquest tauler el primer moviment sera el millor de la jugada anterior
     */
    private int[] getMovimientos(AbracitosGame ag, ArrayList<Point> moves){
        int[] movimientos_disponibles = new int[moves.size()];
        HashInfo hi = this.taula_hash.getInfo(ag);
        
        //establim l'ordre dels moviments
        for (int i = 0; i < moves.size(); i++) {
            movimientos_disponibles[i] = i;
        }
        
        //si ja hem calculat la taula, modifiquem l'ordre per tal que el primer sigui l'anterior millor jugada calculada
        if(hi != null && hi.getMillorFill() > 0 && hi.gettColor() == ag.getBoard_color().toLongArray()[0] && hi.gettOcupat() == ag.getBoard_occupied().toLongArray()[0]){
            movimientos_disponibles[hi.getMillorFill()] = 0;
            movimientos_disponibles[0] = hi.getMillorFill();
        }
        
        return movimientos_disponibles;
    }

    /**
     * Clase per implemtentar el paralelisme
     */
    private class Task implements Callable<AbracitosInfo>{

        private AbracitosGame game_aux;
        private int millor_heur;
        private int posicio;

        /**
         * Constructor de la calse per el paralelisme
         * @param game_aux tauler al cual calcularem l'heuristica
         * @param millor_heur la millor heuristica calculada en el primer moviment que hem calculat de forma sequencial
         * @param millor_posicio_prof la posicio de la tirada que farem
         */
        public Task(AbracitosGame game_aux, int millor_heur, int millor_posicio_prof) {
            this.game_aux = game_aux;
            this.millor_heur = millor_heur;
            this.posicio = millor_posicio_prof;
        }

        /**
         * Funcio de la clase "Callable", aquesta es la funcio que s'executa al cridar els threads
         * @return un objecte amb l'informacio de quina ha sigut la millor heuristica i quina posicio de la tirada ha sigut
         * @throws Exception 
         */
        @Override
        public AbracitosInfo call() throws Exception {
            int alpha = minimitza(game_aux, profunditat_IDS - 1, 1, millor_heur, Integer.MAX_VALUE);

            if (alpha > millor_heur) {
                millor_heur = alpha;
            }
            
            return new AbracitosInfo(millor_heur, posicio);
        }
        
    }

}