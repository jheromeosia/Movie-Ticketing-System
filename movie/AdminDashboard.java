package movie;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.io.File;
import java.util.ArrayList;

//Admin Screens and Shared Account and Movie Data
public final class AdminDashboard extends JFrame implements ActionListener {
    private static final Color BG = new Color(12, 17, 28);
    private static final Color HEADER = new Color(17, 24, 39);
    private static final Color CARD = new Color(25, 34, 51);
    private static final Color CARD_LIGHT = new Color(33, 44, 64);
    private static final Color BORDER = new Color(52, 66, 89);
    private static final Color ACCENT = new Color(255, 190, 72);
    private static final Color TEXT = new Color(242, 245, 250);
    private static final Color MUTED = new Color(157, 170, 190);
    private static final Color LOCKED = new Color(255, 107, 107);

    private UserAccount admin;
    private JPanel root;
    private ArrayList<Movie> selectedMovies;
    private String reportMode;

    //Opening of the Admin Window
    public AdminDashboard(UserAccount admin) {
        super("iScreen - Admin / Manager");
        this.admin = admin;
        selectedMovies = new ArrayList<Movie>();
        reportMode = "DAILY";
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        setBounds(screen);
        root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);
        showMenu();
        setVisible(true);
    }

    //Displaying of the Admin Menu
    private void showMenu() {
        JPanel menu = new JPanel(new GridLayout(3, 3, 10, 10));
        menu.setBackground(BG);
        menu.setBorder(new EmptyBorder(10, 10, 10, 10));
        menu.setPreferredSize(new Dimension(900, 420));
        menu.add(menuTile("MOVIE MANAGEMENT", "Add, edit, or delete movies.", "MOVIES"));
        menu.add(menuTile("STAFF ACCOUNTS", "Review staff accounts and unlock access.", "STAFF"));
        menu.add(menuTile("TRANSACTION HISTORY", "Review bookings and refunds.", "TRANSACTIONS"));
        menu.add(menuTile("SALES REPORT", "Review daily, weekly, monthly, and movie sales.", "REPORT"));
        menu.add(menuTile("DISCOUNT SETTINGS", "Set the priority discount for ticket bookings.", "DISCOUNT"));
        menu.add(menuTile("ADMIN SETTINGS", "Change your admin password.", "SETTINGS"));
        JPanel leftSpace = new JPanel();
        leftSpace.setBackground(BG);
        menu.add(leftSpace);
        menu.add(menuTile("ADD MANAGER ACCOUNT", "Create another manager account.", "ADD_MANAGER"));
        JPanel rightSpace = new JPanel();
        rightSpace.setBackground(BG);
        menu.add(rightSpace);

        JPanel menuHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        menuHolder.setBackground(BG);
        menuHolder.add(menu);
        int verticalGap = (getHeight() - 90 - menu.getPreferredSize().height) / 2;
        if (verticalGap < 0) {
            verticalGap = 0;
        }
        JPanel topSpace = new JPanel();
        topSpace.setBackground(BG);
        topSpace.setPreferredSize(new Dimension(0, verticalGap));
        JPanel bottomSpace = new JPanel();
        bottomSpace.setBackground(BG);
        bottomSpace.setPreferredSize(new Dimension(0, verticalGap));
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(BG);
        body.add(topSpace, BorderLayout.NORTH);
        body.add(menuHolder, BorderLayout.CENTER);
        body.add(bottomSpace, BorderLayout.SOUTH);
        showScreen("ADMIN / MANAGER MENU", "Signed in as " + admin.getUsername(), body);
    }

    //Building of a Menu Tile
    private JPanel menuTile(String title, String description, String command) {
        JPanel tile = new JPanel(new BorderLayout(0, 10));
        tile.setBackground(CARD);
        tile.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(14, 14, 14, 14)));
        tile.add(label(title, 14, ACCENT, true), BorderLayout.NORTH);
        JTextArea details = textArea(description);
        details.setRows(2);
        tile.add(details, BorderLayout.CENTER);
        attachMenuAction(tile, command);
        return tile;
    }

    //Attaching of Menu Actions to Tiles
    private void attachMenuAction(Component component, String command) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        component.addMouseListener(new MenuTileMouseListener(command));
        if (component instanceof Container) {
            Component[] children = ((Container) component).getComponents();
            for (int i = 0; i < children.length; i++) {
                attachMenuAction(children[i], command);
            }
        }
    }

    //Displaying of the Movie List (Edit and Delete)
    private void showMovies(boolean choosingToDelete) {
        selectedMovies.clear();
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(18, 25, 18, 25));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        actions.setBackground(BG);
        actions.add(button("ADD MOVIE", "ADD_MOVIE", true));
        if (choosingToDelete) {
            actions.add(button("CANCEL", "CANCEL_DELETE", false));
            actions.add(button("DELETE SELECTED", "DELETE_SELECTED", false));
        } else {
            actions.add(button("DELETE", "START_DELETE", false));
        }
        body.add(actions, BorderLayout.NORTH);

        ArrayList<Movie> movies = MovieTicketSystem.sharedMovies();
        JPanel rows = new JPanel(new GridLayout(0, 1, 0, 10));
        rows.setBackground(BG);
        if (movies.size() == 0) {
            rows.add(cardMessage("No movies have been added yet."));
        }
        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            JPanel row = new JPanel(new BorderLayout(12, 5));
            row.setBackground(CARD);
            row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(12, 14, 12, 14)));
            JPanel movieInfo = new JPanel(new BorderLayout(12, 0));
            movieInfo.setBackground(CARD);
            JLabel poster = new JLabel("", SwingConstants.CENTER);
            poster.setForeground(TEXT);
            poster.setPreferredSize(new Dimension(68, 102));
            Image posterImage = movie.getPosterImage();
            if (posterImage != null) {
                Image scaledPoster = posterImage.getScaledInstance(68, 102, Image.SCALE_SMOOTH);
                poster.setText("");
                poster.setIcon(new ImageIcon(scaledPoster));
            }
            JPanel posterHolder = new JPanel(new BorderLayout());
            posterHolder.setBackground(BG);
            posterHolder.add(poster, BorderLayout.CENTER);
            movieInfo.add(posterHolder, BorderLayout.WEST);
            JPanel details = new JPanel(new GridLayout(0, 1, 4, 4));
            details.setBackground(CARD);
            JLabel movieTitle = label(movie.getMovieTitle(), 18, TEXT, true);
            movieTitle.setHorizontalAlignment(SwingConstants.CENTER);
            JLabel movieFacts = label(movie.getGenre() + " | " + movie.getAgeRestriction() + " | PHP " + movie.getTicketPrice(), 16, TEXT, false);
            movieFacts.setHorizontalAlignment(SwingConstants.CENTER);
            JLabel movieShowtimes = label("Showtimes: " + join(movie.getShowtimes()), 15, TEXT, false);
            movieShowtimes.setHorizontalAlignment(SwingConstants.CENTER);
            details.add(movieTitle);
            details.add(movieFacts);
            details.add(movieShowtimes);
            movieInfo.add(details, BorderLayout.CENTER);
            row.add(movieInfo, BorderLayout.CENTER);
            if (choosingToDelete) {
                JCheckBox select = new JCheckBox("Select");
                select.setForeground(TEXT);
                select.setBackground(CARD);
                select.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                select.setActionCommand("SELECT_DELETE:" + i);
                select.addActionListener(this);
                row.add(select, BorderLayout.WEST);
            }
            JButton edit = compactActionButton("EDIT / REPLACE", "EDIT_MOVIE:" + i);
            row.add(edit, BorderLayout.EAST);
            rows.add(row);
        }
        JScrollPane scroll = new JScrollPane(rows);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        body.add(scroll, BorderLayout.CENTER);
        showScreen("MOVIE MANAGEMENT", "Changes update the staff movie catalog.", body);
    }

    //Movie Form Panel (Add and Edit)
    private class MovieFormPanel extends JPanel {
        private JTextField titleField;
        private JComboBox<String> genreField;
        private JTextField priceField;
        private JTextField ageField;
        private JTextField timesField;
        private boolean editing;

        //Building of the Movie Form
        MovieFormPanel(Movie movie) {
            editing = movie != null;
            setLayout(new GridLayout(0, 2, 8, 7));
            setBackground(CARD);
            setBorder(new EmptyBorder(12, 12, 12, 12));
            titleField = new JTextField();
            genreField = new JComboBox<String>();
            genreField.addItem("ACTION");
            genreField.addItem("HORROR");
            genreField.addItem("COMEDY");
            genreField.addItem("ROMANCE");
            genreField.addItem("ANIMATION");
            genreField.addItem("DRAMA");
            genreField.setEditable(false);
            priceField = new JTextField();
            ageField = new JTextField();
            timesField = new JTextField();

            styleField(titleField);
            styleField(priceField);
            styleField(ageField);
            styleField(timesField);

            if (editing) {
                titleField.setText(movie.getMovieTitle());
                genreField.setSelectedItem(movie.getGenre());
                priceField.setText(String.valueOf(movie.getTicketPrice()));
                ageField.setText(movie.getAgeRestriction());
                timesField.setText(join(movie.getShowtimes()));
            } else {
                genreField.setSelectedIndex(-1);
            }
            addMovieField("Movie title", titleField);
            addMovieField("Genre", genreField);
            addMovieField("Ticket price", priceField);
            addMovieField("Age restriction", ageField);
            addMovieField("Showtimes (comma separated)", timesField);
            setPreferredSize(new Dimension(540, 250));
        }

        //Adding of a Form Field
        private void addMovieField(String name, JComponent field) {
            JLabel fieldLabel = label(name, 12, MUTED, false);
            add(fieldLabel);
            add(field);
        }

        //Getting of the Selected Genre
        private String getGenre() {
            Object selected = genreField.getSelectedItem();
            if (selected == null) {
                return "";
            }
            return selected.toString();
        }
    }

    //Adding of a New Movie
    private void addMovie() {
        MovieFormPanel form = new MovieFormPanel(null);
        int answer = JOptionPane.showConfirmDialog(this, form, "Add movie", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;
        String title = form.titleField.getText().trim();
        String genre = form.getGenre().trim();
        String age = form.ageField.getText().trim();
        String timesText = form.timesField.getText().trim();
        if (title.length() == 0 || genre.length() == 0 || age.length() == 0 || timesText.length() == 0 || form.priceField.getText().trim().length() == 0) {
            showMessage("Complete the title, genre, price, age restriction, and showtimes.", "Movie information required");
            return;
        }
        double price = readPrice(form.priceField.getText().trim());
        if (price < 0.0) return;
        ArrayList<String> times = parseTimes(timesText);
        if (times.size() == 0) {
            showMessage("Enter at least one showtime.", "Showtime required");
            return;
        }
        Movie movie = new Movie(title, genre.toUpperCase(), price, age, colorForGenre(genre));
        movie.updateDetails(title, genre.toUpperCase(), price, age, colorForGenre(genre), times);
        movie.setPosterImage(askForPosterImage(null));
        MovieTicketSystem.sharedMovies().add(movie);
        showMovies(false);
    }

    //Editing of an Existing Movie
    private void editMovie(int index) {
        ArrayList<Movie> movies = MovieTicketSystem.sharedMovies();
        if (index < 0 || index >= movies.size()) 
            return;
        Movie movie = movies.get(index);
        MovieFormPanel form = new MovieFormPanel(movie);
        int answer = JOptionPane.showConfirmDialog(this, form, "Edit movie", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;
        String title = form.titleField.getText().trim();
        String genre = form.getGenre().trim();
        String priceText = form.priceField.getText().trim();
        String age = form.ageField.getText().trim();
        String timesText = form.timesField.getText().trim();
        if (title.length() == 0 || genre.length() == 0 || priceText.length() == 0 || age.length() == 0 || timesText.length() == 0) {
            showMessage("Complete the title, genre, price, age restriction, and showtimes.", "Movie information required");
            return;
        }
        double price = readPrice(priceText);
        if (price < 0.0) return;
        ArrayList<String> times = parseTimes(timesText);
        if (times.size() == 0) {
            showMessage("Enter at least one showtime.", "Showtime required");
            return;
        }
        Color posterColor = movie.getPosterColor();
        if (!genre.equalsIgnoreCase(movie.getGenre())) {
            posterColor = colorForGenre(genre);
        }
        movie.updateDetails(title, genre.toUpperCase(), price, age, posterColor, times);
        movie.setPosterImage(askForPosterImage(movie.getPosterImage()));
        showMovies(false);
    }

    //Deleting of Selected Movies
    private void deleteSelectedMovies() {
        if (selectedMovies.size() == 0) {
            showMessage("Select at least one movie.", "No movies selected");
            return;
        }
        String names = "";
        for (Movie movie : selectedMovies) {
            names = names + "\n- " + movie.getMovieTitle();
        }
        int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete these movies?" + names, "Confirm movie deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        String password = askPassword("Admin password required", "Enter your password before proceeding.");
        if (password == null) return;
        if (!admin.verifyPassword(password)) {
            showMessage("The admin password is incorrect. No movies were deleted.", "Password verification failed");
            return;
        }
        MovieTicketSystem.sharedMovies().removeAll(selectedMovies);
        showMovies(false);
    }

    //Selecting of Movies for Deletion
    private void selectMovieForDeletion(int index, boolean selected) {
        ArrayList<Movie> movies = MovieTicketSystem.sharedMovies();
        if (index < 0 || index >= movies.size()) 
            return;
        Movie movie = movies.get(index);
        if (selected && !selectedMovies.contains(movie)) {
            selectedMovies.add(movie);
        }
        if (!selected) {
            selectedMovies.remove(movie);
        }
    }

    //Displaying of Staff Accounts and Unlock Buttons
    private void showStaffAccounts() {
        ArrayList<UserAccount> staff = AccountStore.staffAccounts();
        if (staff.size() == 0) {
            showMessage("There are no staff accounts yet.", "Staff accounts");
            return;
        }
        JPanel rows = new JPanel(new GridLayout(0, 1, 0, 8));
        rows.setBackground(BG);
        for (int i = 0; i < staff.size(); i++) {
            UserAccount account = staff.get(i);
            String status = "ACTIVE";
            Color statusColor = ACCENT;
            if (account.isLocked()) {
                status = "DISABLED - " + account.getFailedAttempts() + " failed attempts";
                statusColor = LOCKED;
            }
            JPanel row = new JPanel(new BorderLayout(10, 5));
            row.setBackground(CARD);
            row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(10, 14, 10, 14)));
            JPanel accountInfo = new JPanel(new GridLayout(0, 1, 3, 3));
            accountInfo.setBackground(CARD);
            accountInfo.add(label(account.getUsername() + " | " + account.getWorkId(), 16, TEXT, true));
            accountInfo.add(label(status, 14, statusColor, true));
            row.add(accountInfo, BorderLayout.CENTER);
            JButton unlock = compactActionButton("UNLOCK", "UNLOCK_STAFF:" + i);
            if (account.isLocked()) {
                unlock.setBackground(ACCENT);
                unlock.setForeground(new Color(28, 28, 30));
            } else {
                unlock.setEnabled(false);
                unlock.setCursor(Cursor.getDefaultCursor());
            }
            JPanel unlockHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            unlockHolder.setBackground(CARD);
            unlockHolder.add(unlock);
            row.add(unlockHolder, BorderLayout.EAST);
            row.setPreferredSize(new Dimension(900, 76));
            rows.add(row);
        }
        JPanel list = new JPanel(new BorderLayout());
        list.setBackground(BG);
        list.add(rows, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(list);
        JPanel staffBody = new JPanel(new BorderLayout());
        staffBody.setBackground(BG);
        staffBody.setBorder(new EmptyBorder(18, 25, 18, 25));
        staffBody.add(scroll, BorderLayout.CENTER);
        showScreen("STAFF ACCOUNTS", "Review staff access and unlock disabled accounts.", staffBody);
    }

    //Unlocking of a Staff Account
    private void unlockStaff(int index) {
        ArrayList<UserAccount> staff = AccountStore.staffAccounts();
        if (index < 0 || index >= staff.size()) return;
        UserAccount account = staff.get(index);
        String password = askPassword("Admin password required", "Enter your admin password to unlock \"" + account.getUsername() + "\".");
        //Cancel Pressed (No Changes)
        if (password == null) return;
        if (!admin.verifyPassword(password)) {
            showMessage("The admin password is incorrect. The account was not unlocked.", "Password verification failed");
            return;
        }
        account.unlock();
        showMessage("The account " + account.getUsername() + " was unlocked. The staff member can log in again.", "Account unlocked");
        showStaffAccounts();
    }

    //Displaying of Booking Transactions
    private void showTransactions() {
        ArrayList<MovieTicketSystem.TransactionRecord> transactions = MovieTicketSystem.sharedTransactions();
        if (transactions.size() == 0) {
            showMessage("There are no bookings yet.", "Transaction history");
            return;
        }
        JPanel rows = new JPanel(new GridLayout(0, 1, 0, 8));
        rows.setBackground(BG);
        for (int i = 0; i < transactions.size(); i++) {
            MovieTicketSystem.TransactionRecord transaction = transactions.get(i);
            String status = "COMPLETED";
            if (transaction.isRefunded()) status = "REFUNDED";
            JPanel row = new JPanel(new BorderLayout(10, 5));
            row.setBackground(CARD);
            row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(9, 12, 9, 12)));
            JPanel bookingInfo = new JPanel(new GridLayout(0, 1, 2, 2));
            bookingInfo.setBackground(CARD);
            bookingInfo.add(label("Booking #" + transaction.getBookingNumber(), 15, ACCENT, true));
            bookingInfo.add(label(transaction.getMovieTitle(), 16, TEXT, true));
            bookingInfo.add(label("Seats: " + transaction.getSeatNames(), 13, MUTED, false));
            JPanel showInfo = new JPanel(new GridLayout(0, 1, 2, 2));
            showInfo.setBackground(CARD);
            showInfo.add(label("Showtime: " + transaction.getShowtime(),14, TEXT, false));
            showInfo.add(label("Payment: Cash", 13, MUTED, false));
            showInfo.add(label("Status: " + status, 13, MUTED, false));
            JPanel rightInfo = new JPanel(new BorderLayout(0, 5));
            JPanel dateAndTotal = new JPanel(new GridLayout(0, 1, 2, 2));
            dateAndTotal.setBackground(CARD);
            dateAndTotal.add(label(transaction.getBookingDate().toString(), 12, MUTED, false));
            dateAndTotal.add(label("PHP " + money(transaction.getTotal()), 17, ACCENT, true));
            JButton view = compactActionButton("VIEW / REFUND", "TRANSACTION:" + i);
            view.setEnabled(!transaction.isRefunded());
            if (transaction.isRefunded()) { 
                view.setCursor(Cursor.getDefaultCursor());
            }
            JPanel viewHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            viewHolder.setBackground(CARD);
            viewHolder.add(view);
            rightInfo.add(dateAndTotal, BorderLayout.NORTH);
            rightInfo.add(viewHolder, BorderLayout.SOUTH);
            row.add(bookingInfo, BorderLayout.WEST);
            row.add(showInfo, BorderLayout.CENTER);
            rightInfo.setBackground(CARD);
            row.add(rightInfo, BorderLayout.EAST);
            row.setPreferredSize(new Dimension(900, 118));
            rows.add(row);
        }
        JPanel list = new JPanel(new BorderLayout());
        list.setBackground(BG);
        list.add(rows, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(list);
        showScreen("TRANSACTION HISTORY", "Review bookings and process refunds.", scrollPanel(scroll));
    }

    //Displaying of One Transaction and Refund
    private void showTransaction(int index) {
        ArrayList<MovieTicketSystem.TransactionRecord> transactions = MovieTicketSystem.sharedTransactions();
        if (index < 0 || index >= transactions.size()) return;
        MovieTicketSystem.TransactionRecord record = transactions.get(index);
        String details = "Booking " + record.getBookingNumber()
                + "\nMovie: " + record.getMovieTitle()
                + "\nShowtime: " + record.getShowtime()
                + "\nSeats: " + record.getSeatNames()
                + "\nTotal: PHP " + record.getTotal();
        if (record.isRefunded()) {
            showMessage(details + "\nStatus: Refunded", "Booking details");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, details + "\n\nRefund this order and release its seats?", "Booking details", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            ArrayList<String> seats = record.getSeats();
            for (String seat : seats) {
                MovieTicketSystem.releaseSeat(record.getMovie(), record.getShowtime(), seat);
            }
            record.markRefunded();
            showMessage("The booking was marked as refunded and the seats were released.", "Refund complete");
            showTransactions();
        }
    }

    //Displaying of the Sales Report
    private void showSalesReport() {
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setBackground(BG);
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        tabs.setBackground(BG);
        tabs.add(button("DAILY", "REPORT_DAILY", reportMode.equals("DAILY")));
        tabs.add(button("WEEKLY", "REPORT_WEEKLY", reportMode.equals("WEEKLY")));
        tabs.add(button("MONTHLY", "REPORT_MONTHLY", reportMode.equals("MONTHLY")));
        tabs.add(button("BY MOVIE", "REPORT_MOVIE", reportMode.equals("BY MOVIE")));
        body.add(tabs, BorderLayout.NORTH);
        JPanel report = new JPanel(new BorderLayout(10, 10));
        report.setBackground(BG);
        JPanel totalCard = new JPanel(new BorderLayout(8, 0));
        totalCard.setBackground(CARD);
        totalCard.setBorder(BorderFactory.createCompoundBorder( BorderFactory.createLineBorder(ACCENT, 2), new EmptyBorder(12, 16, 12, 16)));
        totalCard.add(label("TOTAL SALES", 12, MUTED, true), BorderLayout.NORTH);
        totalCard.add(label("PHP " + money(buildReportTotal()), 23, ACCENT, true), BorderLayout.WEST);
        report.add(totalCard, BorderLayout.NORTH);
        report.add(buildReportPanel(), BorderLayout.CENTER);
        body.add(report, BorderLayout.CENTER);
        showScreen("SALES REPORT", "Review completed ticket sales.", body);
    }

    //Computing of the Report Total
    private double buildReportTotal() {
        ArrayList<MovieTicketSystem.TransactionRecord> transactions = MovieTicketSystem.sharedTransactions();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        YearMonth month = YearMonth.from(today);
        double total = 0.0;
        for (MovieTicketSystem.TransactionRecord transaction : transactions) {
            if (!transaction.isRefunded() && transactionMatches(transaction, today, weekStart, month)) {
                total = total + transaction.getTotal();
            }
        }
        return total;
    }

    //Building of the Report Panel
    private JPanel buildReportPanel() {
        ArrayList<MovieTicketSystem.TransactionRecord> transactions = MovieTicketSystem.sharedTransactions();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        YearMonth month = YearMonth.from(today);
        int bookingCount = 0;
        int ticketCount = 0;
        JPanel reportCard = new JPanel(new BorderLayout(10, 10));
        reportCard.setBackground(CARD);
        reportCard.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(18, 20, 18, 20)));
        reportCard.add(label(reportMode + " SALES REPORT", 20, ACCENT, true), BorderLayout.NORTH);

        if (reportMode.equals("BY MOVIE")) {
            ArrayList<Movie> movies = MovieTicketSystem.sharedMovies();
            JPanel movieRows = new JPanel(new GridLayout(0, 1, 0, 8));
            movieRows.setBackground(CARD);
            for (Movie movie : movies) {
                double movieSales = 0.0;
                int movieTickets = 0;
                for (MovieTicketSystem.TransactionRecord transaction : transactions) {
                    if (!transaction.isRefunded() && transaction.getMovieTitle().equals(movie.getMovieTitle())) {
                        movieSales = movieSales + transaction.getTotal();
                        movieTickets = movieTickets + transaction.getSeats().size();
                    }
                }
                JPanel movieRow = new JPanel(new GridLayout(0, 1, 3, 3));
                movieRow.setBackground(CARD);
                movieRow.setBorder(BorderFactory.createCompoundBorder( BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(5, 4, 8, 4)));
                movieRow.add(label(movie.getMovieTitle(), 16, TEXT, true));
                JPanel ticketLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                ticketLine.setBackground(CARD);
                ticketLine.add(label("Tickets sold:", 14, TEXT, false));
                ticketLine.add(label(String.valueOf(movieTickets), 14, ACCENT, true));
                movieRow.add(ticketLine);
                JPanel salesLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                salesLine.setBackground(CARD);
                salesLine.add(label("Sales total: PHP", 14, TEXT, false));
                salesLine.add(label(money(movieSales), 14, ACCENT, true));
                movieRow.add(salesLine);
                movieRows.add(movieRow);
            }
            JScrollPane scroll = new JScrollPane(movieRows);
            scroll.setBorder(BorderFactory.createLineBorder(BORDER));
            reportCard.add(scroll, BorderLayout.CENTER);
            return reportCard;
        }

        for (MovieTicketSystem.TransactionRecord transaction : transactions) {
            if (transaction.isRefunded()) continue;
            if (!transactionMatches(transaction, today, weekStart, month)) continue;
            bookingCount = bookingCount + 1;
            ticketCount = ticketCount + transaction.getSeats().size();
        }
        JPanel totals = new JPanel(new GridLayout(0, 1, 3, 3));
        totals.setBackground(CARD);
        totals.setPreferredSize(new Dimension(300, 58));
        JPanel bookingsLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        bookingsLine.setBackground(CARD);
        bookingsLine.add(label("Bookings:", 18, TEXT, false));
        bookingsLine.add(label(String.valueOf(bookingCount), 18, ACCENT, true));
        JPanel ticketsLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        ticketsLine.setBackground(CARD);
        ticketsLine.add(label("Tickets sold:", 18, TEXT, false));
        ticketsLine.add(label(String.valueOf(ticketCount), 18, ACCENT, true));
        totals.add(bookingsLine);
        totals.add(ticketsLine);
        JPanel reportContent = new JPanel(new BorderLayout(0, 10));
        reportContent.setBackground(CARD);
        reportContent.add(totals, BorderLayout.NORTH);

        JPanel bookingRows = new JPanel(new GridLayout(0, 1, 0, 8));
        bookingRows.setBackground(CARD);
        int matchingBookings = 0;
        for (MovieTicketSystem.TransactionRecord transaction : transactions) {
            if (transaction.isRefunded() || !transactionMatches(transaction, today, weekStart, month)) {
                continue;
            }
            bookingRows.add(reportBookingRow(transaction));
            matchingBookings = matchingBookings + 1;
        }
        if (matchingBookings == 0) {
            bookingRows.add(label("No bookings for this period.", 14, MUTED, false));
        }
        JPanel bookingList = new JPanel(new BorderLayout());
        bookingList.setBackground(CARD);
        bookingList.add(bookingRows, BorderLayout.NORTH);
        JScrollPane bookingScroll = new JScrollPane(bookingList);
        bookingScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        reportContent.add(bookingScroll, BorderLayout.CENTER);
        reportCard.add(reportContent, BorderLayout.CENTER);
        return reportCard;
    }

    //Building of a Report Booking Row
    private JPanel reportBookingRow(MovieTicketSystem.TransactionRecord transaction) {
        JPanel row = new JPanel(new BorderLayout(10, 4));
        row.setBackground(CARD_LIGHT);
        row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 10, 8, 10)));
        JPanel bookingInfo = new JPanel(new GridLayout(0, 1, 2, 2));
        bookingInfo.setBackground(CARD_LIGHT);
        bookingInfo.add(label("Booking #" + transaction.getBookingNumber(), 13, ACCENT, true));
        bookingInfo.add(label(transaction.getMovieTitle(), 15, TEXT, true));
        JPanel showInfo = new JPanel(new GridLayout(0, 1, 2, 2));
        showInfo.setBackground(CARD_LIGHT);
        JPanel ticketLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        ticketLine.setBackground(CARD_LIGHT);
        ticketLine.add(label("Tickets sold:", 13, TEXT, false));
        ticketLine.add(label(String.valueOf(transaction.getSeats().size()), 13, ACCENT, true));
        showInfo.add(ticketLine);
        showInfo.add(label("Showtime: " + transaction.getShowtime(), 13, TEXT, false));
        showInfo.add(label("Seats: " + transaction.getSeatNames(), 12, MUTED, false));
        JPanel amountInfo = new JPanel(new GridLayout(0, 1, 2, 2));
        amountInfo.setBackground(CARD_LIGHT);
        amountInfo.add(label(transaction.getBookingDate().toString(), 11, MUTED, false));
        amountInfo.add(label("PHP " + money(transaction.getTotal()), 16, ACCENT, true));
        row.add(bookingInfo, BorderLayout.WEST);
        row.add(showInfo, BorderLayout.CENTER);
        row.add(amountInfo, BorderLayout.EAST);
        row.setPreferredSize(new Dimension(900, 94));
        return row;
    }

    //Checking of the Report Period
    private boolean transactionMatches(MovieTicketSystem.TransactionRecord transaction, LocalDate today, LocalDate weekStart, YearMonth month) {
        LocalDate bookingDate = transaction.getBookingDate();
        if (reportMode.equals("DAILY")) {
            return bookingDate.equals(today);
        }
        if (reportMode.equals("WEEKLY")) {
            return !bookingDate.isBefore(weekStart) && !bookingDate.isAfter(today);
        }
        if (reportMode.equals("MONTHLY")) {
            return YearMonth.from(bookingDate).equals(month);
        }
        return true;
    }

    //Changing of the Priority Discount
    private void changeDiscount() {
        int currentPercent = (int) Math.round(MovieTicketSystem.getPriorityDiscountRate() * 100.0);
        JSpinner percentSpinner = new JSpinner( new SpinnerNumberModel(currentPercent, 0, 50, 1));
        JSpinner.DefaultEditor spinnerEditor =(JSpinner.DefaultEditor) percentSpinner.getEditor();
        spinnerEditor.getTextField().setColumns(2);
        spinnerEditor.getTextField().setEditable(false);
        //Centering of the Spinner Number
        spinnerEditor.getTextField().setHorizontalAlignment(JTextField.CENTER);
        //Sizing of the Spinner Box
        percentSpinner.setPreferredSize(new Dimension(52, 28));
        JPanel form = new JPanel(new BorderLayout(0, 8));
        //Centering of the Discount Text
        JLabel discountText = new JLabel("Priority discount percentage (0 to 50):");
        discountText.setHorizontalAlignment(SwingConstants.CENTER);
        form.add(discountText, BorderLayout.NORTH);
        JPanel spinnerHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        spinnerHolder.add(percentSpinner);
        form.add(spinnerHolder, BorderLayout.CENTER);
        int answer = JOptionPane.showConfirmDialog(this, form, "Discount settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) 
            return;
        int percent = ((Number) percentSpinner.getValue()).intValue();
        MovieTicketSystem.setPriorityDiscountRate(percent / 100.0);
        showMessage("The priority discount was saved.", "Discount updated");
    }

    //Changing of the Admin Password
    private void changeAdminPassword() {
        //Confirmation Before Sending the Email
        int sure = JOptionPane.showConfirmDialog(this, "Are you sure you want to change your password?\n" + "A verification code will be sent to " + admin.getMaskedEmail() + ".", "Change password", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (sure != JOptionPane.YES_OPTION) return;
        JOptionPane.showMessageDialog(this, "A verification code was sent to " + admin.getMaskedEmail() + ".", "Change password", JOptionPane.INFORMATION_MESSAGE);
        String code = JOptionPane.showInputDialog(this, "Enter the verification code.", "Change password", JOptionPane.QUESTION_MESSAGE);
        if (code == null) return;
        if (!UserAccount.isValidPasswordResetCode(code)) {
            showMessage("The verification code is incorrect.", "Code not accepted");
            return;
        }
        JPasswordField newPassword = new JPasswordField();
        JPasswordField confirmation = new JPasswordField();
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("New password"));
        form.add(newPassword);
        form.add(new JLabel("Confirm password"));
        form.add(confirmation);
        int answer = JOptionPane.showConfirmDialog(this, form, "Change admin password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;
        String password = new String(newPassword.getPassword());
        String confirm = new String(confirmation.getPassword());
        if (password.length() < 6) {
            showMessage("Use a password with at least 6 characters.", "Password too short");
            return;
        }
        if (!password.equals(confirm)) {
            showMessage("The passwords do not match.", "Check the passwords");
            return;
        }
        admin.changePassword(password);
        showMessage("Your admin password was changed.", "Password updated");
    }

    //Creating of a Manager Account
    private void createManagerAccount() {
        String username = askText("Username", "Add manager account");
        if (username == null) 
            return;
        String id = askText("Manager ID", "Add manager account");
        if (id == null) 
            return;
        String email = askText("Email (example: name@gmail.com)", "Add manager account");
        if (email == null) 
            return;
        if (!UserAccount.isValidEmail(email)) {
            showMessage("Enter a valid email address, such as name@gmail.com.", "Invalid email");
            return;
        }
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Password"));
        form.add(passwordField);
        form.add(new JLabel("Confirm password"));
        form.add(confirmField);
        int answer = JOptionPane.showConfirmDialog(this, form, "Add manager account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        if (password.length() < 6) {
            showMessage("Use a password with at least 6 characters.", "Password too short");
            return;
        }
        if (!password.equals(confirm)) {
            showMessage("The passwords do not match.", "Check the passwords");
            return;
        }
        String error = AccountStore.registerAdditionalManager(username, id, email, password);
        if (error != null) {
            showMessage(error, "Manager not created");
        } else {
            showMessage("The new manager account was created.", "Account created");
        }
    }

    //Choosing of the Movie Poster Image
    private Image askForPosterImage(Image currentImage) {
        int answer = JOptionPane.showConfirmDialog(this, "Choose a poster image now? Choose No to keep the current poster or genre color.", "Movie poster", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return currentImage;
        }
        JFileChooser chooser = new JFileChooser();
        File downloads = new File(System.getProperty("user.home"), "Downloads");
        if (downloads.exists() && downloads.isDirectory()) {
            chooser.setCurrentDirectory(downloads);
        }
        chooser.setFileFilter(new FileNameExtensionFilter( "Image files", "png", "jpg", "jpeg", "gif"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return currentImage;
        }
        File chosen = chooser.getSelectedFile();
        if (chosen == null || !chosen.isFile()) {
            showMessage("The poster image could not be opened.", "Poster not loaded");
            return currentImage;
        }
        ImageIcon icon = new ImageIcon(chosen.getPath());
        if (icon.getImageLoadStatus() != MediaTracker.COMPLETE || icon.getIconWidth() <= 0) {
            showMessage("Choose a supported image file.", "Invalid poster");
            return currentImage;
        }
        return icon.getImage();
    }

    //Placing of a Scroll Pane on the Dark Background
    private JPanel scrollPanel(JScrollPane scroll) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    //Creating of Read-Only Text Areas
    private JTextArea textArea(String text) {
        JTextArea area = new JTextArea(text, 8, 42);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("SansSerif", Font.PLAIN, 14));
        area.setForeground(TEXT);
        area.setBackground(CARD);
        return area;
    }

    //Building of Message Cards
    private JPanel cardMessage(String text) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(20, 22, 20, 22)));
        card.add(textArea(text), BorderLayout.CENTER);
        return card;
    }

    //Splitting of Showtimes and Removing of Duplicates
    private ArrayList<String> parseTimes(String text) {
        ArrayList<String> times = new ArrayList<String>();
        int start = 0;
        for (int i = 0; i <= text.length(); i++) {
            if (i == text.length() || text.charAt(i) == ',') {
                String time = text.substring(start, i).trim();
                if (time.length() > 0 && !times.contains(time)) times.add(time);
                start = i + 1;
            }
        }
        return times;
    }

    //Joining of Values for Display
    private String join(ArrayList<String> values) {
        String result = "";
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) result = result + ", ";
            result = result + values.get(i);
        }
        return result;
    }

    //Reading of the Ticket Price
    private double readPrice(String text) {
        if (text.startsWith("-")) {
            showMessage("Price cannot be negative.", "Invalid price");
            return -1.0;
        }
        //Validation of the Price Text
        if (!isValidAmountText(text)) {
            showMessage("Enter a valid price.", "Invalid price");
            return -1.0;
        }
        return Double.parseDouble(text);
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

    //Choosing of the Poster Color by Genre
    private Color colorForGenre(String genre) {
        String name = genre.toUpperCase();
        if (name.equals("ACTION")) return new Color(41, 98, 255);
        if (name.equals("HORROR")) return new Color(75, 35, 85);
        if (name.equals("COMEDY")) return new Color(46, 139, 87);
        if (name.equals("ROMANCE")) return new Color(219, 68, 130);
        if (name.equals("ANIMATION")) return new Color(20, 150, 180);
        return new Color(120, 90, 60);
    }

    //Asking of Text Input
    private String askText(String message, String title) {
        return JOptionPane.showInputDialog(this, message, title, JOptionPane.QUESTION_MESSAGE);
    }

    //Asking of the Admin Password
    private String askPassword(String title, String message) {
        JPasswordField field = new JPasswordField(18);
        Object[] prompt = {message, field};
        int result = JOptionPane.showConfirmDialog(this, prompt, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) 
            return null;
        return new String(field.getPassword());
    }

    //Building of the Header Navigation Bar
    private JPanel buttonBar(String screenTitle) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bar.setBackground(HEADER);
        if (screenTitle.equals("ADMIN / MANAGER MENU")) {
            bar.add(button("SIGN OUT", "SIGN_OUT", false));
        } else {
            bar.add(button("MENU", "MENU", false));
        }
        return bar;
    }

    //Styling of Movie Form Fields
    private void styleField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBackground(CARD_LIGHT);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(6, 8, 6, 8)));
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
        result.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(9, 13, 9, 13)));
        result.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return result;
    }

    //Creating of Compact Row Buttons
    private JButton compactActionButton(String text, String command) {
        JButton result = button(text, command, false);
        result.setFont(new Font("SansSerif", Font.BOLD, 10));
        result.setMargin(new Insets(4, 8, 4, 8));
        result.setPreferredSize(new Dimension(112, 30));
        return result;
    }

    //Creating of Labels
    private JLabel label(String text, int size, Color color, boolean bold) {
        JLabel result = new JLabel(text);
        int style = Font.PLAIN;
        if (bold) style = Font.BOLD;
        result.setFont(new Font("SansSerif", style, size));
        result.setForeground(color);
        return result;
    }

    //Formatting of Money
    private String money(double amount) {
        return String.format("%,.2f", amount);
    }

    //Displaying of Messages
    private void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    //Replacing of the Current Admin Screen
    private void showScreen(String title, String subtitle, JPanel body) {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(BG);
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(HEADER);
        header.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, ACCENT), new EmptyBorder(18, 28, 16, 28)));
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setBackground(HEADER);
        titles.add(label(title, 22, TEXT, true));
        titles.add(label(subtitle, 12, MUTED, false));
        header.add(titles, BorderLayout.CENTER);
        header.add(buttonBar(title), BorderLayout.EAST);
        screen.add(header, BorderLayout.NORTH);
        body.setBackground(BG);
        screen.add(body, BorderLayout.CENTER);
        root.removeAll();
        root.add(screen, BorderLayout.CENTER);
        root.revalidate();
        root.repaint();
    }

    //Confirming of Sign Out
    private void confirmSignOut() {
        int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?", "Sign out", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) 
            return;
        dispose();
        new LoginPage();
    }

    //Handling of Menu and Row Actions
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (command.equals("MOVIES")) {
            showMovies(false);
        } else if (command.equals("STAFF")) {
            showStaffAccounts();
        } else if (command.equals("TRANSACTIONS")) {
            showTransactions();
        } else if (command.equals("REPORT")) {
            showSalesReport();
        } else if (command.equals("DISCOUNT")) {
            changeDiscount();
        } else if (command.equals("SETTINGS")) {
            changeAdminPassword();
        } else if (command.equals("ADD_MANAGER")) {
            createManagerAccount();
        } else if (command.equals("SIGN_OUT")) {
            confirmSignOut();
        } else if (command.equals("MENU")) {
            showMenu();
        } else if (command.equals("ADD_MOVIE")) {
            addMovie();
        } else if (command.equals("START_DELETE")) {
            showMovies(true);
        } else if (command.equals("CANCEL_DELETE")) {
            showMovies(false);
        } else if (command.equals("DELETE_SELECTED")) {
            deleteSelectedMovies();
        } else if (command.startsWith("EDIT_MOVIE:")) {
            int index = Integer.parseInt(command.substring(11));
            editMovie(index);
        } else if (command.startsWith("SELECT_DELETE:")) {
            int index = Integer.parseInt(command.substring(14));
            JCheckBox box = (JCheckBox) event.getSource();
            selectMovieForDeletion(index, box.isSelected());
        } else if (command.startsWith("UNLOCK_STAFF:")) {
            int index = Integer.parseInt(command.substring(13));
            unlockStaff(index);
        } else if (command.startsWith("TRANSACTION:")) {
            int index = Integer.parseInt(command.substring(12));
            showTransaction(index);
        } else if (command.equals("REPORT_DAILY")) {
            reportMode = "DAILY";
            showSalesReport();
        } else if (command.equals("REPORT_WEEKLY")) {
            reportMode = "WEEKLY";
            showSalesReport();
        } else if (command.equals("REPORT_MONTHLY")) {
            reportMode = "MONTHLY";
            showSalesReport();
        } else if (command.equals("REPORT_MOVIE")) {
            reportMode = "BY MOVIE";
            showSalesReport();
        }
    }

    //Menu Tile Mouse Listener
    private class MenuTileMouseListener extends MouseAdapter {
        private String command;

        //Saving of the Tile Command
        MenuTileMouseListener(String command) {
            this.command = command;
        }

        //Forwarding of Tile Clicks to the Action Handler
        public void mouseClicked(MouseEvent event) {
            ActionEvent action = new ActionEvent(AdminDashboard.this, ActionEvent.ACTION_PERFORMED, command);
            actionPerformed(action);
        }
    }
}