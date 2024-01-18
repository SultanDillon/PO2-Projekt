import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class LoginManager extends JFrame {

    private JTextField userField; // Zmienna przeniesiona na poziom klasy
    private JPasswordField passwordField; // Przeniesiona zmienna dla pola hasła
    private boolean successfulLogin = false;

    public LoginManager() {
        setTitle("Wybór roli");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Wybór roli (gość/pracownik)
        String[] options = {"Gość", "Pracownik"};
        int roleChoice = JOptionPane.showOptionDialog(null, "Wybierz rolę", "Wybór roli", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (roleChoice == 1) {  // Jeśli wybrano "Pracownik"
            showLoginPanel();
        } else {
            successfulLogin = true;  // Oznacz jako pomyślne logowanie dla gościa
            dispose(); // Zamknij okno, jeśli gość
        }
    }

    private void showLoginPanel() {
        setTitle("Logowanie");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JLabel nameLabel = new JLabel("Użytkownik:");
        userField = new JTextField(); // Inicjalizacja zmiennej na poziomie klasy

        JLabel passwordLabel = new JLabel("Hasło:");
        passwordField = new JPasswordField(); // Przeniesiona zmienna dla pola hasła

        JButton loginButton = new JButton("Zaloguj");

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authenticateUser(); // Przeniesiono kod uwierzytelniania do osobnej metody
            }
        });

        panel.add(nameLabel);
        panel.add(userField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(new JLabel()); // Pusty label dla estetyki
        panel.add(loginButton);

        add(panel);

        setLocationRelativeTo(null); // Wyśrodkuj okno na ekranie
        setVisible(true);
    }

    private void authenticateUser() {
        String username = userField.getText();
        char[] password = passwordField.getPassword();

        if (authenticate(username, password)) {
            successfulLogin = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(null, "Nieprawidłowy login lub hasło", "Błąd logowania", JOptionPane.ERROR_MESSAGE);
            userField.setText("");
            passwordField.setText("");
        }
    }

    private boolean authenticate(String username, char[] password) {
        // Implementacja logiki autentykacji
         if (username.equals("admin") && Arrays.equals(password, "admin".toCharArray())) {
             new WypozyczalniaDVDFrame();; // Dla pracownika z poprawnym hasłem
             return true;
        } else {
            return false;
        }
    }

    public boolean isSuccessfulLogin() {
        return successfulLogin;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginManager loginManager = new LoginManager();
                if (loginManager.isSuccessfulLogin()) {
                    new WypozyczalniaDVDFrame();
                }
            }
        });
    }
}
