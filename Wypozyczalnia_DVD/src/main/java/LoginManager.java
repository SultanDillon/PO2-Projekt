import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginManager extends JFrame {
    private static final Logger logger = LogManager.getLogger(LoginManager.class);
    private JTextField userField;
    private JPasswordField passwordField;
    private boolean successfulLogin = false;

    public LoginManager() {
        setTitle("Wybór roli");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Wybór roli (Klient/pracownik)
        String[] options = {"Klient", "Pracownik"};
        int roleChoice = JOptionPane.showOptionDialog(null, "Wybierz rolę", "Wybór roli", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (roleChoice == 1) {  // Jeśli wybrano "Pracownik"
            logger.info("Wybrano pracownika");
            showLoginPanel();
        } else {
            logger.info("Wybrano klienta");
            successfulLogin = true;  // Pomyślne logowanie dla gościa
            dispose(); // Zamknij okno, jeśli Klient
        }
    }

    private void showLoginPanel() {
        logger.info("Inicjalizacja okna logowania");
        setTitle("Logowanie");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JLabel nameLabel = new JLabel("Użytkownik:");
        userField = new JTextField();

        JLabel passwordLabel = new JLabel("Hasło:");
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Zaloguj");
        logger.info("Zainicjowano okno logowania");

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
        panel.add(new JLabel());
        panel.add(loginButton);

        add(panel);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void authenticateUser() {
        String username = userField.getText();
        char[] password = passwordField.getPassword();

        if (authenticate(username, password)) {
            successfulLogin = true;
            logger.info("Zalogowano się kontem administratora");
            dispose();
            if (successfulLogin) {
                new WypozyczalniaDVDPracownik(); // Otwórz pracownika po pomyślnym zalogowaniu
            }
        } else {
            JOptionPane.showMessageDialog(null, "Nieprawidłowy login lub hasło", "Błąd logowania", JOptionPane.ERROR_MESSAGE);
            logger.warn("Błąd logowania się kontem administratora");
            userField.setText("");
            passwordField.setText("");
        }
    }



    public boolean authenticate(String username, char[] password) {
        if (username.equals("admin") && Arrays.equals(password, "admin".toCharArray())) {
            successfulLogin = true; // Ustaw flagę na true po pomyślnym uwierzytelnieniu
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
