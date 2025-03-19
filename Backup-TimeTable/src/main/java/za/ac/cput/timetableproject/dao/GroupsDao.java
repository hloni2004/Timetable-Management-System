package za.ac.cput.timetableproject.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import za.ac.cput.timetableproject.connection.DatabaseConnection;
import za.ac.cput.timetableproject.domain.Group;

public class GroupsDao {

    private Connection con;

    public GroupsDao() {
        try {
            this.con = DatabaseConnection.createConnection();
            createGroupTable();
            JOptionPane.showMessageDialog(null, "Connection Established");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL error occurred: " + e.getMessage());
        }
    }

    private void ensureConnection() throws SQLException {
        // Check if the connection is closed or null and create a new one if necessary
        if (con == null || con.isClosed()) {
            con = DatabaseConnection.createConnection();
        }
    }

    public void createGroupTable() throws SQLException {
        ensureConnection();  // Ensure connection before using it
        String createTableSql = "CREATE TABLE \"Group\" (GroupID INT PRIMARY KEY, GroupName VARCHAR(50))";
        
        try (PreparedStatement checkPs = con.prepareStatement("SELECT * FROM \"Group\" FETCH FIRST 1 ROWS ONLY");
             ResultSet rs = checkPs.executeQuery()) {
            // No rows means the table does not exist
        } catch (SQLException e) {
            // If table does not exist, create it
            if (e.getSQLState().equals("42X05")) { // Table does not exist
                try (PreparedStatement createPs = con.prepareStatement(createTableSql)) {
                    createPs.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Table created successfully.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "SQL error occurred: " + e.getMessage());
            }
        }
    }

    public boolean save(Group group) throws SQLException {
        ensureConnection();  // Ensure connection before using it
        String checkSql = "SELECT COUNT(*) FROM \"Group\" WHERE GroupID = ?";
        String insertSql = "INSERT INTO \"Group\" (GroupID, GroupName) VALUES (?, ?)";

        try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
            checkPs.setInt(1, group.getGroupId());
            try (ResultSet rs = checkPs.executeQuery()) {
                rs.next();
                int count = rs.getInt(1);

                if (count > 0) {
                    JOptionPane.showMessageDialog(null, "Error: GroupID already exists.");
                    return false;
                }
            }

            try (PreparedStatement insertPs = con.prepareStatement(insertSql)) {
                insertPs.setInt(1, group.getGroupId());
                insertPs.setString(2, group.getGroupName());
                int rowsAffected = insertPs.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage());
        }
        return false;
    }

    public void updateGroup(int groupId, String newGroupName) throws SQLException {
        ensureConnection();  // Ensure connection before using it
        String updateSql = "UPDATE \"Group\" SET GroupName = ? WHERE GroupID = ?";

        try (PreparedStatement updatePs = con.prepareStatement(updateSql)) {
            updatePs.setString(1, newGroupName);
            updatePs.setInt(2, groupId);
            int rowsUpdated = updatePs.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Group updated successfully.");
            } else {
                JOptionPane.showMessageDialog(null, "No group found with the provided GroupID.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage());
        }
    }

    public void deleteGroup(int groupId) throws SQLException {
        ensureConnection();  // Ensure connection before using it
        String deleteSql = "DELETE FROM \"Group\" WHERE GroupID = ?";

        try (PreparedStatement deletePs = con.prepareStatement(deleteSql)) {
            deletePs.setInt(1, groupId);
            int rowsDeleted = deletePs.executeUpdate();

            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(null, "Group deleted successfully.");
            } else {
                JOptionPane.showMessageDialog(null, "No group found with the provided GroupID.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage());
        }
    }

    public boolean isGroupIdExists(int groupId) throws SQLException {
        ensureConnection();  // Ensure connection before using it
        String checkSql = "SELECT COUNT(*) FROM \"Group\" WHERE GroupID = ?";
        
        try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
            checkPs.setInt(1, groupId);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public ArrayList<Group> readGroups() throws SQLException {
        ensureConnection();  // Ensure connection before using it
        ArrayList<Group> list = new ArrayList<>();
        String selectSql = "SELECT * FROM \"Group\"";

        try (PreparedStatement selectPs = con.prepareStatement(selectSql);
             ResultSet rs = selectPs.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("GroupID");
                String groupName = rs.getString("GroupName");
                list.add(new Group(id, groupName));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage());
        }
        return list;
    }
    
   public Group selectedGroup(int groupId) throws SQLException {
        ensureConnection();  // Ensure connection before using it
        Group group = null;  // Initialize group to null in case it doesn't exist
        String selectSql = "SELECT * FROM \"Group\" WHERE GroupID = ?";

        try (PreparedStatement selectPs = con.prepareStatement(selectSql)) {
            selectPs.setInt(1, groupId);
            try (ResultSet rs = selectPs.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("GroupID");
                    String groupName = rs.getString("GroupName");
                    group = new Group(id, groupName);  // Create a new Group object
                } else {
                    JOptionPane.showMessageDialog(null, "No group found with the provided GroupID.");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage());
        }
        return group;  // Return the found group or null if not found
    }
}
