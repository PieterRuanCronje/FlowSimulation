# FlowSimulation

Simulate fluid flow within a random three-dimensional block system using Java.

## Overview

A Java-based program designed to model and visualise the behaviour of fluid flowing through a 3D grid system populated with randomly placed blocks. It uses bitwise operations for efficient type management and a stack-based flood fill algorithm. Visualisation is done by using the characteristics of isometric blocks, and is explained under "Mathematics for Isometric Visualisation" heading.

## Implementation Features

- **Block and Fluid Types**: Uses bitwise operations to define and manipulate various composite types of elements in the grid (`BLOCK`, `FLUID`, `HOM_VIS`, `HET_VIS`).
- **Random Initialisation**: Generates a 3D grid with blocks placed based on a specified probability (`p`).
- **Fluid Propagation**: Simulates fluid flow from the top layer down through the grid using an iterative approach to avoid recursion limits.
- **Isometric Visualization**: Provides a 3D isometric view of the simulation using a 2D drawing library, with options for optimised rendering and double buffering.

## Requirements

- Ensure you have Java installed on your system.
- The [StdDraw library](https://algs4.cs.princeton.edu/code/javadoc/edu/princeton/cs/algs4/StdDraw.html) for graphical visualization is required.
- The [StdOut library](https://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/StdOut.java.html) is required by StdDraw. (Both are provided in `lib`, there's no need to go download these)

## Getting Started

### Setup Instructions

1. **Clone or Download Repository**: 
   Download all files from this repository and store them in a single directory.

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
java -cp bin FlowSimulation 10 0.6 1 1
```
![Simulation with n=10, p=0.6](https://user-images.githubusercontent.com/79271609/175651697-25b41dc4-dbf2-47cd-9800-b33a84ae06db.png)

#### Example 2:
```bash
java -cp bin FlowSimulation 100 0.65 1 1
```
![Simulation with n=100, p=0.65](https://user-images.githubusercontent.com/79271609/175651869-a3a60035-d8df-4231-8869-9bcfa5c823a5.png)

## Mathematics for Isometric Visualisation

TODO

## Acknowledgments

- The project uses the StdDraw library from Algorithms, 4th Edition by Robert Sedgewick and Kevin Wayne.

---
