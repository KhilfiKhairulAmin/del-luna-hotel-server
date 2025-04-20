package controllers;

import static spark.Spark.post;
import static spark.Spark.get;

import utils.Logger;

import java.security.Key;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dao.GuestDao;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import models.Guest;
import utils.Hash;
import utils.ResponseBody;
import utils.ErrorHandler;
import utils.JwtUtil;

public class GuestController {
  private final GuestDao guestDao;
  private final ObjectMapper mapper = new ObjectMapper();
  private Logger logger = Logger.getInstance();
  private Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

  // These are regular expressions for input validation
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
  private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[(]?[0-9]{3}[)]?[-\\s.]?[0-9]{3}[-\\s.]?[0-9]{4,6}$");
  private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$");
  // private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

  public GuestController(GuestDao guestDao) {
    this.guestDao = guestDao;
    registerRoutes();
  }
  

  private void registerRoutes() {
    post("/guest/update_profile", (req, res) -> {
      try {
        Guest guest = mapper.readValue(req.body(), Guest.class);
        ResponseBody resb = new ResponseBody();
        boolean isValid = true;
        String curProperty;
        res.status(400);

        // Tag validation
        curProperty = "tags";
        if (guest.fullName.isEmpty()) {
          resb.setProperty(curProperty, "Tags is required :)");
          isValid = false;
        }
        
        // Email validation
        curProperty = "email";
        if (guest.email.isEmpty()) {
            resb.setProperty(curProperty, "Email is required");
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(guest.email).matches()) {
          resb.setProperty(curProperty, "Invalid email format");
            isValid = false;
        }
        
        // Phone validation
        curProperty = "phone";
        if (guest.phone.isEmpty()) {
            resb.setProperty(curProperty, "Phone is required");
            isValid = false;
        } else if (!PHONE_PATTERN.matcher(guest.phone).matches()) {
            resb.setProperty(curProperty, "Invalid phone format");
            isValid = false;
        }

        if (isValid) {
          Guest g = guestDao.getGuestByEmail(guest.email);
          boolean isExist = false;
          if (g != null && !g.guestId.equals(guest.guestId)) {
            isExist = true;
          }
          
          if (isExist) {
            resb.clearProperties();
          } else {
            resb.clearProperties();
            res.status(200);
            resb.isSuccess = true;
            guestDao.updateGuest(guest);
          }
        }
        return gson.toJson(resb);
      } catch (Exception e) {
        return ErrorHandler.handleError(e);
      }

  });

    get("/guest/:id", (req, res) -> {

      String idParam = req.params(":id");
  
      Guest guest = guestDao.getGuestById(idParam);

      guest.passwordHash = "";  // Hide password hash from user
  
      res.status(200);
      return gson.toJson(guest);

  });

    // Sign in guest
    post("/guest/sign_in", (req, res) -> {
      try {
        Guest guest = mapper.readValue(req.body(), Guest.class);
        ResponseBody resb = new ResponseBody();
        boolean isValid = true;
        String curProperty;
        res.status(400);
        
        // Validate email
        curProperty = "email";
        if (guest.email.isEmpty()) {
          resb.setProperty(curProperty, "Email is required");
          isValid = false;
        }
        else if (!EMAIL_PATTERN.matcher(guest.email).matches()) {
          resb.setProperty(curProperty, "Invalid email format");
          isValid = false;
        }

        // Validate password
        curProperty = "password";
        if (guest.passwordHash.isEmpty()) {
          resb.setProperty(curProperty, "Password is required");
          isValid = false;
        }

        if (isValid) {
          if (guestDao.authenticate(guest.email, Hash.hashString(guest.passwordHash)) != null) {
            res.status(200);
            Guest g = guestDao.getGuestByEmail(guest.email);
            String token = JwtUtil.generateToken(g.guestId);
            res.header("Authorization", "Bearer " + token);
            resb.isSuccess = true;
          } else {
            resb.clearProperties();
          }
        }
        String json = gson.toJson(resb);
        System.out.println(json);
        return json;

      } catch (Exception e) {
        return ErrorHandler.handleError(e);
      }
    });

    post("/guest/sign_up", (req, res) -> {
      Guest guest = mapper.readValue(req.body(), Guest.class);
      ResponseBody resb = new ResponseBody();
      resb.isSuccess = false;
      boolean isValid = true;
      String curProperty;
      res.status(400);

        
      // Name validation
      curProperty = "fullName";
      if (guest.fullName.isEmpty()) {
        resb.setProperty(curProperty, "Name is required");
        isValid = false;
      } else if (guest.fullName.length() < 3) {
        resb.setProperty(curProperty, "Name must be at least 3 characters");
        isValid = false;
      }
      
      // Email validation
      curProperty = "email";
      if (guest.email.isEmpty()) {
          resb.setProperty(curProperty, "Email is required");
          isValid = false;
      } else if (!EMAIL_PATTERN.matcher(guest.email).matches()) {
        resb.setProperty(curProperty, "Invalid email format");
          isValid = false;
      }
      
      // Phone validation
      curProperty = "phone";
      if (guest.phone.isEmpty()) {
          resb.setProperty(curProperty, "Phone is required");
          isValid = false;
      } else if (!PHONE_PATTERN.matcher(guest.phone).matches()) {
          resb.setProperty(curProperty, "Invalid phone format");
          isValid = false;
      }
      
      // Gender validation
      curProperty = "gender";
      if (guest.gender.isEmpty()) {
        resb.setProperty(curProperty, "Gender is required");
        isValid = false;
      }
      
      // Password validation
      curProperty = "password";
      if (guest.passwordHash.isEmpty()) {
        resb.setProperty(curProperty, "Password is required");
        isValid = false;
      } else if (!PASSWORD_PATTERN.matcher(guest.passwordHash).matches()) {
        resb.setProperty(curProperty, "Password must be 8+ chars with uppercase, lowercase, number and special char");
        isValid = false;
      }

      if (isValid) {
        List<Guest> guests = guestDao.getAllGuests();
        boolean isExist = false;
        for (Guest g: guests) {
          if (g.email.equals(guest.email)) {
            isExist = true;
            break;
          }
        }
        
        if (isExist) {
          resb.clearProperties();
        } else {
          Guest newGuest = guestDao.createGuest(guest);
          guestDao.saveToFile();
          res.status(200);
          resb.isSuccess = true;
          String token = JwtUtil.generateToken(newGuest.guestId);
          res.header("Authorization", "Bearer " + token);
        }
      }
      return gson.toJson(resb);
    });
  }
}
