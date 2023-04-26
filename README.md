# Algorithmique 2 - Projet R-Tree

## Introduction
Ce projet vise à implémenter une façon de vérifier si un point donné est situé dans un endroit géographique.
Pour ce faire, nous avons utilisé un R-Tree (qui contient des Minimum Bounding Rectangles), qui permet de hiérarchiser, par une relation de qui contient qui, les rectangles représentant des zones géographiques diverses.
### Librairies utilisées :

## Comment compiler et exécuter les tests
mvn clean install

## Comment exécuter le code
mvn exec:java -Dexec.mainClass="org.geotools.tutorial.quickstart.Quickstart"