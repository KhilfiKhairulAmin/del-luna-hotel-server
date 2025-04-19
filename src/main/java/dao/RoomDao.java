package dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import models.Room;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoomDao {
    private static final String FILE_PATH = "rooms.json";
    private List<Room> rooms = new ArrayList<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void loadFromFile() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            Type roomListType = new TypeToken<ArrayList<Room>>(){}.getType();
            rooms = gson.fromJson(reader, roomListType);
            if (rooms == null) {
                rooms = new ArrayList<>(); // Initialize if file is empty
            }
        } catch (IOException e) {
            e.printStackTrace();
            rooms = new ArrayList<>();
        }
    }

    public void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(rooms, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CRUD Operations
    public Room createRoom(Room room) {
        if (getRoom(room.roomNum) != null) {
            return null; // Room already exists
        }
        rooms.add(room);
        return room;
    }

    public Room getRoom(String roomNum) {
        return rooms.stream()
                .filter(r -> r.roomNum.equals(roomNum))
                .findFirst()
                .orElse(null);
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms);
    }

    public void deleteRoom(String roomNum) {
        rooms.removeIf(r -> r.roomNum.equals(roomNum));
    }

    public Room updateRoom(Room updatedRoom) {
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).roomNum.equals(updatedRoom.roomNum)) {
                rooms.set(i, updatedRoom);
                return updatedRoom;
            }
        }
        return null;
    }

    // Special queries
    public List<Room> getRoomsByFloor(int floorLevel) {
        return rooms.stream()
                .filter(r -> r.floorLevel == floorLevel)
                .collect(Collectors.toList());
    }

    public List<Room> getRoomsByType(String roomType) {
        return rooms.stream()
                .filter(r -> r.roomType.equals(roomType))
                .collect(Collectors.toList());
    }

    public boolean addReviewToRoom(String roomNum, String reviewId) {
        Room room = getRoom(roomNum);
        if (room == null) return false;

        // Initialize reviewIds if null
        if (room.reviewIds == null) {
            room.reviewIds = new String[0];
        }

        // Check if review already exists
        for (String id : room.reviewIds) {
            if (id.equals(reviewId)) {
                return false;
            }
        }

        // Add new review
        String[] newReviewIds = new String[room.reviewIds.length + 1];
        System.arraycopy(room.reviewIds, 0, newReviewIds, 0, room.reviewIds.length);
        newReviewIds[room.reviewIds.length] = reviewId;
        room.reviewIds = newReviewIds;
        return true;
    }
}