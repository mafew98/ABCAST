Project Description:
-------------------

The code in this repository attempts to complete a totally ordered communication broadcast of 500 messages between 5 different nodes - C1, C2, C3, C4 and C5, ie, all five proccesses must deliver 500 messages in the exact same order. Following are the particulars of the system:

        • Each process has a stream socket connection with the other four processes. The stream sockets are established with the following logic - "Each process sends a connection request to all processes with a higher node number than itself and accepts connection requests from nodes with number lower than its own.". Using this logic, pairwise connectivity is established amongst all the processes.

        • Once all pair-wise connections are set up, the five processes elect one of the processes as the sequencer process that will assign a total ordering to each message. 
        
        • With the sequencer selected and known to all the processes in the system, each of the five processes perform causally ordered broadcasting by executing the following two steps in a loop, for a total of 100 message broadcasts:
                – Wait for a random amount of time.
                – Send a message to all processes emulating broadcast as a set of unicasts along all the local socket connections.
        
        • When a process recieves an unsequenced message, one of the two actions is done:
                - if the process is the sequencer, it will sequence the message, deliver it according to the sequence number locally and broadcast the sequenced message with the sequence number attached to it.
                - if the process is not the sequencer, it will buffer the message and wait for the sequence number from the sequencer to be received.
        
        • When a non sequencer process receives a sequenced message, the process will remove the message from the buffer and deliver the message.

        • The whole thing terminates once 500 messages are received in the same order across all processes.

Further details of the project, algorithm used and references are present in design.pdf.

Compilation Instructions:
------------------------

1. Copy the totalbroadcast package to all the machines to run to communication on and extract the package. This can be done by:

    1.1 Create a tarball of the package using 
            tar -uvf totalbroadcast.tar totalbroadcast/

    1.2 Copy the tarball using scp to all the nodes:
            scp -i <ssh-key> totalbroadcast.tar <user>@<nodeName/IP>:~

    1.3 Extract the package in the home directory using
            tar -xvf totalbroadcast.tar

    * Optional:
        scp can go wrong sometimes. A good practice to unsure that the package has not corrupted is to validate the checksum of the tarball at both the sender and the receiver.
        This can be done using the following:
            sha256sum <file>

2. Ssh to all 4 nodes at the same time. This can be done using terminals like iterm. Execute the following commands

    2.1 Compile the package using
            javac -d . *.java

    2.2 Run the package using
            java totalbroadcast.comDriver | tee <nodename>.log


3. To verify the causal ordering, collect the logfile you want to verify and run the message verifier.

    3.1 Compile the verifier using
            javac MessageVerifier.java

    3.2 Run the verifier using
            java MessageVerifier


4. To verify the total ordering across all the logfiles, place them all in the same folder and run the abVerifier.

    4.1 Compile the verifier using
            javac abVerifier.java

    4.2 Run the verifier using
            java abVerifier <path to log folder>

Modification Instructions:
-------------------------
- The current implementation has the number of nodes hardcoded as 5 in VectorClock.java, ChannelManager.java and MessageBroker.java. This can be incremented to work for more nodes. Unifying this into a singular input in the system properties file is trivial.

- The efficiency of the communication depends on the network you run it on. This implementation is not aware of the network load and hence is prone to buffer bloat. Adjust the random delay amount present in MessageBroadcaster.java to fit your network.

- The maximum messages generated are fixed at 500 currently. This can be changed at the MessageBroadcaster, Broker, ConnectionContext, Sequencer and Receiver.

Potential Improvements:
----------------------
The current implementation statically assigns the first node as the sequencer node. An election is not explicitly done. Implementing an explicit election algorithm that runs every few messages received such that the sequencer moves between the nodes would be an improvement.

Dependencies:
------------
This project requires Java SE Development Kit (JDK) installed on your system. This code is written for Java 7.

Contributing:
------------
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Footnote:
--------
 To manually view all the delivered messages only (VSCode Regex formula):
        Run find with the following regex - ^(?!Delivered:\s\d+(?:,\d+)*:Message\sno\.\d+\sfrom\s\d+$).*
