# FlowSimulation

## Overview

A Java-based program designed to model and visualise fluid behaviour within a 3D grid system populated with randomly placed blocks. It employs bitwise operations for efficient type management and utilises a stack-based flood fill algorithm. Visualisation is achieved using the characteristics of isometric blocks, detailed under the "Geometry for Isometric Visualisation" heading.

## Implementation Features

- **Block and Fluid Types**: Utilises bitwise operations to define and manipulate various composite types of elements in the grid (`BLOCK`, `FLUID`, `HOM_VIS`, `HET_VIS`).
- **Random Initialisation**: Generates a 3D grid with blocks placed based on a specified probability (`p`).
- **Fluid Propagation**: Simulates fluid flow from the top layer down through the grid using an iterative approach to avoid recursion limits.
- **Isometric Visualisation**: Provides a 3D isometric view of the simulation using a 2D drawing library, offering options for optimised rendering and double buffering.

## Requirements

- Ensure Java is installed on your system.
- The [Stopwatch library](https://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/Stopwatch.java.html) to time the simulation.
- The [StdDraw library](https://algs4.cs.princeton.edu/code/javadoc/edu/princeton/cs/algs4/StdDraw.html) for graphical visualisation is required.
- The [StdOut library](https://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/StdOut.java.html) is required by StdDraw. (All libraries are provided in `lib`, there's no need to go download these)

## Getting Started

### Setup Instructions

1. **Clone or Download Repository**: Download all files from this repository and store them in a single directory.

2. **Compile the Java Files**:
   Open a terminal in that directory and compile the Java files using:
   ```bash
   javac -d bin src/*.java lib/*.java
   ```

3. **Run the Simulation**:
   Execute the simulation with:
   ```bash
   java -cp bin FlowSimulation n p opt db
   ```
   Replace `n`, `p`, `opt`, and `db` with appropriate values:
   - `n`: Size of the system (e.g., 50 for a 50x50x50 grid).
   - `p`: Probability of a block being occupied (e.g., 0.6).
   - `opt`: Optimisation flag (1 for optimised rendering, 0 otherwise).
   - `db`: Double buffering flag (1 to show only the final result (fastest), 0 to show the progress).

### Example Commands

#### Example 1:
```bash
java -cp bin FlowSimulation 10 0.67 1 1
```

![Simulation with n=10, p=0.67](https://github.com/PieterRuanCronje/FlowSimulation/assets/79271609/1a53880f-de78-4c73-8db5-fc1c3ed33607)

#### Example 2:
```bash
java -cp bin FlowSimulation 500 0.66 1 1
```
![Simulation with n=500, p=0.66](https://github.com/user-attachments/assets/16a6157f-63ce-4dcd-b425-e95fa099a0fa)

## Implementation Details

### Flooding the System

The block system is flooded with fluid from the top layer. This is achieved using a simple stack-based percolation algorithm. The algorithm was initially recursively defined, but I found that for large values of `n` this would cause a stack-overflow error. The algorithm was easily coverted to an iterative approach. The following method, `flow`, is called for each open element in the top layer of the system.

(`system`: the cubic system containing all elements, `check`: method to check if an element is of a certain type, `set`: sets an element to a specified type, `FLUID`: liquid type, `BLOCK`: solid type)

```java
/**
 * Continue the flow of liquid through the system.
 * 
 * @param i vertical index
 * @param j horizontal index
 * @param k depth index
 */
public void flow(int startI, int startJ, int startK) {

    /*
     * Converted from recursive to iterative approach for
     * better scalability.
     */

    Stack<int[]> stack = new Stack<>();
    stack.push(new int[]{startI, startJ, startK});

    while (!stack.isEmpty()) {

        int[] position = stack.pop();

        int i = position[0];
        int j = position[1];
        int k = position[2];

        if (i < 0 || i >= n || j < 0 || j >= n || k < 0 || k >= n)
            continue;

        if (check(system[i][j][k], BLOCK) || check(system[i][j][k], FLUID))
            continue;

        system[i][j][k] = set(system[i][j][k], FLUID);

        // For flow to go upwards as well, push {i+1, j, k} to the stack.
        stack.push(new int[]{i-1, j, k});
        stack.push(new int[]{i, j-1, k});
        stack.push(new int[]{i, j+1, k});
        stack.push(new int[]{i, j, k+1});
        stack.push(new int[]{i, j, k-1});
    }
}
```

### Optimisation

When optimisation is enabled, an algorithm is used that scans for visible elements using diagonal lines, the `scan` method is called for each outer element of the three visible sides of the isometric system. At most one block will be drawn per line, the block will only be redundant when elements in the diagonal lines directly through the sides and above an element are also visible. Checking for this would provide negligible drawing speed improvements and will lead to a slower overall simulation due to processing requirements.

(`system`: the cubic system containing all elements, `check`: method to check if an element is of a certain type, `set`: sets an element to a specified type, `HOM_VIS`: homogenous visibility type, for the section of the visualisation containing only elements of the same type, `HET_VIS`: heterogenous visibility type, for the section of the visualisation containing both fluid and block elements)

```java
/**
 * Scans for visible blocks through straight diagonal lines sent from the three visible
 * sides of the isometric system.
 * 
 * @param i vertical index
 * @param j horizontal index
 * @param k depth index
 * @param type the element type for which to check visibility (BLOCK, FLUID, or BOTH)
 */
public void scan(int i, int j, int k, byte type) {

    byte element;

    while (i >= 0 && i < n && j >= 0 && j < n && k >= 0 && k < n) {

        element = system[i][j][k];

        if (type == BOTH && (check(element, BLOCK) || check(element, FLUID))) {
            system[i][j][k] = set(element, HET_VIS);
            break;

        // (type != BOTH) ensures the omission of empty space
        } else if (type != BOTH && check(element, type)) {
            system[i][j][k] = set(element, HOM_VIS);
            break;

        } else { i--; j++; k++; }
    }
}
```

To get the fastest simulation, parameters `opt=1` and `db=1` must be used.

### Simulation times in seconds with: `p=0.66`, `db=1`

| n   | unoptimised (opt=0) | optimised (opt=1) |
|-----|---------------------|-------------------|
| 10  | 0.568               | 0.495             |
| 25  | 0.932               | 0.538             |
| 50  | 2.163               | 0.647             |
| 100 | 7.492               | 0.947             |
| 150 | 19.323              | 1.401             |
| 200 | 37.444              | 1.986             |
| 250 | -                   | 2.801             |
| 500 | -                   | 10.578            |

## Double Buffering

When double buffering is enabled, the program processes the visualisation completely before displaying the result, which is ideal for large systems. When disabled, the program shows how the grid is populated with blocks and fluid from the bottom upwards.

#### Example 1: Unoptimised Partial Visualisation without Double Buffering
```bash
java -cp bin FlowSimulation 25 0.66 0 0
```
![Unoptimised with Double Buffering](https://github.com/PieterRuanCronje/FlowSimulation/assets/79271609/7d726012-fc65-4c5c-beab-8b2223779d56)

#### Example 2: Optimised Partial Visualisation without Double Buffering
```bash
java -cp bin FlowSimulation 25 0.66 1 0
```
![Optimised with Double Buffering](https://github.com/PieterRuanCronje/FlowSimulation/assets/79271609/cfcf3e26-274f-4fd4-a0cd-0889e2fbd4ad)

## Geometry for Isometric Visualisation

![isometric_geometry](https://github.com/user-attachments/assets/e72a3b80-fc12-4c8d-89bd-d523240aa3b6)

The bottom X-coordinate of an element in the system is found at the intersection of the two lines:

- y = tan(π/6) * (x-0.5) + 0.05 + j*SIDE
- y = -tan(π/6) * (x-0.5) + 0.05 + k*SIDE

The Y-coordinate can then be found by substituting the X-coordinate into one of these lines and adding an additional i*SIDE units for vertical scaling. The translation of 0.5 units on the X-axis centers the visualisation, and 0.05 in the Y-axis is just for better spacing. 

H and V (denoted by `HOR` and `VER` in the code) refer to the horizontal and vertical lengths of the triangle shown in the illustration above, and is used to obtain the coordinates of the various vertices.

```java
/**
 * Calculates the plotting coordinates of an element using the characteristics of
 * isometric cubes.
 * 
 * @param i vertical index
 * @param j horizontal index
 * @param k depth index
 * @param xRight x-coordinates of the right section of the isometric cube
 * @param xLeft x-coordinates of the left section of the isometric cube
 * @param xTop x-coordinates of the top section of the isometric cube
 * @param ySides y-coordinates of the side sections of the isometric cube
 * @param yTop y-coordinates of the top section of the isometric cube
 */
public void calculateCoordinates(int i, int j, int k,
        double[] xRight, double[] xLeft, double[] xTop, double[] ySides, double[] yTop) {

    /*
     * Refer to the README for details on the formulas.
     */

    // Bottom X & Y coordinates of the block.
    double X = (k-j)*SIDE/(2*M) + 0.5;
    double Y = M*(X-0.5) + 0.05 + (j+i)*SIDE;

    xRight[0] = X;
    xRight[1] = X;
    xRight[2] = X + HOR;
    xRight[3] = X + HOR;

    xLeft[0] = X;
    xLeft[1] = X;
    xLeft[2] = X - HOR;
    xLeft[3] = X - HOR;

    xTop[0] = X;
    xTop[1] = X + HOR;
    xTop[2] = X;
    xTop[3] = X - HOR;

    ySides[0] = Y;
    ySides[1] = Y + SIDE;
    ySides[2] = Y + VER + SIDE;
    ySides[3] = Y + VER;

    yTop[0] = Y + SIDE;
    yTop[1] = Y + VER + SIDE;
    yTop[2] = Y + 2*SIDE;
    yTop[3] = Y + VER + SIDE;
}
```

## Acknowledgements

- This project uses the StdDraw, StdOut, and Stopwatch libraries from "Algorithms, 4th Edition" by Robert Sedgewick and Kevin Wayne.

---
