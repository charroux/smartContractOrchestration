ORCHA
========

Gradle 2.2.1
Spring Tool Suite 3.8.3.RELEASE or highter for auto completion in the properties files
Groovy Eclipse plugin snapshot builds for Eclipse 4.6 : http://dist.springsource.org/snapshot/GRECLIPSE/e4.6/

Configuration property classes (annotated @ConfigurationProperties) with should be Java classes (not Groovy).
The spring-configuration-metadata.json is generated automatically in the forder build\classes\main\META-INF only
when gradle build in used on a no gradle project and no eclipse project. This file should be copied manually in the folder
src/main/resources/META-INF where it can be edited. Then the autocompletion works 


