public class EjemploPracticaAnalizador {
    public static void main(String[] args) {
        int octal1 = -0123, octal2 = 0381;
        short dato = 12A12;
        double PI = 3.1416, CteGrav = -6.67E-19;
        float prom = (float) 1.4;
        double val;

        /* Calculos Generales
        for (int i = 1; i < 100; i++){
            prom += i;
        }
        double pot = 7E+;
        val = CteGrav * dato * pot;
        System.out.println("Prom = " + (prom / PI) + "Result = " + (val * 0xAxB));
    }
}