public class FlowSimulation {
    public static void main(String[] args) {
        /* 
            First command-line argument, the size of the system. (n x n x n)
        */
        int    n = Integer.parseInt(args[0]);

        /*
            Second command-line argument, the probability of a block to be occupied.
        */
        double p = Double.parseDouble(args[1]);

        /*
            Create a new flow-system and let the liquid flow through it.
        */
        boolean[][][] system = createSystem(p, n);
        boolean[][][] flowState = flow(system, n);

        /*
            Set up the size and background of the window.
        */
        StdDraw.setCanvasSize(512, (int) Math.round(512 * 1.15));
        StdDraw.setYscale(0, 1.15);
        StdDraw.setPenColor(StdDraw.BOOK_RED);
        StdDraw.filledRectangle(512.0/2.0, 512.0*1.15/2.0, 512.0/2.0, 512.0*1.15/2.0);

        /*
            Show the results after it has been processed.
        */
        StdDraw.enableDoubleBuffering();
        show(system, flowState, n);
        StdDraw.show();
    }

    /*
        Initialize the flow system.
    */
    public static boolean[][][] createSystem(double p, int n) {
        boolean[][][] system = new boolean[n][n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                for (int k = 0; k < n; k++)
                    system[i][j][k] = StdRandom.bernoulli(p);
        return system;
    }

    /*
        Start the flow at the top level and then recursively flow downwards.
    */
    public static boolean[][][] flow(boolean[][][] system, int n) {
        boolean[][][] flowState = new boolean[n][n][n];
        for (int j = 0; j < n; j++) {
            for (int k = 0; k < n; k++) {
                flow(system, flowState, n, 0, j, k);
            }
        }
        return flowState;
    }

    /*
        Continue the flow of the system.
    */
    public static void flow(boolean[][][] system, boolean[][][] flowState, int n, int i, int j, int k) {
        if (i < 0 || i > n-1) return;
        if (j < 0 || j > n-1) return;
        if (k < 0 || k > n-1) return;

        if (system[i][j][k]) return;
        if (flowState[i][j][k]) return;

        if(!system[i][j][k]) flowState[i][j][k] = true;

        /*
            If you want the flow to go upwards as well, uncomment the line below.
        */

        // flow(system, flowState, n, i-1, j, k);
        flow(system, flowState, n, i+1, j, k);
        flow(system, flowState, n, i, j-1, k);
        flow(system, flowState, n, i, j+1, k);
        flow(system, flowState, n, i, j, k+1);
        flow(system, flowState, n, i, j, k-1);
    }

    /*
        Visualize the results of the simulation.
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

    /*
        Place a block on screen according to its position in the array.
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
            sideLength = length of a side of the block (calculated with using Pythagoras' theorom)
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

        /*
            Bottom X & Y coordinates of the block.
        */
        double X = (cN + 2*opp*(n-j-1) - cP - 2*opp*k)/(2*ratio);
        double Y = ratio*X + cP + 2*k*opp + sideLength*(n-i-1);

        double[] x = new double[4];
        double[] y = new double[4];

        /*
            Draw the block using the characteristics of an isometric 3D block. 
        */
        if (isBlock) StdDraw.setPenColor(StdDraw.BLACK);
        else StdDraw.setPenColor(StdDraw.BOOK_BLUE);
        x[0] = X      ; y[0] = Y;
        x[1] = X      ; y[1] = Y + sideLength;
        x[2] = X + adj; y[2] = Y + opp + sideLength;
        x[3] = X + adj; y[3] = Y + opp;
        StdDraw.filledPolygon(x, y);
        
        if (isBlock) StdDraw.setPenColor(StdDraw.GRAY);
        else StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
        x[0] = X      ; y[0] = Y;
        x[1] = X      ; y[1] = Y + sideLength;
        x[2] = X - adj; y[2] = Y + opp + sideLength;
        x[3] = X - adj; y[3] = Y + opp;
        StdDraw.filledPolygon(x, y);

        if (isBlock) StdDraw.setPenColor(StdDraw.DARK_GRAY);
        else StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
        x[0] = X      ; y[0] = Y + sideLength;
        x[1] = X + adj; y[1] = Y + opp + sideLength;
        x[2] = X      ; y[2] = Y + 2*opp + sideLength;
        x[3] = X - adj; y[3] = Y + opp + sideLength;
        StdDraw.filledPolygon(x, y);
    }
}
