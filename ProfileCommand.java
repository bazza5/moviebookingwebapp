/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unsw.comp9321.logic;

import edu.unsw.comp9321.exceptions.ServiceLocatorException;
import edu.unsw.comp9321.jdbc.BookingDAO;
import edu.unsw.comp9321.jdbc.BookingDTO;
import edu.unsw.comp9321.jdbc.PostgresBookingDAO;
import edu.unsw.comp9321.jdbc.PostgresShowtimeDAO;
import edu.unsw.comp9321.jdbc.PostgresUserDAO;
import edu.unsw.comp9321.jdbc.ShowtimeDAO;
import edu.unsw.comp9321.jdbc.ShowtimeDTO;
import edu.unsw.comp9321.jdbc.UserDAO;
import edu.unsw.comp9321.jdbc.UserDTO;
import static edu.unsw.comp9321.logic.FrontController.logger;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 */
class ProfileCommand implements Command {

    private UserDAO user;
    private BookingDAO booking;
    private ShowtimeDAO showtime;

    public ProfileCommand() {
        try {
            user = new PostgresUserDAO();
        } catch (ServiceLocatorException e) {
            logger.severe("Trouble connecting to database " + e.getStackTrace());
        } catch (SQLException e) {
            logger.severe("Trouble connecting to database " + e.getStackTrace());
        }
        try {
            booking = new PostgresBookingDAO();
        } catch (ServiceLocatorException e) {
            logger.severe("Trouble connecting to database " + e.getStackTrace());
        } catch (SQLException e) {
            logger.severe("Trouble connecting to database " + e.getStackTrace());
        }
        try {
            showtime = new PostgresShowtimeDAO();
        } catch (ServiceLocatorException e) {
            logger.severe("Trouble connecting to database " + e.getStackTrace());
        } catch (SQLException e) {
            logger.severe("Trouble connecting to database " + e.getStackTrace());
        }
    }

    @Override
    public String execute(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        String action = request.getRequestURI().split("ass2-0")[1].split(";")[0];
        String ret = "profile";

        if (action.equals("/profileSubmit")) {
            if (request.getParameter("password").equals("")) {
                request.setAttribute("message", "nopassword");
                ret = "profile";
            } else {
                UserDTO updatedUser = (UserDTO) session.getAttribute("user");
                updatedUser.setNickname(request.getParameter("nickname"));
                updatedUser.setFirstName(request.getParameter("firstName"));
                updatedUser.setLastName(request.getParameter("lastName"));
                updatedUser.setEmail(request.getParameter("email"));

                String salt = generateSalt();
                String hash = null;
                try {
                    hash = generateHash(1000, filter(request.getParameter("password")), salt);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(ProfileCommand.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidKeySpecException ex) {
                    Logger.getLogger(ProfileCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
                updatedUser.setHash(hash);
                updatedUser.setSalt(salt);
                user.updateUser(updatedUser);
                request.setAttribute("message", "profileupdate");
            }

        } else if (action.equals("/profile")){
            UserDTO currentUser = (UserDTO) session.getAttribute("user");
            ArrayList<BookingDTO> userBookings = booking.getUserBookings(currentUser.getUserID());
            request.setAttribute("userBookings", userBookings);
            ArrayList<ShowtimeDTO> showtimes = showtime.getAllShowtimes();
            request.setAttribute("showtimes", showtimes);

            ret = "profile";
        }else{
            
        }
        return ret;
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[24];
        random.nextBytes(salt);
        return Arrays.toString(salt);
    }

    private String generateHash(int iteration, String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), iteration, 24 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return Arrays.toString(skf.generateSecret(spec).getEncoded());
    }

    private String filter(String arg) {
        arg = arg.replaceAll("[^a-zA-Z0-9.]", "");

        return arg;
    }

}
