all:
	@echo "Compiling..."
	javac *.java

clean:
	rm -rf *.class

fast:
	rm -rf *.class *.txt
	javac *.java
	wait
	java Tournament full 
