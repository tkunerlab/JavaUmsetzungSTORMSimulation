


## MacOS - Tested on Catalina 10.16

1. Install maven build management, preferably through homebrew

```bash
brew install mvn
```

2. build project 
*  due to changing restrictions on spawning gui threads starting in OS 10.14, a specific combination of java and the apple SDK is required.
* a working solution from 23.09.2020 is to install the [zulu java version](https://www.azul.com/downloads/zulu-community/?version=java-14&os=macos&architecture=x86-64-bit&package=jdk).
* Then build the project with the new java version, see commands below

```bash
# list java versions on machine
/usr/libexec/java_home -V
# make zulu default java
export JAVA_ZULU_HOME=/Library/Java/JavaVirtualMachines/zulu-11.jdk/Contents/Home
export JAVA_HOME=$JAVA_ZULU_HOME
# build with maven
mvn clean
mvn install
mvn package
```

3. run the application via the command line

```bash
./run_macos.sh /path/to/target
# Example: 
./run_macos.sh STORMVis/target
```

4. (Optional) add java version to eclipse and run in eclipse
* See [doc/eclipse.md](doc/eclipse.md)


# Troubleshooting

## MacOS - NSInternalInconsistencyException when loading examples
```bash
2020-09-22 16:45:04.520 java[64585:7666166] *** Terminating app due to uncaught exception 'NSInternalInconsistencyException', reason: 'NSWindow drag regions should only be invalidated on the Main Thread!'
*** First throw call stack:
(
	0   CoreFoundation                      0x00007fff338dcbe7 __exceptionPreprocess + 250
	1   libobjc.A.dylib                     0x00007fff6c51b5bf objc_exception_throw + 48
	2   CoreFoundation                      0x00007fff339053dc -[NSException raise] + 9
	3   AppKit                              0x00007fff30b03ddc -[NSWindow(NSWindow_Theme) _postWindowNeedsToResetDragMarginsUnlessPostingDisabled] + 310
	4   AppKit                              0x00007fff30aeb842 -[NSWindow _initContent:styleMask:backing:defer:contentView:] + 1416
	5   AppKit                              0x00007fff30aeb2b3 -[NSWindow initWithContentRect:styleMask:backing:defer:] + 42
	6   libnativewindow_macosx.jnilib       0x00000001a1ea90c8 Java_jogamp_nativewindow_macosx_OSXUtil_CreateNSWindow0 + 440
	7   ???                                 0x000000010bfd3330 0x0 + 4496110384
)
libc++abi.dylib: terminating with uncaught exception of type NSException
```
**Known issues**:
* https://github.com/dscalzi/HeliosLauncher/issues/70
* https://github.com/AdoptOpenJDK/openjdk-support/issues/101

**FIXED (23.09.2020)**:
* Fixed by switching to zulu-java 11
* https://www.azul.com/downloads/zulu-community/?version=java-14&os=macos&architecture=x86-64-bit&package=jdk
* Simply download and install the package


* To list the java versions installed on your machine run 

```bash
/usr/libexec/java_home -V

Matching Java Virtual Machines (3):
    11.0.8, x86_64:	"Zulu 11.41.23"	/Library/Java/JavaVirtualMachines/zulu-11.jdk/Contents/Home
    11.0.8, x86_64:	"AdoptOpenJDK 11"	/Library/Java/JavaVirtualMachines/adoptopenjdk-11.jdk/Contents/Home

/Library/Java/JavaVirtualMachines/zulu-11.jdk/Contents/Home
```
* to switch the default java version to the newly installed one, run

```bash
# simple way to switch java versions 
# from https://medium.com/w-logs/installing-java-11-on-macos-with-homebrew-7f73c1e9fadf
export JAVA_ZULU_HOME=/Library/Java/JavaVirtualMachines/zulu-11.jdk/Contents/Home
export JAVA_HOME=$JAVA_ZULU_HOME
```

* to reset the default java version, just open a new shell

## other links related to MAC issues


* https://stackoverflow.com/questions/8826881/maven-install-on-mac-os-x
* https://www.thethingsnetwork.org/forum/t/lora-development-utility-on-macos-no-devices-found/21423
* https://www.oracle.com/java/technologies/javase/jdk-jre-macos-catalina.html
* https://github.com/facelessuser/Rummage/issues/353
