/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.unsw.comp9321.logic;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public class CommandInvoker {
    private Command cmd;

    public CommandInvoker() {
    }

    public Command invoker(HttpServletRequest request) {
        String temp = request.getRequestURI().split("ass2-0")[1].split(";")[0];
            
        System.out.println("********CommandInvoker********");
        System.out.println(temp);
        
        if (temp.equals("/login") || temp.equals("/logout")) {            
            cmd = new AuthenticateCommand();
            
        } else if (temp.equals("/registrationConf") || temp.equals("/registration")) {
            cmd = new RegistrationCommand();
            
        } else if (temp.equals("/searchTitle") || temp.equals("/searchGenre")) {
            cmd = new SearchCommand();
            
        } else if (temp.equals("/profileSubmit")|| temp.equals("/profile")) {
            cmd = new ProfileCommand();
            
        } else if (temp.equals("/detail") || temp.equals("/review")) {
            cmd = new MovieCommand();
            
        } else if (temp.equals("/bookingConfirm") || temp.equals("/checkout") || temp.equals("/booking")) {
            cmd = new BookingCommand();
            
        } else if (temp.equals("/addCinema") || temp.equals("/addMovie") || temp.equals("/addShowtime")) {
            cmd = new OwnersCommand();
        }
        return cmd;
    }

}
