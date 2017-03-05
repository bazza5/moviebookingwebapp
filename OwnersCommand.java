/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unsw.comp9321.logic;

import edu.unsw.comp9321.exceptions.ServiceLocatorException;
import edu.unsw.comp9321.jdbc.CinemaDAO;
import edu.unsw.comp9321.jdbc.CinemaDTO;
import edu.unsw.comp9321.jdbc.MovieDAO;
import edu.unsw.comp9321.jdbc.MovieDTO;
import edu.unsw.comp9321.jdbc.PostgresCinemaDAO;
import edu.unsw.comp9321.jdbc.PostgresMovieDAO;
import edu.unsw.comp9321.jdbc.PostgresShowtimeDAO;
import edu.unsw.comp9321.jdbc.ShowtimeDAO;
import edu.unsw.comp9321.jdbc.ShowtimeDTO;
import static edu.unsw.comp9321.logic.FrontController.logger;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 */
class OwnersCommand implements Command {

    private CinemaDAO cinema;
    private MovieDAO movie;
    private ShowtimeDAO showtime;

    public OwnersCommand() {
        try {
            cinema = new PostgresCinemaDAO();
        } catch (ServiceLocatorException e) {
            logger.severe("Trouble connecting to database " + e.getStackTrace());
        } catch (SQLException e) {
            logger.severe("Trouble connecting to database " + e.getStackTrace());
        }
        try {
            movie = new PostgresMovieDAO();
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
        String ret = "/owners";

        if (action.equals("/addCinema")) {
            String location = filter(request.getParameter("location"));
            String capacity = (filter(request.getParameter("capacity")));
            String amentites = filter(Arrays.toString(request.getParameterValues("amentites")));

            try {
                int capacityNum = Integer.parseInt(capacity);
                if (location.equals("") || location == null || amentites.equals("") || amentites == null) {
                    request.setAttribute("message", "cinemafieldmissing");
                } else {
                    CinemaDTO newCinema = new CinemaDTO(location, capacityNum, amentites);
                    cinema.createCinema(newCinema);
                    ArrayList<CinemaDTO> cinemasUpdate = cinema.getAllCinemas();
                    session.setAttribute("cinemas", cinemasUpdate);
                    request.setAttribute("message", "cinemasuccess");
                }
            } catch (NumberFormatException e) {
                request.setAttribute("message", "cinemafieldmissing");
            }

        } else if (action.equals("/addMovie")) {
            String title = filter(request.getParameter("title"));
            String imagePath = filter(request.getParameter("image"));
            String actors = filter(request.getParameter("actors"));
            String genre = filter(request.getParameter("genre"));
            String director = filter(request.getParameter("director"));
            String synopsis = filter(request.getParameter("synopsis"));
            String ageRating = filter(request.getParameter("age"));
            Date releaseDate;
            try {
                releaseDate = new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("year") + "-" + request.getParameter("month") + "-" + request.getParameter("date"));
            } catch (ParseException e) {
                releaseDate = null;
                e.printStackTrace();
            }

            if (title.equals("") || title == null || imagePath.equals("") || actors == null || actors.equals("") || actors == null || genre.equals("") || genre == null
                    || director == null || director.equals("") || synopsis == null || synopsis.equals("") || ageRating == null || ageRating.equals("") || releaseDate == null) {
                request.setAttribute("message", "moviefieldmissing");

            } else if (releaseDate.before(new Date())) {
                request.setAttribute("message", "beforetoday");
            } else {
                MovieDTO newMovie = new MovieDTO(title, imagePath, actors, genre, director, synopsis, ageRating, releaseDate, 0);
                movie.createMovie(newMovie);
                ArrayList<MovieDTO> nowShowingUpdate = movie.getNowShowing();
                session.setAttribute("nowShowing", nowShowingUpdate);
                request.setAttribute("message", "moviesuccess");
            }
        } else {
            String movieID = (filter(request.getParameter("movieID")));
            String cinemaID = (filter(request.getParameter("cinemaID")));
            Date time;
            try {
                time = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(request.getParameter("year") + "-" + request.getParameter("month") + "-" + request.getParameter("date") + " " + request.getParameter("hour") + ":" + request.getParameter("minute"));
            } catch (ParseException e) {
                time = null;
                e.printStackTrace();
            }

            if (movieID.equals("") || movieID == null || cinemaID.equals("") || cinemaID == null || time == null) {
                request.setAttribute("message", "showtimefieldmissing");

            } else {
                int movieIDNum = Integer.parseInt(movieID);
                int cinemaIDNum = Integer.parseInt(cinemaID);
                ShowtimeDTO newShowtime = new ShowtimeDTO(movieIDNum, cinemaIDNum, time);
                showtime.createShowtime(newShowtime);
                request.setAttribute("message", "showtimesuccess");
            }
        }
        return "owners";
    }

    private String filter(String arg) {
        arg = arg.replaceAll("[^a-zA-Z0-9. ]", "");

        return arg;
    }

}
