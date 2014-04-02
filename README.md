For local use, change the following in RunGame.java
 * remove org.bson and com.mongodb imports
 * remove DB db
 * in go(), remove the path ending with .sh from both new IORobot() declarations
 * remove everything in saveGame(), you can add your own code here to print the winner for example

Then compile as follows:
Windows (from cmd):

    [go to the directory containing the .java files]
    dir /b /s *.java>sources.txt
    md classes
    javac -d classes @sources.txt
    del sources.txt

Linux:

    [go to the directory containing the .java files]
    ls *.java > sources.txt
    mkdir classes
    javac -d classes @sources.txt
    rm sources.txt

Then to run:

    cd classes
    java main.RunGame 0 0 0 "java bot.BotStarter" "java bot.BotStarter" 2>err.txt 1>out.txt

change the bots according to your own bots, error log will be outputted to err.txt and out log will be outputted to out.txt
