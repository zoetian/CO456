all:
	@echo "Compiling..."
	javac *.java

clean:
	rm -rf *.class result.txt analysis.txt

fast:
	rm -rf *.class result.txt analysis.txt
	javac *.java
	wait
	java Tournament full > result.txt
	wait
	python3 parse.py > analysis.txt
	wait
	cat analysis.txt

parse:
	python3 parse.py > analysis.txt
	wait
	cat analysis.txt
