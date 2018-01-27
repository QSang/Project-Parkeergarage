package Parkeersimulator.main;

import javax.swing.*;

import java.awt.*;

import Parkeersimulator.controller.*;
import Parkeersimulator.logic.*;
import Parkeersimulator.view.*;

public class CarParkMain {
    private JFrame screen;
    private Model model;

    private AbstractView carParkView;
    private AbstractView  displayQueueView;
    private AbstractView typeOfCarView;
    private AbstractView pieView;

    private GegevensController controller2;
    private SimulatorController controller;

    public CarParkMain() {
        model = new Model();

        controller = new SimulatorController(model);
        controller2 = new GegevensController(model);

        screen = new JFrame("Parkeersimulator");
        screen.setSize(880, 800);

        carParkView = new CarParkView(model);
        displayQueueView = new DisplayQueueView(model);
        typeOfCarView = new TypeOfCarView(model);
        pieView = new PieView(model);


        screen.setResizable(false);

        screen.setLayout(null);

        screen.getContentPane().add(carParkView);
        screen.getContentPane().add(displayQueueView);
        screen.getContentPane().add(typeOfCarView);
        screen.getContentPane().add(pieView);


        screen.getContentPane().add(controller);
        screen.getContentPane().add(controller2);

        controller.setBorder(BorderFactory.createLineBorder(Color.black));
        controller2.setBorder(BorderFactory.createLineBorder(Color.black));
        carParkView.setBorder(BorderFactory.createLineBorder(Color.black));
        displayQueueView.setBorder(BorderFactory.createLineBorder(Color.black));
        typeOfCarView.setBorder(BorderFactory.createLineBorder(Color.black));
        pieView.setBorder(BorderFactory.createLineBorder(Color.black));

        controller.setBounds(-2, 730, 875, 500);
        controller2.setBounds(5, 440, 200, 280);

        carParkView.setBounds(5,10,860,420);
        displayQueueView.setBounds(245,640,300,80);
        typeOfCarView.setBounds(245, 500, 300, 130);
        pieView.setBounds(585, 440, 280, 280);
        Color displayQueueBg = new Color(0xDBEEF4);

        screen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        screen.setBackground(displayQueueBg);

        screen.setVisible(true);

    }
}
