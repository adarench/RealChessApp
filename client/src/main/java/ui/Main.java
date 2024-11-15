package ui;

import java.util.Scanner;

public class Main {

  private static boolean isLoggedIn = false; // Track whether the user is logged in
  private static Scanner scanner = new Scanner(System.in); // Scanner to read user input

  public static void main(String[] args) {
    showPreloginMenu();
  }

  private static void showPreloginMenu() {
    while (true) {
      System.out.println("\n== Chess Client ==");
      System.out.println("Enter a command: help, login, register, quit");
      System.out.print("> ");

      String command = scanner.nextLine().trim().toLowerCase();

      switch (command) {
        case "help":
          showHelp();
          break;
        case "login":
          login();
          break;
        case "register":
          register();
          break;
        case "quit":
          quit();
          return; // Exit the loop and terminate the application
        default:
          System.out.println("Invalid command. Type 'help' for a list of commands.");
      }
    }
  }

  // Display help text
  private static void showHelp() {
    System.out.println("Commands:");
    System.out.println("  help    - Display available commands");
    System.out.println("  login   - Log in to your account");
    System.out.println("  register - Register a new account");
    System.out.println("  quit    - Exit the application");
  }

  // Placeholder for login functionality
  private static void login() {
    System.out.println("Login functionality will go here.");
    // Once implemented, this will call ServerFacade to handle login
    // If login is successful, set isLoggedIn to true and transition to the post-login menu
  }

  // Placeholder for register functionality
  private static void register() {
    System.out.println("Register functionality will go here.");
    // Once implemented, this will call ServerFacade to handle registration
    // If registration is successful, set isLoggedIn to true and transition to the post-login menu
  }

  // Quit the application
  private static void quit() {
    System.out.println("Goodbye!");
    scanner.close(); // Close the scanner before exiting
  }

  // Method to show the postlogin menu will be added later
}
