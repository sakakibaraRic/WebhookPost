package classes;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/WebhookSubscription")
public class ServletTest extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html; charset=Shift_JIS");
        response.setStatus(200);
        PrintWriter out = response.getWriter();
        out.println("<html><head></head><body>");
        out.println("<p>POSTリクエストを受信</p>");
        out.println("</body></html>");
    }
}