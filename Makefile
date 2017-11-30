all:
	@echo "Compiling..."
	javac *.java

clean:
	rm -rf *.class

fast:
	rm -rf *.class
	javac *.java
	wait
	java Tournament full 
