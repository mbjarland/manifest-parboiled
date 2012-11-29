Work in progress
----------------
This repository was just created. The parser will be completed within the 
next week or two. 

Why do this?
------------
The java Manifest class (used for parsing MANIFEST.MF files found in 
jaf files, ear files, etc) and the corresponding Attributes class implementations
are some of the worst code I have seen in the JDK. When the manifest class 
encounters an error, such as illegal attribute names, lines which are too 
long, invalid characrters etc it: 

* Throws an IOException. Just to be sure you got that right: not a specific exception, 
  an IOException. Catching IOException will also catch things like FileNotFond, 
  MalformedURLException, ZipException, etc. So the only way to figure out if this 
  was a manifest parsing exception is to read the source code for Attributes.java,
  find all messages it can send out, and then proceed to proudly do string parsing 
  on the message contained in the caught excpetion. Yeah, way to go.  
* It does not include ANY information on what file or line/column in 
  that file the error was encountered in. None.  
* It gives you no chance of collecting multiple errors and dealing with 
  them in a controlled manner.

Am I upset about this piece of code. Well...a bit. 

As an example, I have implemented a build framework for e-commerce projects 
where we on a daily basis and for a number of customers scan hundreds of 
manifest files for certain meta data. 

When something goes wrong with one of the manifest files, all we get from java
is things like: 

  new IOException("invalid header field")

This tells us close to nothing. No information on what header attribute the issue
was with, what file it was encountered in, what line, or what column we are supposed
to be looking at. I understand the importance of having generic methods which take
an InputStream etc, but when the user _is_ actually sending in a file object, 
how about giving us some more information? 

Needless to say, digging through 500 manifest files using divide 
and conquer is not a joyful experience. Hence this project. 
