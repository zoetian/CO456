all:
	@echo "Compiling..."
	javac *.java

clean:
	rm -rf *.class result.txt

fast:
	rm -rf *.class result.txt
	javac *.java
	wait
	java Tournament full
