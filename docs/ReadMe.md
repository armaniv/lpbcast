# Lightweight Probabilistic Broadcast

This project implements the lightweight probabilistic broadcast (lpbcast) gossip-based broadcast algorithm. All the informations about protocol specifications can be found in the original paper.

## Requirements
* Java 11 
* [Eclipse IDE Version: 2019-06 (4.12.0)](https://www.eclipse.org/downloads/packages/release/2019-06)
* [Symphony Version: 2.7](https://repast.github.io/)
* [gnuplot](http://www.gnuplot.info/) (OPTIONAL)

## Simulator configuration
Our simulator is based on Repast Symphony, an agent-based modeling toolkit
and  cross-platform  Java-based  modeling  system .   This  technology  allows
represent graphically a complex system with the usage of efficient visualization
features and also plot computed statistics, such as histograms or time series, in
order to allow easier analysis. However some of the statistics are not computed using Repast mechanisms.

## Installation
* Clone the repository
* Import the project as a Repast project into Eclipse IDE
* Run the project

## Simulation parameters
When the simulator is running, a GUI is showed to the user which presents many input boxes that can be used to modify the parameters of the simulation. These parameters are well described in the report.


## Test
In order to test the implementation we have two types of statistics which can be computed and analyzed:
1. Expected number of infected Processes for a given round: Computed  in  one  shot  at  a  specified  tick  count,  outputted  using  Java System.out.println(), collected and used to plot graphics using gnuplot
2. Delivery ratio: computed using the Repast data collection mechanisms. We  create  a  Repast  Data  Set  and  collect  at  the  end  of  each  tick  the delivery ratio of each node, average it among all nodes and plot the result in function of the tick count as a time series.

Hence, in order to output the results of the experiments related to the expected number of infected processes for a given round, you have to check the standard output of the java program and analyze the numbers or use gnuplot or similar tools to plot charts.  If you want instead to check the average delivery ratio you can simply inspect the graphic produced by Repast simulator in the GUI.

## Reference

Patrick Th. Eugster, Rachid Guerraoui, Sidath B. Handurukande, Petr Kouznetsov, Anne-Marie Kermarrec: Lightweight probabilistic broadcast. ACM Trans.Comput. Syst. 21(4): 341-374 (2003)


## Authors

* **Valentino Armani** - [armaniv](https://github.com/armaniv)
* **Marian Alexandru Diaconu** - [neboduus](https://github.com/neboduus)
