# 2D Particle Spring System
Project for particle spring system.

## Set up

Java Version

+ ***[1.8.0](https://java.com/en/download/manual.jsp)***


## Run the demo

In order to compile and run the application, we recommend using IDE Eclipse Java (Version. 2019-09 or newer) with JRE 1.8.0.
App window can be initiated by running **`/A2App.java`** in Eclipse.

## Design Remarks

1. Alpha is computed using dot product inspired by projection. This computes the alpha even if three particles is not co-linear. This is extremely useful when it comes to compute repulsion.
2. Normal is computed by rotating vector ab by -90 degrees, i.e. clockwise 90 degrees, then normalized.
3. An epsilon of 1e-6 is added to avoid floating point errors in solving CCD.
4. In repulsion part, normal is defined to be pointing to the third particle C. This makes sure the d value is calculated correctly.

## Little Movie - Pinball-Puzzle!

1. A starting test system called "pinball" is added to TestSystem.java as a starting point for creating an amusing pinball-puzzle!
2. You can customize your puzzle by clicking screen and connecting the particles.
3. Gravity is not used as we are simulating a pinball on a table.
4. You can disable repulsion to avoid pinball to lose too much kinetic energies after bouncing around walls.
5. Setting a large restitution coefficient is necessary for the longevity of animation.