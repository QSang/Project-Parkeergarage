package parkeersimulator.logic;

import java.util.Random;
import javax.swing.JOptionPane;
import parkeersimulator.view.*;
public class Model extends AbstractModel {

    private int numberOfFloors;
    private int numberOfRows;
    private int numberOfPlaces;
    private int numberOfOpenSpots;
    private int numberOfOpenReservedSpots;
    private Car[][][] cars;

    private static final String AD_HOC = "1";
    private static final String PASS = "2";
    private static final String RVC = "3";

    private CarQueue entranceCarQueue;
    private CarQueue entranceReservedQueue;
    private CarQueue entrancePassQueue;
    private CarQueue paymentCarQueue;
    private CarQueue exitCarQueue;

    private int day = 1;
    private int hour = 0;
    private int minute = 0;

    private int tickPause = 250  ;

    private boolean stop;
    private boolean start;
    private boolean set;

    int weekDayArrivals = 100; // average number of arriving cars per hour
    int weekendArrivals = 200; // average number of arriving cars per hour
    int weekDayPassArrivals = 50; // average number of arriving cars per hour
    int weekendPassArrivals = 5; // average number of arriving cars per hour
    int weekDayReservedArrivals = 25; // average number of arriving cars per hour
    int weekendReservedArrivals = 15; // average number of arriving cars per hour

    int enterSpeed = 3; // number of cars that can enter per minute
    int paymentSpeed = 7; // number of cars that can pay per minute
    int exitSpeed = 5; // number of cars that can leave per minute

    double turnoverTotal;

    double price;
    double priceReduced;

    private int totalCarsIndex = 0;
    private int exitIndex = 0;
    private int payingCars = 0;

    public Model() {
        this.numberOfFloors = 3;
        this.numberOfRows = 6;
        this.numberOfPlaces = 30;
        this.numberOfOpenSpots = (numberOfFloors - 1) * numberOfRows * numberOfPlaces;
        this.numberOfOpenReservedSpots = numberOfRows * numberOfPlaces;

        cars = new Car[numberOfFloors][numberOfRows][numberOfPlaces];
        entranceCarQueue = new CarQueue();
        entranceReservedQueue = new CarQueue();
        entrancePassQueue = new CarQueue();
        paymentCarQueue = new CarQueue();
        exitCarQueue = new CarQueue();
        price = 2.4;
        priceReduced = 2.0;
        turnoverTotal = 0.0;

    }

    public void runCommand(int getal) {
        int i = getal;
        if (!start) {
            setStart(true);
            while (i > 0) {
                tick();
                i--;
                if (stop) return;
                if (i <= 0) setStart(false);
            }
        }
    }

    /**
     * Progressess the application for 1 minute.
     */
    private void tick() {
        advanceTime();
        handleExit();
        updateViews();

        // Pause.
        try {
            Thread.sleep(tickPause);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        handleEntrance();

    }

    /**
     * Advance the time by one minute.
     */
    private void advanceTime() {
        minute++;
        while (minute > 59) {
            minute -= 60;
            hour++;
        }
        while (hour > 23) {
            hour -= 24;
            day++;
        }
        while (day > 6) {
            day -= 7;
        }

    }

    /**
     * Handles cars entering the car park.
     */
    private void handleEntrance() {
        carsArriving();
        carsEntering(entrancePassQueue);
        carsEntering(entranceCarQueue);
        carsEntering(entranceReservedQueue);
    }

    /**
     * Handles cars exiting the car park.
     */
    private void handleExit() {
        carsReadyToLeave();
        carsPaying();
        carsLeaving();
    }

    /**
     * Updates the car park view.
     */
    public void updateViews() {
        tick(turnoverTotal);
        notifyView();
    }

    /**
     * Adds arriving cars to their representative queues.
     */
    private void carsArriving() {
        int numberOfCars = getNumberOfCars(weekDayArrivals, weekendArrivals);
        addArrivingCars(numberOfCars, AD_HOC);
        numberOfCars = getNumberOfCars(weekDayPassArrivals, weekendPassArrivals);
        addArrivingCars(numberOfCars, PASS);
        numberOfCars = getNumberOfCars(weekDayReservedArrivals, weekendReservedArrivals);
        addArrivingCars(numberOfCars,RVC);
    }

    /**
     * Removes cars from the front of the queue and assigns them to a parking space. After the amount of cars that can enter per minute has been handled, the rest of the queue is dismissed.
     *
     * @param queue Queue at the entrance of the car park.
     */
    private void carsEntering(CarQueue queue) {
        while (queue.carsInQueue() > 0 ) {

                 if (queue == entrancePassQueue){

                    Location freeLocation = getFirstFreePassLocation();
                    if (freeLocation != null) {
                        Car car = queue.removeCar();
                        setCarAt(freeLocation, car);
                    }
                    else{

                    }
                }
                else if (queue == entranceCarQueue || queue == entranceReservedQueue) {

                    Location freeLocation = getFirstFreeLocation();
                    if (freeLocation != null) {
                       Car car = queue.removeCar();
                        setCarAt(freeLocation, car);
                    }
                    else{

                    }




                }
            }
        }


    /**
     * Removes cars from parking spots and if necessary, adds cars to the payment queue.
     */
    private void carsReadyToLeave() {
        Car car = getFirstLeavingCar();
        while (car != null) {
            if (car.getHasToPay()) {
                car.setIsPaying(true);
                paymentCarQueue.addCar(car);
            } else {
                carLeavesSpot(car);
            }
            car = getFirstLeavingCar();
        }
    }

    /**
     * Processess payment. Cars currently just leave the payment queue and leave their spot.
     */
    private void carsPaying() {
        int i = 0;
        while (paymentCarQueue.carsInQueue() > 0 && i < paymentSpeed) {
            Car car = paymentCarQueue.removeCar();

            double priceTemp = price * (car.getMinutesTotal() / (double) 60);
            turnoverTotal += priceTemp;

            carLeavesSpot(car);
            i++;
            payingCars++;
            exitIndex ++;
        }
    }

    /**
     * Cars leave the queue and are removed from the application.
     */
    private void carsLeaving() {
        // Let cars leave.
        int i = 0;
        while (exitCarQueue.carsInQueue() > 0 && i < exitSpeed) {
            exitCarQueue.removeCar();
            i++;
        }
    }

    /**
     * Calculates the amount of cars that are entering this minute, based on the day of the week, the average amount of cars per hour and a random number.
     * Number is rounded down to the closest integer.
     * Varies from Math.round((average number of cars per hour) / 60) to Math.round((average number of cars per hour) * 1,3 / 60)
     *
     * @param weekDay Average number of cars per hour on a weekday
     * @param weekend Average number of cars per hour on a weekendday.
     * @return Number of cars entering this minute.
     */
    private int getNumberOfCars(int weekDay, int weekend) {
        Random random = new Random();

        // Get the average number of cars that arrive per hour.
        int averageNumberOfCarsPerHour = day < 5
                ? weekDay
                : weekend;

        // Calculate the number of cars that arrive this minute.
        double standardDeviation = averageNumberOfCarsPerHour * 0.3;
        double numberOfCarsPerHour = averageNumberOfCarsPerHour + random.nextGaussian() * standardDeviation;
        return (int) Math.round(numberOfCarsPerHour / 60);
    }

    /**
     * Adds arriving cars to their representative entrance queue.
     *
     * @param numberOfCars Number of cars entering this minute.
     * @param type         Type of car.
     */
    private void addArrivingCars(int numberOfCars, String type) {
        switch (type) {
            case AD_HOC:
                for (int i = 0; i < numberOfCars; i++) {
                    entranceCarQueue.addCar(new AdHocCar());
                    totalCarsIndex++;
                }
                break;
            case PASS:
                for (int i = 0; i < numberOfCars; i++) {
                    entrancePassQueue.addCar(new ParkingPassCar());
                    totalCarsIndex++;
                }
                break;
            case RVC:
                for (int i = 0; i < numberOfCars; i++) {
                    entranceReservedQueue.addCar(new ReserveringCar());
                    totalCarsIndex++;
                }
                break;

        }
    }

    /**
     * Car leaves spot and joins the exit queue.
     *
     * @param car Car that is leaving his spot.
     */
    private void carLeavesSpot(Car car) {
        removeCarAt(car.getLocation());
        exitCarQueue.addCar(car);
    }

    /**
     * @return Number of floors in the car park
     */
    public int getNumberOfFloors() {
        return numberOfFloors;
    }

    /**
     * @return Number of rows per floor
     */
    public int getNumberOfRows() {
        return numberOfRows;
    }

    /**
     * @return Number of places per row
     */
    public int getNumberOfPlaces() {
        return numberOfPlaces;
    }

    /**
     * @return Number of open spots in the car park
     */
    public int getNumberOfOpenSpots() {
        return numberOfOpenSpots;
    }

    /**
     * @return Number of open reserved spots in the car park
     */
    public int getNumberOfOpenReservedSpots() {
        return numberOfOpenReservedSpots;
    }

    /**
     * Returns a car on the given location. If no car is found, returns null.
     *
     * @param location Location to be checked.
     * @return Car in given location. If no car is found, returns null.
     */
    public Car getCarAt(Location location) {
        if (!locationIsValid(location)) {
            return null;
        }
        return cars[location.getFloor()][location.getRow()][location.getPlace()];
    }

    /**
     * Tries to park a given car in a given spot. If spot is taken or invalid, returns false.
     *
     * @param location Parking spot where the car is trying to park.
     * @param car      Car that is trying to park.
     * @return Returns false if parking spot is invalid or alraedy taken, true if car successfully parked.
     */
    public boolean setCarAt(Location location, Car car) {
        if (!locationIsValid(location)) {
            return false;
        }
        Car oldCar = getCarAt(location);
        if (oldCar == null) {
            cars[location.getFloor()][location.getRow()][location.getPlace()] = car;
            car.setLocation(location);
            if (car.getHasReserved()) {
                numberOfOpenReservedSpots--;
            } else {
                numberOfOpenSpots--;
            }
            return true;
        }
        return false;
    }

    /**
     * Tries to remove a car from a given spot and returns the car. If spot is empty or invalid, returns null.
     *
     * @param location Location to remove a car from
     * @return Returns null if spot is invalid or already empty, returns the car if process was succesful
     */
    public Car removeCarAt(Location location) {
        if (!locationIsValid(location)) {
            return null;
        }
        Car car = getCarAt(location);
        if (car == null) {
            return null;
        }
        cars[location.getFloor()][location.getRow()][location.getPlace()] = null;
        car.setLocation(null);
        if (car.getHasReserved()) {
            numberOfOpenReservedSpots++;
        } else {
            numberOfOpenSpots++;
        }
        return car;
    }

    /**
     * @return First free location in the car park.
     */
    public Location getFirstFreeLocation() {
        for (int floor = 0; floor < getNumberOfFloors() - 1; floor++) {
            for (int row = 0; row < getNumberOfRows(); row++) {
                for (int place = 0; place < getNumberOfPlaces(); place++) {
                    Location location = new Location(floor, row, place);
                    if (getCarAt(location) == null) {
                        return location;
                    }
                }
            }
        }
        return null;
    }

    public Location getFirstFreePassLocation() {
        for (int floor = 2; floor < getNumberOfFloors(); floor++) {
            for (int row = 0; row < getNumberOfRows(); row++) {
                for (int place = 0; place < getNumberOfPlaces(); place++) {
                    Location location = new Location(floor, row, place);
                    if (getCarAt(location) == null) {
                        return location;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Runs through all parking spots and checks if it is occupied. If so, it checks if the car has to leave yet and if the car is not currently not paying.
     *
     * @return Returns first car leaving. If no cars are found, returns null.
     */
    public Car getFirstLeavingCar() {
        for (int floor = 0; floor < getNumberOfFloors(); floor++) {
            for (int row = 0; row < getNumberOfRows(); row++) {
                for (int place = 0; place < getNumberOfPlaces(); place++) {
                    Location location = new Location(floor, row, place);
                    Car car = getCarAt(location);
                    if (car != null && car.getMinutesLeft() <= 0 && !car.getIsPaying()) {
                        return car;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Runs through all parking spots. If occupied, gives the car 1 tick, making the cars minutes left go down by 1.
     */
    public void tick(double turnoverTotal) {
        for (int floor = 0; floor < getNumberOfFloors(); floor++) {
            for (int row = 0; row < getNumberOfRows(); row++) {
                for (int place = 0; place < getNumberOfPlaces(); place++) {
                    Location location = new Location(floor, row, place);
                    Car car = getCarAt(location);
                    if (car != null) {
                        car.tick();
                    }
                }
            }
        }

        String text = String.format("%.2f", (double) turnoverTotal);
    }



    public int getEntranceCarQueue() {
        return entranceCarQueue.carsInQueue();
    }

    public int getEntrancePassQueue() {
        int passReservedQueue = (entrancePassQueue.carsInQueue()+entranceReservedQueue.carsInQueue());
        return passReservedQueue;
    }

    public int getExitCarQueue() {
        return exitCarQueue.carsInQueue();
    }


    public int getTotalCarsIndex() {
        return totalCarsIndex;
    }

    public int getExitIndex() {
        return exitIndex;
    }

    public int getTotalAllCarsInQueue() {
        return payingCars;
    }

    public int getPayingCars() {
        return payingCars;
    }
        /**
         * Getter for the pay cash index.
         * @return int pay cash index.
         */


        /**
         * Checks if location is within the given bounds of the car park.
         *
         * @param location  Location to have its validity checked
         * @return False if location is invalid, true if location is valid.
         */
        private boolean locationIsValid (Location location){
            int floor = location.getFloor();
            int row = location.getRow();
            int place = location.getPlace();
            if (floor < 0 || floor >= numberOfFloors || row < 0 || row > numberOfRows || place < 0 || place > numberOfPlaces) {
                return false;
            }
            return true;
        }

        public void ChangeWeekAr ( int getal){
            this.weekDayArrivals = getal;
        }
        public void ChangeWeekendAr ( int getal){
            this.weekendArrivals = getal;
        }
        public void ChangeWeekP ( int getal){
            this.weekDayPassArrivals = getal;
        }
        public void ChangeWeekendP ( int getal){
            this.weekendPassArrivals = getal;
        }
        public void ChangeWeekRes ( int getal){
            this.weekDayReservedArrivals = getal;
        }
        public void ChangeWeekendRes ( int getal){
            this.weekendReservedArrivals = getal;
        }

        public void ChangeEntrySpeed ( int getal){
            this.enterSpeed = getal;
        }

        public void ChangePaySpeed ( int getal){
            this.paymentSpeed = getal;
        }

        public void ChangeExitSpeed ( int getal){
            this.exitSpeed = getal;
        }

        public void setSet ( boolean check){
            this.set = check;
        }

        public void setStop ( boolean stopping)
        {
            stop = stopping;
            if (stop) {
                setStart(false);
            }
        }

        public void setStart ( boolean starting)
        {
            start = starting;
        }

    public void totalCars(){
        int i = CarParkView.GetAdHoc() + CarParkView.GetParkPass();
        int max = 20;
        if(max < i){
            CarQueue.warningOverCrowdedCars();
        }
    }

    public void totalCarsInQueue(){
        int i = getEntranceCarQueue() + getEntrancePassQueue();
        int max = 10;
        if(max < i){
            CarQueue.warningOverCrowdedQueue();
        }
    }
    public int getHours()
    {
        return hour;
    }
    public int getMinutes()
    {
        return minute;
    }
    public int getDays()
    {
        return day;
    }
}