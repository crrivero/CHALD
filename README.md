# CHALD
Configuration Helper for Automated Link Discovery

## How to run
1.- Run edu.rit.goal.chald.tools.Sweep for scenario N. This will create a 'Sweep-N.txt' file in the results folder. <br>
2.- Run edu.rit.goal.chald.tools.ConfigurationSelection (adding your scenario if needed at the beginning). This will create files 'Results-{Algo}-{N}.txt' in the results folder, where Algo={CHALD, SLINT}. <br>
3.- Run edu.rit.goal.chald.tools.ExtractPAndR (adding your scenario if needed at the beginning). This will create files 'PAndR-{Algo}-{N}.txt', where Algo={Sweep, CHALD, SLINT}. You can use these files to plot results. <br>

## How to include a new scenario
TODO

## How to include a new approach
TODO
