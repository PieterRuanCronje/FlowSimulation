import java.awt.Color;
import java.util.Stack;

/**
 * Visualises the simulated flow of water through a 3-dimensional
 * random block system.
 */
public class FlowSimulation {

    /*********************************************************************************************/

    private static final byte BLOCK = 8;   // Block type (b1000)
    private static final byte FLUID = 4;   // Fluid type (b0100)
    private static final byte HOM_VIS = 2; // Visibility in the homogenous visualisation (b0010)
    private static final byte HET_VIS = 1; // Visibility in the heterogenous visualisation (b0001)
    private static final byte BOTH = 0;    // Used to disregard the type of the element (b0000)

    /**
     * Check if an element in the system is of a certain type.
     * 
     * @param element an element in the system
     * @param type the element characteristics
     * @return {@code true} if the element is of the specified type
     */
    public boolean check(byte element, byte type) {
        // Use bitwise AND to check if the bits corresponding to 'type' are set in 'element'
        return (element & type) == type;
    }

    /**
     * Set an element to a specified type. (Elements can have multiple types)
     * 
     * @param element an element in the system
     * @param type the type to set the element to
     * @return the new element with its type altered
     */
    public byte set(byte space, byte type) {
        // Use bitwise OR to set the bits corresponding to 'type' in 'element'
        return (byte) (space | type);
    }

    /*********************************************************************************************/

    private byte[][][] system; // Flow simulation system, refer to constants for clarity.
    private int n; // Size of the system.
    private double p; // Probability of a space being occupied by a block.

    /*
     * Used to check which elements have been visited when determining visibiity.
     * This variable has to be reset after processing for each material is done.
     * (BLOCK, FLUID, and BOTH)
     */
    private boolean[][][] visited = null;

    private final Color BOOK_MEDIUM_BLUE = new Color(40, 150, 204); // Top section of the fluid.

    /**
     * Initialise and run the flow simulation.
     * 
     * @param n the size of the system (n x n x n)
     * @param p probability of a space being occupied (0 <= p <= 1)
     */
    public FlowSimulation(int n, double p) {

        this.n = n;
        this.p = p;

        // Set up the system and run the flow simulation.
        system = new byte[n][n][n];
        createSystem();
        flow();

        // Determine the visibility of each element in the visualisation.
        visited = new boolean[n][n][n];
        determineVisibility(BOTH);

        visited = new boolean[n][n][n];
        determineVisibility(BLOCK);

        visited = new boolean[n][n][n];
        determineVisibility(FLUID);
    }

    /**
     * Initialise the flow system.
     */
    public void createSystem() {
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                for (int k = 0; k < n; k++)
                    if (Math.random() < p)
                        system[i][j][k] = set(system[i][j][k], BLOCK);
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

        if (i < 0 || i >= n || j < 0 || j >= n || k < 0 || k >= n)
            return;

        if (check(system[i][j][k], BLOCK) || check(system[i][j][k], FLUID))
            return;

        system[i][j][k] = set(system[i][j][k], FLUID);

        flow(i+1, j, k);
        flow(i, j-1, k);
        flow(i, j+1, k);
        flow(i, j, k+1);
        flow(i, j, k-1);
    }

    /**
     * Determines which elements in the system will be visible in the visualisation.
     * 
     * @param type the element type for which to check visibility (BLOCK, FLUID, or BOTH)
     */
    public void determineVisibility(byte type) {

        for (int j = 0; j < n; j++)
            for (int k = 0; k < n; k++)
                determineVisibility(0, j, k, type);

        for (int i = 0; i < n; i++)
            for (int k = 0; k < n; k++)
                determineVisibility(i, n-1, k, type);

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                determineVisibility(i, j, 0, type);
    }

    /**
     * Determines the visibility of the current element and checks the neighbouring positions.
     * 
     * @param i vertical index
     * @param j horizontal index
     * @param k depth index
     * @param type the element type for which to check visibility (BLOCK, FLUID, or BOTH)
     */
    public void determineVisibility(int startI, int startJ, int startK, byte type) {

        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{startI, startJ, startK});

        while (!stack.isEmpty()) {

            int[] pos = stack.pop();

            int i = pos[0];
            int j = pos[1];
            int k = pos[2];

            // Boundary and visited checks.
            if (i < 0 || i >= n || j < 0 || j >= n || k < 0 || k >= n || visited[i][j][k])
                continue;

            visited[i][j][k] = true;

            byte element = system[i][j][k];

            // Determine visibility.
            if (type == BOTH && (check(element, BLOCK) || check(element, FLUID)))
                system[i][j][k] = set(element, HET_VIS);

            // (type != BOTH) ensures the omission of empty space.
            else if (type != BOTH && check(element, type))
                system[i][j][k] = set(element, HOM_VIS);

            else {
                // Push neighbors onto the stack.
                stack.push(new int[]{i + 1, j, k});
                stack.push(new int[]{i - 1, j, k});
                stack.push(new int[]{i, j + 1, k});
                stack.push(new int[]{i, j - 1, k});
                stack.push(new int[]{i, j, k + 1});
                stack.push(new int[]{i, j, k - 1});
            }
        }
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

        // Loop from bottom back corner to top front corner for correct block placement
        for (int i = n-1; i >= 0; i--)
            for (int j = 0; j < n; j++)
                for (int k = n - 1; k >= 0; k--)
                    placeBlock(i, j, k);

        // Show the reuslt of the simulation.
        StdDraw.show();
    }

    /**
     * Display a block according to its position in the 3-dimensional array.
     * 
     * @param i vertical index
     * @param j horizontal index
     * @param k depth index
     */
    public void placeBlock(int i, int j, int k) {

        /*
         *  The position of a block is calculated by representing the bottom corner of
         *  a block as the intersection of two lines with gradients of tan(PI/6) and -tan(PI/6)
         *  to create a 3D isometric plotting volume.
         *
         *  y = m*x + c + adjustment
         *
         *  equations:
         *      y = tan(PI/6)*x + 0.5 - tan(PI/6)*0.5 + 2*opp*k
         *      y = -tan(PI/6)*x + 0.5 + tan(PI/6)*0.5 + 2*opp*(n-j-1)
         *
         *  ratio = the gradient, tan(PI/6) or -tan(PI/6) {equivalent to 30 degree lines} 
         *  opp = opposite side of the triangle
         *  adj = adjacent side of the triangle
         *  sideLength = length of a side of the block (calculated using Pythagoras' theorem)
         *  cP = 'c' value of the function with positive gradient
         *  cN = 'c' value of the function with negative gradient
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

        byte element = system[i][j][k];

        boolean isBlock = check(element, BLOCK);
        boolean isFluid = check(element, FLUID);
        boolean heterogenousVisibility = check(element, HET_VIS);
        boolean homogenousVisibility = check(element, HOM_VIS);

        if ((isBlock || isFluid) && heterogenousVisibility) { // Draw solid and fluid elements on the left.

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

            StdDraw.setPenColor(isBlock ? StdDraw.DARK_GRAY : BOOK_MEDIUM_BLUE);
            x[0] = X      ; y[0] = Y + sideLength;
            x[1] = X + adj; y[1] = Y + opp + sideLength;
            x[2] = X      ; y[2] = Y + 2*opp + sideLength;
            x[3] = X - adj; y[3] = Y + opp + sideLength;
            StdDraw.filledPolygon(x, y);
        }
        
        if (isBlock && homogenousVisibility) { // Draw solid elements in the middle.

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

        } else if (isFluid && homogenousVisibility) { // Draw fluid elements on the right.

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

            StdDraw.setPenColor(BOOK_MEDIUM_BLUE);
            x[0] = X + 2      ; y[0] = Y + sideLength;
            x[1] = X + adj + 2; y[1] = Y + opp + sideLength;
            x[2] = X + 2      ; y[2] = Y + 2*opp + sideLength;
            x[3] = X - adj + 2; y[3] = Y + opp + sideLength;
            StdDraw.filledPolygon(x, y);
        }
    }

    /**
     * Main function for the flow simulation.
     * Program usage: java -cp bin FlowSimulation n p
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Program usage: java -cp bin FlowSimulation n p");
            System.exit(0);
        }

        int n = 0; // First command-line argument, the size of the system.
        double p = 0; // Second command-line argument, the probability of a block being occupied.

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

        Stopwatch stopwatch = new Stopwatch();

        // Create a new flow simulation.
        FlowSimulation flowSimulation = new FlowSimulation(n, p);

        // Display the result of the simulation.
        flowSimulation.display();

        System.out.println(stopwatch.elapsedTime());
    }
}