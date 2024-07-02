/**
 * Visualises the simulated flow of water through a 3-dimensional
 * random block system.
 */
public class FlowSimulation {
    
    /**
     * Initialise the flow system
     * 
     * @param p the probability of a space being occupied
     * @param n the size of the system (n x n x n)
     * @return the flow system where {@code true} indicates an occupied space
     */
    public static boolean[][][] createSystem(double p, int n) {
        boolean[][][] system = new boolean[n][n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                for (int k = 0; k < n; k++)
                    system[i][j][k] = StdRandom.bernoulli(p);
        return system;
    }

    /**
     * Start the flow at the top level and then recursively flow downwards.
     * 
     * @param system the flow system 
     * @param n the size of the flow system
     * @return the result of the flow simulation, {@code true} for all the spaces the water reached
     */
    public static boolean[][][] flow(boolean[][][] system, int n) {
        boolean[][][] flowState = new boolean[n][n][n];
        for (int j = 0; j < n; j++)
            for (int k = 0; k < n; k++)
                flow(system, flowState, n, 0, j, k);
        return flowState;
    }

    /**
     * Continue the flow of the system.
     * 
     * @param system the flow system
     * @param flowState current progress of the flowing liquid
     * @param n size of the flow system
     * @param i vertical index
     * @param j horizontal index
     * @param k depth index
     */
    public static void flow(boolean[][][] system, boolean[][][] flowState, int n, int i, int j, int k) {
        
        if (i < 0 || i > n-1) return;
        if (j < 0 || j > n-1) return;
        if (k < 0 || k > n-1) return;

        if (system[i][j][k]) return;
        if (flowState[i][j][k]) return;

        if(!system[i][j][k]) flowState[i][j][k] = true;

        flow(system, flowState, n, i+1, j, k);
        flow(system, flowState, n, i, j-1, k);
        flow(system, flowState, n, i, j+1, k);
        flow(system, flowState, n, i, j, k+1);
        flow(system, flowState, n, i, j, k-1);
    }

    /**
     * Visualise the result of the flow simulation.
     * 
     * @param system the flow system
     * @param flowState the result of fluid flow
     * @param n the size of the system
     */
    public static void show(boolean[][][] system, boolean[][][] flowState, int n) {
        for (int i = n-1; i >= 0; i--) {
            for (int j = 0; j < n; j++) {
                for (int k = n - 1; k >= 0; k--) {
                    if (system[i][j][k]) placeBlock(i, j, k, n, true);
                    if (flowState[i][j][k]) placeBlock(i, j, k, n, false);
                }
            }
        }
    }

    /**
     * Display a block according to its position in the 3-dimensional array.
     * 
     * @param n size of the flow system
     * @param i vertical index
     * @param j horizontal index
     * @param n the size of the system
     * @param isBlock {@code true} for blocks, {@code false} for liquid
     */
    public static void placeBlock(int i, int j, int k, int n, boolean isBlock) {
        /*
            The position of a block is calculated by representing the bottom corner of
            a block as the intersection of two lines with gradients of tan(PI/6) and -tan(PI/6)
            to create a 3D isometric plotting volume.

            y = m*x + c + adjustment

            equations:
                y = tan(PI/6)*x + 0.5 - tan(PI/6)*0.5 + 2*opp*k
                y = -tan(PI/6)*x + 0.5 + tan(PI/6)*0.5 + 2*opp*(n-j-1)
            
            ratio = dY/dX 
            opp = opposite side of the triangle
            adj = adjacent side of the triangle
            sideLength = length of a side of the block (calculated using Pythagoras' theorom)
            cP = 'c' value of the function with positive gradient
            cN = 'c' value of the function with negative gradient
        */
        double N = (double) n;
        double ratio = Math.tan(Math.PI/6.0);
        double opp = (ratio*0.45)/N;
        double adj = 0.45/N;
        double sideLength = Math.sqrt((0.45 * ratio)*(0.45 * ratio) + (0.45*0.45))/N;
        double cP = 0.05 - ratio*0.5;
        double cN = 0.05 + ratio*0.5; 

        // Bottom X & Y coordinates of the block.
        double X = (cN + 2*opp*(n-j-1) - cP - 2*opp*k)/(2*ratio);
        double Y = ratio*X + cP + 2*k*opp + sideLength*(n-i-1);

        double[] x = new double[4];
        double[] y = new double[4];

        // Draw both solid and liquid blocks on the left of the display
        StdDraw.setPenColor(isBlock ? StdDraw.BLACK : StdDraw.BOOK_BLUE);
        x[0] = X      ; y[0] = Y;
        x[1] = X      ; y[1] = Y + sideLength;
        x[2] = X + adj; y[2] = Y + opp + sideLength;
        x[3] = X + adj; y[3] = Y + opp;
        StdDraw.filledPolygon(x, y);
        
        StdDraw.setPenColor(isBlock ? StdDraw.GRAY : StdDraw.BOOK_LIGHT_BLUE);
        x[0] = X      ; y[0] = Y;
        x[1] = X      ; y[1] = Y + sideLength;
        x[2] = X - adj; y[2] = Y + opp + sideLength;
        x[3] = X - adj; y[3] = Y + opp;
        StdDraw.filledPolygon(x, y);

        StdDraw.setPenColor(isBlock ? StdDraw.DARK_GRAY : StdDraw.BOOK_LIGHT_BLUE);
        x[0] = X      ; y[0] = Y + sideLength;
        x[1] = X + adj; y[1] = Y + opp + sideLength;
        x[2] = X      ; y[2] = Y + 2*opp + sideLength;
        x[3] = X - adj; y[3] = Y + opp + sideLength;
        StdDraw.filledPolygon(x, y);

        // Draw solid blocks in the center of the display, and liquid blocks on the right.
        if (isBlock) {
            StdDraw.setPenColor(StdDraw.BLACK);
            x[0] = X + 1.0      ; y[0] = Y;
            x[1] = X + 1.0      ; y[1] = Y + sideLength;
            x[2] = X + adj + 1.0; y[2] = Y + opp + sideLength;
            x[3] = X + adj + 1.0; y[3] = Y + opp;
            StdDraw.filledPolygon(x, y);

            StdDraw.setPenColor(StdDraw.GRAY);
            x[0] = X + 1.0      ; y[0] = Y;
            x[1] = X + 1.0      ; y[1] = Y + sideLength;
            x[2] = X - adj + 1.0; y[2] = Y + opp + sideLength;
            x[3] = X - adj + 1.0; y[3] = Y + opp;
            StdDraw.filledPolygon(x, y);

            StdDraw.setPenColor(StdDraw.DARK_GRAY);
            x[0] = X + 1.0      ; y[0] = Y + sideLength;
            x[1] = X + adj + 1.0; y[1] = Y + opp + sideLength;
            x[2] = X + 1.0      ; y[2] = Y + 2*opp + sideLength;
            x[3] = X - adj + 1.0; y[3] = Y + opp + sideLength;
            StdDraw.filledPolygon(x, y);
        } else {
            StdDraw.setPenColor(StdDraw.BOOK_BLUE);
            x[0] = X + 2.0      ; y[0] = Y;
            x[1] = X + 2.0      ; y[1] = Y + sideLength;
            x[2] = X + adj + 2.0; y[2] = Y + opp + sideLength;
            x[3] = X + adj + 2.0; y[3] = Y + opp;
            StdDraw.filledPolygon(x, y);

            StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
            x[0] = X + 2.0      ; y[0] = Y;
            x[1] = X + 2.0      ; y[1] = Y + sideLength;
            x[2] = X - adj + 2.0; y[2] = Y + opp + sideLength;
            x[3] = X - adj + 2.0; y[3] = Y + opp;
            StdDraw.filledPolygon(x, y);

            StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
            x[0] = X + 2.0      ; y[0] = Y + sideLength;
            x[1] = X + adj + 2.0; y[1] = Y + opp + sideLength;
            x[2] = X + 2.0      ; y[2] = Y + 2*opp + sideLength;
            x[3] = X - adj + 2.0; y[3] = Y + opp + sideLength;
            StdDraw.filledPolygon(x, y);
        }
    }

    /**
     * Main function fof the flow visualisation.
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        
        // First command-line argument, the size of the system. (n x n x n)
        int n = Integer.parseInt(args[0]);

        // Second command-line argument, the probability of a block to be occupied.
        double p = Double.parseDouble(args[1]);

        // Create a new flow-system and let the liquid flow through it.
        boolean[][][] system = createSystem(p, n);
        boolean[][][] flowState = flow(system, n);

        // Set up the size and background of the window.
        StdDraw.setCanvasSize(512*3, (int) Math.round(512 * 1.15));
        StdDraw.setXscale(0, 3);
        StdDraw.setYscale(0, 1.15);
        StdDraw.setPenColor(StdDraw.BOOK_RED);
        StdDraw.filledRectangle(512.0/2.0, 512.0*1.15/2.0, 512.0/2.0, 512.0*1.15/2.0);

        // Display the results after it has been processed.
        StdDraw.enableDoubleBuffering();
        show(system, flowState, n);
        StdDraw.show();
    }
}
