import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.getBoolean;

public class WypozyczalniaDVDFrame extends JFrame {
    private static final Logger logger = LogManager.getLogger(WypozyczalniaDVDFrame.class);
    private static Connection connection;

    public WypozyczalniaDVDFrame() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:wypozyczalnia_dvd.db");
            logger.info("Połączono z bazą");
            initializeDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Błąd połączenia z bazą danych");
            logger.error("Błąd połączenia z bazą danych");
            System.exit(1);
        }

        setTitle("Wypożyczalnia DVD");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            // Ustawienia wyglądu
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

            UIManager.put("nimbusBase", new Color(68, 181, 100));
            UIManager.put("nimbusBlueGrey", new Color(68, 181, 100));
            UIManager.put("control", new Color(68, 181, 100));

            SwingUtilities.updateComponentTreeUI(this);
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException e) {
            logger.error("Napotkano błąd");
            e.printStackTrace();
        }

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2));

        JLabel titleLabel = new JLabel("Tytuł DVD:");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15));
        String[] dvdTitles = getDVDTitles();
        JComboBox<String> dvdTitleComboBox = new JComboBox<>(dvdTitles);

        JLabel clientLabel = new JLabel("Imię i nazwisko:");
        clientLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15));
        JTextField clientField = new JTextField();

        JLabel peselLabel = new JLabel("PESEL:");
        peselLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15));
        JTextField peselField = new JTextField();

        peselField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
                    evt.consume();
                }
                logger.info("Klawisz został wciśnięty");
            }

            public void keyReleased(KeyEvent e) {
                JTextField textField = (JTextField) e.getSource();
                String text = textField.getText();
                if (text.length() > 11) {
                    textField.setText(text.substring(0, 11));
                }
                logger.info("Klawisz został odpuszczony");
            }
        });

        peselField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                JTextField textField = (JTextField) evt.getSource();
                String pesel = textField.getText();
                if (pesel.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "PESEL nie może być pusty", "Błąd", JOptionPane.ERROR_MESSAGE);
                    logger.error("PESEL nie może być pusty");
                } else if (pesel.length() != 11) {
                    JOptionPane.showMessageDialog(null, "PESEL powinien mieć dokładnie 11 cyfr", "Błąd", JOptionPane.ERROR_MESSAGE);
                    logger.error("PESEL powinien mieć dokładnie 11 cyfr");
                    textField.setText("");
                }
            }
        });

        JButton rentButton = new JButton("Wypożycz");
        JButton returnButton = new JButton("Zwróć");
        JButton reserveButton = new JButton("Zarezerwuj");
        JButton extendButton = new JButton("Przedłuż termin oddania");
        JButton showAllButton = new JButton("Pokaż wszystkie DVD");
        returnButton.setFont(returnButton.getFont().deriveFont(Font.BOLD, 14));
        rentButton.setFont(rentButton.getFont().deriveFont(Font.BOLD, 14));
        extendButton.setFont(extendButton.getFont().deriveFont(Font.BOLD, 14));
        showAllButton.setFont(showAllButton.getFont().deriveFont(Font.BOLD, 14));
        reserveButton.setFont(reserveButton.getFont().deriveFont(Font.BOLD, 14));


        showAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAllDVDs();
            }
        });

        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String titleToReturn = (String) dvdTitleComboBox.getSelectedItem();
                String clientName = clientField.getText();
                String pesel = peselField.getText();

                // Sprawdź czy pola nie są puste
                if (titleToReturn.isEmpty() || clientName.isEmpty() || pesel.isEmpty()) {
                    JOptionPane.showMessageDialog(WypozyczalniaDVDFrame.this, "Wypełnij wszystkie pola", "Błąd", JOptionPane.ERROR_MESSAGE);
                    return; // Przerwij akcję zwrotu
                }

                returnDVD(titleToReturn, clientName, pesel);
            }
        });

        reserveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = (String) dvdTitleComboBox.getSelectedItem();
                String client = clientField.getText();
                String pesel = peselField.getText();

                if (pesel.isEmpty() || pesel.length() != 11) {
                    JOptionPane.showMessageDialog(null, "PESEL powinien mieć dokładnie 11 cyfr", "Błąd", JOptionPane.ERROR_MESSAGE);
                    logger.error("PESEL powinien mieć dokładnie 11 cyfr");
                    return;
                }

                reserveDVD(title, client, pesel);
            }
        });

        rentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Przycisk 'Wypożycz' został naciśnięty.");
                String title = (String) dvdTitleComboBox.getSelectedItem();
                String client = clientField.getText();
                String pesel = peselField.getText();

                if (pesel.length() != 11) {
                    JOptionPane.showMessageDialog(null, "PESEL powinien mieć dokładnie 11 cyfr", "Błąd", JOptionPane.ERROR_MESSAGE);
                    logger.error("PESEL powinien mieć dokładnie 11 cyfr");
                    return;
                }

                rentDVD(title, client, pesel);
            }
        });

        extendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String titleToExtend = (String) dvdTitleComboBox.getSelectedItem();
                extendDueDate(titleToExtend);
            }
        });

        panel.add(titleLabel);
        panel.add(dvdTitleComboBox);
        panel.add(clientLabel);
        panel.add(clientField);
        panel.add(peselLabel);
        panel.add(peselField);
        panel.add(rentButton);
        panel.add(returnButton);
        panel.add(reserveButton);
        panel.add(extendButton);
        panel.add(showAllButton);

        add(panel);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        int windowWidth = getWidth();
        int windowHeight = getHeight();
        setLocation((screenWidth - windowWidth) / 2, (screenHeight - windowHeight) / 2);

        setVisible(true);
    }

    private String[] getDVDTitles() {
        List<String> titlesList = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT title FROM dvd");
            while (resultSet.next()) {
                titlesList.add(resultSet.getString("title"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            logger.error("Błąd podczas pobierania tytułów DVD z bazy danych");
        }
        return titlesList.toArray(new String[0]);
    }

    public static boolean initializeDatabase() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS dvd (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, reserved BOOLEAN DEFAULT 0, due_date DATE, client_id INTEGER, extended BOOLEAN DEFAULT 0, rented BOOLEAN DEFAULT 0)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS clients (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, pesel TEXT UNIQUE)");
            logger.info("Baza danych została zainicjonowana");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Nie udało się zainicjować bazy");
            return false;
        }
    }

    private void showAllDVDs() {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT dvd.id, dvd.title, clients.name AS client_name, dvd.due_date, dvd.reserved " +
                    "FROM dvd LEFT JOIN clients ON dvd.client_id = clients.id");

            JPanel panel = new JPanel(new GridLayout(0, 2));

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String clientName = resultSet.getString("client_name");
                String dueDate = resultSet.getString("due_date");
                boolean reserved = resultSet.getBoolean("reserved");

                String status;
                if (reserved) {
                    status = "Zarezerwowane";
                } else if (clientName != null) {
                    status = "Wypożyczone";
                } else {
                    status = "Dostępne";
                }

                JPanel dvdPanel = new JPanel(new GridLayout(4, 1));
                dvdPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                dvdPanel.setBackground(new Color(54,49,47)); //

                JLabel titleLabel = new JLabel("Tytuł: " + title);
                JLabel statusLabel = new JLabel("Status: " + status);
                JLabel dueDateLabel = new JLabel("Termin oddania: " + (dueDate == null ? "Brak" : dueDate));

                Font titleFont = new Font("Arial", Font.BOLD, 15);
                titleLabel.setFont(titleFont);

                Font statusFont = new Font("Arial", Font.PLAIN, 13);
                statusLabel.setFont(statusFont);

                Font dueDateFont = new Font("Arial", Font.PLAIN, 12);
                dueDateLabel.setFont(dueDateFont);

                titleLabel.setForeground(Color.WHITE);
                statusLabel.setForeground(Color.WHITE);
                dueDateLabel.setForeground(Color.WHITE);

                dvdPanel.add(titleLabel);
                dvdPanel.add(statusLabel);
                dvdPanel.add(dueDateLabel);
                panel.add(dvdPanel);
            }

            if (panel.getComponentCount() > 0) {
                JScrollPane scrollPane = new JScrollPane(panel);
                JOptionPane.showMessageDialog(this, scrollPane);
            } else {
                JOptionPane.showMessageDialog(this, "Brak dostępnych DVD");
                logger.error("Brak dostępnych DVD");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd podczas pobierania listy DVD");
            logger.error("Błąd podczas pobierania listy DVD");
        }
    }

    private void rentDVD(String title, String client, String pesel) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT OR IGNORE INTO clients (name, pesel) VALUES (?, ?)")) {
            preparedStatement.setString(1, client);
            preparedStatement.setString(2, pesel);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Nie udało się wstawić rekordu do bazy");
            e.printStackTrace();
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE dvd SET reserved = 0, rented = 1,  due_date = date('now', '+7 days'), client_id = (SELECT id FROM clients WHERE pesel = ?) WHERE title = ? AND reserved = 1")) {
            preparedStatement.setString(1, pesel);
            preparedStatement.setString(2, title);
            int updatedRows = preparedStatement.executeUpdate();
            if (updatedRows > 0) {
                JOptionPane.showMessageDialog(this, "Wypożyczono DVD: " + title + " dla klienta: " + client + "\nTermin oddania: " + getDueDate(title));
                logger.info("Wypożyczono DVD dla klienta");
            } else {
                JOptionPane.showMessageDialog(this, "DVD: " + title + " jest już wypożyczone/zarezerwowane lub nie istnieje");
                logger.warn("DVD: jest już wypożyczne/zarezerwowane lub nie istnieje");
            }
        } catch (SQLException e) {
            logger.error("Nie udało się wstawić rekordu do bazy");
            e.printStackTrace();
        }
    }

    private void returnDVD(String title, String clientName, String pesel) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT rented, due_date, clients.name AS client_name, clients.pesel AS client_pesel FROM dvd LEFT JOIN clients ON dvd.client_id = clients.id WHERE title = ? AND (rented = 1 OR client_id IS NOT NULL)")) {
            preparedStatement.setString(1, title);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                boolean rented = resultSet.getBoolean("rented");
                String dueDateStr = resultSet.getString("due_date");
                String storedClientName = resultSet.getString("client_name");
                String storedPesel = resultSet.getString("client_pesel");

                if (dueDateStr != null && storedClientName != null && storedPesel != null && rented) {
                    try {
                        Date dueDate = Date.valueOf(dueDateStr);
                        Date currentDate = Date.valueOf(LocalDate.now());

                        // Sprawdź, czy termin oddania minął
                        if (currentDate.after(dueDate)) {
                            // Oblicz opóźnienie i nalicz odsetki
                            long daysLate = ChronoUnit.DAYS.between(dueDate.toLocalDate(), currentDate.toLocalDate());
                            double lateFee = daysLate * 0.50;

                            JOptionPane.showMessageDialog(this, "DVD: " + title + " jest opóźnione.\nOpłata za opóźnienie: " + lateFee + " PLN \nUdaj się do placówki by uregulowac należność");
                            logger.info("DVD jest opóźnione. Naliczono opłatę za opóźnienie: " + lateFee + " PLN");

                            // Zablokuj możliwość oddania DVD
                            JOptionPane.showMessageDialog(this, "Nie można zwrócić opóźnionego DVD.");
                            logger.info("Nie można zwrócić opóźnionego DVD.");
                            return;
                        }

                        // Sprawdź, czy dane klienta sa ok
                        if (!clientName.equalsIgnoreCase(storedClientName) || !pesel.equals(storedPesel)) {
                            JOptionPane.showMessageDialog(this, "Błędne dane klienta. Proszę podać poprawne imię, nazwisko i PESEL.");
                            logger.warn("Błędne dane klienta. Podane imię, nazwisko lub PESEL nie zgadzają się z danymi w bazie.");
                            return;
                        }
                    } catch (IllegalArgumentException e) {
                        logger.error("Błąd przetwarzania daty oddania DVD: " + e.getMessage());
                    }
                }
            }

            // Normalne oddawanie DVD
            try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE dvd SET reserved = 0, rented=0, extended = 0, due_date = NULL, client_id = NULL WHERE title = ? AND (rented = 1 OR client_id IS NOT NULL)")) {
                updateStatement.setString(1, title);
                int updatedRows = updateStatement.executeUpdate();
                if (updatedRows > 0) {
                    JOptionPane.showMessageDialog(this, "DVD: " + title + " zostało zwrócone przez: " + clientName);
                    logger.info("DVD: " + title + " zostało zwrócone przez: " + clientName);
                } else {
                    JOptionPane.showMessageDialog(this, "DVD: " + title + " nie jest obecnie wypożyczone ani zarezerwowane.");
                    logger.info("DVD: " + title + " nie jest obecnie wypożyczone ani zarezerwowane.");
                }
            } catch (SQLException e) {
                logger.error("Nie udało się zaktualizować rekordu w bazie danych.");
                e.printStackTrace();
            }

        } catch (SQLException e) {
            logger.error("Nie udało się pobrać rekordu z bazy danych.");
            e.printStackTrace();
        }
    }

    private void reserveDVD(String title, String client, String pesel) {
        try (PreparedStatement checkStatement = connection.prepareStatement("SELECT reserved FROM dvd WHERE title = ? AND client_id IS NOT NULL")) {
            checkStatement.setString(1, title);
            ResultSet resultSet = checkStatement.executeQuery();
            Boolean zarezerwowane = resultSet.getBoolean("reserved");
            if (zarezerwowane) {
                // DVD is already rented, cannot reserve
                JOptionPane.showMessageDialog(this, "DVD: " + title + " jest już zarezerwowane");
                logger.error("DVD jest już zarezerwowane");
                return;
            }
        } catch (SQLException e) {
            logger.error("Nie udało się pobrać rekordu z bazy");
            e.printStackTrace();
            return;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT OR IGNORE INTO clients (name, pesel) VALUES (?, ?)")) {
            preparedStatement.setString(1, client);
            preparedStatement.setString(2, pesel);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Nie udało się wstawić rekord do bazy");
            e.printStackTrace();
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE dvd SET reserved = 1, due_date = DATE('now', '+3 days'), client_id = (SELECT id FROM clients WHERE pesel = ?) WHERE title = ? AND reserved = 0")) {
            preparedStatement.setString(1, pesel);
            preparedStatement.setString(2, title);
            int updatedRows = preparedStatement.executeUpdate();
            if (updatedRows > 0) {
                JOptionPane.showMessageDialog(this, "Zarezerwowano DVD: " + title + " dla klienta: " + client);
            } else {
                JOptionPane.showMessageDialog(this, "DVD: " + title + " jest już zarezerwowane lub nie istnieje");
            }
        } catch (SQLException e) {
            logger.error("Nie udało się zaaktulizować rekordu w bazie");
            e.printStackTrace();
        }
    }

    private void extendDueDate(String title) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT reserved FROM dvd WHERE title = ? AND reserved = 1")) {
            preparedStatement.setString(1, title);
            ResultSet resultSet = preparedStatement.executeQuery();
            Boolean zarezerwowane = resultSet.getBoolean("reserved");
            if (zarezerwowane) {
                JOptionPane.showMessageDialog(this, "Nie można przedłużyć terminu oddania dla zarezerwowanego DVD: " + title);
                logger.warn("Nie można przedłużyć terminu oddania dla zarezerwowanego DVD");
                return;
            }
        } catch (SQLException e) {
            logger.error("Nie udało się pobrać rekordu z bazy");
            e.printStackTrace();
        }
        try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT dvd.extended, dvd.due_date FROM dvd WHERE title=?")) {
            preparedStatement.setString(1, title);
            ResultSet result = preparedStatement.executeQuery();
            String data = result.getString("due_date");
            Boolean extended = result.getBoolean("extended");
            if(extended) {
                JOptionPane.showMessageDialog(this, "Nie można przedłużyc: " + title + " ponieważ termin oddania został już przedłużony");
                logger.warn("Nie można przedłużyć terminu oddania dla przedłużonego DVD");
                return;
            }
            try (PreparedStatement query = connection.prepareStatement("UPDATE dvd SET due_date = date(?, '+7 days'), extended = 1 WHERE title = ? AND rented = 1 AND client_id IS NOT NULL")) {
                query.setString(1, data);
                query.setString(2, title);
                int updatedRows = query.executeUpdate();
                if (updatedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Przedłużono termin oddania DVD: " + title + "\nNowy termin oddania: " + getDueDate(title));
                } else {
                    JOptionPane.showMessageDialog(this, "DVD: " + title + " nie jest obecnie wypożyczone");
                }
            } catch (SQLException e) {
                logger.error("Nie udało się zaaktulizować rekordu w bazie");
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getDueDate(String title) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT due_date FROM dvd WHERE title = ?")) {
            preparedStatement.setString(1, title);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("due_date");
            } else {
                return "Nieznany";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Nie udało się pobrać rekordu z bazy");
            return "Błąd";
        }
    }

    public static void main(String[] args) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:wypozyczalnia_dvd.db");
            initializeDatabase();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new WypozyczalniaDVDFrame();
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Błąd połączenia z bazą danych");
            logger.error("Nie udało się połączyć");
            System.exit(1);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error("Nie udało się połączyć");
                e.printStackTrace();
            }
        }
    }
}
