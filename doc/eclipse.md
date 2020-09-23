# Add new Java version to Eclipse Project

1. Rightclick the project root, --> `Run AS` --> `Run Configurations`
![eclipse/eclipse_setup_1.png](eclipse/eclipse_setup_1.png)

2. Under `Maven Build` --> `JRE` --> `Check Alternate JRE`--> `Installed JREs`
![eclipse/eclipse_setup_2.png](eclipse/eclipse_setup_2.png)


3. `Add` --> `JRE home` paste java home path obtained from `/usr/libexec/java_home -V`
![eclipse/eclipse_setup_3.png](eclipse/eclipse_setup_3.png)

4. Build with maven. First run `Maven clean`, then `Maven install` and then `Maven build`. Note: If eclipse complains that a compile goal has to be specified, one has to `Maven build ...` and enter `package` under `goals`.

![eclipse/eclipse_setup_4.png](eclipse/eclipse_setup_4.png)

5. Run GUI

![eclipse/eclipse_setup_5.png](eclipse/eclipse_setup_5.png)