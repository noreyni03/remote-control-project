# Projet de Contrôle à Distance

Ce projet consiste en un système de contrôle à distance client-serveur permettant d'exécuter des commandes système à distance. L'application comprend une interface graphique (GUI) pour le serveur et le client, facilitant l'interaction.

## Fonctionnalités

*   **Exécution de commandes à distance:** Le client peut envoyer des commandes au serveur pour qu'il les exécute sur la machine serveur.
*   **Gestion de multiples clients:** Le serveur peut gérer plusieurs clients simultanément grâce à un pool de threads.
*   **Interface graphique (GUI):** Le serveur et le client disposent d'interfaces graphiques conviviales pour une meilleure expérience utilisateur.
*   **Journalisation:** Le serveur enregistre les événements et les commandes exécutées dans une zone de log.
*   **Historique des commandes:** Le client conserve l'historique des commandes exécutées.
* **Connexion/Déconnexion :** Le client peut se connecter et se déconnecter du serveur à tout moment.
* **Affichage de la liste des clients connectés :** le serveur affiche la liste des clients qui lui sont connectés.

## Prérequis

Avant de lancer le projet, assurez-vous d'avoir les éléments suivants installés sur votre système :

*   **Java Development Kit (JDK) 11 ou supérieur:** Le projet est développé en Java, vous devez donc avoir le JDK installé. Vous pouvez le télécharger sur le site d'Oracle ou utiliser une distribution OpenJDK.
*   **Maven:** Maven est utilisé pour la gestion du projet et de ses dépendances. Vous pouvez le télécharger sur le site officiel d'Apache Maven.
*   **Un IDE (facultatif):** Bien que non obligatoire, un IDE comme IntelliJ IDEA, Eclipse ou NetBeans facilitera grandement le développement et l'exécution du projet.

## Instructions d'exécution

Voici les étapes pour exécuter le projet :

1.  **Cloner le dépôt:**
    ```bash
    git clone https://github.com/noreyni03/remote-control-project.git
    cd remote-control-project
    ```

2.  **Compiler le projet avec Maven:**
    Ouvrez un terminal et naviguez vers le répertoire racine du projet (`remote-control-project`). Exécutez la commande suivante :
    ```bash
    mvn clean install
    ```
    Cette commande va nettoyer les anciens fichiers de compilation, compiler le code source et générer les fichiers `.jar` dans le répertoire `target`.

3.  **Exécuter le serveur:**
    *   **Méthode 1 : Via Maven (recommandé pour le développement)**
        ```bash
        mvn exec:java -pl :server -Dexec.mainClass="fr.uvsq.server.gui.ServerGUI"
        ```
        Cette commande va démarrer la GUI du serveur.
    *   **Méthode 2 : Via le fichier JAR (pour une exécution autonome)**
        Naviguez vers le répertoire `target` du module `server` :
        ```bash
        cd target
        ```
        Puis, exécutez la commande suivante :
        ```bash
        java -jar [NOM_DU_FICHIER_JAR_SERVEUR].jar
        ```
        Remplacez `[NOM_DU_FICHIER_JAR_SERVEUR]` par le nom du fichier JAR du serveur (il se trouve dans le dossier `server/target` et son nom commencera par `server-`).

4.  **Exécuter le client:**
    *   **Méthode 1 : Via Maven (recommandé pour le développement)**
        Ouvrez un nouveau terminal (en plus de celui utilisé pour le serveur) et naviguez vers le répertoire racine du projet (`remote-control-project`). Exécutez la commande suivante :
        ```bash
        mvn exec:java -pl :client -Dexec.mainClass="fr.uvsq.client.gui.ClientGUI"
        ```
    *   **Méthode 2 : Via le fichier JAR (pour une exécution autonome)**
        Naviguez vers le répertoire `target` du module `client`:
        ```bash
        cd client/target
        ```
        Puis, exécutez la commande suivante :
        ```bash
        java -jar [NOM_DU_FICHIER_JAR_CLIENT].jar
        ```
        Remplacez `[NOM_DU_FICHIER_JAR_CLIENT]` par le nom du fichier JAR du client (il se trouve dans le dossier `client/target` et son nom commencera par `client-`).

5.  **Utiliser l'application:**
    *   **Serveur:** L'interface graphique du serveur affichera les logs et la liste des clients connectés.
    *   **Client:** Dans l'interface du client, vous pouvez :
        *   Cliquer sur le bouton "Connect" pour vous connecter au serveur (l'adresse IP du serveur est `127.0.0.1` par défaut et le port est `5001`).
        *   Entrer une commande système dans le champ de texte.
        *   Cliquer sur "Execute" pour exécuter la commande sur le serveur.
        *   La sortie de la commande s'affichera dans la zone de sortie.
        * Les commandes exécutées seront affichées dans l'historique.
        * cliquer sur clear pour vider la zone de sortie.

## Remarques

*   **Adresse IP du serveur:** Par défaut, le client se connecte à l'adresse IP `127.0.0.1` (localhost). Si le serveur est sur une autre machine ou une machine virtuelle, vous devez modifier la variable `serverIP` dans la classe `ClientGUI` en conséquence.
*   **Port du serveur:** Le serveur écoute sur le port `5001` par défaut.
* **Exécution sur WSL :** Si vous utilisez le sous-système Windows pour Linux (WSL), assurez vous d'utiliser l'ip de WSL dans le client, et non 127.0.0.1.
* **Erreur :** Si le serveur n'est pas lancé, le client affichera une boite d'erreur.
