package controllers;

import static spark.Spark.post;

import utils.Logger;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dao.GuestDao;
import models.Guest;
import utils.Hash;
import utils.ResponseBody;
import utils.ErrorHandler;

public class GuestController {
  private final GuestDao guestDao;
  private final ObjectMapper mapper = new ObjectMapper();
  private Logger logger = Logger.getInstance();
  private Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

  public GuestController(GuestDao guestDao) {
    this.guestDao = guestDao;
    registerRoutes();
  }

  private void registerRoutes() {
    // Sign in guest
    post("/guest/sign_in", (req, res) -> {
      try {
        Guest guest = mapper.readValue(req.body(), Guest.class);
        ResponseBody resb = new ResponseBody();
        String curProperty;
        
        // Validate email
        curProperty = "email";
        if (guest.email.isEmpty()) {
          resb.setProperty(curProperty, "Email is required");
        }
        else if (!EMAIL_PATTERN.matcher(guest.email).matches()) {
          resb.setProperty(curProperty, "Invalid email format");
        }

        // Validate password
        curProperty = "password";
        if (guest.passwordHash.isEmpty()) {
          resb.setProperty(curProperty, "Password is required");
        }

        if (guestDao.authenticate(guest.email, Hash.hashString(guest.passwordHash)) != null) {
          resb.isSuccess = true;
        } else {
          resb.setProperty(curProperty, "Invalid credential");
          resb.setProperty("email", "Invalid credential");
        }

        return gson.toJson(resb);

      } catch (Exception e) {
        return ErrorHandler.handleError(e);
      }

    });
  }
}
