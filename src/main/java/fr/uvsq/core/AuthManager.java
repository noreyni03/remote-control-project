package fr.uvsq.core;

/**
 * La classe `AuthManager` gère l'authentification des clients.
 * Elle vérifie si les identifiants (login et mot de passe) sont corrects.
 */
public class AuthManager {
    // Identifiants valides codés en dur (simples pour ce projet)
    private static final String VALID_LOGIN = "admin";
    private static final String VALID_PASSWORD = "password123";

    /**
     * Vérifie si le login et le mot de passe fournis sont corrects.
     *
     * @param login Le login envoyé par le client.
     * @param password Le mot de passe envoyé par le client.
     * @return true si les identifiants sont corrects, false sinon.
     */
    public boolean authenticate(String login, String password) {
        // Compare le login et le mot de passe avec les valeurs valides
        if (login == null || password == null) {
            return false; // Si l'un des deux est vide, on refuse
        }
        return login.equals(VALID_LOGIN) && password.equals(VALID_PASSWORD);
    }
}