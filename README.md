# Algorithmique 2 - Projet R-Tree
## Auteurs : Moïra Vanderslagmolen & Andrius Ežerskis

## Introduction
Ce projet vise à implémenter une façon de vérifier si un point donné est situé dans un endroit géographique.
Pour ce faire, nous avons utilisé un R-Tree (qui contient des Minimum Bounding Rectangles), qui permet de hiérarchiser, par une relation de qui contient qui, les rectangles représentant des zones géographiques diverses.
### Librairies utilisées :

java.util.ArrayList
java.util.Random;
geotools

## Comment compiler et exécuter les tests
mvn clean install

## Comment exécuter le code
mvn exec:java -Dexec.mainClass="org.geotools.tutorial.quickstart.Quickstart"

## Liens pour les shapefiles des différentes zones géographiques
Japon : https://data.humdata.org/dataset/cod-ab-jpn?
Belgique (carte de 1972) : https://statbel.fgov.be/fr/open-data/secteurs-statistiques-2022
France (Export 2018) : https://www.data.gouv.fr/fr/datasets/contours-des-regions-francaises-sur-openstreetmap/
Monde (World Country Polygons - Very High Definition) : https://datacatalog.worldbank.org/search/dataset/0038272/World-Bank-Official-Boundaries
