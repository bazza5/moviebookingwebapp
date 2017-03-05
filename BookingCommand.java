/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unsw.comp9321.logic;

import edu.unsw.comp9321.exceptions.ServiceLocatorException;
import edu.unsw.comp9321.jdbc.BookingDAO;
import edu.unsw.comp9321.jdbc.BookingDTO;
import edu.unsw.comp9321.jdbc.MovieDAO;
import edu.unsw.comp9321.jdbc.PostgresBookingDAO;
import edu.unsw.comp9321.jdbc.PostgresMovieDAO;
import edu.unsw.comp9321.jdbc.PostgresShowtimeDAO;
import edu.unsw.comp9321.jdbc.ShowtimeDAO;
import edu.unsw.comp9321.jdbc.ShowtimeDTO;
import static edu.unsw.comp9321.logic.FrontController.logger;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 */
class BookingCommand implements Command {

    private MovieDAO movie;
    private ShowtimeDAO showtime;
    private BookingDAO booking;

    public BookingCommand() {
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
        try {
            booking = new PostgresBookingDAO();
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
        String ret = null;
        System.out.println("********Booking********");
        System.out.println(action);

        if (action.equals("/booking")) {
            int movieID = Integer.parseInt(filter(request.getParameter("movieID")));
            Date date;
            try {
                date = new SimpleDateFormat("yyyy-MM-dd").parse(filter(request.getParameter("year")) + "-"
                        + filter(request.getParameter("month")) + "-"
                        + filter(request.getParameter("date")));
            } catch (ParseException e1) {
                date = null;
                e1.printStackTrace();
            }
            if (date == null) {
                request.setAttribute("message", "bookingdatemissing");
            } else if (date.before(new Date())) {
                request.setAttribute("message", "beforetoday");
            } else {
                ArrayList<ShowtimeDTO> dateShowtimes = showtime.getDateShowtime(movieID, date);
                String dateString = new SimpleDateFormat("EEEE, dd MMMM yyyy").format(date).toString();
                session.setAttribute("date", dateString);
                session.setAttribute("movie", movie.getMovie(movieID));
                session.setAttribute("dateShowtimes", dateShowtimes);
            }

            ret = "booking";
        } else if (action.equals("/bookingConfirm")) {
            String ccard = request.getParameter("ccard");
            if (ccard.equals("")) {
                request.setAttribute("message", "nocc");
                ret = "checkout";

            } else {

                try {
                    int num = Integer.parseInt(ccard);
                    BookingDTO confirmedBooking = new BookingDTO(Integer.parseInt(request.getParameter("showtimeID")), Integer.parseInt(request.getParameter("userID")), Integer.parseInt(request.getParameter("numTickets")), Integer.parseInt(request.getParameter("ccard")));
                    ShowtimeDTO showtimeRequested = showtime.getShowtime(confirmedBooking.getShowtimeID());
                    booking.createBooking(confirmedBooking);
                    showtimeRequested.setSeatsLeft(showtimeRequested.getSeatsLeft() - confirmedBooking.getNumTickets());
                    showtime.updateShowtime(showtimeRequested);
                    BookingDTO newBooking = booking.mostRecent();
                    request.setAttribute("newBooking", newBooking);
                    ret = "bookingConfirm";
                } catch (NumberFormatException e) {
                    request.setAttribute("message", "nocc");
                    ret = "checkout";

                }
            }

        } else if (action.equals("/checkout")) {
            ret = "checkout";
            String seatsRequested = (filter(request.getParameter("seats")));
            String showtimeID = (filter(request.getParameter("showtime")));

            try {
                int seatsRequestedNum = Integer.parseInt(seatsRequested);
                try {
                    int showtimeIDNum = Integer.parseInt(showtimeID);
                    ShowtimeDTO showtimeRequested = showtime.getShowtime(showtimeIDNum);
                    if (showtimeRequested.getSeatsLeft() - seatsRequestedNum >= 0) {
                        session.setAttribute("seatsRequested", seatsRequested);
                        session.setAttribute("showtimeRequested", showtimeRequested);
                    } else {
                        request.setAttribute("message", "noseats");
                        ret = "booking";
                    }
                } catch (NumberFormatException e) {
                    request.setAttribute("message", "checkoutfieldmissing");
                }
            } catch (NumberFormatException e) {
                request.setAttribute("message", "checkoutfieldmissing");
            }
        } else {
            ret = "home";
        }
        return ret;
    }

    private String filter(String arg) {
        arg = arg.replaceAll("[^a-zA-Z0-9-]", "");

        return arg;
    }

}
