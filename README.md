# FlowSimulation

Simulate fluid flow within a random three-dimensional block system using this Java-based FlowSimulation tool.

## Requirements
Ensure you have Java installed on your system.

## Getting Started
1. Download all the files from this repository and store them in a single directory.
2. Open a terminal in that directory.
3. Compile the Java files with the command:
   ```bash
   javac *.java
   ```
4. Run the simulation with the command:
   ```bash
   java FlowSimulation n p
   ```
   (Replace `n` with the number of blocks in a row and `p` with the probability of a space being occupied.)

## Example Simulations

### Simulation with n=10, p=0.6
```bash
java FlowSimulation 10 0.6
```
![Simulation with n=10, p=0.6](https://user-images.githubusercontent.com/79271609/175651697-25b41dc4-dbf2-47cd-9800-b33a84ae06db.png)

### Simulation with n=100, p=0.65
```bash
java FlowSimulation 100 0.65
```
![Simulation with n=100, p=0.65](https://user-images.githubusercontent.com/79271609/175651869-a3a60035-d8df-4231-8869-9bcfa5c823a5.png)

## Possible Improvements
Implement an algorithm to only draw blocks that will be visible.
