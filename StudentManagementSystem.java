import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentManagementSystem {
    private static final String URL = "jdbc:mysql://localhost/studentdata";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static void main(String[] args){
        JFrame frame = new JFrame("Student Management System App");
        frame.setSize(1000,500);
        frame.setBackground(Color.red);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextField nameField = new JTextField(15);
        JTextField rollField = new JTextField(5);
        JTextField ageField = new JTextField(5);
        JTextField gradeField = new JTextField(5);

        JButton addbtn = new JButton("Add");
        JButton editbtn = new JButton("Edit");
        JButton deletebtn = new JButton("Delete");
        JButton refreshbtn = new JButton("Refresh");

        String[] columnNames = {"Name", "Roll Number", "Age", "Grade"};
        DefaultTableModel model = new DefaultTableModel(columnNames,0);

        JTable table = new JTable(model);
        table.setBackground(Color.orange);
        table.setFont(new Font("Arial", Font.PLAIN, 15));
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel panel1 = new JPanel();
        panel1.add(new JLabel("Name: "));
        panel1.add(nameField);
        panel1.add(new JLabel("Roll Number: "));
        panel1.add(rollField);
        panel1.add(new JLabel("Age: "));
        panel1.add(ageField);
        panel1.add(new JLabel("Grade: "));
        panel1.add(gradeField);
        panel1.add(addbtn);
        panel1.add(editbtn);
        panel1.add(deletebtn);
        panel1.add(refreshbtn);
        panel1.add(scrollPane);
        panel1.setBackground(Color.cyan);
//        panel1.setLayout(new GridLayout(1,0));

        frame.getContentPane().add(panel1, "North");
        frame.getContentPane().add(scrollPane,"Center");

        refreshbtn.addActionListener(e -> {
            model.setRowCount(0);
            fetchAndDisplayData(model);
        });

        fetchAndDisplayData(model);

        addbtn.addActionListener(e -> {
            String name = nameField.getText();
            int rollNumber = Integer.parseInt(rollField.getText());
            int age = Integer.parseInt(ageField.getText());
            int grade = Integer.parseInt(gradeField.getText());

            Connection connection1;
            PreparedStatement insert;

            Object[] rowData = {name, rollNumber, age, grade};
            model.addRow(rowData);

            try {
                Class.forName("com.mysql.jdbc.Driver");
                connection1 = DriverManager.getConnection("jdbc:mysql://localhost/studentdata", "root", "");
                insert = connection1.prepareStatement("insert into records(name,roll,age,grade)values(?,?,?,?)");
                insert.setString(1,name);
                insert.setInt(2,rollNumber);
                insert.setInt(3,age);
                insert.setInt(4,grade);
                insert.executeUpdate();
                JOptionPane.showMessageDialog(null, "Record Added Successfully");

                nameField.setText("");
                rollField.setText("");
                ageField.setText("");
                gradeField.setText("");
                nameField.requestFocus();


            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);

            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });


        editbtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a row to edit.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String name = nameField.getText();
            int rollNumber = Integer.parseInt(rollField.getText());
            int age = Integer.parseInt(ageField.getText());
            int grade = Integer.parseInt(gradeField.getText());

            model.setValueAt(name, selectedRow, 0);
            model.setValueAt(rollNumber, selectedRow, 1);
            model.setValueAt(age, selectedRow, 2);
            model.setValueAt(grade, selectedRow, 3);


            // Here we are Update the database
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                String query = "UPDATE records SET name=?, age=?, grade=? WHERE roll=?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, name);
                    statement.setInt(2, age);
                    statement.setInt(3, grade);
                    statement.setInt(4, rollNumber);
                    statement.executeUpdate();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error: Failed to update record.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        });

        deletebtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a row to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int rollNumber = (int) model.getValueAt(selectedRow, 1);

            model.removeRow(selectedRow);

            // Here we Delete the record from the database
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                String query = "DELETE FROM records WHERE roll=?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, rollNumber);
                    statement.executeUpdate();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error: Failed to delete record.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        });


        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void fetchAndDisplayData(DefaultTableModel model) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT * FROM records";
            try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    int rollNumber = resultSet.getInt("roll");
                    int age = resultSet.getInt("age");
                    int grade = resultSet.getInt("grade");
                    model.addRow(new Object[]{name, rollNumber, age, grade});
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: Failed to fetch data from the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}