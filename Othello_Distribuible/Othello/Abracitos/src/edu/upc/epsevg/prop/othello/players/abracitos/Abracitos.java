package edu.upc.epsevg.prop.othello.players.abracitos;

import edu.upc.epsevg.prop.othello.GameStatus;
import edu.upc.epsevg.prop.othello.IAuto;
import edu.upc.epsevg.prop.othello.IPlayer;
import edu.upc.epsevg.prop.othello.Move;
import edu.upc.epsevg.prop.othello.SearchType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Dario, Xavi
 */
public class Abracitos implements IPlayer, IAuto {

    private String name;
    private GameStatus s;

    public Abracitos() {
        this.name = "puto";
    }

    @Override
    public void timeout() {
        // Nothing to do! I'm so fast, I never timeout 8-)
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

        ArrayList<Point> moves =  s.getMoves();
        if(moves.isEmpty())
        {
            // no podem moure, el moviment (de tipus Point) es passa null.
            return new Move(null, 0L,0,  SearchType.RANDOM); 
        } else {
            Random rand = new Random();
            int q = rand.nextInt(moves.size());
            return new Move( moves.get(q), 0L, 0, SearchType.RANDOM);         
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
    
        public int nova_tirada(Tauler t, int profunditat) {
        this.heur = 0;
        int millor_heur = Integer.MIN_VALUE;
        int millor_columna = -1;
        for (int i = 0; i < t.getMida(); i++) {
            int alpha = Integer.MIN_VALUE;
            if (t.movpossible(i)) {
                Tauler tauler_aux = new Tauler(t);
                tauler_aux.afegeix(i, this.color);
                if (!tauler_aux.solucio(i, this.color)) {
                    //Descomentar si es vol sense poda
                    //alpha = minimitza(tauler_aux, profunditat - 1);
                    
                    //Comentar si es vol sense poda
                    alpha = minimitza(tauler_aux, profunditat - 1, millor_heur, Integer.MAX_VALUE);
                    
                    if (alpha > millor_heur || millor_columna == -1) {
                        millor_columna = i;
                        millor_heur = alpha;
                    }
                }
                else {
                    return i;
                }
            }
         }
        this.heur += this.result_heur;
        System.out.println("Heuristica de la tirada "+this.num_tirades+": "+this.heur);
        return millor_columna;
    }
    

    
    /**
     * Funcion que ens indica l'heuristica mes gran trobada per totes les tirades analitzades.
     * @param t taulell sobre el que s'esta jugant
     * @param profunditat profunditat fins la que s'explorara l'arbre de posibilitats
     * @param alpha valor heuristic mes alt trobat fins al moment per fer la poda
     * @param beta valor heuristic mes baix trobat fins al moment per fer la poda
     * @return retorna la heuristica mes alta de totes les tirades analitzades
     */
    
    //Descomentar si es vol sense poda
    //public int maximitza (Tauler t,int profunditat){
    //Comentar si es vol sense poda
    public int maximitza (Tauler t,int profunditat,int alpha,int beta){
        if (profunditat <= 0 || !(t.espotmoure())) {
            return heur(t);
        }
        
        int nova_alpha = Integer.MIN_VALUE;
        for (int i = 0; i < t.getMida(); i++) {
            if (t.movpossible(i)) {
                Tauler taulell_aux = new Tauler(t);
                taulell_aux.afegeix(i, this.color);
                if (!taulell_aux.solucio(i, this.color)) {
                    //Descomentar si es vol sense poda
                    //nova_alpha = Math.max(nova_alpha, minimitza(taulell_aux, profunditat - 1));
                    
                    //---Comentar si es vol sense poda-----------------
                    
                    nova_alpha = Math.max(nova_alpha, minimitza(taulell_aux, profunditat - 1, alpha, beta));
                    alpha = Math.max(nova_alpha, alpha);
                    if (alpha >= beta) {
                        return alpha;
                    }

                    //-------------------------------------------------
                }
                else {
                    return Integer.MAX_VALUE;
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
    //Descomentar si es vol sense poda
    //public int minimitza (Tauler t,int profunditat){
    //Comentar si es vol sense poda
    public int minimitza (Tauler t,int profunditat,int alpha,int beta){
        if (profunditat <= 0 || !(t.espotmoure())) {
            return heur(t);
        }
        int nova_beta = Integer.MAX_VALUE;
        for (int i = 0; i < t.getMida(); i++) {
            if (t.movpossible(i)) {
                Tauler taulell_aux = new Tauler(t);
                taulell_aux.afegeix(i, this.color*-1);
                if (!taulell_aux.solucio(i, this.color*-1)) {
                    //Descomentar si es vol sense poda
                    //nova_beta = Math.min(nova_beta, maximitza(taulell_aux, profunditat - 1));
                    
                    //---Comentar si es vol sense poda-----------------
                    
                    nova_beta = Math.min(nova_beta, maximitza(taulell_aux, profunditat - 1, alpha, beta));
                    beta = Math.min(nova_beta, beta);
                    if (alpha >= beta) {
                        return beta;
                    }

                    //-------------------------------------------------
                }
                else {
                    return Integer.MIN_VALUE;
                }
            }
        }
        return nova_beta;
    }
}
