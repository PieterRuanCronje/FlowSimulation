/**
 * Visualises the simulated flow of water through a 3-dimensional
 * random block system.
 */
public class FlowSimulation {

    private boolean[][][] system; // block placement, {@code true} for an occupied space
    private boolean[][][] flowState; // flow progress, {@code true} for a filled space
    private int n; // size of the system
    private double p; // probability of a space being occupied by a block

    /**
     * Initialise the flow simulation.
     * 
     * @param n the size of the system (n x n x n)
     * @param p probability of a space being occupied (0 <= p <= 1)
     */
    public FlowSimulation(int n, double p) {

        this.n = n;
        this.p = p;
        
        // Set up the system.
        system = new boolean[n][n][n];
        createSystem();

        // Simulate the flow.
        flowState = new boolean[n][n][n];
        flow();
    }

    /**
     * Initialise the flow system.
     */
    public void createSystem() {
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                for (int k = 0; k < n; k++)
                    system[i][j][k] = StdRandom.bernoulli(p);
    }

    /**
     * Start the flow at the top level and then recursively flow downwards.
     */
    public void flow() {
        for (int j = 0; j < n; j++)
            for (int k = 0; k < n; k++)
                flow(0, j, k);
    }

    /**
     * Continue the flow of the system.
     * 
     * @param i vertical index
     * @param j horizontal index
     * @param k depth index
     */
    public void flow(int i, int j, int k) {
        
        if (i < 0 || i > n-1) return;
        if (j < 0 || j > n-1) return;
        if (k < 0 || k > n-1) return;

        if (system[i][j][k] || flowState[i][j][k]) return;

        if(!system[i][j][k])
            flowState[i][j][k] = true;

        flow(i+1, j, k);
        flow(i, j-1, k);
        flow(i, j+1, k);
        flow(i, j, k+1);
        flow(i, j, k-1);
    }

    /**
     * Display the result of the flow simulation.
     */
    public void display() {

        // Set up the size and background of the window.
        StdDraw.setCanvasSize(512*3, (int) Math.round(512 * 1.15));
        StdDraw.setXscale(0, 3);
        StdDraw.setYscale(0, 1.15);
        StdDraw.setPenColor(StdDraw.BOOK_RED);
        StdDraw.filledRectangle(512.0/2.0, 512.0*1.15/2.0, 512.0/2.0, 512.0*1.15/2.0);

        // Display the results after it has been processed.
        StdDraw.enableDoubleBuffering();

        for (int i = n-1; i >= 0; i--) {
            for (int j = 0; j < n; j++) {
                for (int k = n - 1; k >= 0; k--) {
                    if (system[i][j][k]) placeBlock(i, j, k, true);
                    if (flowState[i][j][k]) placeBlock(i, j, k, false);
                }
            }
        }

        // Show the reuslt of the simulation.
        StdDraw.show();
    }

    /**
     * Display a block according to its position in the 3-dimensional array.
     * 
     * @param i vertical index
     * @param j horizontal index
     * @param k depth index
     * @param isBlock {@code true} for blocks, {@code false} for liquid
     */
    public void placeBlock(int i, int j, int k, boolean isBlock) {
        /*
            The position of a block is calculated by representing the bottom corner of
            a block as the intersection of two lines with gradients of tan(PI/6) and -tan(PI/6)
            to create a 3D isometric plotting volume.

            y = m*x + c + adjustment

            equations:
                y = tan(PI/6)*x + 0.5 - tan(PI/6)*0.5 + 2*opp*k
                y = -tan(PI/6)*x + 0.5 + tan(PI/6)*0.5 + 2*opp*(n-j-1)
            
            ratio = the gradient, tan(PI/6) or -tan(PI/6) {equivalent to 30 degree lines} 
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
            x[0] = X + 1      ; y[0] = Y;
            x[1] = X + 1      ; y[1] = Y + sideLength;
            x[2] = X + adj + 1; y[2] = Y + opp + sideLength;
            x[3] = X + adj + 1; y[3] = Y + opp;
            StdDraw.filledPolygon(x, y);

            StdDraw.setPenColor(StdDraw.GRAY);
            x[0] = X + 1      ; y[0] = Y;
            x[1] = X + 1      ; y[1] = Y + sideLength;
            x[2] = X - adj + 1; y[2] = Y + opp + sideLength;
            x[3] = X - adj + 1; y[3] = Y + opp;
            StdDraw.filledPolygon(x, y);

            StdDraw.setPenColor(StdDraw.DARK_GRAY);
            x[0] = X + 1      ; y[0] = Y + sideLength;
            x[1] = X + adj + 1; y[1] = Y + opp + sideLength;
            x[2] = X + 1      ; y[2] = Y + 2*opp + sideLength;
            x[3] = X - adj + 1; y[3] = Y + opp + sideLength;
            StdDraw.filledPolygon(x, y);
        } else {
            StdDraw.setPenColor(StdDraw.BOOK_BLUE);
            x[0] = X + 2      ; y[0] = Y;
            x[1] = X + 2      ; y[1] = Y + sideLength;
            x[2] = X + adj + 2; y[2] = Y + opp + sideLength;
            x[3] = X + adj + 2; y[3] = Y + opp;
            StdDraw.filledPolygon(x, y);

            StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
            x[0] = X + 2      ; y[0] = Y;
            x[1] = X + 2      ; y[1] = Y + sideLength;
            x[2] = X - adj + 2; y[2] = Y + opp + sideLength;
            x[3] = X - adj + 2; y[3] = Y + opp;
            StdDraw.filledPolygon(x, y);

            StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
            x[0] = X + 2      ; y[0] = Y + sideLength;
            x[1] = X + adj + 2; y[1] = Y + opp + sideLength;
            x[2] = X + 2      ; y[2] = Y + 2*opp + sideLength;
            x[3] = X - adj + 2; y[3] = Y + opp + sideLength;
            StdDraw.filledPolygon(x, y);
        }
    }

    /**
     * Main function for the flow simulation.
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Program usage: java -cp bin FlowSimulation n p");
            System.exit(0);
        }

        int n = 0; // first command-line argument, the size of the system
        double p = 0; // second command-line argument, the probability of a block to be occupied

        try {
            n = Integer.parseInt(args[0]);
            p = Double.parseDouble(args[1]);
        } catch (Exception ignored) {
            System.out.println("Arguments 'n' and 'p' must be numbers.");
            System.exit(0);
        }

        if (n < 0) {
            System.out.println("Argument 'n' must be a positive integer.");
            System.exit(n);
        }

        if (p < 0 || p > 1) {
            System.out.println("Argument 'p' must be between 0 and 1.");
            System.exit(0);
        }

        // Create a new flow-system and let the liquid flow through it.
        FlowSimulation flowSimulation = new FlowSimulation(n, p);
        flowSimulation.display();
    }
}
