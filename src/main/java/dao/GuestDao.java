package dao;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import models.Guest;
import utils.Hash;

public class GuestDao {
    private static final String FILE_PATH = "src/main/resources/guests.txt";
    private List<Guest> guests = new ArrayList<>();

    public void loadFromFile() {
        guests.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            // Skip header
            reader.readLine();
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1); // Preserve empty fields
                if (parts.length < 9) continue; // Minimum required fields

                Guest guest = new Guest();
                guest.guestId = parts[0];
                guest.email = parts[1];
                guest.fullName = parts[2];
                guest.phone = parts[3];
                guest.gender = parts[4];
                guest.points = Integer.parseInt(parts[5]);
                guest.level = parts[6];
                guest.tag = parts[7];
                guest.passwordHash = parts.length > 8 ? parts[8] : "";
                guest.sessionId = parts.length > 9 ? parts[9] : "";
                
                guests.add(guest);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            // Write header
            writer.write("guestId,email,fullName,phone,gender,points,level,tags,passwordHash,sessionId\n");
            
            for (Guest guest : guests) {
                writer.write(String.join(",",
                    guest.guestId,
                    guest.email,
                    guest.fullName,
                    guest.phone,
                    guest.gender,
                    String.valueOf(guest.points),
                    guest.level,
                    guest.tag,
                    guest.passwordHash,
                    guest.sessionId != null ? guest.sessionId : ""
                ));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CRUD operations
    public Guest createGuest(Guest guest) {
        if (getGuest(guest.guestId) != null) return null;
        guest.level = "First-time User";
        guest.points = 0;
        guest.tag = "Newcomer";
        guest.passwordHash = Hash.hashString(guest.passwordHash);
        guest.guestId = Integer.toString(Integer.parseInt(guests.get(guests.size()-1).guestId)+1);
        guests.add(guest);
        return guest;
    }

    public boolean updateGuest(Guest updatedGuest) {
        for (int i = 0; i < guests.size(); i++) {
            Guest existingGuest = guests.get(i);
            if (existingGuest.guestId.equals(updatedGuest.guestId)) {
                // Preserve fields that shouldn't be updated directly
                updatedGuest.points = existingGuest.points;
                updatedGuest.level = existingGuest.level;
                // updatedGuest.tag = existingGuest.tag;
                updatedGuest.passwordHash = existingGuest.passwordHash;
                updatedGuest.sessionId = existingGuest.sessionId;
                
                guests.set(i, updatedGuest);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    public Guest getGuest(String guestId) {
        return guests.stream()
                .filter(g -> g.guestId.equals(guestId))
                .findFirst()
                .orElse(null);
    }

    public List<Guest> getAllGuests() {
        return new ArrayList<>(guests);
    }

    public void deleteGuest(String guestId) {
        guests.removeIf(g -> g.guestId.equals(guestId));
    }

    // Authentication methods
    public Guest authenticate(String email, String passwordHash) {
        return guests.stream()
                .filter(g -> g.email.equalsIgnoreCase(email) 
                           && g.passwordHash.equals(passwordHash))
                .findFirst()
                .orElse(null);
    }

    public void updateSession(String guestId, String sessionId) {
        guests.stream()
                .filter(g -> g.guestId.equals(guestId))
                .findFirst()
                .ifPresent(g -> g.sessionId = sessionId);
    }

    public Guest getGuestBySession(String sessionId) {
        return guests.stream()
                .filter(g -> g.sessionId != null 
                           && g.sessionId.equals(sessionId))
                .findFirst()
                .orElse(null);
    }

    public Guest getGuestByEmail(String email) {
        return guests.stream()
                .filter(g -> g.email != null 
                           && g.email.equals(email))
                .findFirst()
                .orElse(null);
    }

    public Guest getGuestById(String guestId) {
        return guests.stream()
                .filter(g -> g.guestId != null 
                           && g.guestId.equals(guestId))
                .findFirst()
                .orElse(null);
    }

    // Points management
    public boolean addPoints(String guestId, int points) {
        Guest guest = getGuest(guestId);
        if (guest != null) {
            guest.points += points;
            return true;
        }
        return false;
    }
}