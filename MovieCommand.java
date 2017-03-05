/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unsw.comp9321.logic;

import edu.unsw.comp9321.exceptions.ServiceLocatorException;
import edu.unsw.comp9321.jdbc.MovieDAO;
import edu.unsw.comp9321.jdbc.MovieDTO;
import edu.unsw.comp9321.jdbc.PostgresMovieDAO;
import edu.unsw.comp9321.jdbc.PostgresReviewDAO;
import edu.unsw.comp9321.jdbc.ReviewDAO;
import edu.unsw.comp9321.jdbc.ReviewDTO;
import edu.unsw.comp9321.jdbc.UserDTO;
import static edu.unsw.comp9321.logic.FrontController.logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 */
class MovieCommand implements Command {

    private MovieDAO movie;
    private ReviewDAO review;

    public MovieCommand() {
        try {
            movie = new PostgresMovieDAO();
        } catch (ServiceLocatorException e) {
            logger.severe("Trouble connecting to database " + e.getStackTrace());
        } catch (SQLException e) {
            logger.severe("Trouble connecting to database " + e.getStackTrace());
        }

        try {
            review = new PostgresReviewDAO();
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

        if (action.equals("/review")) {

            String rating = filter(request.getParameter("rating"));
            String reviewString = filter(request.getParameter("review"));

            try {
                int capacityNum = Integer.parseInt(rating);
                if (rating.equals("") || rating == null || review.equals("") || review == null) {
                    request.setAttribute("message", "reviewfieldmissing");
                } else {
                    UserDTO currentUser = (UserDTO) session.getAttribute("user");
                    int movieID = Integer.parseInt(filter(request.getParameter("movieID")));
                    MovieDTO currentMovie = movie.getMovie(movieID);
                    ReviewDTO newReview = new ReviewDTO(movieID, currentUser.getUserID(),
                            Integer.parseInt(rating), reviewString);
                    review.createReview(newReview);
                    currentMovie.setAvgRating(movie.getAvgRating(movieID));
                    movie.updateMovie(currentMovie);
                    ArrayList<ReviewDTO> reviews = review.getMovieReviews(movieID);
                    session.setAttribute("reviews", reviews);
                    session.setAttribute("movie", currentMovie);
                }
            } catch (NumberFormatException e) {
                request.setAttribute("message", "reviewfieldmissing");
            }

        } else {
            int movieID = Integer.parseInt(filter(request.getParameter("movieID")));
            MovieDTO movieSelected = movie.getMovie(movieID);
            if (movieSelected.getReleaseDate().before(new Date())) {
                session.setAttribute("released", "yes");
            }
            session.setAttribute("movie", movieSelected);
            ArrayList<ReviewDTO> reviews = review.getMovieReviews(movieID);
            session.setAttribute("reviews", reviews);

        }
        return "detail";
    }

    private String filter(String arg) {
        arg = arg.replaceAll("[^a-zA-Z0-9. ]", "");

        return arg;
    }
}
