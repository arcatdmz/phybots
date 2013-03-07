Phybots - a toolkit for making robotic thing
================================================================
Copyright (C) 2009-2013 Jun Kato, version 1.0.2
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

Phybots is a Java/Processing toolkit to prototype "robotic things."

## Install

### Processing

Download [PhybotsP5.zip](https://github.com/arcatdmz/phybots/blob/master/dist/PhybotsP5.zip) and extract its contents into libraries directory, e.g. *C:\Users\hoge\arc\Documents\Processing\libraries\PhybotsP5*.

Before you start writing code, select *PhybtsP5* from *Sketch > Import Library...* menu.

### Java

Download phybots-full-*.zip from [GitHub site](https://github.com/arcatdmz/phybots/tree/master/dist), extract its contents to wherever you want, and add all jar files to the classpath.

### Prepare other dependent library

 * To use serial/parallel connector with Java version, you need [RXTXlib](rxtx.qbang.org/wiki/index.php/Download) on the classpath.
 * To use DirectShow capture on Windows, you need [DirectShow for Java](www.humatic.de/htools/dsj.htm) on the classpath.
 * To use JMF video capture on Linux, you need JMF library on the classpath.

## Write code

As usual, [Javadoc](http://phybots.com/javadoc/) is the bible. Some tutorials are available on [phybots.com](http://phybots.com/). For concrete Processing examples, see examples directory in the library package. For Java, see [src.sample directory](https://github.com/arcatdmz/phybots/tree/master/phybots/src.sample).

## License

This library is distributed under MPL 1.1/GPL 2.0/LGPL 2.1
triple license. Please read LICENSE.txt for the detail.
You can get the source code by visiting its official site.

"napkit" is required when you want to know position and
rotation of robots and other physical entities in your
application. It uses a webcam to detect ARToolKit markers.
"napkit" is distributed under GNU GPLv3.

Please see http://phybots.com/ for details.

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
http://phybots.com/
arc (at) digitalmuseum.jp