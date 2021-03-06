/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jala.efeeder.wheel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jala.efeeder.api.command.Command;
import org.jala.efeeder.api.command.CommandUnit;
import org.jala.efeeder.api.command.In;
import org.jala.efeeder.api.command.Out;
import org.jala.efeeder.api.command.impl.DefaultOut;


/**
 * @author alexander_castro
 */
@Command
public class WheeldecideCommand implements CommandUnit{

    @Override
    public Out execute(In context) throws Exception {       
        PreparedStatement pStatement = context.getConnection()
                .prepareStatement("select name from user,orders where id_food_meeting=? and id=id_user");
        
        String message = "";
        
        String parameter = context.getParameter("id_food_meeting2");
        
        message = "Parameter is: " + parameter;
        
        try
        {
            pStatement.setInt(1, Integer.valueOf(parameter));
        }
        catch (NumberFormatException e)
        {
            message = "Not a number: " + parameter;
        }
        
        ResultSet resultSet;
        
        try
        {
            resultSet = pStatement.executeQuery();
        }
        catch (SQLException e)
        {
            resultSet = null;
            message += ", query failed!";
        }
                
        int count = 0;
        StringBuilder jsonData = new StringBuilder(0xFF);
        
        
        if (resultSet != null)
        {
            jsonData.append("'{\"items\":[");
            while (resultSet.next()) {
                if (count > 0) jsonData.append(",");
                jsonData.append(escapeJsonString(resultSet.getString(1)));
                ++count;
            }
            jsonData.append("],\"chosen\":");
            jsonData.append(getRandomIndexPerson(count));
            jsonData.append("}'");
            
        }
        else
        {
            jsonData.append("'{\"items\":[");
            jsonData.append(escapeJsonString(message));
            jsonData.append("],\"chosen\":0}'");
        }
	
	Out out = new DefaultOut();
        out.addResult("jsonData", jsonData.toString());
        out.forward("wheeldecide/wheel.jsp");
        return out;
    }
    
    private static int getRandomIndexPerson(int numberOfPersons){
        return (int)Math.floor(Math.random() * numberOfPersons);
    }
    
    private static String escapeJsonString(String string) {
        if (null == string || 0 == string.length()) {
            return "\"\"";
        }

        final int len = string.length();
        StringBuilder sb = new StringBuilder(len + 8);
        sb.append('"');

        final String escapeInput = "\\\"/\b\t\n\f\r";
        final String escapeOutput = "\\\"/btnfr";
        for (int i = 0; i < len; ++i) {
            final char c = string.charAt(i);
            final int index = escapeInput.indexOf(c);
            if (index != -1)
            {
                sb.append('\\');
                sb.append(escapeOutput.charAt(index));
            }
            else if (c < ' ') {
                sb.append(String.format("\\u%04x", c));
            } else {
                sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    
}
