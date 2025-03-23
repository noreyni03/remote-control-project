package fr.uvsq.core;

/**
 * La classe `AuthManager` gère l'authentification des clients.
 * Elle vérifie si les identifiants (login et mot de passe) fournis par le client sont corrects.
 * Cette classe implémente une authentification simple, avec des identifiants codés en dur.
 */
public class AuthManager {
    // Identifiants valides codés en dur (simples pour ce projet)
    // Dans une application réelle, ces identifiants ne devraient pas être codés en dur.
    private static final String VALID_LOGIN = "admin";
    private static final String VALID_PASSWORD = "password123";

    /**
     * Vérifie si le login et le mot de passe fournis par le client sont corrects.
     * Cette méthode compare les identifiants reçus avec les identifiants valides stockés dans la classe.
     *
     * @param login    Le login envoyé par le client.
     * @param password Le mot de passe envoyé par le client.
     * @return true si les identifiants sont corrects, false sinon.
     *         Retourne également `false` si le login ou le mot de passe est `null`.
     */
    public boolean authenticate(String login, String password) {
        // Compare le login et le mot de passe avec les valeurs valides
        if (login == null || password == null) {
            return false; // Si l'un des deux est vide (null), on refuse l'authentification
        }
        return login.equals(VALID_LOGIN) && password.equals(VALID_PASSWORD);
    }
}