import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Vector;

class DBHelper {
    public static Connection getConnection() throws SQLException {
        try {
      		Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
    		e.printStackTrace();
   		 System.out.println("JDBC Driver not found.");
	    }

        String url = "jdbc:mysql://sql12.freesqldatabase.com:3306/sql12751537?useSSL=false";
        String username = "sql12751537";
        String password = "KepXY5dtSN";

        return DriverManager.getConnection(url, username, password);
    }
}

class IceCreamService {
    final String ADMIN_PASSWORD = "siws";
    private HashMap<String, Integer> stock = new HashMap<>();
    private HashMap<String, Integer> prices = new HashMap<>();

    public IceCreamService() {
        stock.put("Vanilla", 50);
        stock.put("Chocolate", 40);
        stock.put("Strawberry", 30);

        prices.put("Vanilla", 100);
        prices.put("Chocolate", 120);
        prices.put("Strawberry", 110);
    }

    public void placeOrder(String name, String iceCreamType, int quantity, String address) {
        if (quantity <= 0) {
            JOptionPane.showMessageDialog(null, "Quantity must be a positive integer.");
            return;
        }

        if (!stock.containsKey(iceCreamType) || stock.get(iceCreamType) < quantity) {
            JOptionPane.showMessageDialog(null, "Sorry, insufficient stock for " + iceCreamType);
            return;
        }

        stock.put(iceCreamType, stock.get(iceCreamType) - quantity);
        int totalAmount = prices.get(iceCreamType) * quantity;

        try (Connection conn = DBHelper.getConnection()) {
            String insertOrderQuery = "INSERT INTO customer_orders (name, order_type, quantity, address, order_time) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertOrderQuery);
            stmt.setString(1, name);
            stmt.setString(2, iceCreamType);
            stmt.setInt(3, quantity);
            stmt.setString(4, address);
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Order placed successfully! Total Bill: Rs " + totalAmount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void viewStockAndPrices() {
        StringBuilder stockInfo = new StringBuilder();
        for (String type : stock.keySet()) {
            stockInfo.append(type)
                    .append(" - Price: Rs ")
                    .append(prices.get(type))
                    .append(" | Stock: ")
                    .append(stock.get(type))
                    .append("\n");
        }
        JOptionPane.showMessageDialog(null, stockInfo.toString());
    }

    public void updateStock(String iceCreamType, int newStock) {
        if (stock.containsKey(iceCreamType)) {
            stock.put(iceCreamType, newStock);
            JOptionPane.showMessageDialog(null, "Stock updated successfully for " + iceCreamType);
        } else {
            JOptionPane.showMessageDialog(null, "Invalid ice cream type.");
        }
    }

    public void updatePrice(String iceCreamType, int newPrice) {
        if (prices.containsKey(iceCreamType)) {
            prices.put(iceCreamType, newPrice);
            JOptionPane.showMessageDialog(null, "Price updated successfully for " + iceCreamType);
        } else {
            JOptionPane.showMessageDialog(null, "Invalid ice cream type.");
        }
    }

    public boolean validateAdminPassword(String enteredPassword) {
        return ADMIN_PASSWORD.equals(enteredPassword);
    }

    public Vector<String> getIceCreamTypes() {
        return new Vector<>(stock.keySet());
    }

    public void viewCustomerOrders() {
        StringBuilder orderDetails = new StringBuilder();
        try (Connection conn = DBHelper.getConnection()) {
            String selectOrdersQuery = "SELECT * FROM customer_orders";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(selectOrdersQuery);

            while (rs.next()) {
                String name = rs.getString("name");
                String orderType = rs.getString("order_type");
                int quantity = rs.getInt("quantity");
                String address = rs.getString("address");
                Timestamp orderTime = rs.getTimestamp("order_time");

                orderDetails.append("Name: ").append(name)
                        .append(", Order: ").append(orderType)
                        .append(", Quantity: ").append(quantity)
                        .append(", Address: ").append(address)
                        .append(", Time: ").append(orderTime)
                        .append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (orderDetails.length() > 0) {
            JOptionPane.showMessageDialog(null, orderDetails.toString());
        } else {
            JOptionPane.showMessageDialog(null, "No orders found.");
        }
    }
}

class IceCreamManagementGUI {
    IceCreamService service = new IceCreamService();

    public IceCreamManagementGUI() {
        JFrame frame = new JFrame("Ice Cream Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(230, 240, 250));

        JLabel titleLabel = new JLabel("Welcome to Ice Cream Management System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton customerButton = new JButton("Customer Mode");
        styleButton(customerButton);
        customerButton.addActionListener(e -> openCustomerMode());

        JButton adminButton = new JButton("Admin Mode");
        styleButton(adminButton);
        adminButton.addActionListener(e -> openAdminMode());

        JButton exitButton = new JButton("Exit");
        styleButton(exitButton);
        exitButton.addActionListener(e -> System.exit(0));

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(customerButton);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(adminButton);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(exitButton);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }

    private void openCustomerMode() {
        JFrame customerFrame = new JFrame("Customer Mode");
        customerFrame.setSize(400, 300);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(240, 248, 255));

        JTextField nameField = new JTextField(20);
        JTextField addressField = new JTextField(20);
        JComboBox<String> iceCreamComboBox = new JComboBox<>(service.getIceCreamTypes());
        JTextField quantityField = new JTextField(20);

        JButton placeOrderButton = new JButton("Place Order");
        styleButton(placeOrderButton);
        placeOrderButton.addActionListener(e -> {
            String name = nameField.getText();
            String address = addressField.getText();
            String iceCreamType = (String) iceCreamComboBox.getSelectedItem();

            try {
                int quantity = Integer.parseInt(quantityField.getText());
                service.placeOrder(name, iceCreamType, quantity, address);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter a valid quantity.");
            }
        });

        panel.add(createLabeledComponent("Name:", nameField));
        panel.add(createLabeledComponent("Address:", addressField));
        panel.add(createLabeledComponent("Ice Cream Type:", iceCreamComboBox));
        panel.add(createLabeledComponent("Quantity:", quantityField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(placeOrderButton);

        customerFrame.add(panel);
        customerFrame.setVisible(true);
    }

    private void openAdminMode() {
        String password = JOptionPane.showInputDialog("Enter Admin Password");

        if (service.validateAdminPassword(password)) {
            JFrame adminFrame = new JFrame("Admin Mode");
            adminFrame.setSize(400, 300);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(new Color(240, 248, 255));

            JButton viewStockButton = new JButton("View Stock and Prices");
            styleButton(viewStockButton);
            viewStockButton.addActionListener(e -> service.viewStockAndPrices());

            JButton updateStockButton = new JButton("Update Stock");
            styleButton(updateStockButton);
            updateStockButton.addActionListener(e -> {
                String iceCreamType = (String) JOptionPane.showInputDialog(
                        adminFrame, "Select Ice Cream Type:", "Update Stock", JOptionPane.QUESTION_MESSAGE, null,
                        service.getIceCreamTypes().toArray(), null);
                if (iceCreamType != null) {
                    String newStockStr = JOptionPane.showInputDialog(adminFrame, "Enter new stock for " + iceCreamType);
                    try {
                        int newStock = Integer.parseInt(newStockStr);
                        service.updateStock(iceCreamType, newStock);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid number.");
                    }
                }
            });

            JButton updatePriceButton = new JButton("Update Price");
            styleButton(updatePriceButton);
            updatePriceButton.addActionListener(e -> {
                String iceCreamType = (String) JOptionPane.showInputDialog(
                        adminFrame, "Select Ice Cream Type:", "Update Price", JOptionPane.QUESTION_MESSAGE, null,
                        service.getIceCreamTypes().toArray(), null);
                if (iceCreamType != null) {
                    String newPriceStr = JOptionPane.showInputDialog(adminFrame, "Enter new price for " + iceCreamType);
                    try {
                        int newPrice = Integer.parseInt(newPriceStr);
                        service.updatePrice(iceCreamType, newPrice);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid price.");
                    }
                }
            });

            JButton viewOrdersButton = new JButton("View Customer Orders");
            styleButton(viewOrdersButton);
            viewOrdersButton.addActionListener(e -> service.viewCustomerOrders());

            panel.add(viewStockButton);
            panel.add(Box.createVerticalStrut(10));
            panel.add(updateStockButton);
            panel.add(Box.createVerticalStrut(10));
            panel.add(updatePriceButton);
            panel.add(Box.createVerticalStrut(10));
            panel.add(viewOrdersButton);

            adminFrame.add(panel);
            adminFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, "Incorrect password.");
        }
    }

    private JPanel createLabeledComponent(String labelText, JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText);
        panel.add(label);
        panel.add(component);
        return panel;
    }
}

public class V4_18041 {
    public static void main(String[] args) {
        new IceCreamManagementGUI();
    }
}
