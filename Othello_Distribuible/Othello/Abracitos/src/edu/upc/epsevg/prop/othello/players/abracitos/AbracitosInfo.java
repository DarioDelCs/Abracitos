package edu.upc.epsevg.prop.othello.players.abracitos;

/**
 * Clase on guardem informacio dels threads
 * @author Dario, Xavi
 */
public class AbracitosInfo {
    private int millor_heur;
    private int posicio;

    /**
     * Constructor de la clase
     * @param millor_heur la millor heuristica trobada per el thread
     * @param posicio la posicio de la tirada que ha calculat el thread
     */
    public AbracitosInfo(int millor_heur, int posicio) {
        this.millor_heur = millor_heur;
        this.posicio = posicio;
    }

    /**
     * Funcio per retornar la millor heuristica del thread
     * @return la millor heuristica del thread
     */
    public int getMillor_heur() {
        return millor_heur;
    }

    /**
     * Funcio per retornar la posicio de la tirada que ha calculat el thread
     * @return la posicio de la tirada que ha calculat el thread
     */
    public int getPosicio() {
        return posicio;
    }
}
