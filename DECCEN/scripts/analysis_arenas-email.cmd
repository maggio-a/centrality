echo Fraction > results/arenas-email/analysis_arenas-email.txt
echo CCerr CCPercentileErr CCpercInv BCerr BCPercentileErr BCpercInv SCerr SCPercentileErr SCpercInv >> results/arenas-email/analysis_arenas-email.txt
echo 0.05 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email005.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.10 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email010.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.15 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email015.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.20 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email020.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.25 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email025.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.30 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email030.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.35 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email035.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.40 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email040.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.45 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email045.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.50 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email050.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.55 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email055.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.60 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email060.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.65 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email065.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.70 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email070.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.75 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email075.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.80 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email080.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.85 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email085.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.90 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email090.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 0.95 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email095.txt 10 >> results/arenas-email/analysis_arenas-email.txt
echo 1.0 >> results/arenas-email/analysis_arenas-email.txt
java -jar centrality.jar -a results/arenas-email/arenas-email.indices results/arenas-email/approximation_arenas-email100.txt 1 >> results/arenas-email/analysis_arenas-email.txt
