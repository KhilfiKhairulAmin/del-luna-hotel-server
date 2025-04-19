package dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import models.RoomType;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoomTypeDao {
    private static final String FILE_PATH = "roomTypes.json";
    private List<RoomType> roomTypes = new ArrayList<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void loadFromFile() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            Type roomTypeListType = new TypeToken<ArrayList<RoomType>>(){}.getType();
            roomTypes = gson.fromJson(reader, roomTypeListType);
            if (roomTypes == null) {
                roomTypes = new ArrayList<>(); // Initialize if file is empty
            }
        } catch (IOException e) {
            e.printStackTrace();
            roomTypes = new ArrayList<>();
        }
    }

    public void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(roomTypes, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CRUD Operations
    public RoomType createRoomType(RoomType roomType) {
        if (getRoomType(roomType.typeId) != null) {
            return null; // Room type already exists
        }
        roomTypes.add(roomType);
        return roomType;
    }

    public RoomType getRoomType(String typeId) {
        return roomTypes.stream()
                .filter(rt -> rt.typeId.equals(typeId))
                .findFirst()
                .orElse(null);
    }

    public List<RoomType> getAllRoomTypes() {
        return new ArrayList<>(roomTypes);
    }

    public void deleteRoomType(String typeId) {
        roomTypes.removeIf(rt -> rt.typeId.equals(typeId));
    }

    public RoomType updateRoomType(RoomType updatedRoomType) {
        for (int i = 0; i < roomTypes.size(); i++) {
            if (roomTypes.get(i).typeId.equals(updatedRoomType.typeId)) {
                roomTypes.set(i, updatedRoomType);
                return updatedRoomType;
            }
        }
        return null;
    }

    // Special queries
    public List<RoomType> getRoomTypesByPriceRange(double minPrice, double maxPrice) {
        return roomTypes.stream()
                .filter(rt -> rt.pricePerNight >= minPrice && rt.pricePerNight <= maxPrice)
                .collect(Collectors.toList());
    }

    public List<RoomType> getRoomTypesByCapacity(int minPax) {
        return roomTypes.stream()
                .filter(rt -> rt.numOfPax >= minPax)
                .collect(Collectors.toList());
    }

    public List<RoomType> getRoomTypesByMood(String mood) {
        return roomTypes.stream()
                .filter(rt -> {
                    if (rt.moods == null) return false;
                    for (String m : rt.moods) {
                        if (m.equalsIgnoreCase(mood)) return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    public List<RoomType> getRoomTypesByAmenity(String amenity) {
        return roomTypes.stream()
                .filter(rt -> {
                    if (rt.amenities == null) return false;
                    for (String a : rt.amenities) {
                        if (a.equalsIgnoreCase(amenity)) return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }
}
