package dao;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import models.Credential;

public class CredentialDao {
    private static final String FILE_PATH = "credentials.txt";
    private List<Credential> credentials = new ArrayList<>();

    public void loadFromFile() {
        credentials.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            // Skip header
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 2) continue; // At least guestId and passwordHash required

                Credential credential = new Credential();
                credential.guestId = parts[0];
                credential.passwordHash = parts[1];
                credential.sessionId = parts.length > 2 ? parts[2] : "";
                credentials.add(credential);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write("guestId,passwordHash,sessionId\n");
            for (Credential credential : credentials) {
                writer.write(String.join(",",
                        credential.guestId,
                        credential.passwordHash,
                        credential.sessionId != null ? credential.sessionId : ""));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CRUD Operations
    public Credential createCredential(Credential credential) {
        if (getCredential(credential.guestId) != null) {
            return null; // Credential exists
        }
        credentials.add(credential);
        return credential;
    }

    public Credential getCredential(String guestId) {
        return credentials.stream()
                .filter(c -> c.guestId.equals(guestId))
                .findFirst()
                .orElse(null);
    }

    public List<Credential> getAllCredentials() {
        return new ArrayList<>(credentials);
    }

    public void deleteCredential(String guestId) {
        credentials.removeIf(c -> c.guestId.equals(guestId));
    }

    // Authentication-specific methods
    public boolean validateCredentials(String guestId, String passwordHash) {
        return credentials.stream()
                .anyMatch(c -> c.guestId.equals(guestId) && 
                             c.passwordHash.equals(passwordHash));
    }

    public void updateSessionId(String guestId, String sessionId) {
        credentials.stream()
                .filter(c -> c.guestId.equals(guestId))
                .findFirst()
                .ifPresent(c -> c.sessionId = sessionId);
    }

    public Optional<Credential> getBySessionId(String sessionId) {
        return credentials.stream()
                .filter(c -> sessionId != null && 
                            !sessionId.isEmpty() && 
                            sessionId.equals(c.sessionId))
                .findFirst();
    }
}