Compilation Instructions:
------------------------

1. Copy the causalbroadcast package to all the machines to run to communication on and extract the package. This can be done by:
    1.1 Create a tarball of the package using 
            tar -uvf causalbroadcast.tar causalbroadcast/
    1.2 Copy the tarball using scp to all the nodes:
            scp -i <ssh-key> causalbroadcast.tar <user>@dc<nodeno>.utdallas.edu:~
    1.3 Extract the package in the home directory using
            tar -xvf causalbroadcast.tar
    * Optional:
        scp can go wrong sometimes. A good practice to unsure that the package has not corrupted is to validate the checksum of the tarball at both the sender and the receiver.
        This can be done using the following:
            sha256sum <file>

2. Ssh to all 4 nodes at the same time. This can be done using terminals like iterm. Execute the following commands
    2.1 Compile the package using
            javac -d . *.java
    2.2 Run the package using
            java causalbroadcast.comDriver | tee <nodename>.log

3. To verify the results, collect the logfile you want to verify and run the message verifier.
    3.1 Compile the verifier using
            javac MessageVerifier.java
    3.2 Run the verifier using
            java MessageVerifier
