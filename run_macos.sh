# Run with ./run_macos.sh /path/to/target/folder
# Example: ./run_macos.sh STORMVis/target



java \
-Dfile.encoding=UTF-8 \
-classpath "\
$1/classes:\
/Users/$USER/.m2/repository/org/jzy3d/jzy3d-api/0.9.1/jzy3d-api-0.9.1.jar:\
/Users/$USER/.m2/repository/org/jzy3d/jzy3d-jdt-core/0.9.1/jzy3d-jdt-core-0.9.1.jar:\
/Users/$USER/.m2/repository/commons-io/commons-io/2.3/commons-io-2.3.jar:\
/Users/$USER/.m2/repository/org/apache/commons/commons-lang3/3.1/commons-lang3-3.1.jar:\
/Users/$USER/.m2/repository/log4j/log4j/1.2.16/log4j-1.2.16.jar:\
/Users/$USER/.m2/repository/net/sf/opencsv/opencsv/2.1/opencsv-2.1.jar:\
/Users/$USER/.m2/repository/junit/junit/4.11/junit-4.11.jar:\
/Users/$USER/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:\
/Users/$USER/.m2/repository/net/sourceforge/jregex/jregex/1.2_01/jregex-1.2_01.jar:\
/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt-main/2.0.2/gluegen-rt-main-2.0.2.jar:\
/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2.jar:\
/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-android-armv6.jar:\
/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-linux-amd64.jar:\
/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-linux-armv6.jar:\
/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-linux-armv6hf.jar:\
/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-linux-i586.jar:\
/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-macosx-universal.jar:\
/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-solaris-amd64.jar:\
/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-solaris-i586.jar:\
/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-windows-amd64.jar:\
/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-windows-i586.jar:\
/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all-main/2.0.2/jogl-all-main-2.0.2.jar:\
/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2.jar:\
/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-android-armv6.jar:\
/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-linux-amd64.jar:\
/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-linux-armv6.jar:\
/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-linux-armv6hf.jar:\
/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-linux-i586.jar:\
/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-macosx-universal.jar:\
/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-solaris-amd64.jar:\
/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-solaris-i586.jar:\
/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-windows-amd64.jar:\
/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-windows-i586.jar:\
/Users/$USER/.m2/repository/java3d/vecmath/1.3.1/vecmath-1.3.1.jar:\
/Users/$USER/.m2/repository/org/javatuples/javatuples/1.2/javatuples-1.2.jar:\
/Users/$USER/.m2/repository/net/sf/trove4j/trove4j/3.0.3/trove4j-3.0.3.jar:\
/Users/$USER/.m2/repository/gov/nih/imagej/imagej/1.47/imagej-1.47.jar\
" \
gui.Gui


# java -Dfile.encoding=UTF-8 -classpath /Users/$USER/Projekte/multnano/tkunerlab_suresim/STORMVis/target/classes:/Users/$USER/.m2/repository/org/jzy3d/jzy3d-api/0.9.1/jzy3d-api-0.9.1.jar:/Users/$USER/.m2/repository/org/jzy3d/jzy3d-jdt-core/0.9.1/jzy3d-jdt-core-0.9.1.jar:/Users/$USER/.m2/repository/commons-io/commons-io/2.3/commons-io-2.3.jar:/Users/$USER/.m2/repository/org/apache/commons/commons-lang3/3.1/commons-lang3-3.1.jar:/Users/$USER/.m2/repository/log4j/log4j/1.2.16/log4j-1.2.16.jar:/Users/$USER/.m2/repository/net/sf/opencsv/opencsv/2.1/opencsv-2.1.jar:/Users/$USER/.m2/repository/junit/junit/4.11/junit-4.11.jar:/Users/$USER/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:/Users/$USER/.m2/repository/net/sourceforge/jregex/jregex/1.2_01/jregex-1.2_01.jar:/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt-main/2.0.2/gluegen-rt-main-2.0.2.jar:/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2.jar:/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-android-armv6.jar:/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-linux-amd64.jar:/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-linux-armv6.jar:/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-linux-armv6hf.jar:/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-linux-i586.jar:/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-macosx-universal.jar:/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-solaris-amd64.jar:/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-solaris-i586.jar:/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-windows-amd64.jar:/Users/$USER/.m2/repository/org/jogamp/gluegen/gluegen-rt/2.0.2/gluegen-rt-2.0.2-natives-windows-i586.jar:/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all-main/2.0.2/jogl-all-main-2.0.2.jar:/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2.jar:/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-android-armv6.jar:/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-linux-amd64.jar:/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-linux-armv6.jar:/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-linux-armv6hf.jar:/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-linux-i586.jar:/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-macosx-universal.jar:/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-solaris-amd64.jar:/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-solaris-i586.jar:/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-windows-amd64.jar:/Users/$USER/.m2/repository/org/jogamp/jogl/jogl-all/2.0.2/jogl-all-2.0.2-natives-windows-i586.jar:/Users/$USER/.m2/repository/java3d/vecmath/1.3.1/vecmath-1.3.1.jar:/Users/$USER/.m2/repository/org/javatuples/javatuples/1.2/javatuples-1.2.jar:/Users/$USER/.m2/repository/net/sf/trove4j/trove4j/3.0.3/trove4j-3.0.3.jar:/Users/$USER/.m2/repository/gov/nih/imagej/imagej/1.47/imagej-1.47.jar gui.Gui