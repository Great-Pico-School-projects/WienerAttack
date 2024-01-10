import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Developped by Corentin Piquerez
 */

import static java.lang.Math.round;

public class WienerAttack {
    private int c, n;
    private long cryptedMessage;

    /** Construit un objet Wiener Attack avec les clés publiques et un message crypté */
    public WienerAttack(int n, int c, long cryptedMessage) {
        this.c = c;
        this.n = n;
        this.cryptedMessage = cryptedMessage;
    }

    /** Calcule la valeur de la fraction continue grâce à tous les ai */
    public static double calculateContinuedFraction(ArrayList<Integer> denominators){
        double result = (double) 1/denominators.get(denominators.size()-1);

        for (int i =0; i<denominators.size()-1; i++){
            result = (double) 1/(denominators.get(denominators.size()-2-i)+result);
        }
        return result;
    }

    /** Trouve la relation de bezout en divisant nb par nb2 */
    public static int[] euclide(int nb, int nb2){
        if (nb<nb2) {
            int tmp = nb;
            nb = nb2;
            nb2= tmp;
        }
        int tmpNb = nb, tmpNb2 = nb2;
        int q = nb/nb2;
        int[] u = {1, 0}, v = {0, 1};
        int r = nb-(nb2*q);
        nb = nb2;
        nb2 = r;
        int tmpu = u[0]-q*u[1];
        int tmpv = v[0]-q*v[1];
        u[0] = u[1];
        v[0] = v[1];
        u[1] = tmpu;
        v[1] = tmpv;
        while (r!=0){
            q = nb/nb2;
            r = nb-(nb2*q);
            if (r!=0){
                nb = nb2;
                nb2 = r;
                tmpu = u[0]-q*u[1];
                tmpv = v[0]-q*v[1];
                u[0] = u[1];
                v[0] = v[1];
                u[1] = tmpu;
                v[1] = tmpv;
            }
        }

        // résultat de l'équation de bezout
        int bezout = tmpNb*u[1]+tmpNb2*v[1];

        // messages de succès ou d'échec avec affichage du nombre de bezout en cas d'échec
        if (bezout==1) {
            System.out.println("-------------Bezout Success !!!-------------");
        }
        else System.out.println("-------------Bezout Failure /!\\-------------\nNombre de bezout :" + bezout);

        return new int[] {tmpNb, u[1], tmpNb2, v[1]};
    }

    /** Trouve les ai permettant de faire la fraction continue représentant numerator/denominator */
    public static ArrayList<Integer> findContinuedFraction(int numerator, int denominator) {
        ArrayList<Integer> denominators = new ArrayList<>();
        // initialisation
        int i = 0;
        int result1 = numerator/denominator; // chiffre entier de la division (1e des qi)
        double result = 0; // sert à calculer la valeur de la fraction continue
        double base = (double)numerator/(double)denominator; // vaut la première division
        numerator-=denominator*result1;
        // inversion numérateur et dénominateur
        int tmp = numerator;
        numerator = denominator;
        denominator = tmp;
        double epsilon = 0.000001; // valeur pour comparer des doubles (sinon risque boucle infinie)
        while (base>result+epsilon || base<result-epsilon)
        {
            int division = numerator/denominator;
            denominators.add(division);
            result = result1+calculateContinuedFraction(denominators);
            numerator-=denominators.get(i)*denominator;

            // inversion numérateur et dénominateur
            tmp = numerator;
            numerator = denominator;
            denominator = tmp;
            i++;
        }
        ArrayList<Integer> results = new ArrayList<>();
        results.add(result1);
        results.addAll(denominators);
        return results;
    }

    /** Trouve n premiers convergents de numerator/denominator */
    public static ArrayList<Integer[]> findFirstsConvergentsFraction(int numerator, int denominator) {
        ArrayList<Integer> denominators = new ArrayList<>();
        ArrayList<Integer[]> convergents = new ArrayList<>();
        // initialisation
        int i = 0;
        int result1 = numerator/denominator; // chiffre entier de la division (1e des qi)
        double result = 0; // sert à calculer la valeur de la fraction continue
        double base = (double)numerator/(double)denominator; // vaut la première division
        numerator-=denominator*result1;
        // inversion numérateur et dénominateur
        int tmp = numerator;
        numerator = denominator;
        denominator = tmp;
        double epsilon = 0.000001; // valeur pour comparer des doubles (sinon risque boucle infinie)
        while (base!=result)
        {
            if (denominator==0){
                break;
            }
            int division = numerator/denominator;
            denominators.add(division);
            if (i==0) {
                convergents.add(new Integer[] {result1, 1});
            } else if (i==1) {
                convergents.add(new Integer[] {1, denominators.get(i-1)});
            } else {
                convergents.add(new Integer[] {denominators.get(i-1)*convergents.get(i-1)[0]+convergents.get(i-2)[0], denominators.get(i-1)*convergents.get(i-1)[1]+convergents.get(i-2)[1]});
            }
            result = result1+calculateContinuedFraction(denominators);
            numerator-=denominators.get(i)*denominator;

            // inversion numérateur et dénominateur
            tmp = numerator;
            numerator = denominator;
            denominator = tmp;
            i++;
        }

        // Permet d'afficher les convergents si décommenté :
        /** for (int index =0; index<convergents.size(); index++){
            System.out.println(convergents.get(index)[0] + " / " + convergents.get(index)[1]);
        }*/

        return convergents;
    }

    /** Décrypte le message cryptedMessage */
    public void decrypt() {
        System.out.println("message crypté : " + cryptedMessage + " ; n = " + n + " ; c = " + c);
        ArrayList<Integer> a = findContinuedFraction(c, n);
        ArrayList<Integer[]> convergents = findFirstsConvergentsFraction(c, n);
        System.out.println("solutions ai pour la fraction continue :" + a);
        long x1 = 0, x2 = 0, N=0, M=0, delta = -1, k = 0;

        // trouve le bon convergent et résout l'équation du second degré -> détermination p et q
        for (int i =1; i<convergents.size(); i++) {
            long b = n - (convergents.get(i)[1] * c - 1) / convergents.get(i)[0] + 1;
            delta = (long) ((b * b) - (4 * n));
            x1 = round((-b - Math.sqrt(delta)) / 2);
            x2 = round((-b + Math.sqrt(delta)) / 2);

            if (x1 < 0 && x2 < 0) {
                x1 *= -1;
                x2 *= -1;
            }
            N = (x1 * x2);
            M = ((x1 - 1) * (x2 - 1));
            k = convergents.get(i)[0];
            if (n == N && convergents.get(i)[1]<Math.pow(N, 0.25)) {
                System.out.println("convergent utilisé : "+convergents.get(i)[0]+" / "+convergents.get(i)[1]);
                System.out.println("-------------N Success !!!-------------");
                break;
            }
        }

        // message d'erreur si delta < 0
        if (delta < 0) {
            System.out.println(("-------------Delta Failure /!\\-------------\nDelta : " + delta));
            System.exit(1);
        }

        System.out.println("p : "+x1+" ; q : "+x2);
        System.out.println("k = "+k);
        System.out.println("N = "+N);
        System.out.println("M = "+M);

        // trouve la relation de bezout
        int [] euclide = WienerAttack.euclide((int)M, c);
        System.out.println("(u = k) "+euclide[1]+" * "+euclide[0]+" + "+ euclide[2] + " * (v = d) " +euclide[3]);
        int d = euclide[3];

        // décrypte le message grâce à d
        BigInteger decryptedMessage = BigInteger.valueOf(cryptedMessage);
        decryptedMessage = decryptedMessage.modPow(BigInteger.valueOf(d), BigInteger.valueOf(n));

        // message de réussite / échec en réencryptant le message décrypté
        if ((decryptedMessage.modPow(BigInteger.valueOf(c),(BigInteger.valueOf(n)))).equals(BigInteger.valueOf(cryptedMessage)))
            System.out.println(("\033[1;34m"+"-------------MESSAGE DECRYPTE AVEC SUCCES-------------\n"+"\033[0m"));
        else
            System.out.println(("\033[1;31m"+"-------------FAILURE /!\\ MESSAGE NON DECRYPTE-------------\n"+"\033[0m"));
        System.out.println("message decrypté : "+ "\033[1;32m"+ decryptedMessage + "\033[0m");

    }

    public static void main(String[] args) {
        // différents jeux de données
        int[] c = {563067, 160138687};
        int[] n = {846451, 373695599};
        int[] message = {802403, 345187774};

        // Décryptage des différents jeux de données
        for (int i =0; i<c.length; i++){
            System.out.println("\n-------------\nJEU DE DONNEES "+(i+1)+"\n-------------\n");
            WienerAttack w = new WienerAttack(n[i],c[i], message[i]);
            w.decrypt();
            System.out.println();
        }

        // Jeu de données de test wikipedia
        System.out.println("\n-------------\nESSAI WIKIPEDIA \n-------------\n");
        WienerAttack w = new WienerAttack(90581, 17993, 34);
        w.decrypt();
    }
}
