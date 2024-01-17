import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class WypozyczalniaDVDFrame extends JFrame {

    private Connection connection;

    public WypozyczalniaDVDFrame() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:wypozyczalnia_dvd.db");
            initializeDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd połączenia z bazą danych");
            System.exit(1);
        }

        setTitle("Wypożyczalnia DVD");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2));

        JLabel titleLabel = new JLabel("Tytuł DVD:");
        JTextField titleField = new JTextField();

        JLabel clientLabel = new JLabel("Imię i nazwisko klienta:");
        JTextField clientField = new JTextField();

        JLabel peselLabel = new JLabel("PESEL klienta:");
        JTextField peselField = new JTextField();

        JButton rentButton = new JButton("Wypożycz");
        JButton returnButton = new JButton("Zwróć");
        JButton reserveButton = new JButton("Zarezerwuj");
        JButton addButton = new JButton("Dodaj DVD");
        JButton extendButton = new JButton("Przedłuż termin oddania");
        JButton showAllButton = new JButton("Pokaż wszystkie DVD");

        rentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = titleField.getText();
                String client = clientField.getText();
                String pesel = peselField.getText();
                rentDVD(title, client, pesel);
            }
        });

        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = titleField.getText();
                returnDVD(title);
            }
        });

        reserveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = titleField.getText();
                String client = clientField.getText();
                String pesel = peselField.getText();
                reserveDVD(title, client, pesel);
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = titleField.getText();
                addDVD(title);
            }
        });

        extendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = titleField.getText();
                extendDueDate(title);
            }
        });

        showAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAllDVDs();
            }
        });

        panel.add(titleLabel);
        panel.add(titleField);
        panel.add(clientLabel);
        panel.add(clientField);
        panel.add(peselLabel);
        panel.add(peselField);
        panel.add(rentButton);
        panel.add(returnButton);
        panel.add(reserveButton);
        panel.add(addButton);
        panel.add(extendButton);
        panel.add(showAllButton);

        add(panel);

        setVisible(true);
    }

    private void initializeDatabase() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS dvd (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, reserved BOOLEAN DEFAULT 0, due_date DATE)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS clients (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, pesel TEXT UNIQUE)");
        } catch (SQLException e) {
            e.printStackTrace();
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

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE dvd SET reserved = 1, due_date = DATE('now', '+7 days') WHERE title = ? AND reserved = 0")) {
            preparedStatement.setString(1, title);
            int updatedRows = preparedStatement.executeUpdate();
            if (updatedRows > 0) {
                JOptionPane.showMessageDialog(this, "Wypożyczono DVD: " + title + " dla klienta: " + client + "\nTermin oddania: " + getDueDate(title));
            } else {
                JOptionPane.showMessageDialog(this, "DVD: " + title + " jest już wypożyczone lub nie istnieje");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void returnDVD(String title) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE dvd SET reserved = 0, due_date = NULL WHERE title = ? AND reserved = 1")) {
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
                JOptionPane.showMessageDialog(this, "DVD: " + title + " nie jest obecnie wypożyczone");
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
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT OR IGNORE INTO clients (name, pesel) VALUES (?, ?)")) {
            preparedStatement.setString(1, client);
            preparedStatement.setString(2, pesel);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE dvd SET reserved = 1 WHERE title = ? AND reserved = 0")) {
            preparedStatement.setString(1, title);
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
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE dvd SET due_date = DATE(due_date, '+7 days') WHERE title = ? AND reserved = 1")) {
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

    private void showAllDVDs() {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM dvd");
            StringBuilder result = new StringBuilder("Lista wszystkich DVD:\n");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                boolean reserved = resultSet.getBoolean("reserved");
                String dueDate = resultSet.getString("due_date");

                result.append("ID: ").append(id).append(", Tytuł: ").append(title)
                        .append(", Zarezerwowane: ").append(reserved)
                        .append(", Termin oddania: ").append(dueDate).append("\n");
            }

            JOptionPane.showMessageDialog(this, result.toString());
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd podczas pobierania listy DVD");
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new WypozyczalniaDVDFrame();
            }
        });
    }
}