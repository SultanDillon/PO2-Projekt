import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class WypozyczalniaDVDPracownik extends JFrame {


    private static Connection connection;

    public WypozyczalniaDVDPracownik() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:wypozyczalnia_dvd.db");
            initializeDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Błąd połączenia z bazą danych");
            System.exit(1);
        }

        setTitle("Wypożyczalnia DVD");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            // Ustawienia wyglądu Nimbus
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

            UIManager.put("nimbusBase", new Color(68, 181, 100));
            UIManager.put("nimbusBlueGrey", new Color(68, 181, 100));
            UIManager.put("control", new Color(68, 181, 100));

            SwingUtilities.updateComponentTreeUI(this);
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException e) {
            e.printStackTrace();
        }

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2));

        JLabel titleLabel = new JLabel("Tytuł DVD:");
        JTextField titleField = new JTextField();

        JLabel clientLabel = new JLabel("Imię i nazwisko klienta:");
        JTextField clientField = new JTextField();

        JLabel peselLabel = new JLabel("PESEL klienta:");
        JTextField peselField = new JTextField();

        peselField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
                    evt.consume();
                }
            }

            public void keyReleased(KeyEvent e) {
                JTextField textField = (JTextField) e.getSource();
                String text = textField.getText();
                if (text.length() > 11) {
                    textField.setText(text.substring(0, 11));
                }
            }
        });

        peselField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                JTextField textField = (JTextField) evt.getSource();
                String pesel = textField.getText();
                if (pesel.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "PESEL nie może być pusty", "Błąd", JOptionPane.ERROR_MESSAGE);
                } else if (pesel.length() != 11) {
                    JOptionPane.showMessageDialog(null, "PESEL powinien mieć dokładnie 11 cyfr", "Błąd", JOptionPane.ERROR_MESSAGE);
                    textField.setText("");
                }
            }
        });

       // JButton rentButton = new JButton("Wypożycz");
        JButton returnButton = new JButton("Zwróć");
       // JButton reserveButton = new JButton("Zarezerwuj");
        JButton addButton = new JButton("Dodaj DVD");
        JButton extendButton = new JButton("Przedłuż termin oddania");
        JButton showAllButton = new JButton("Pokaż wszystkie DVD");

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newDVDTitle = JOptionPane.showInputDialog("Podaj tytuł nowego DVD:");
                if (newDVDTitle != null && !newDVDTitle.isEmpty()) {
                    addDVD(newDVDTitle);
                    showAllDVDs(); // Refresh the displayed DVD list after adding a new DVD
                }
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
                String titleToReturn = titleField.getText();
                returnDVD(titleToReturn);
            }
        });



        extendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String titleToExtend = titleField.getText();
                extendDueDate(titleToExtend);
            }
        });

        panel.add(titleLabel);
        panel.add(titleField);
        panel.add(clientLabel);
        panel.add(clientField);
        panel.add(peselLabel);
        panel.add(peselField);
     //   panel.add(rentButton);
        panel.add(returnButton);
      //  panel.add(reserveButton);
        panel.add(addButton);
        panel.add(extendButton);
        panel.add(showAllButton);

        add(panel);
        setVisible(true);
    }

    private static void initializeDatabase() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS dvd (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, reserved BOOLEAN DEFAULT 0, due_date DATE, client_id INTEGER)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS clients (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, pesel TEXT UNIQUE)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAllDVDs() {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT dvd.id, dvd.title, clients.name AS client_name, dvd.due_date, dvd.reserved " +
                    "FROM dvd LEFT JOIN clients ON dvd.client_id = clients.id");
            StringBuilder result = new StringBuilder("Lista wszystkich DVD:\n");

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

                result.append("ID: ").append(id).append(", Tytuł: ").append(title)
                        .append(", Status: ").append(status)
                        .append(", Klient: ").append(clientName == null ? "Brak" : clientName)
                        .append(", Termin oddania: ").append(dueDate).append("\n");
            }

            if (result.length() > 20) {
                JOptionPane.showMessageDialog(this, result.toString());
            } else {
                JOptionPane.showMessageDialog(this, "Brak dostępnych DVD");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd podczas pobierania listy DVD");
        }
    }


    private void rentDVD(String title, String client, String pesel) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT OR IGNORE INTO clients (name, pesel) VALUES (?, ?)")) {
            preparedStatement.setString(1, client);
            preparedStatement.setString(2, pesel);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE dvd SET reserved = 0, due_date = DATE('now', '+7 days'), client_id = (SELECT id FROM clients WHERE pesel = ?) WHERE title = ? AND reserved = 0")) {
            preparedStatement.setString(1, pesel);
            preparedStatement.setString(2, title);
            int updatedRows = preparedStatement.executeUpdate();
            if (updatedRows > 0) {
                JOptionPane.showMessageDialog(this, "Wypożyczono DVD: " + title + " dla klienta: " + client + "\nTermin oddania: " + getDueDate(title));
            } else {
                JOptionPane.showMessageDialog(this, "DVD: " + title + " jest już wypożyczone/zarezerwowane lub nie istnieje");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void returnDVD(String title) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE dvd SET reserved = 0, due_date = NULL, client_id = NULL WHERE title = ? AND (reserved = 1 OR client_id IS NOT NULL)")) {
            preparedStatement.setString(1, title);
            int updatedRows = preparedStatement.executeUpdate();
            if (updatedRows > 0) {
                double lateFee = calculateLateFee(title);
                if (lateFee > 0) {
                    JOptionPane.showMessageDialog(this, "Zwrócono DVD: " + title + "\nOpłata za opóźnienie: " + lateFee + " PLN");
                } else {
                    JOptionPane.showMessageDialog(this, "Zwrócono DVD: " + title);
                }
            } else {
                JOptionPane.showMessageDialog(this, "DVD: " + title + " nie jest obecnie wypożyczone ani zarezerwowane");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private double calculateLateFee(String title) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT due_date FROM dvd WHERE title = ? AND reserved = 1")) {
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
            e.printStackTrace();
        }
        return 0;
    }

    private void reserveDVD(String title, String client, String pesel) {
        try (PreparedStatement checkStatement = connection.prepareStatement("SELECT reserved FROM dvd WHERE title = ? AND client_id IS NOT NULL")) {
            checkStatement.setString(1, title);
            ResultSet resultSet = checkStatement.executeQuery();
            if (resultSet.next()) {
                // DVD is already rented, cannot reserve
                JOptionPane.showMessageDialog(this, "DVD: " + title + " jest już wypożyczone i nie może być zarezerwowane");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Rest of the code for reserving DVD
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT OR IGNORE INTO clients (name, pesel) VALUES (?, ?)")) {
            preparedStatement.setString(1, client);
            preparedStatement.setString(2, pesel);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE dvd SET reserved = 1, due_date = DATE('now', '+7 days'), client_id = (SELECT id FROM clients WHERE pesel = ?) WHERE title = ? AND reserved = 0")) {
            preparedStatement.setString(1, pesel);
            preparedStatement.setString(2, title);
            int updatedRows = preparedStatement.executeUpdate();
            if (updatedRows > 0) {
                JOptionPane.showMessageDialog(this, "Zarezerwowano DVD: " + title + " dla klienta: " + client);
            } else {
                JOptionPane.showMessageDialog(this, "DVD: " + title + " jest już zarezerwowane lub nie istnieje");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void addDVD(String title) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO dvd (title, reserved, due_date) VALUES (?, 0, NULL)")) {
            preparedStatement.setString(1, title);
            preparedStatement.executeUpdate();
            JOptionPane.showMessageDialog(this, "Dodano nowe DVD: " + title);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void extendDueDate(String title) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT reserved FROM dvd WHERE title = ? AND reserved = 1")) {
            preparedStatement.setString(1, title);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                JOptionPane.showMessageDialog(this, "Nie można przedłużyć terminu oddania dla zarezerwowanego DVD: " + title);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE dvd SET due_date = DATE(due_date, '+7 days') WHERE title = ? AND reserved = 0 AND client_id IS NOT NULL")) {
            preparedStatement.setString(1, title);
            int updatedRows = preparedStatement.executeUpdate();
            if (updatedRows > 0) {
                JOptionPane.showMessageDialog(this, "Przedłużono termin oddania DVD: " + title + "\nNowy termin oddania: " + getDueDate(title));
            } else {
                JOptionPane.showMessageDialog(this, "DVD: " + title + " nie jest obecnie wypożyczone");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private String getDueDate(String title) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT due_date FROM dvd WHERE title = ? AND reserved = 1")) {
            preparedStatement.setString(1, title);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("due_date");
            } else {
                return "Nieznany";
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
                    new WypozyczalniaDVDPracownik();
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Błąd połączenia z bazą danych");
            System.exit(1);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}
