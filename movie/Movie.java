package movie;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;

//Movie, Showtimes and Seat Maps
public class Movie {
    public static final int TOTAL_FLOORS = 2;
    public static final int TOTAL_ROWS = 4;
    public static final int TOTAL_COLUMNS = 8;

    private String movieTitle;
    private String genre;
    private double ticketPrice;
    private String ageRestriction;
    private Color posterColor;
    private Image posterImage;
    private ArrayList<String> showtimes;
    private ArrayList<MovieTicketSystem.Seat[][][]> seatMaps;

    //Creating of a Movie With Standard Showtimes
    public Movie(String movieTitle, String genre, double ticketPrice, String ageRestriction, Color posterColor) {
        this.movieTitle = movieTitle;
        this.genre = genre;
        this.ticketPrice = ticketPrice;
        this.ageRestriction = ageRestriction;
        this.posterColor = posterColor;
        showtimes = new ArrayList<String>();
        seatMaps = new ArrayList<MovieTicketSystem.Seat[][][]>();
        addShowtime("9:00 AM");
        addShowtime("11:30 AM");
        addShowtime("2:00 PM");
        addShowtime("4:30 PM");
        addShowtime("7:00 PM");
        addShowtime("9:00 PM");
    }

    //Creating of a Movie With Admin-Chosen Showtimes
    public Movie(String movieTitle, String genre, double ticketPrice, String ageRestriction, Color posterColor, ArrayList<String> times) {
        this.movieTitle = movieTitle;
        this.genre = genre;
        this.ticketPrice = ticketPrice;
        this.ageRestriction = ageRestriction;
        this.posterColor = posterColor;
        showtimes = new ArrayList<String>();
        seatMaps = new ArrayList<MovieTicketSystem.Seat[][][]>();
        for (String time : times) {
            addShowtime(time);
        }
    }

    //Adding of a Showtime and Its Seat Map
    private void addShowtime(String time) {
        showtimes.add(time);
        seatMaps.add(buildSeatMap());
    }

    //Updating of Movie Details (Keeping Booked Seats)
    public void updateDetails(String title, String newGenre, double price,
                              String age, Color color, ArrayList<String> times) {
        ArrayList<String> oldTimes = showtimes;
        ArrayList<MovieTicketSystem.Seat[][][]> oldMaps = seatMaps;
        ArrayList<String> newTimes = new ArrayList<String>();
        ArrayList<MovieTicketSystem.Seat[][][]> newMaps = new ArrayList<MovieTicketSystem.Seat[][][]>();

        for (String time : times) {
            int oldIndex = oldTimes.indexOf(time);
            newTimes.add(time);
            if (oldIndex >= 0) {
                newMaps.add(oldMaps.get(oldIndex));
            } else {
                newMaps.add(buildSeatMap());
            }
        }
        movieTitle = title;
        genre = newGenre;
        ticketPrice = price;
        ageRestriction = age;
        posterColor = color;
        showtimes = newTimes;
        seatMaps = newMaps;
    }

    //Building of the Seat Map (Floors, Rows and Columns)
    private MovieTicketSystem.Seat[][][] buildSeatMap() {
        MovieTicketSystem.Seat[][][] seats =
                new MovieTicketSystem.Seat[TOTAL_FLOORS][TOTAL_ROWS][TOTAL_COLUMNS];
        for (int floor = 0; floor < TOTAL_FLOORS; floor++) {
            for (int row = 0; row < TOTAL_ROWS; row++) {
                char rowLetter = (char) ('A' + (floor * TOTAL_ROWS) + row);
                for (int column = 0; column < TOTAL_COLUMNS; column++) {
                    String seatName = rowLetter + String.valueOf(column + 1);
                    seats[floor][row][column] = new MovieTicketSystem.Seat(seatName);
                }
            }
        }
        return seats;
    }

    //Getting of the Movie Title
    public String getMovieTitle() {
        return movieTitle;
    }

    //Getting of the Genre
    public String getGenre() {
        return genre;
    }

    //Getting of the Ticket Price
    public double getTicketPrice() {
        return ticketPrice;
    }

    //Getting of the Age Restriction
    public String getAgeRestriction() {
        return ageRestriction;
    }

    //Getting of the Poster Color
    public Color getPosterColor() {
        return posterColor;
    }

    //Getting of the Poster Image
    public Image getPosterImage() {
        return posterImage;
    }

    //Setting of the Poster Image
    public void setPosterImage(Image image) {
        posterImage = image;
    }

    //Getting of the Showtimes
    public ArrayList<String> getShowtimes() {
        return new ArrayList<String>(showtimes);
    }

    //Getting of the Seat Map for a Showtime
    public MovieTicketSystem.Seat[][][] getSeatMap(String time) {
        int index = showtimes.indexOf(time);
        if (index < 0) {
            return null;
        }
        return seatMaps.get(index);
    }
}