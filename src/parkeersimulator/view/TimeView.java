package parkeersimulator.view;

import parkeersimulator.logic.Model;

import javax.swing.*;
import java.awt.*;

public class TimeView extends AbstractView {

    private JLabel txtTime;
    private JLabel txtDays;

    private Model model;

    public TimeView(Model model) {
        super(model);
        this.model = model;

        /**
         *
         */

        txtTime = new JLabel("Time: 00:00");
        txtTime.setOpaque(false);
        txtTime.setBorder(BorderFactory.createLineBorder(Color.black,1));
        add(txtTime);

        txtDays = new JLabel("Day: 1");
        txtDays.setOpaque(false);
        txtDays.setBorder(BorderFactory.createLineBorder(Color.black,1));
        add(txtDays);

    }

    /**
     * Updates the view so the numbers inside the textfield change accordingly
     */
    public void updateView(){
        Model model = (Model) super.model;

        String hour = String.valueOf(model.getHours());
        String minutes= String.valueOf(model.getMinutes());
        String days = String.valueOf(model.getDays());
        if (model.getHours() < 10)
        {
            hour = "0"+ hour;
        }
        if (model.getMinutes() < 10)
        {
            minutes = "0"+ minutes;
        }
        txtTime.setText("Time: " + hour+ ":" + minutes);
        txtDays.setText("Day: "+ days);

        setVisible(true);

        super.updateView();

    }


}
