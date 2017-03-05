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
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 */
@WebServlet({"/", "/home", "/registration", "/login"})
public class FrontController extends HttpServlet {

    static final Logger logger = Logger.getLogger(FrontController.class.getName());
    private MovieDAO movie;
    private CinemaDAO cinema;
    CommandInvoker ic;

    /**
     * @throws ServletException
     * @see HttpServlet#HttpServlet()
     */
    public FrontController() throws ServletException {
        super();
        try {
            movie = new PostgresMovieDAO();
        } catch (ServiceLocatorException e) {
            logger.severe("Trouble connecting to database " + e.getStackTrace());
        } catch (SQLException e) {
            logger.severe("Trouble connecting to database " + e.getStackTrace());
        }
        try {
            cinema = new PostgresCinemaDAO();
        } catch (ServiceLocatorException e) {
            logger.severe("Trouble connecting to database " + e.getStackTrace());
        } catch (SQLException e) {
            logger.severe("Trouble connecting to database " + e.getStackTrace());
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        ic = new CommandInvoker();
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getRequestURI().split("ass2-0")[1].split(";")[0];
        RequestDispatcher rd;
        HttpSession session = request.getSession();

        ArrayList<MovieDTO> nowShowing = movie.getNowShowing();
        ArrayList<MovieDTO> comingSoon = movie.getComingSoon();
        ArrayList<CinemaDTO> cinemas = cinema.getAllCinemas();

        session.setAttribute("nowShowing", nowShowing);
        session.setAttribute("comingSoon", comingSoon);
        session.setAttribute("cinemas", cinemas);

        if (action.equals("/")) {
            rd = request.getRequestDispatcher("home.jsp");
            rd.forward(request, response);

        } else if (action.equals("/loginPage")) {
            rd = request.getRequestDispatcher("login.jsp");
            rd.forward(request, response);
        } else if (action.equals("/registrationPage")) {
            rd = request.getRequestDispatcher("registration.jsp");
            rd.forward(request, response);
        } else if (action.equals("/searchPage")) {
            rd = request.getRequestDispatcher("search.jsp");
            rd.forward(request, response);
        } else if (action.equals("/owners")) {
            rd = request.getRequestDispatcher(action + ".jsp");
            rd.forward(request, response);
        } else {

        Command cmd = ic.invoker(request);
        String next = cmd.execute(request, response);
        rd = request.getRequestDispatcher(next + ".jsp");
        rd.forward(request, response);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Command cmd = ic.invoker(request);        
        String next = cmd.execute(request, response);        
        System.out.println("********Redirecting to:********");
        System.out.println(next);
        response.sendRedirect(next + ".jsp");
    }
}
