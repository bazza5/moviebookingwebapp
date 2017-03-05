/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unsw.comp9321.logic;

import edu.unsw.comp9321.exceptions.ServiceLocatorException;
import edu.unsw.comp9321.jdbc.PostgresUserDAO;
import edu.unsw.comp9321.jdbc.UserDAO;
import edu.unsw.comp9321.jdbc.UserDTO;
import static edu.unsw.comp9321.logic.FrontController.logger;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Random;
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
public class AuthenticateCommand implements Command {

    private UserDAO user;

    public AuthenticateCommand() {
        try {
            user = new PostgresUserDAO();
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
        String ret = "login";

        if (action.equals("/login")) {
            String username = filter(request.getParameter("username"));
            String password = filter(request.getParameter("password"));

            if (username == null || username == "") {
                request.setAttribute("message", "nousername");

            } else if (password == null || password == "") {
                request.setAttribute("message", "nopassword");

            } else {
                UserDTO currentUser = user.findUsername(username);
                if (currentUser == null) {
                    request.setAttribute("message", "username");

                } else {
                    String hash = currentUser.getHash();
                    String salt = currentUser.getSalt();

                    try {
                        if (!isPassword(password, salt, hash)) {
                            request.setAttribute("message", "password");

                        } else if (!currentUser.getConfirmed()) {
                            request.setAttribute("message", "confirm");

                        } else {
                            session.setAttribute("user", currentUser);
                            if (currentUser.getIsOwner()) {
                                session.setAttribute("loggedin", "owner");

                            } else {
                                session.setAttribute("loggedin", "user");
                            }
                            ret = "home";
                        }
                    } catch (NoSuchAlgorithmException ex) {
                        Logger.getLogger(AuthenticateCommand.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InvalidKeySpecException ex) {
                        Logger.getLogger(AuthenticateCommand.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } else if (action.equals("/logout")) {
            session.setAttribute("loggedin", "false");
            ret = "home";
        } else {

        }
        return ret;
    }

    private static boolean isPassword(String password, String salt, String hash)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        Boolean ret = false;

        String testHash = checkHash(1000, password, salt);

        if (hash.equals(testHash)) {
            ret = true;
        }
        return ret;
    }

    private static String checkHash(int iteration, String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), iteration, 24 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return Arrays.toString(skf.generateSecret(spec).getEncoded());
    }

    private String filter(String arg) {
        arg = arg.replaceAll("[^a-zA-Z0-9]", "");

        return arg;
    }
}
