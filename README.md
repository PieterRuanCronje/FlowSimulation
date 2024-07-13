# FlowSimulation

## Overview

A Java-based program designed to model and visualise fluid behaviour within a 3D grid system populated with randomly placed blocks. It employs bitwise operations for efficient type management and utilises a stack-based flood fill algorithm. Visualisation is achieved using the characteristics of isometric blocks, detailed under the "Mathematics for Isometric Visualisation" heading.

## Implementation Features

- **Block and Fluid Types**: Utilises bitwise operations to define and manipulate various composite types of elements in the grid (`BLOCK`, `FLUID`, `HOM_VIS`, `HET_VIS`).
- **Random Initialisation**: Generates a 3D grid with blocks placed based on a specified probability (`p`).
- **Fluid Propagation**: Simulates fluid flow from the top layer down through the grid using an iterative approach to avoid recursion limits.
- **Isometric Visualisation**: Provides a 3D isometric view of the simulation using a 2D drawing library, offering options for optimised rendering and double buffering.

## Requirements

- Ensure Java is installed on your system.
- The [StdDraw library](https://algs4.cs.princeton.edu/code/javadoc/edu/princeton/cs/algs4/StdDraw.html) for graphical visualisation is required.
- The [StdOut library](https://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/StdOut.java.html) is required by StdDraw. (Both are provided in `lib`, there's no need to go download these)

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
java -cp bin FlowSimulation 150 0.66 1 1
```
![Simulation with n=150, p=0.66](https://github.com/PieterRuanCronje/FlowSimulation/assets/79271609/790fdeec-47f2-4ddc-970c-e3e88b5ae290)

#### Example 3:
```bash
java -cp bin FlowSimulation 15 0.75 1 1
```
![Simulation with n=15, p=0.75](https://github.com/PieterRuanCronje/FlowSimulation/assets/79271609/cec393c0-fec9-47e2-8a31-a798b67bd749)

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
### Performance
To get the fastest simulation, parameters `opt=1` and `db=1` must be used. I experimented with parallelism in the flow section of the simulation, it provided minimal improvements in speed and made the code more complex so decided to keep the serial implementation. Because of the way the visualisation is done it would be infeasible in parralel, the synchronisation required to place blocks in the correct order would be extremely difficult and I suspect that it would slow down the simulation.

## Double Buffering

When double buffering is enabled, the program processes the visualisation completely before displaying the result, which is ideal for large systems. When disabled, the program shows how the grid is populated with blocks and fluid from the bottom upwards.

#### Example 1: Unoptimised Visualisation without Double Buffering
```bash
java -cp bin FlowSimulation 25 0.66 0 0
```
![Unoptimised with Double Buffering](https://github.com/PieterRuanCronje/FlowSimulation/assets/79271609/7d726012-fc65-4c5c-beab-8b2223779d56)

#### Example 2: Optimised Visualisation without Double Buffering
```bash
java -cp bin FlowSimulation 25 0.66 1 0
```
![Optimised with Double Buffering](https://github.com/PieterRuanCronje/FlowSimulation/assets/79271609/cfcf3e26-274f-4fd4-a0cd-0889e2fbd4ad)

## Mathematics for Isometric Visualisation

TODO

## Acknowledgements

- The project uses the StdDraw library from "Algorithms, 4th Edition" by Robert Sedgewick and Kevin Wayne.

---
