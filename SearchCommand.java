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
import static edu.unsw.comp9321.logic.FrontController.logger;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 */
class SearchCommand implements Command {

    private MovieDAO movie;

    public SearchCommand() {
        try {
            movie = new PostgresMovieDAO();
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
        String ret = "search";

        if (action.equals("/searchGenre")) {
            String searchTerm = filter(request.getParameter("searchTerm"));
            if (searchTerm.equals("")) {
                request.setAttribute("message", "nogenre");
            } else {
                ArrayList<MovieDTO> searchResults = movie.searchGenre(searchTerm);
                request.setAttribute("searchResults", searchResults);
                ret = "results";
            }

        } else {
            String searchTerm = filter(request.getParameter("searchTerm"));
            if (searchTerm.equals("")) {
                request.setAttribute("message", "noterm");
            } else {
                ArrayList<MovieDTO> searchResults = movie.searchTitle(searchTerm);
                request.setAttribute("searchResults", searchResults);
                ret = "results";
            }
        }
        return ret;
    }

    private String filter(String arg) {
        arg = arg.replaceAll("[^a-zA-Z0-9]", "");

        return arg;
    }
}
