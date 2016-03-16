P2P Course final project
========================

The project consists in the definition and simulation of decentralized algorithms to compute centrality indices under the assumption of a synchronous communication model.

Project files
-------------

	./
	  src/             [Root of the source code files]
	  lib/             [PeerSim library files]
	  data/            [Datasets used in the experiments]
	  sim/             [Example config files to run simulations]
	  build.xml        [Ant build file]
	  README.md        
	  report.pdf       [Full report of the project]

Building the project
--------------------

The easiest way to build the project is by using `ant`. The `ant` build file provides targets for compiling the project (`compile`), building a runnable jar file (`jar`), running the simulator (`run`) and cleaning up build files (`clean`). The `compile` target creates a `bin/` directory and stores `.class` files in it, while the `jar` target generates a runnable `CentralitySimulation.jar` file in the root directory of the project.

Otherwise the source files along with the PeerSim library and its dependencies (available in the `lib` directory) can be imported and compiled with an IDE.

Running the project
-------------------

The `sim/` directory contains some example configuration files that can be used to run simulations.

To run a simulation with `ant`, invoke the `run` target with the required `arg` option to provide a configuration file. For example:

	ant run -Darg=sim/deccen_dolphins.cfg

To run the jar file directly, pass the configuration file as the first command line argument:

	java -jar CentralitySimulation.jar sum/deccen_dolphins.cfg

When running the simulation without generating a jar file, select `peersim.Simulator` as the main class. In this case the PeerSim library and its dependencies must be added to the classpath.

The networks used for the experiments are available in the `data/` directory.

