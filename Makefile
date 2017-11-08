all:
	@echo "Compiling..."
	javac *.java

clean:
	rm -rf *.class recvInfo channelInfo *.txt

fast:
	rm -rf *.class recvInfo channelInfo *.txt
	javac *.java
