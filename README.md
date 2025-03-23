# 🎯 Remote Control Pro - Système de Contrôle à Distance

## 🚀 Version : 1.0.0

### 👨‍💻 Développé par :
- **Abdoulaye Lah** ([GitHub](https://github.com/layelah))
- **Ousmane Mbaye** ([GitHub](https://github.com/noreyni03))
- **Moustapha Diagne** ([GitHub](https://github.com/moustaph18))

Bienvenue dans **Remote Control Pro**, une application client-serveur avancée conçue dans le cadre du cours de **Java Avancé** à l'**École Supérieure Polytechnique de Dakar (Master 1 GLSI)**. Ce projet propose une solution moderne pour **contrôler à distance** des ordinateurs via une **interface graphique intuitive et sécurisée**. 🔥

---

![CommandExecution](https://github.com/user-attachments/assets/fe852ed2-0c57-417a-93e8-b9104bd639eb)

---

![ThreadGestion](https://github.com/user-attachments/assets/2ee5456d-166b-4e3d-b6d4-e591919151e9)

---

## 📌 Description de l'Application

### 🌍 Aperçu Général
🔹 **Remote Control Pro** permet à un utilisateur (client) de se connecter à une machine distante (serveur) pour :
✅ Exécuter des **commandes système**
✅ **Transférer des fichiers**
✅ **Surveiller les activités** en temps réel

### 🔥 Fonctionnalités Principales
- **🖥️ Exécution de commandes à distance** : Exécutez `dir` (Windows) ou `ls` (Linux/macOS) et recevez le résultat instantanément.
- **🔄 Gestion multi-clients** : Supporte jusqu'à **10 clients** simultanément via un pool de threads.
- **🎨 Interface graphique intuitive** : Basée sur **JavaFX**, offrant une expérience utilisateur fluide.
- **🔒 Sécurité avancée** :
    - Authentification par **login/mot de passe** (`admin/password123` pour la démo).
    - **Communication SSL/TLS** chiffrée.
- **📂 Transfert de fichiers** : Envoi et récupération de fichiers entre client et serveur.
- **📜 Journalisation avancée** : Logs d'activités enregistrés dans des fichiers.
- **⏳ Historique des commandes** : Affichage des commandes précédemment exécutées.

---

## ⚙️ Architecture Technique
- **💻 Langage** : Java (version 21)
- **📚 Bibliothèques** : JavaFX, SLF4J/Logback, Java Sockets/Threads
- **🔗 Communication** : TCP avec SSL
- **📦 Modularité** : Organisation en packages (`client`, `server`, `core`)

---

## 💼 Cas d'Utilisation
🔹 **Administrateurs système** : Gestion à distance.
🔹 **Enseignement** : Démonstration des concepts réseau avancés.
🔹 **Entreprises** : Gestion sécurisée des serveurs.

---

## 🛠️ Prérequis pour le Déploiement
✅ **Java Development Kit (JDK) 21** - [Installation](https://adoptium.net/)
✅ **Maven** - [Installation](https://maven.apache.org/download.cgi)
✅ **Git (optionnel)** - [Installation](https://git-scm.com/downloads)

---

## 📥 Guide d'Installation et de Déploiement

### 1️⃣ Installer le JDK 21
🔹 **Windows** : Installez le `.msi`.
🔹 **macOS/Linux** : Installez le `.pkg` ou `.tar.gz`.
🔹 **Vérification** : `java -version` doit afficher `21`.

### 2️⃣ Installer Maven
🔹 Téléchargez et extrayez **Apache Maven**.
🔹 Ajoutez `bin` à la variable **PATH**.
🔹 Vérifiez avec `mvn -version`.

### 3️⃣ Télécharger le Projet
🔹 Accédez à [GitHub](https://github.com/noreyni03/remote-control-project).
🔹 Cliquez sur **Download ZIP**.
🔹 Extrayez l'archive.

### 4️⃣ Lancer le Serveur 🚀
```bash
cd chemin/vers/remote-control-project
mvn clean install
mvn exec:java -pl :server -Dexec.mainClass="fr.uvsq.server.gui.ServerGUI"
```
✅ Une fenêtre **Remote Control Server - v1.0** s'ouvrira.
✅ Cliquez sur **Start Server**.

### 5️⃣ Lancer le Client 🎯
1️⃣ Ouvrez une autre console.
2️⃣ Naviguez à nouveau vers le projet.
3️⃣ Exécutez :
```bash
mvn exec:java -pl :client -Dexec.mainClass="fr.uvsq.client.gui.ClientGUI"
```
4️⃣ Connectez-vous avec : **admin/password123**.

---

## 🖥️ Utilisation de l'Application

### 🎮 Côté Client
🔹 **Exécution de commandes** : Entrez une commande et cliquez sur **Execute**.
🔹 **Transfert de fichiers** : **Upload File** pour envoyer, **Download File** pour recevoir.
🔹 **Déconnexion** : Cliquez sur **Disconnect**.

### 📊 Côté Serveur
🔹 Liste des **clients connectés**.
🔹 Affichage des **logs d'activité**.
🔹 **Arrêt du serveur** avec **Stop Server**.

---

## ⚠️ Remarques Importantes
❗ **Localhost** : Par défaut `127.0.0.1`. Modifier si nécessaire.
❗ **Port** : `5001` (vérifier le pare-feu).
❗ **Erreurs courantes** : Assurez-vous que le **serveur** est bien démarré avant de lancer le **client**.

---

## 📞 Contact
- **Abdoulaye Lah** : [GitHub](https://github.com/layelah)
- **Ousmane Mbaye** : [GitHub](https://github.com/noreyni03)
- **Moustapha Diagne** : [GitHub](https://github.com/moustaph18)

🚀 Merci d'utiliser **Remote Control Pro** ! 🎯

