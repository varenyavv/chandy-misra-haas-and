# Chandy-Misra-Haas (CMH) algorithm for the AND Model

A java based console application to demonstrate the implementation of CMH algorithm for the AND model. It is used for
the purpose of deadlock detection in a distributed system.

### Assumptions

- Only one initiator can initiate the algorithm at a time.
- In addition to sending probe messages to the processes present at different sites, they are also being sent to the
  processes upon which a process is depending locally.
- Therefore, the Wait-For Graph (WFG) being used as input is being assumed to be the equivalent of a Global WFG.

### Deliverables

- Source file - `CmhAndImpl.java`
- Executable file - `cmh-and-model-2021mt13019.jar`
- Screenshots of output - `Program_Output_Screenshot.png`
- `README.txt`

### Steps to run the application

- An executable Jar (Java Archive) file `cmh-and-model-2021mt13019.jar` is included in the zip file.
- JRE 11 (or above) is required to execute this jar.
- In a command prompt, `cd` into the directory where this jar is located at and execute the below command to run the
  application:

```
 java -jar cmh-and-model-2021mt13019.jar
```

## Sample Inputs and Outputs

### Case-1 Deadlock is detected

#### Input:
No. of processes = 5; Initiating process = P1

WFG:
```
    P1 P2 P3 P4 P5
P1  0  0  1  0  0  
P2  1  0  0  1  0  
P3  0  1  0  0  1  
P4  0  0  0  0  0  
P5  0  0  0  0  0
```

#### Output:
Deadlock detected at `Probe(i=1,j=2,k=1)`

### Case-2 No deadlock is detected
#### Input:
No. of processes = 5; Initiating process = P1

WFG:
```
   P1 P2 P3 P4 P5
P1  0  0  1  0  0  
P2  1  0  0  1  0  
P3  0  1  0  0  1  
P4  0  0  0  0  0  
P5  0  0  0  0  0

```

#### Output:
No deadlock detected