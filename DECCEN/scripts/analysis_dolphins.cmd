echo Fraction > results/dolphins/analysis_dolphins.txt
echo CCerr CCPercentileErr CCpercInv BCerr BCPercentileErr BCpercInv SCerr SCPercentileErr SCpercInv >> results/dolphins/analysis_dolphins.txt
echo 0.05 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins005.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.10 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins010.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.15 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins015.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.20 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins020.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.25 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins025.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.30 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins030.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.35 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins035.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.40 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins040.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.45 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins045.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.50 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins050.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.55 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins055.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.60 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins060.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.65 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins065.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.70 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins070.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.75 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins075.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.80 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins080.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.85 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins085.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.90 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins090.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 0.95 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins095.txt 10 >> results/dolphins/analysis_dolphins.txt
echo 1.0 >> results/dolphins/analysis_dolphins.txt
java -jar centrality.jar -a results/dolphins/dolphins.indices results/dolphins/approximation_dolphins100.txt 1 >> results/dolphins/analysis_dolphins.txt
