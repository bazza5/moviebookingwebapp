/*
 * RegistrationCommand
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 */
public class RegistrationCommand implements Command {

    private UserDAO user;

    public RegistrationCommand() {
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
        String ret = "registration";

        if (action.equals("/registrationConf")) {
            Map<String, String> urlParams = getQueryMap(request.getQueryString());
            String username = filter(urlParams.get("username"));
            user.confirmUser(username);
            request.setAttribute("message", "regconfirm");

            ret = "login";

        } else {

            String username = filter(request.getParameter("username"));
            String password = filter(request.getParameter("password"));
            String email = (request.getParameter("email"));

			if (username.equals("")){
				request.setAttribute("message", "nousername");
			}
			else if (password.equals("")){
				request.setAttribute("message", "nopassword");
			}
			else if (email.equals("")){
				request.setAttribute("message", "noemail");
			}
			else if (user.findUsername(username) != null){
				request.setAttribute("message", "duplicate");
            } else {
                String salt = generateSalt();
                String hash = null;
                try {
                    hash = generateHash(1000, password, salt);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(RegistrationCommand.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvalidKeySpecException ex) {
                    Logger.getLogger(RegistrationCommand.class.getName()).log(Level.SEVERE, null, ex);
                }

                UserDTO newUser = new UserDTO(username, hash, salt, email);
                user.createUser(newUser);
                RegistrationCommand.sendConfirmationEmail(username, email);
                request.setAttribute("message", "emailsent");
                ret = "home";

            }

        }
        return ret;
    }

    private static void sendConfirmationEmail(String username, String email) {
        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("awesomomovies@gmail.com", "movies123");
                    }
                });
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("sbazza5@gmail.com"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("MovieSite Confirmation Email");
            message.setText("Click me to confirm your account. http://localhost:8080/Ass2/registrationConf?username=" + username);
            Transport.send(message);
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    // Converts the query string into a Map
    public static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
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
    
    private String filter (String arg){
         arg = arg.replaceAll("[^a-zA-Z0-9.]", "");
        
        return arg;
    }
}
