package dao;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import models.Guest;

public class GuestDao {
    private static final String FILE_PATH = "guests.txt";
    private List<Guest> guests = new ArrayList<>();

    public void loadFromFile() {
        guests.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            // Skip header
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 8) continue;

                Guest guest = new Guest();
                guest.guestId = parts[0];
                guest.email = parts[1];
                guest.fullName = parts[2];
                guest.phone = parts[3];
                guest.gender = parts[4];
                guest.points = Integer.parseInt(parts[5]);
                guest.level = parts[6];
                guest.tag = parts[7];
                guests.add(guest);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write("guestId,email,fullName,phone,gender,points,level,tags\n");
            for (Guest guest : guests) {
                writer.write(String.join(",",
                        guest.guestId,
                        guest.email,
                        guest.fullName,
                        guest.phone,
                        guest.gender,
                        String.valueOf(guest.points),
                        guest.level,
                        guest.tag));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Guest createGuest(Guest guest) {
        if (getGuest(guest.guestId) != null) {
            return null; // Guest exists
        }
        guests.add(guest);
        return guest;
    }

    public Guest getGuest(String id) {
        return guests.stream()
                .filter(g -> g.guestId.equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Guest> getAllGuests() {
        return new ArrayList<>(guests);
    }

    public void deleteGuest(String id) {
        guests.removeIf(g -> g.guestId.equals(id));
    }
}
