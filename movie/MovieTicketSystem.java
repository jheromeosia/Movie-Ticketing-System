package movie;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

//Movie Catalog, Seat Map, Checkout and Receipt Screens
public class MovieTicketSystem extends JFrame implements ActionListener {

    private static final Color BG = new Color(12, 17, 28);
    private static final Color HEADER = new Color(17, 24, 39);
    private static final Color CARD = new Color(25, 34, 51);
    private static final Color CARD_LIGHT = new Color(33, 44, 64);
    private static final Color BORDER = new Color(52, 66, 89);
    private static final Color ACCENT = new Color(255, 190, 72);
    private static final Color TEXT = new Color(242, 245, 250);
    private static final Color MUTED = new Color(157, 170, 190);
    private static final Color AVAILABLE = new Color(46, 160, 111);
    private static final Color SELECTED = new Color(67, 132, 245);
    private static final Color RESERVED = new Color(83, 94, 111);
    private static final double MAX_CASH = 30000.0;

    private static ArrayList<Movie> allMovies = new ArrayList<Movie>();
    private static ArrayList<TransactionRecord> transactionHistory = new ArrayList<TransactionRecord>();
    private static boolean sharedDataInitialized = false;
    private static double priorityDiscountRate = 0.12;
    private static int nextBookingNumber = 1001;

    private JPanel mainContainer;
    private UserAccount staffAccount;
    private String currentGenre;
    private Movie selectedMovie;
    private String selectedShowtime;
    private ArrayList<Seat> selectedSeats;
    private JLabel selectedSeatsLabel;
    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel vatLabel;
    private JLabel priceBeforeVatLabel;
    private JLabel totalLabel;
    private JLabel discountAppliedLabel;
    private JTextField cashField;
    private boolean priorityDiscountApplied;
    private double currentSubtotal;
    private double currentDiscount;
    private int currentDiscountPercent;
    private double currentVat;
    private double currentPriceBeforeVat;
    private double currentTotal;
    private TransactionRecord completedTransaction;
    private double completedCash;

    //Opening of the Staff Window
    public MovieTicketSystem(UserAccount staffAccount) {
        super("iScreen - Movie Ticketing");
        this.staffAccount = staffAccount;
        currentGenre = "ALL";
        selectedSeats = new ArrayList<Seat>();
        mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BG);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        setBounds(screen);
        setContentPane(mainContainer);
        initializeSharedData();
        showDashboardScreen();
        setVisible(true);
    }

    //Starting of the Program
    public static void main(String[] args) {
        new LoginPage();
    }

    //Creating of the Sample Movie Catalog
    static void initializeSharedData() {
        if (sharedDataInitialized) {
            return;
        }
        allMovies.add(new Movie("Avengers: Doomsday", "ACTION", 320.00,
                "13+", new Color(41, 98, 255)));
        allMovies.add(new Movie("Spider-Man: Brand New Day", "ACTION", 300.00,
                "13+", new Color(200, 30, 30)));
        allMovies.add(new Movie("The Conjuring: The Last Rites", "HORROR", 250.00,
                "18+", new Color(60, 60, 60)));
        allMovies.add(new Movie("Insidious: Out of the Further", "HORROR", 240.00,
                "16+", new Color(90, 20, 90)));
        allMovies.add(new Movie("Home Alone", "COMEDY", 200.00,
                "13+", new Color(46, 139, 87)));
        allMovies.add(new Movie("Rush Hour", "COMEDY", 210.00,
                "16+", new Color(230, 140, 20)));
        allMovies.add(new Movie("Rewind", "ROMANCE", 220.00,
                "16+", new Color(219, 68, 130)));
        allMovies.add(new Movie("The Notebook", "ROMANCE", 230.00,
                "13+", new Color(160, 82, 45)));
        sharedDataInitialized = true;
    }

    //Getting of the Shared Movie List
    static ArrayList<Movie> sharedMovies() {
        initializeSharedData();
        return allMovies;
    }

    //Getting of the Transaction History
    static ArrayList<TransactionRecord> sharedTransactions() {
        initializeSharedData();
        return transactionHistory;
    }

    //Getting of the Priority Discount Rate
    static double getPriorityDiscountRate() {
        return priorityDiscountRate;
    }

    //Saving of the Priority Discount Rate
    static void setPriorityDiscountRate(double newRate) {
        if (newRate >= 0.0 && newRate <= 0.50) {
            priorityDiscountRate = newRate;
        }
    }

    //Checking of the Manager Discount Password
    static boolean isPriorityDiscountPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.equals("iscreen2bg1");
    }

    //Releasing of a Refunded Seat
    static void releaseSeat(Movie movie, String showtime, String seatName) {
        Seat[][][] seatMap = movie.getSeatMap(showtime);
        if (seatMap == null) {
            return;
        }
        for (int floor = 0; floor < seatMap.length; floor++) {
            for (int row = 0; row < seatMap[floor].length; row++) {
                for (int column = 0; column < seatMap[floor][row].length; column++) {
                    Seat seat = seatMap[floor][row][column];
                    if (seat.getLabel().equals(seatName)) {
                        seat.setReserved(false);
                    }
                }
            }
        }
    }

    //Displaying of the Movie Dashboard and Genre Filters
    private void showDashboardScreen() {
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel genreBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        genreBar.setBackground(BG);
        genreBar.add(genreButton("ALL"));
        genreBar.add(genreButton("ACTION"));
        genreBar.add(genreButton("HORROR"));
        genreBar.add(genreButton("COMEDY"));        
        genreBar.add(genreButton("ROMANCE"));
        ArrayList<String> extraGenres = new ArrayList<String>();
        ArrayList<Movie> genreSource = sharedMovies();
        for (int i = 0; i < genreSource.size(); i++) {
            String movieGenre = genreSource.get(i).getGenre();
            if (!movieGenre.equals("ACTION") && !movieGenre.equals("HORROR") && !movieGenre.equals("COMEDY") && !movieGenre.equals("ROMANCE") && !extraGenres.contains(movieGenre)) {
                extraGenres.add(movieGenre);
                genreBar.add(genreButton(movieGenre));
            }
        }
        body.add(genreBar, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(0, 1, 0, 12));
        cards.setBackground(BG);
        ArrayList<Movie> movies = sharedMovies();
        int shown = 0;
        int cardsInRow = 0;
        JPanel movieRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 5));
        movieRow.setBackground(BG);
        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            if (currentGenre.equals("ALL") || currentGenre.equals(movie.getGenre())) {
                movieRow.add(buildMovieCard(movie, i));
                shown = shown + 1;
                cardsInRow = cardsInRow + 1;
                if (cardsInRow == 4) {
                    cards.add(movieRow);
                    movieRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 5));
                    movieRow.setBackground(BG);
                    cardsInRow = 0;
                }
            }
        }
        if (shown == 0) {
            cards.add(cardMessage("No movies are available in this genre."));
        } else if (cardsInRow > 0) {
            cards.add(movieRow);
        }
        JScrollPane scroll = new JScrollPane(cards);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        body.add(scroll, BorderLayout.CENTER);
        showScreen("MOVIE TICKETING SYSTEM", "Choose a movie to see showtimes.", body);
    }

    //Building of a Movie Card
    private JPanel buildMovieCard(Movie movie, int index) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(10, 10, 10, 10)));
        card.setPreferredSize(new Dimension(216, 450));
        JPanel poster = new JPanel(new BorderLayout());
        poster.setBackground(BG);
        poster.setPreferredSize(new Dimension(196, 294));
        JLabel artwork = new JLabel("", SwingConstants.CENTER);
        artwork.setForeground(TEXT);
        artwork.setFont(new Font("SansSerif", Font.BOLD, 17));
        artwork.setPreferredSize(new Dimension(196, 294));
        Image posterImage = movie.getPosterImage();
        if (posterImage != null) {
            Image scaledPoster = posterImage.getScaledInstance(196, 294, Image.SCALE_SMOOTH);
            artwork.setIcon(new ImageIcon(scaledPoster));
        }
        poster.add(artwork, BorderLayout.CENTER);
        card.add(poster, BorderLayout.NORTH);
        //Layout of the Movie Title
        JPanel details = new JPanel(new BorderLayout(0, 3));
        details.setBackground(CARD);
        JTextArea title = new JTextArea(movie.getMovieTitle());
        title.setEditable(false);
        title.setLineWrap(true);
        title.setWrapStyleWord(true);
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(TEXT);
        title.setBackground(CARD);
        details.add(title, BorderLayout.CENTER);
        JPanel facts = new JPanel(new GridLayout(0, 1, 0, 3));
        facts.setBackground(CARD);
        facts.add(label("PHP " + money(movie.getTicketPrice()) + " per ticket", 12, MUTED, false));
        //Displaying of the Age Restriction
        facts.add(label("Age: " + movie.getAgeRestriction(), 12, MUTED, false));
        details.add(facts, BorderLayout.SOUTH);
        card.add(details, BorderLayout.CENTER);
        card.add(selectShowtimeButton("SELECT SHOWTIME", "MOVIE:" + index), BorderLayout.SOUTH);
        return card;
    }

    //Highlighting of the Selected Genre Button
    private JButton genreButton(String genre) {
        return button(genre, "GENRE:" + genre, currentGenre.equals(genre));
    }

    //Creating of Compact Buttons
    private JButton compactButton(String text, String command, Color background) {
        JButton result = button(text, command, false);
        result.setFont(new Font("SansSerif", Font.BOLD, 15));
        result.setBackground(background);
        result.setMargin(new Insets(8, 12, 8, 12));
        result.setPreferredSize(new Dimension(182, 46));
        return result;
    }

    //Creating of the Select Showtime Button
    private JButton selectShowtimeButton(String text, String command) {
        JButton result = button(text, command, true);
        result.setFont(new Font("SansSerif", Font.BOLD, 12));
        result.setMargin(new Insets(6, 10, 6, 10));
        result.setPreferredSize(new Dimension(196, 38));
        return result;
    }

    //Displaying of the Showtime Buttons
    private void showShowtimeSelection() {
        JPanel body = new JPanel(new BorderLayout(12, 12));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(25, 30, 25, 30));
        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 6));
        heading.setBackground(BG);
        heading.add(label(selectedMovie.getMovieTitle(), 24, TEXT, true));
        heading.add(label(selectedMovie.getGenre() + " | " + selectedMovie.getAgeRestriction() + " | PHP " + money(selectedMovie.getTicketPrice()), 14, MUTED, false));
        body.add(heading, BorderLayout.NORTH);
        ArrayList<String> times = selectedMovie.getShowtimes();
        JPanel timeButtons = new JPanel(new GridLayout(0, 3, 14, 16));
        timeButtons.setBackground(CARD);
        timeButtons.setBorder(new EmptyBorder(14, 18, 14, 18));
        Random random = new Random(selectedMovie.getMovieTitle().hashCode());
        int unavailableCount = 0;
        for (int i = 0; i < times.size(); i++) {
            JPanel timeCell = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            timeCell.setBackground(CARD);
            JButton timeButton = compactButton(times.get(i), "SHOWTIME:" + i, BG);
            boolean unavailable = random.nextInt(100) < 35;
            if (i == times.size() - 1 && unavailableCount == times.size() - 1) {
                unavailable = false;
            }
            if (unavailable) {
                unavailableCount++;
                timeButton.setEnabled(false);
                timeButton.setCursor(Cursor.getDefaultCursor());
            }
            timeCell.add(timeButton);
            timeButtons.add(timeCell);
        }
        JPanel timeCard = new JPanel(new BorderLayout(8, 8));
        timeCard.setBackground(CARD);
        timeCard.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(12, 12, 12, 12)));
        JLabel timeHeading = label("AVAILABLE SHOWTIMES", 15, ACCENT, true);
        timeHeading.setHorizontalAlignment(SwingConstants.CENTER);
        timeCard.add(timeHeading, BorderLayout.NORTH);
        timeCard.add(timeButtons, BorderLayout.CENTER);
        timeCard.setPreferredSize(new Dimension(760, 320));
        body.add(centerFixedComponent(timeCard, 130), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setBackground(BG);
        actions.add(button("BACK TO MOVIES", "BACK_MOVIES", false));
        body.add(actions, BorderLayout.SOUTH);
        showScreen("SELECT SHOWTIME", "Choose a showtime for " + selectedMovie.getMovieTitle(), body);
    }

    //Displaying of the Seat Selection Screen
    private void showSeatSelection() {
        Seat[][][] seatMap = selectedMovie.getSeatMap(selectedShowtime);
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(15, 22, 15, 22));
        JPanel intro = new JPanel(new GridLayout(2, 1, 0, 5));
        intro.setBackground(BG);
        intro.add(label(selectedMovie.getMovieTitle() + " | " + selectedShowtime, 20, TEXT, true));
        intro.add(label("Choose available seats. Blue seats are selected.", 13, MUTED, false));
        body.add(intro, BorderLayout.NORTH);

        JPanel seating = new JPanel(new BorderLayout(0, 8));
        seating.setBackground(BG);
        JPanel screenHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        screenHolder.setBackground(BG);
        JLabel screenText = label("SCREEN", 13, TEXT, true);
        screenText.setHorizontalAlignment(SwingConstants.CENTER);
        screenText.setPreferredSize(new Dimension(400, 26));
        screenHolder.add(screenText);
        seating.add(screenHolder, BorderLayout.NORTH);
        JPanel floorStack = new JPanel(new GridLayout(2, 1, 0, 6));
        floorStack.setBackground(BG);
        for (int floor = 0; floor < seatMap.length; floor++) {
            JPanel floorPanel = new JPanel(new BorderLayout(0, 8));
            floorPanel.setBackground(CARD);
            String floorName = "MAIN FLOOR";
            if (floor == 1) {
                floorName = "BALCONY - SECOND FLOOR";
            }
            floorPanel.add(label(floorName, 14, ACCENT, true), BorderLayout.NORTH);
            JPanel rows = new JPanel(new GridLayout(4, 1, 2, 2));
            rows.setBackground(CARD);
            for (int row = 0; row < seatMap[floor].length; row++) {
                JPanel seatRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                seatRow.setBackground(CARD);
                for (int column = 0; column < seatMap[floor][row].length; column++) {
                    Seat seat = seatMap[floor][row][column];
                    JButton seatButton = button(seat.getLabel(), "SEAT:" + floor + ":" + row + ":" + column, false);
                    seatButton.setFont(new Font("SansSerif", Font.BOLD, 10));
                    seatButton.setMargin(new Insets(3, 5, 3, 5));
                    seatButton.setPreferredSize(new Dimension(46, 24));
                    if (seat.isReserved()) {
                        seatButton.setBackground(RESERVED);
                        seatButton.setEnabled(false);
                        seatButton.setCursor(Cursor.getDefaultCursor());
                    } else if (seat.isSelected()) {
                        seatButton.setBackground(SELECTED);
                    } else {
                        seatButton.setBackground(AVAILABLE);
                    }
                    seatRow.add(seatButton);
                }
                rows.add(seatRow);
            }
            floorPanel.add(rows, BorderLayout.CENTER);
            floorPanel.setBorder(new EmptyBorder(8, 12, 8, 12));
            floorStack.add(floorPanel);
        }
        seating.add(floorStack, BorderLayout.CENTER);
        seating.setPreferredSize(new Dimension(820, 490));
        JScrollPane scroll = new JScrollPane(seating);
        scroll.setPreferredSize(new Dimension(840, 510));
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        body.add(centerFixedComponent(scroll, 145), BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.setBackground(BG);
        selectedSeatsLabel = label("Selected seats: none", 13, TEXT, false);
        footer.add(selectedSeatsLabel, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        actions.setBackground(BG);
        actions.add(button("BACK", "BACK_SHOWTIMES", false));
        actions.add(button("CONTINUE TO PAYMENT", "OPEN_CHECKOUT", true));
        footer.add(actions, BorderLayout.EAST);
        body.add(footer, BorderLayout.SOUTH);
        showScreen("SELECT SEATS", "Select one or more available seats.", body);
    }

    //Selecting and Deselecting of Seats
    private void toggleSeat(int floor, int row, int column, JButton source) {
        Seat[][][] seatMap = selectedMovie.getSeatMap(selectedShowtime);
        Seat seat = seatMap[floor][row][column];
        if (seat.isReserved()) {
            return;
        }
        if (seat.isSelected()) {
            seat.setSelected(false);
            selectedSeats.remove(seat);
            source.setBackground(AVAILABLE);
        } else {
            seat.setSelected(true);
            selectedSeats.add(seat);
            source.setBackground(SELECTED);
        }
        updateSelectedSeatLabel();
    }

    //Clearing of Selected Seats
    private void clearSelectedSeats() {
        for (Seat seat : selectedSeats) {
            seat.setSelected(false);
        }
        selectedSeats.clear();
    }

    //Updating of the Selected Seats Label
    private void updateSelectedSeatLabel() {
        if (selectedSeatsLabel == null) {
            return;
        }
        String names = "";
        for (int i = 0; i < selectedSeats.size(); i++) {
            if (i > 0) {
                names = names + ", ";
            }
            names = names + selectedSeats.get(i).getLabel();
        }
        if (names.length() == 0) {
            names = "none";
        }
        selectedSeatsLabel.setText("Selected seats: " + names);
    }

    //Displaying of the Checkout Screen
    private void showCheckout() {
        if (selectedSeats.size() == 0) {
            showMessage("Select at least one available seat.", "No seats selected");
            return;
        }
        priorityDiscountApplied = false;
        JPanel summary = new JPanel(new GridLayout(0, 2, 10, 10));
        summary.setBackground(CARD);
        summary.setBorder(new EmptyBorder(20, 24, 20, 24));
        addSummaryLine(summary, "Movie", selectedMovie.getMovieTitle());
        addSummaryLine(summary, "Showtime", selectedShowtime);
        addSummaryLine(summary, "Seats", selectedSeatNames());
        addSummaryLine(summary, "Ticket count", String.valueOf(selectedSeats.size()));
        subtotalLabel = label("", 14, TEXT, false);
        discountLabel = label("", 14, TEXT, false);
        priceBeforeVatLabel = label("", 14, TEXT, false);
        vatLabel = label("", 14, TEXT, false);
        totalLabel = label("", 17, ACCENT, true);
        discountAppliedLabel = label("No manager discount applied", 13, MUTED, false);
        discountAppliedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        summary.add(label("Subtotal", 14, MUTED, false));
        summary.add(subtotalLabel);
        summary.add(label("Discount", 14, MUTED, false));
        summary.add(discountLabel);
        summary.add(label("Original Price", 14, MUTED, false));
        summary.add(priceBeforeVatLabel);
        summary.add(label("VAT (12% included)", 14, MUTED, false));
        summary.add(vatLabel);
        summary.add(label("Total", 17, TEXT, true));
        summary.add(totalLabel);
        summary.setPreferredSize(new Dimension(650, 340));

        JPanel lower = new JPanel(new GridLayout(0, 1, 8, 8));
        lower.setBackground(BG);
        //Placement of the Discount Status Label
        lower.add(discountAppliedLabel);
        lower.add(button("MANAGER DISCOUNT", "APPLY_DISCOUNT", false));
        lower.add(label("Cash received (maximum PHP " + money(MAX_CASH) + ")", 13, MUTED, false));
        cashField = new JTextField();
        styleField(cashField);
        lower.add(cashField);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(BG);
        actions.add(button("BACK TO SEATS", "BACK_SEATS", false));
        actions.add(button("CONFIRM PAYMENT", "PAY", true));
        lower.add(actions);
        lower.setPreferredSize(new Dimension(650, 215));
        JPanel paymentCard = new JPanel(new BorderLayout(10, 10));
        paymentCard.setBackground(BG);
        paymentCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        paymentCard.add(summary, BorderLayout.CENTER);
        paymentCard.add(lower, BorderLayout.SOUTH);
        paymentCard.setPreferredSize(new Dimension(700, 590));
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(12, 20, 12, 20));
        body.add(centerFixedComponent(paymentCard, 30), BorderLayout.CENTER);
        refreshAmounts();
        showScreen("PAYMENT", "Review the booking and enter cash received.", body);
    }

    //Adding of a Checkout Summary Line
    private void addSummaryLine(JPanel panel, String name, String value) {
        panel.add(label(name, 14, MUTED, false));
        panel.add(label(value, 14, TEXT, false));
    }

    //Computing of Prices and Discounts
    private void refreshAmounts() {
        currentSubtotal = selectedMovie.getTicketPrice() * selectedSeats.size();
        TicketPricing pricing;
        if (priorityDiscountApplied) {
            pricing = new PriorityTicketPricing(priorityDiscountRate);
        } else {
            pricing = new StandardTicketPricing();
        }
        currentDiscount = pricing.calculateDiscount(currentSubtotal);
        currentDiscountPercent = pricing.getDiscountPercent();
        currentVat = pricing.calculateVat(currentSubtotal);
        currentTotal = pricing.calculateTotal(currentSubtotal);
        currentPriceBeforeVat = pricing.calculatePriceBeforeVat(currentSubtotal);
        subtotalLabel.setText("PHP " + money(currentSubtotal));
        //Displaying of the Discount Amount
        discountLabel.setText(currentDiscountPercent + "% (PHP " + money(currentDiscount) + ")");
        priceBeforeVatLabel.setText("PHP " + money(currentPriceBeforeVat));
        vatLabel.setText("PHP " + money(currentVat));
        totalLabel.setText("PHP " + money(currentTotal));
        if (priorityDiscountApplied) {
            discountAppliedLabel.setText("Manager discount applied");
            discountAppliedLabel.setForeground(ACCENT);
        } else {
            discountAppliedLabel.setText("No manager discount applied");
            discountAppliedLabel.setForeground(MUTED);
        }
    }

    //Applying of the Priority Discount
    private void applyPriorityDiscount() {
        JPasswordField passwordField = new JPasswordField(16);
        Object[] prompt = {"Enter the manager discount approval password.", passwordField};
        int answer = JOptionPane.showConfirmDialog(this, prompt, "Priority discount", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }
        String password = new String(passwordField.getPassword());
        if (isPriorityDiscountPassword(password)) {
            priorityDiscountApplied = true;
            refreshAmounts();
            showMessage("The priority discount was applied.", "Discount approved");
        } else if (password != null) {
            showMessage("The manager password is incorrect.", "Discount not approved");
        }
    }

    //Processing of the Payment
    private void completePayment() {
        String text = cashField.getText().trim();
        //Validation of the Cash Amount
        if (!isValidAmountText(text)) {
            showMessage("Enter a valid cash amount.", "Invalid amount");
            return;
        }
        double cash = Double.parseDouble(text);
        if (cash <= 0.0) {
            showMessage("Enter a valid cash amount.", "Invalid amount");
            return;
        }
        if (cash > MAX_CASH) {
            showMessage("The cash received cannot be more than PHP " + money(MAX_CASH) + ".", "Amount too high");
            return;
        }
        if (cash < currentTotal) {
            showMessage("The cash received is not enough.", "Payment too low");
            return;
        }
        String confirmation = "Confirm payment and create the receipt?\n\n" + selectedMovie.getMovieTitle() + "\n" + selectedShowtime + "\nSeats: " + selectedSeatNames() + "\nTotal: PHP " + money(currentTotal);
        int answer = JOptionPane.showConfirmDialog(this, confirmation, "Confirm booking", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        for (Seat seat : selectedSeats) {
            seat.setReserved(true);
            seat.setSelected(false);
        }
        completedTransaction = new TransactionRecord(nextBookingNumber, selectedMovie, selectedShowtime, selectedSeats, currentTotal, currentSubtotal, currentDiscount, currentVat, priorityDiscountApplied);
        completedCash = cash;
        transactionHistory.add(completedTransaction);
        nextBookingNumber = nextBookingNumber + 1;
        showReceipt();
    }

    //Displaying of the Booking Receipt
    private void showReceipt() {
        JPanel receipt = new JPanel(new GridLayout(0, 1, 6, 6));
        receipt.setBackground(CARD);
        receipt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ACCENT, 2), new EmptyBorder(14, 18, 14, 18)));
        receipt.add(label("BOOKING CONFIRMED", 18, ACCENT, true));
        receipt.add(label("Booking number: " + completedTransaction.getBookingNumber(), 14, TEXT, false));
        receipt.add(label("Movie: " + completedTransaction.getMovieTitle(), 14, TEXT, false));
        receipt.add(label("Showtime: " + completedTransaction.getShowtime(), 14, TEXT, false));
        receipt.add(label("Seats: " + completedTransaction.getSeatNames(), 14, TEXT, false));
        receipt.add(label("Subtotal: PHP " + money(currentSubtotal), 14, TEXT, false));
        receipt.add(label("Discount (" + currentDiscountPercent + "%): PHP " + money(currentDiscount), 14, TEXT, false));
        receipt.add(label("Original Price: PHP " + money(currentPriceBeforeVat), 14, TEXT, false));
        receipt.add(label("VAT (12% included): PHP " + money(currentVat), 14, TEXT, false));
        receipt.add(label("Total: PHP " + money(currentTotal), 17, ACCENT, true));
        receipt.add(label("Cash received: PHP " + money(completedCash), 14, TEXT, false));
        receipt.add(label("Change: PHP " + money(completedCash - currentTotal), 14, TEXT, false));
        receipt.setPreferredSize(new Dimension(700, 590));
        JOptionPane.showMessageDialog(this, receipt, "Booking confirmed", JOptionPane.PLAIN_MESSAGE);
        selectedMovie = null;
        selectedShowtime = null;
        selectedSeats.clear();
        showDashboardScreen();
    }

    //Getting of the Selected Seat Names
    private String selectedSeatNames() {
        String names = "";
        for (int i = 0; i < selectedSeats.size(); i++) {
            if (i > 0) {
                names = names + ", ";
            }
            names = names + selectedSeats.get(i).getLabel();
        }
        return names;
    }

    //Joining of Values for Display
    private String join(ArrayList<String> values) {
        String result = "";
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                result = result + ", ";
            }
            result = result + values.get(i);
        }
        return result;
    }

    //Validation of the Amount Text
    private boolean isValidAmountText(String text) {
        int digits = 0;
        int dots = 0;
        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            if (letter >= '0' && letter <= '9') {
                digits = digits + 1;
            } else if (letter == '.') {
                dots = dots + 1;
            } else {
                return false;
            }
        }
        return digits > 0 && dots <= 1;
    }

    //Formatting of Money
    private String money(double amount) {
        return String.format("%,.2f", amount);
    }

    //Building of Message Cards
    private JPanel cardMessage(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));
        panel.add(label(message, 15, TEXT, false), BorderLayout.CENTER);
        return panel;
    }

    //Building of the Header and Navigation
    private JPanel buildHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(HEADER);
        header.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, ACCENT), new EmptyBorder(16, 24, 14, 24)));
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setBackground(HEADER);
        titles.add(label(title, 21, TEXT, true));
        titles.add(label(subtitle, 12, MUTED, false));
        header.add(titles, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        actions.setBackground(HEADER);
        actions.add(label("iScreen", 18, ACCENT, true));
        actions.add(button("SIGN OUT", "SIGN_OUT", false));
        actions.add(button("EXIT", "EXIT", false));
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    //Replacing of the Current Screen
    private void showScreen(String title, String subtitle, JPanel body) {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(BG);
        screen.add(buildHeader(title, subtitle), BorderLayout.NORTH);
        body.setBackground(BG);
        screen.add(body, BorderLayout.CENTER);
        mainContainer.removeAll();
        mainContainer.add(screen, BorderLayout.CENTER);
        mainContainer.revalidate();
        mainContainer.repaint();
    }

    //Centering of Fixed-Size Sections
    private JPanel centerFixedComponent(JComponent component, int reservedHeight) {
        int verticalGap = (getHeight() - 85 - reservedHeight - component.getPreferredSize().height) / 2;
        if (verticalGap < 0) {
            verticalGap = 0;
        }
        JPanel holder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        holder.setBackground(BG);
        holder.add(component);
        JPanel topSpace = new JPanel();
        topSpace.setBackground(BG);
        topSpace.setPreferredSize(new Dimension(0, verticalGap));
        JPanel bottomSpace = new JPanel();
        bottomSpace.setBackground(BG);
        bottomSpace.setPreferredSize(new Dimension(0, verticalGap));
        JPanel centered = new JPanel(new BorderLayout());
        centered.setBackground(BG);
        centered.add(topSpace, BorderLayout.NORTH);
        centered.add(holder, BorderLayout.CENTER);
        centered.add(bottomSpace, BorderLayout.SOUTH);
        return centered;
    }

    //Creating of Styled Buttons
    private JButton button(String text, String command, boolean primary) {
        JButton result = new JButton(text);
        result.setActionCommand(command);
        result.addActionListener(this);
        result.setFont(new Font("SansSerif", Font.BOLD, 12));
        if (primary) {
            result.setForeground(new Color(28, 28, 30));
            result.setBackground(ACCENT);
        } else {
            result.setForeground(TEXT);
            result.setBackground(CARD_LIGHT);
        }
        result.setFocusPainted(false);
        result.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 12, 8, 12)));
        //Hand Cursor for Clickable Buttons
        result.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return result;
    }

    //Styling of Text Fields
    private void styleField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBackground(CARD_LIGHT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 10, 8, 10)));
    }

    //Creating of Labels
    private JLabel label(String text, int size, Color color, boolean bold) {
        JLabel result = new JLabel(text);
        int style = Font.PLAIN;
        if (bold) {
            style = Font.BOLD;
        }
        result.setFont(new Font("SansSerif", style, size));
        result.setForeground(color);
        return result;
    }

    //Displaying of Messages
    private void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    //Handling of Button Clicks
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (command.startsWith("GENRE:")) {
            currentGenre = command.substring(6);
            showDashboardScreen();
        } else if (command.startsWith("MOVIE:")) {
            int index = Integer.parseInt(command.substring(6));
            if (index >= 0 && index < allMovies.size()) {
                clearSelectedSeats();
                selectedMovie = allMovies.get(index);
                showShowtimeSelection();
            }
        } else if (command.startsWith("SHOWTIME:")) {
            int index = Integer.parseInt(command.substring(9));
            ArrayList<String> times = selectedMovie.getShowtimes();
            if (index >= 0 && index < times.size()) {
                clearSelectedSeats();
                selectedShowtime = times.get(index);
                showSeatSelection();
            }
        } else if (command.startsWith("SEAT:")) {
            int floor = command.charAt(5) - '0';
            int row = command.charAt(7) - '0';
            int column = Integer.parseInt(command.substring(9));
            toggleSeat(floor, row, column, (JButton) event.getSource());
        } else if (command.equals("BACK_MOVIES")) {
            showDashboardScreen();
        } else if (command.equals("BACK_SHOWTIMES")) {
            showShowtimeSelection();
        } else if (command.equals("OPEN_CHECKOUT")) {
            showCheckout();
        } else if (command.equals("BACK_SEATS")) {
            showSeatSelection();
        } else if (command.equals("APPLY_DISCOUNT")) {
            applyPriorityDiscount();
        } else if (command.equals("PAY")) {
            completePayment();
        } else if (command.equals("RECEIPT_DONE")) {
            selectedMovie = null;
            selectedShowtime = null;
            selectedSeats.clear();
            showDashboardScreen();
        } else if (command.equals("SIGN_OUT")) {
            int answer = JOptionPane.showConfirmDialog(this, "Return to the login page? Any unfinished booking will be discarded.", "Sign out", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                dispose();
                new LoginPage();
            }
        } else if (command.equals("EXIT")) {
            int answer = JOptionPane.showConfirmDialog(this, "Close iScreen?", "Exit",
                    JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                dispose();
            }
        }
    }

    //Seat Class (Selected and Reserved State)
    static final class Seat {

        private String label;
        private boolean reserved;
        private boolean selected;

        //Creating of a Seat
        Seat(String label) {
            this.label = label;
            reserved = false;
            selected = false;
        }

        //Getting of the Seat Label
        String getLabel() {
            return label;
        }

        //Checking if the Seat is Reserved
        boolean isReserved() {
            return reserved;
        }

        //Setting of the Reserved State
        void setReserved(boolean value) {
            reserved = value;
        }

        //Checking if the Seat is Selected
        boolean isSelected() {
            return selected;
        }

        //Setting of the Selected State
        void setSelected(boolean value) {
            selected = value;
        }
    }

    //TransactionRecord Class (Booking Details and Refund Status)
    static final class TransactionRecord {

        private int bookingNumber;
        private Movie movie;
        private String movieTitle;
        private String showtime;
        private ArrayList<String> seats;
        private double total;
        private double subtotal;
        private double discount;
        private double vat;
        private boolean priorityDiscountApplied;
        private boolean refunded;
        private LocalDateTime bookingDate;

        //Creating of a Transaction Record
        TransactionRecord(int number, Movie movie, String showtime, ArrayList<Seat> selectedSeats, double total, double subtotal, double discount, double vat, boolean discountApplied) {
            bookingNumber = number;
            this.movie = movie;
            movieTitle = movie.getMovieTitle();
            this.showtime = showtime;
            this.total = total;
            this.subtotal = subtotal;
            this.discount = discount;
            this.vat = vat;
            priorityDiscountApplied = discountApplied;
            refunded = false;
            bookingDate = LocalDateTime.now();
            seats = new ArrayList<String>();
            for (Seat seat : selectedSeats) {
                seats.add(seat.getLabel());
            }
        }

        //Getting of the Booking Number
        int getBookingNumber() {
            return bookingNumber;
        }

        //Getting of the Movie Title
        String getMovieTitle() {
            return movieTitle;
        }

        //Getting of the Movie
        Movie getMovie() {
            return movie;
        }

        //Getting of the Showtime
        String getShowtime() {
            return showtime;
        }

        //Getting of the Total Amount
        double getTotal() {
            return total;
        }

        //Getting of the Subtotal
        double getSubtotal() {
            return subtotal;
        }

        //Getting of the Discount Amount
        double getDiscount() {
            return discount;
        }

        //Getting of the VAT Amount
        double getVat() {
            return vat;
        }

        //Checking if the Priority Discount was Used
        boolean isPriorityDiscountApplied() {
            return priorityDiscountApplied;
        }

        //Checking if the Booking is Refunded
        boolean isRefunded() {
            return refunded;
        }

        //Marking of the Booking as Refunded
        void markRefunded() {
            refunded = true;
        }

        //Getting of the Booking Date
        LocalDate getBookingDate() {
            return bookingDate.toLocalDate();
        }

        //Getting of the Booked Seats
        ArrayList<String> getSeats() {
            return new ArrayList<String>(seats);
        }

        //Getting of the Seat Names as Text
        String getSeatNames() {
            String result = "";
            for (int i = 0; i < seats.size(); i++) {
                if (i > 0) {
                    result = result + ", ";
                }
                result = result + seats.get(i);
            }
            return result;
        }
    }
}