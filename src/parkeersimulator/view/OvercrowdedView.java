package parkeersimulator.view;

import parkeersimulator.logic.*;

import javax.swing.*;

/**
 * @author Sang Nguyen, WaiCheong Ng
 * Creates a view for overcrowded queue's
 */

public class OvercrowdedView extends AbstractView {

    private int amountExitCars;
    private int amountQueueCars;
    private int amountPassCars;

    private int percentBar1;
    private int percentBar2;
    private int percentBar3;

    private JProgressBar carQueueBar;
    private JProgressBar passQueueBar;
    private JProgressBar exitQueueBar;

    /**
     * Creates the OvercrowdedView constructor
     * @param model invoke the overridden methods and get information from Model
     */

    public OvercrowdedView(Model model) {
        super(model);

        carQueueBar = new JProgressBar();
        carQueueBar.setBounds(10, 10, 420, 50);
        carQueueBar.setBorderPainted(true);
        carQueueBar.setStringPainted(true);

        add(carQueueBar);

        passQueueBar = new JProgressBar();
        passQueueBar.setBounds(10, 100, 420, 50);
        passQueueBar.setBorderPainted(true);
        passQueueBar.setStringPainted(true);

        add(passQueueBar);

        exitQueueBar = new JProgressBar();
        exitQueueBar.setBounds(10, 190, 420, 50);
        exitQueueBar.setBorderPainted(true);
        exitQueueBar.setStringPainted(true);

        add(exitQueueBar);
        setLayout(null);
        updateView();
    }

    /**
     * Updates the view so the numbers keeps changing in the progressbar
     */

    public void updateView() {
        Model model = super.model;

        amountQueueCars = model.getEntranceCarQueue();
        amountPassCars = model.getEntrancePassQueue();
        amountExitCars = model.getExitCarQueue();

        int maxCars = model.getEntrancePassQueue() + model.getEntranceCarQueue() + model.getExitCarQueue();

        carQueueBar.setMaximum(maxCars);
        carQueueBar.setValue(amountQueueCars);
        percentBar1 = (int) Math.floor(carQueueBar.getPercentComplete() * 100);
        carQueueBar.setString(("Car Queue Bar  ")+ percentBar1 + "%");

        passQueueBar.setMaximum(maxCars);
        passQueueBar.setValue(amountPassCars);
        percentBar2 = (int) Math.floor(passQueueBar.getPercentComplete() * 100);
        passQueueBar.setString(("Pass Queue Bar  ") + percentBar2 + "%");

        exitQueueBar.setMaximum(maxCars);
        exitQueueBar.setValue(amountExitCars);
        percentBar3 = (int) Math.floor(exitQueueBar.getPercentComplete() * 100);
        exitQueueBar.setString(("Exit Queue Bar  ") + percentBar3 + "%");

        setVisible(true);
        super.updateView();
    }
}