#!/bin/bash

#$ -S /bin/bash
#$ -cwd
#$ -q titan.q
#$ -j yes
#$ -N manhattan
#$ -t 1-42

ptSpacingList=($(seq 100 100 4000))
Ndrt="1500"
nReqs="300000"
meanDist="3000"
seed="42"
endTime="36000"
departureIntervalTime="900"
outputDir="900Mu0.77Zeta3AlphaNewStartLinks720Constr"

#for SGE_TASK_ID in $(seq 1 42); do

((idx=SGE_TASK_ID-1))
#((mod40=$idx % 40))
#((div40=$idx / 40))

if [[ $idx == 40 ]]; then
    ptSpacing="3000"
    mode="unimodal"
elif [[ $idx == 41 ]]; then 
    ptSpacing="3000"
    mode="car"
else
    ptSpacing=${ptSpacingList[$idx]}
    mode="bimodal"
fi

dCut=$(echo "0.77 * $ptSpacing / 1" | bc)
args="config.xml $mode $meanDist $dCut $ptSpacing $Ndrt $nReqs $seed $endTime $departureIntervalTime NaN $outputDir"
echo $args
#done

/scratch01.local/hheuer/jdk-11.0.2/bin/java -Xmx6g -jar /scratch01.local/hheuer/matsim-bimodal-InverseGamma3-OSM-1.0-SNAPSHOT-jar-with-dependencies.jar $args
