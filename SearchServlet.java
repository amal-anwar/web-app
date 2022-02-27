/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author amal
 */
public class SearchServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/json;charset=UTF-8");
        try (PrintWriter out = new PrintWriter(response.getOutputStream())) {

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con;
                Statement st;
                ResultSet rs;

                con = DriverManager.getConnection("jdbc:mysql://localhost:8080/covidstats"
                        + "?useTimeZone=true&serverTimezone=UTC&autoReconnect=true&useSSL=false",
                        "root", "0000");
                st = con.createStatement();
                st.executeQuery("USE covidstats");
                String sqlQuery = "SELECT country_name FROM Countries WHERE country_name = ? AND last_update >= now() - INTERVAL 1 DAY";
                PreparedStatement ps = con.prepareStatement(sqlQuery);
                ps.setString(1, request.getParameter("searchQuery"));
                rs = ps.executeQuery();

                System.out.println("----" + rs);
                if (rs.next()) {
                    
                
                    // id response is null (sql doesnt fetch any data)
                    // then call api fron client connection and store fetched response in database

                    JSONArray jsonArr;
                    try {
                        String uri = "https://api.apify.com/v2/key-value-stores/tVaYRsPHLjNdNBu7S/records/LATEST";
                        Client client = ClientBuilder.newClient();
                        String req_response = client.target(uri)
                                .queryParam("disableRedirect", true)
                                .request(MediaType.APPLICATION_JSON)
                                .get(String.class);
                        System.out.println(req_response);
                        client.close();

                        jsonArr = new JSONArray(req_response);
                        JSONObject jsonObj;
                        boolean isFound = false;
                        for (int i = 0; i < jsonArr.length(); i++) {
                            jsonObj = jsonArr.getJSONObject(i);
                            //save this jsonObj into sql
                            //
                            //
                            if (jsonObj.get("country").equals(request.getParameter("searchQuery"))) {
                                out.print(jsonObj);
                                isFound = true;
                                response.setStatus(200);
                                break;
                            }

                        }
                        if (!isFound) {
                            out.write("country not found");
                            response.setStatus(404);
                        }
                    } catch (JSONException e) {
                        //ignore error
                        out.write("error found " + e.getMessage());
                        response.setStatus(500);
                    }

                    response.setContentType("text/json;charset=UTF-8");
                } else {
                    // false
                    System.out.println("its in database ");

                }

            } catch (Exception e) {
                System.err.println("execption from Database " + e);
            }

        }
    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
