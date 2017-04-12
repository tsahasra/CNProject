cd %1
"%JAVA_HOME%"\javac -cp %1\src %1\src\PeerProcess.java
java -cp %1\src PeerProcess %2