import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class WypozyczalniaDVDPracownik extends JFrame {

    private static final Logger logger = LogManager.getLogger(WypozyczalniaDVDPracownik.class);
    private static Connection connection;
    private JComboBox<String> dvdTitleComboBox;

    public WypozyczalniaDVDPracownik() {
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
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        String[] dvdTitles = getDVDTitles();
        dvdTitleComboBox = new JComboBox<>(dvdTitles);

       /*JLabel clientLabel = new JLabel("Imię i nazwisko klienta:");
      JTextField clientField = new JTextField();

      JLabel peselLabel = new JLabel("PESEL klienta:");
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
*/
        JButton returnButton = new JButton("Zwróć");
        JButton addButton = new JButton("Dodaj DVD");
        JButton extendButton = new JButton("Przedłuż termin oddania");
        JButton showAllButton = new JButton("Pokaż wszystkie DVD");
        JButton removeButton = new JButton("Usuń DVD");
        returnButton.setFont(returnButton.getFont().deriveFont(Font.BOLD, 14));
        addButton.setFont(addButton.getFont().deriveFont(Font.BOLD, 14));
        extendButton.setFont(extendButton.getFont().deriveFont(Font.BOLD, 14));
        showAllButton.setFont(showAllButton.getFont().deriveFont(Font.BOLD, 14));
        removeButton.setFont(removeButton.getFont().deriveFont(Font.BOLD, 14));


        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newDVDTitle = JOptionPane.showInputDialog("Podaj tytuł nowego DVD:");
                if (newDVDTitle != null && !newDVDTitle.isEmpty()) {
                    addDVD(newDVDTitle);
                    showAllDVDs(); // Refresh the displayed DVD list after adding a new DVD
                }
                logger.info("Podano tytuł nowego DVD");
            }
        });

        showAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAllDVDs();
            }
        });

        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Add logic for returning DVD
                String titleToReturn = (String) dvdTitleComboBox.getSelectedItem();
                returnDVD(titleToReturn);
            }
        });

        extendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String titleToExtend = (String) dvdTitleComboBox.getSelectedItem();
                extendDueDate(titleToExtend);
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String titleToRemove = (String) dvdTitleComboBox.getSelectedItem();
                removeDVD(titleToRemove);
            }
        });


        panel.add(titleLabel);
        panel.add(dvdTitleComboBox);
     //   panel.add(clientLabel);
     //   panel.add(clientField);
    //    panel.add(peselLabel);
     //   panel.add(peselField);
        panel.add(addButton);
        panel.add(removeButton);
        panel.add(returnButton);
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
        java.util.List<String> titlesList = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT title FROM dvd");
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                titlesList.add(title);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Nie udało się pobrać tytułów DVD z bazy");
        }
        return titlesList.toArray(new String[0]);
    }

    private static void initializeDatabase() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS dvd (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, reserved BOOLEAN DEFAULT 0, due_date DATE, client_id INTEGER, extended BOOLEAN DEFAULT 0, rented BOOLEAN DEFAULT 0)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS clients (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, pesel TEXT UNIQUE)");
            logger.info("Baza danych została zainicjonowana");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Nie udało się zainicjować bazy");
        }
    }

    private void showAllDVDs() {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT dvd.id, dvd.title, clients.name AS client_name, clients.pesel AS client_pesel, dvd.due_date, dvd.reserved " +
                    "FROM dvd LEFT JOIN clients ON dvd.client_id = clients.id");

            JPanel panel = new JPanel(new GridLayout(0, 2));

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String clientName = resultSet.getString("client_name");
                String clientPesel = resultSet.getString("client_pesel");
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

                JPanel dvdPanel = new JPanel(new GridLayout(5, 1));
                dvdPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                dvdPanel.setBackground(new Color(54,49,47));

                JLabel titleLabel = new JLabel("Tytuł: " + title);
                JLabel statusLabel = new JLabel("Status: " + status);
                JLabel clientLabel = new JLabel("Klient: " + (clientName == null ? "Brak" : clientName));
                JLabel peselLabel = new JLabel("PESEL: " + (clientPesel == null ? "Brak" : clientPesel));
                JLabel dueDateLabel = new JLabel("Termin oddania: " + (dueDate == null ? "Brak" : dueDate));

                // Ustawienie czcionki i rozmiaru
                Font titleFont = new Font("Arial", Font.BOLD, 15);
                titleLabel.setFont(titleFont);

                Font statusFont = new Font("Arial", Font.PLAIN, 13);
                statusLabel.setFont(statusFont);

                Font clientFont = new Font("Arial", Font.ITALIC, 12);
                clientLabel.setFont(clientFont);

                Font peselFont = new Font("Arial", Font.PLAIN, 12);
                peselLabel.setFont(peselFont);

                Font dueDateFont = new Font("Arial", Font.PLAIN, 12);
                dueDateLabel.setFont(dueDateFont);

                titleLabel.setForeground(Color.WHITE);
                statusLabel.setForeground(Color.WHITE);
                clientLabel.setForeground(Color.WHITE);
                peselLabel.setForeground(Color.WHITE);
                dueDateLabel.setForeground(Color.WHITE);

                dvdPanel.add(titleLabel);
                dvdPanel.add(statusLabel);
                dvdPanel.add(clientLabel);
                dvdPanel.add(peselLabel);
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



    private void returnDVD(String title) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE dvd SET reserved = 0, extended = 0, rented = 0, due_date = NULL, client_id = NULL WHERE title = ? AND (reserved = 1 OR client_id IS NOT NULL)")) {
            preparedStatement.setString(1, title);
            int updatedRows = preparedStatement.executeUpdate();
            if (updatedRows > 0) {
                double lateFee = calculateLateFee(title);
                if (lateFee > 0) {
                    JOptionPane.showMessageDialog(this, "Zwrócono DVD: " + title + "\nOpłata za opóźnienie: " + lateFee + " PLN");
                    logger.info("Zwrócono DVD z naliczoną opłatą");
                } else {
                    JOptionPane.showMessageDialog(this, "Zwrócono DVD: " + title);
                    logger.info("Zwrócono DVD");
                }
            } else {
                JOptionPane.showMessageDialog(this, "DVD: " + title + " nie jest obecnie wypożyczone ani zarezerwowane");
                logger.info("DVD nie jest obecnie wypożyczone ani zarezerwowane");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private double calculateLateFee(String title) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT due_date FROM dvd WHERE title = ? AND rented = 1")) {
            preparedStatement.setString(1, title);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String dueDateStr = resultSet.getString("due_date");
                if (dueDateStr != null) {
                    Date dueDate = Date.valueOf(dueDateStr);
                    Date currentDate = Date.valueOf(LocalDate.now());
                    long daysLate = Math.max(0, ChronoUnit.DAYS.between(dueDate.toLocalDate(), currentDate.toLocalDate()));
                    return daysLate * 0.50;
                }
            }
        } catch (SQLException e) {
            logger.error("Nie udało się pobrać rekordu z bazy");
            e.printStackTrace();
        }
        return 0;
    }

    public boolean addDVD(String title) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO dvd (title, reserved, due_date) VALUES (?, 0, NULL)")) {
            preparedStatement.setString(1, title);
            preparedStatement.executeUpdate();
            JOptionPane.showMessageDialog(this, "Dodano nowe DVD: " + title);
            logger.info("Dodano nowe DVD: " + title);
            //Odś listy
            updateDVDComboBox();

            return true;
        } catch (SQLException e) {
            logger.error("Nie udało się pobrać rekordu z bazy");
            e.printStackTrace();
        }
        return false;
    }


    private void extendDueDate(String title) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT reserved FROM dvd WHERE title = ? AND reserved = 1")) {
            preparedStatement.setString(1, title);
            ResultSet resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            logger.error("Nie udało się pobrać rekordu z bazy");
            e.printStackTrace();
        }
        try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT dvd.due_date FROM dvd WHERE title=?")) {
            preparedStatement.setString(1, title);
            ResultSet result = preparedStatement.executeQuery();
            String data = result.getString("due_date");
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
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT due_date FROM dvd WHERE title = ? AND rented = 1")) {
            preparedStatement.setString(1, title);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("due_date");
            } else {
                return "Nieznany";
            }
        } catch (SQLException e) {
            logger.error("Nie udało się pobrać rekordu z bazy");
            e.printStackTrace();
            return "Błąd";
        }
    }

    private void removeDVD(String title) {
        int option = JOptionPane.showConfirmDialog(this, "Czy na pewno chcesz usunąć DVD: " + title + "?", "Usuń DVD", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM dvd WHERE title = ?")) {
                preparedStatement.setString(1, title);
                int deletedRows = preparedStatement.executeUpdate();
                if (deletedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Usunięto DVD: " + title);
                    updateDVDComboBox(); // Odśwież listę rozwijaną
                } else {
                    JOptionPane.showMessageDialog(this, "Nie można usunąć DVD: " + title + ". Nie znaleziono w bazie danych.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Błąd podczas usuwania DVD: " + title);
            }
        }
    }

    private void updateDVDComboBox() {
        String[] dvdTitles = getDVDTitles();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(dvdTitles);
        dvdTitleComboBox.setModel(model);
    }


    public static void main(String[] args) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:wypozyczalnia_dvd.db");
            initializeDatabase();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new WypozyczalniaDVDPracownik();
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Błąd połączenia z bazą danych");
            logger.error("Nie udało się pobrać rekordu z bazy");
            System.exit(1);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error("Nie udało się pobrać rekordu z bazy");
                e.printStackTrace();
            }
        }
    }


}
